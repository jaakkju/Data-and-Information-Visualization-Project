package big.marketing.controller.gephi;

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
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import big.marketing.controller.DataController;
import big.marketing.data.Node;
import big.marketing.data.QueryWindowData;
import big.marketing.view.gephi.CustomApplet;
import big.marketing.view.gephi.GraphMouseListener;
import big.marketing.view.gephi.MouseRenderer;

public class GephiController extends Observable implements Observer {
	static Logger logger = Logger.getLogger(GephiController.class);

	private QueryWindowData currentQueryWindow;
	private Node[] selectedNodes;

	private DataController dataController;
	private Workspace workspace;
	private Map<String, Node> ipMap;

	Item lastItem = null;

	CustomApplet applet;

	boolean skip = false;

	public GephiController(DataController dc) {
		ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
		workspace = projectController.getCurrentWorkspace();
		projectController.newProject();

		this.dataController = dc;
		ipMap = dc.getMongoController().getIpToNodeMap();
		GraphMouseListener gml = Lookup.getDefault().lookup(GraphMouseListener.class);
		gml.setGephiController(this);

		// By now the JPanel on which to draw on is not known, so we cannot set up the RenderTarget.
		// This is done in setGraphPanel(...)
	}

	/**
	 * Terminates all current active Threads which name one of the given threadNames. <br>
	 * 
	 * <b>WARNING:</b>Be careful, no security checks are done, can break the VM.
	 * @param terminationTargets
	 */
	@SuppressWarnings("deprecation")
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

	@Override
	public void update(Observable o, Object arg) {

		if ("SkipNextNotify".equals(arg)) {
			skip = true;
		} else if (arg instanceof QueryWindowData) {
			load((QueryWindowData) arg);
		} else if (arg instanceof Node[]) {
			loadSelection((Node[]) arg);
		}

	}

	public void loadSelection(Node[] selectedNodes) {
		if (selectedNodes != null)
			this.selectedNodes = selectedNodes;
		if (skip) {
			skip = false;
			return;
		}
		if (currentQueryWindow != null) {
			// only update if there has been data loaded earlier
			setChanged();
			notifyObservers("SelectionOnly");
		}
	}

	public void load(QueryWindowData newDataset) {
		if (newDataset == null)
			return;

		currentQueryWindow = newDataset;

		if (skip) {
			skip = false;
			return;
		}
		if (this.selectedNodes == null || this.currentQueryWindow == null) {
			logger.info("Not all data yet, not displaying graph");
			return;
		}

		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

		if (graphModel != null) {
			graphModel.getGraph().clear();
			ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
			projectController.cleanWorkspace(workspace);

			// Gephi doesn't terminate these Threads. Since many of these threads lead to OutOfMemory Errors,
			// kill all left-over threads manually.
			terminateThreads("DHNS View Destructor");

		}

		// import to container
		Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
		GephiImporter gImporter = new GephiImporter(currentQueryWindow, ipMap, this.selectedNodes);
		ContainerLoader loader = container.getLoader();
		gImporter.execute(loader);

		// process data from container into internal graph structure
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);
		importController.process(container, new DefaultProcessor(), workspace);

		// update view
		setChanged();
		notifyObservers();
	}

	public void showNodeInfo(float x, float y) {
		Item newItem = getSingleItem(x, y);

		if (newItem != lastItem) {
			MouseRenderer mouseRenderer = Lookup.getDefault().lookup(MouseRenderer.class);
			if (newItem == null)
				mouseRenderer.hideTooltip();
			else
				mouseRenderer.showTooltip(createTooltipText(newItem), x, y);
		}

		lastItem = newItem;
	}

	public String createTooltipText(Item i) {
		StringBuilder sb = new StringBuilder();
		Node node = getNodeForItem(i);
		if (node != null) {
			addLine(sb, "IP", node.getAddress());
			addLine(sb, "Name", node.getHostName());
			if (node.getComment() != null)
				addLine(sb, "Comment", node.getComment());
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

	public Item getSingleItem(float x, float y) {

		Item resultItem = null;
		PreviewModel previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();

		for (Item currentItem : previewModel.getItems(Item.NODE)) {
			float itemSize = currentItem.getData("size");
			float itemX = currentItem.getData("x");
			float itemY = currentItem.getData("y");
			itemSize /= 2;
			if (x >= itemX - itemSize && x <= itemX + itemSize && y >= itemY - itemSize && y <= itemY + itemSize) {
				resultItem = currentItem;
				// assuming non-overlapping nodes, so we can stop here
				break;
			}
		}

		return resultItem;
	}

	private List<Item> getAllItemsInRectangle(float startX, float startY, float endX, float endY) {
		PreviewModel previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
		List<Item> selectedItems = new ArrayList<>();

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

		for (Item currentItem : previewModel.getItems(Item.NODE)) {
			float itemX = currentItem.getData("x");
			float itemY = currentItem.getData("y");
			if (itemX >= startX && itemX <= endX && itemY >= startY && itemY <= endY) {
				selectedItems.add(currentItem);
			}
		}
		return selectedItems;

	}

	private String getIp(Item item) {
		if (item == null)
			return null;
		org.gephi.graph.api.Node n = (org.gephi.graph.api.Node) item.getSource();
		String ip = (String) n.getNodeData().getAttributes().getValue("ip");
		return ip;
	}

	private Node getNodeForItem(Item item) {
		return ipMap.get(getIp(item));
	}

	private List<Node> getNodesForItems(List<Item> items) {
		List<Node> nodes = new ArrayList<>();
		for (Item currentItem : items) {
			Node networkNode = getNodeForItem(currentItem);
			if (networkNode != null)
				nodes.add(networkNode);
		}
		return nodes;
	}

	public void selectNodesFromCoords(int startX, int startY, int endX, int endY) {
		List<Item> selected = getAllItemsInRectangle(startX, startY, endX, endY);
		if (selected.size() > 0) {
			List<Node> tmp = getNodesForItems(selected);
			Node[] selectedNodes = (Node[]) tmp.toArray(new Node[tmp.size()]);
			dataController.setSelectedNodes(selectedNodes);
		} else {
			logger.info("No internal nodes selected, not changing the selection");
		}
	}
}
