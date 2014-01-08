package big.marketing.controller;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import big.marketing.data.FlowMessage;
import big.marketing.view.GraphJPanel;

public class GephiController {
	static Logger logger = Logger.getLogger(GephiController.class);

	private ImportController importController;
	private PreviewController previewController;
	private ProjectController projectController;
	private Workspace workspace;

	private static final String file1 = "data/Java.gexf", file2 = "data/LesMiserables.gexf";
	static String current = file1;

	private GraphJPanel graphPanel;

	public GephiController() {
		projectController = Lookup.getDefault().lookup(ProjectController.class);
		// Init cannot be finished here, because graphPanel is not ready yet
		// Therefore Init is finished in setGraphPanel(...)
	}

	public void load(List<FlowMessage> flows){
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		if (graphModel != null) {
			graphModel.clear();
			projectController.closeCurrentWorkspace();
		}
		prepareLoad();
		
		Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
		GephiImporter gImporter = new GephiImporter(flows);
		ContainerLoader loader=container.getLoader();
		gImporter.execute(loader);
		
		processNewContainer(container);
		
	}
	
	public void loadSampleFile() {
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		if (graphModel != null) {
			graphModel.clear();
			projectController.closeCurrentWorkspace();
		}

		prepareLoad();
		Container dataContainer = loadContainer();
		processNewContainer(dataContainer);
		graphPanel.update(null, null);

	}

	public void prepareLoad() {
		projectController.newProject();
		importController = Lookup.getDefault().lookup(ImportController.class);
		previewController = Lookup.getDefault().lookup(PreviewController.class);
		workspace = projectController.getCurrentWorkspace();
	}

	public Container loadContainer() {
		Container container = null;
		if (current.equals(file1))
			current = file2;
		else
			current = file1;

		try {
			File file = new File(current);
			container = importController.importFile(file);
		} catch (Exception ex) {
			logger.error(ex);
		}
		return container;
	}

	public void processNewContainer(Container container) {
		if (container == null) {
			logger.info("Got no container to display!");
			return;
		}
		importController.process(container, new DefaultProcessor(), workspace);
		graphPanel.setupModel(previewController.getModel());
		previewController.refreshPreview();
	}

	public GraphJPanel getGraphPanel() {
		return graphPanel;
	}

	public void setGraphPanel(GraphJPanel graphPanel) {
		this.graphPanel = graphPanel;
		loadSampleFile();

		RenderTarget rt = previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
		graphPanel.setContent((ProcessingTarget) rt);

	}

	public void render(ProcessingTarget target) {
		previewController.render(target);
	}
}
