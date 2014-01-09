package big.marketing.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
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

	private DataController dc;
	private Workspace workspace;
	private Map<String, String> ipHostMap;
	

	private static final String file1 = "data/Java.gexf", file2 = "data/LesMiserables.gexf";
	static String current = file1;

	private GraphJPanel graphPanel;

	public GephiController(DataController dc) {
		projectController = Lookup.getDefault().lookup(ProjectController.class);
		this.dc = dc;
		ipHostMap = dc.getMongoController().getHostIPMap();
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
		GephiImporter gImporter = new GephiImporter(flows,ipHostMap);
		ContainerLoader loader=container.getLoader();
		gImporter.execute(loader);
		
		processNewContainer(container);
		graphPanel.update(null,null);
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
		
		layoutGraph();
		
		graphPanel.setupModel(previewController.getModel());
		previewController.refreshPreview();
	}

	public void layoutGraph(){
		
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.initAlgo();
		layout.resetPropertiesValues();
		layout.setOptimalDistance(200f);
		
		for (int i = 0; i < 100 && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();
		
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
