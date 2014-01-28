package big.marketing.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.Logger;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.NodeData;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.PreviewController;
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

public class GephiController extends Observable {
	static Logger logger = Logger.getLogger(GephiController.class);

	private ImportController importController;
	private PreviewController previewController;
	private ProjectController projectController;

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
		if (selectedNodes == null)
			selectedNodes = new Node[0];
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
		GephiImporter gImporter = new GephiImporter(newDataset, ipMap, selectedNodes);
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
		graphPanel.setContent((ProcessingTarget) rt);

	}

	public void render(ProcessingTarget target) {
		previewController.render(target);
	}

	public void selectNodesFromCoords(int startX, int startY, int endX, int endY) {
		GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
		org.gephi.graph.api.Node[] nodes = gm.getGraph().getNodes().toArray();
		List<Node> selected = new ArrayList<>();

		if (startX > endX) {
			int tmp = endX;
			endX = startX;
			startX = tmp;
		}
		if (startY > endY) {
			int tmp = endY;
			endY = startY;
			startY = tmp;
		}
		int totalSelectedNodes = 0;
		for (org.gephi.graph.api.Node n : nodes) {
			NodeData nd = n.getNodeData();
			if (nd.x() >= startX && nd.x() <= endX && nd.y() >= startY && nd.y() <= endY) {
				totalSelectedNodes++;
				String ip = (String) nd.getAttributes().getValue("ip");
				Node networkNode = ipMap.get(ip);
				if (networkNode != null)
					selected.add(networkNode);
			}

		}
		logger.info("Selected " + selected.size() + " out of " + totalSelectedNodes + " Nodes...");
		if (selected.size() > 0) {
			Node[] selectedNodes = (Node[]) selected.toArray(new Node[selected.size()]);
			dc.setSelectedNode(selectedNodes);
		}

	}

}
