package big.marketing.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import big.marketing.data.FlowMessage;
import big.marketing.data.Node;
import big.marketing.data.QueryWindowData;
import big.marketing.view.GraphJPanel;
import big.marketing.view.GraphMouseListener;
import big.marketing.view.MouseRenderer;
import big.marketing.view.gephi.MyProcessingApplet;

public class GephiController extends Observable implements Observer {
	static Logger logger = Logger.getLogger(GephiController.class);

	private ImportController importController;
	private PreviewController previewController;
	private ProjectController projectController;
	private QueryWindowData currentQueryWindow;
	private Node[] selectedNodes;

	private DataController dc;
	private Workspace workspace;
	private Map<String, Node> ipMap;

	public GephiController(DataController dc) {
		projectController = Lookup.getDefault().lookup(ProjectController.class);
		this.dc = dc;
		ipMap = dc.getMongoController().getNetwork();
		GraphMouseListener gml = Lookup.getDefault().lookup(GraphMouseListener.class);
		gml.setGephiController(this);
		// load an emtpy graph for initializing the RenderTarget and Applet(in GraphPanel)
		loadEmptyContainer();

		// By now the JPanel on which to draw on is not known, so we cannot set up the RenderTarget.
		// This is done in setGraphPanel(...)
	}

	public void loadEmptyContainer() {
		load(new QueryWindowData(new ArrayList<FlowMessage>(), null, null, null), null);
	}

	/**
	 * Terminates all current active Threads which name one of the given threadNames. <br>
	 * 
	 * <b>WARNING:</b>Be careful, no security checks are done, can break the VM.
	 * @param terminationTargets
	 */
	private void terminateThreads(String... terminationTargets) {
		// All Threads are organized in a tree structure, so get the root of the tree
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		while (tg.getParent() != null) {
			tg = tg.getParent();
		}
		// now we have the root node of the tree and can fetch all current active Threads
		// Allocate some additional slots because new Threads could have been started between call of tg.activeCount() and tg.enumerate()
		Thread[] threads = new Thread[tg.activeCount() + 5];
		int realCount = tg.enumerate(threads);
		for (int i = 0; i < realCount; i++) {
			Thread thread = threads[i];
			if (thread == null)
				continue;
			for (String terminationName : terminationTargets) {
				if (thread.getName().equals(terminationName)) {
					thread.stop();
				}
			}
		}
	}

	public void load(QueryWindowData newDataset, Node[] selectedNodes) {
		if (selectedNodes == null) {
			if (this.selectedNodes == null)
				this.selectedNodes = new Node[0];
		} else
			this.selectedNodes = selectedNodes;
		if (newDataset == null) {
			if (currentQueryWindow == null)
				logger.info("No QueryWindow to display");
		} else
			currentQueryWindow = newDataset;

		if (this.selectedNodes == null || this.currentQueryWindow == null) {
			logger.info("Not all data yet, not displaying graph");
			return;
		}

		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		if (graphModel != null) {
			graphModel.clear();
			projectController.cleanWorkspace(projectController.getCurrentWorkspace());
			//			projectController.closeCurrentWorkspace();

			// REALLY DIRTY HACK !!!
			terminateThreads("DHNS View Destructor");
			// END REALLY DIRTY HACK!!!!

		} else {
			projectController.newProject();
		}

		// init
		importController = Lookup.getDefault().lookup(ImportController.class);
		previewController = Lookup.getDefault().lookup(PreviewController.class);
		workspace = projectController.getCurrentWorkspace();

		// import to container
		Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
		GephiImporter gImporter = new GephiImporter(currentQueryWindow, ipMap, this.selectedNodes);
		ContainerLoader loader = container.getLoader();
		gImporter.execute(loader);

		// process data from container into internal graph structure
		importController.process(container, new DefaultProcessor(), workspace);

		// update view
		setChanged();
		notifyObservers(previewController);
	}

	public void setGraphPanel(GraphJPanel graphPanel) {
		addObserver(graphPanel);
		// now we know the Panel where to draw, so create and set the RenderTarget
		RenderTarget rt = previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
		ProcessingTarget pt = (ProcessingTarget) rt;
		graphPanel.setContent(pt);

	}

	MyProcessingApplet applet;

	public void render(ProcessingTarget target) {
		previewController.render(target);
		applet = (MyProcessingApplet) target.getApplet();
	}

	Item lastItem = null;

	public void showNodeInfo(float x, float y) {
		Item newItem = getSingleNode(x, y);
		String toolTipText = createTooltipText(newItem);
		MouseRenderer mr = Lookup.getDefault().lookup(MouseRenderer.class);

		if (newItem != lastItem) {
			if (newItem == null)
				mr.hideTooltip();
			else
				mr.showTooltip(toolTipText, x, y);
		}

		lastItem = newItem;
	}

	public String createTooltipText(Item i) {
		StringBuilder sb = new StringBuilder();
		Node n = item2Node(i);
		if (n != null) {
			addLine(sb, "IP", n.getAddress());
			addLine(sb, "Name", n.getHostName());
			if (n.getComment() != null)
				addLine(sb, "Comment", n.getComment());
		} else {
			addLine(sb, "IP", getIp(i));
			addLine(sb, "Type", "external");
		}
		return sb.toString();
	}

	private void addLine(StringBuilder sb, String one, String two) {
		sb.append(one);
		sb.append(":\t");
		sb.append(two);
		sb.append("\n");

	}

	public Item getSingleNode(final float x, final float y) {

		Item ii = null;
		PreviewModel pm = Lookup.getDefault().lookup(PreviewController.class).getModel();

		for (Item i : pm.getItems(Item.NODE)) {
			float size = i.getData("size");
			float ix = i.getData("x");
			float iy = i.getData("y");
			size /= 2;
			if (x >= ix - size && x <= ix + size && y >= iy - size && y <= iy + size) {
				ii = i;
				// assuming non-overlapping nodes, so we can stop here
				break;
			}
		}

		//		This code is a bit faster but less precise and does not consider displayed size of node
		//
		//		float threshold = 50;
		//		List<Item> selected = getAllNodes(x - threshold, y - threshold, x + threshold, y + threshold);
		//		if (selected.size() > 0) {
		//			ii = selected.get(0);
		//			if (selected2.size() > 1) {
		//				double minDistance = Float.MAX_VALUE;
		//				for (Item it : selected2) {
		//					float xx = it.getData("x");
		//					float yy = it.getData("y");
		//					double d = distance(xx, x, yy, y);
		//					if (d < minDistance) {
		//						ii = it;
		//						minDistance = d;
		//					}
		//				}
		//			}
		//		}
		return ii;
	}

	private double distance(float x1, float x2, float y1, float y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	private List<Item> getAllNodes(float startX, float startY, float endX, float endY) {
		PreviewModel pm = Lookup.getDefault().lookup(PreviewController.class).getModel();
		List<Item> selected = new ArrayList<>();

		// normalize dragged square
		if (startX > endX) {
			float tmp = endX;
			endX = startX;
			startX = tmp;
		}
		if (startY > endY) {
			float tmp = endY;
			endY = startY;
			startY = tmp;
		}
		for (Item item : pm.getItems(Item.NODE)) {
			float x = item.getData("x");
			float y = item.getData("y");
			if (x >= startX && x <= endX && y >= startY && y <= endY) {
				selected.add(item);
			}
		}
		return selected;

	}

	private String getIp(Item item) {
		if (item == null)
			return null;
		org.gephi.graph.api.Node n = (org.gephi.graph.api.Node) item.getSource();
		String ip = (String) n.getNodeData().getAttributes().getValue("ip");
		return ip;
	}

	private Node item2Node(Item item) {
		return ipMap.get(getIp(item));
	}

	private List<Node> items2Nodes(List<Item> items) {
		List<Node> out = new ArrayList<>();
		for (Item item : items) {
			Node networkNode = item2Node(item);
			if (networkNode != null)
				out.add(networkNode);
		}
		return out;
	}

	public void selectNodesFromCoords(int startX, int startY, int endX, int endY) {
		List<Item> selected = getAllNodes(startX, startY, endX, endY);
		if (selected.size() > 0) {
			List<Node> tmp = items2Nodes(selected);
			Node[] selectedNodes = (Node[]) tmp.toArray(new Node[tmp.size()]);
			dc.setSelectedNodes(selectedNodes);
		} else {
			logger.info("No internal nodes selected, not changing the selection");
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof QueryWindowData) {
			load((QueryWindowData) arg, null);
		} else if (arg instanceof Node[]) {
			load(null, (Node[]) arg);
		}

	}
}
