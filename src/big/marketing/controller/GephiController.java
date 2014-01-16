package big.marketing.controller;

import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.Logger;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
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
		// load an emtpy graph for initializing the RenderTarget and Applet(in GraphPanel)
		loadEmptyContainer();

		// By now the JPanel on which to draw on is not known, so we cannot set up the RenderTarget.
		// This is done in setGraphPanel(...)
	}

	public void loadEmptyContainer() {
		load(new QueryWindowData(new ArrayList<FlowMessage>(), null, null, null));
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
					logger.info("Killing Thread " + thread.getName());
					thread.stop();
				}
			}
		}
	}

	public void load(QueryWindowData newDataset) {
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
		GephiImporter gImporter = new GephiImporter(newDataset, ipMap);
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

}
