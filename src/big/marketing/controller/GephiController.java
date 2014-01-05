package big.marketing.controller;

import java.io.File;

import org.apache.log4j.Logger;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import big.marketing.view.GraphJPanel;

public class GephiController {
	static Logger logger = Logger.getLogger(GephiController.class);

	private ImportController importController;
	private PreviewController previewController;
	private Workspace workspace;

	private GraphJPanel graphPanel;

	public GephiController() {
		ProjectController pc = Lookup.getDefault().lookup(
				ProjectController.class);
		pc.newProject();
		workspace = pc.getCurrentWorkspace();
		importController = Lookup.getDefault().lookup(ImportController.class);
		previewController = Lookup.getDefault().lookup(PreviewController.class);
	}

	public void loadSampleFile() {
		Container container = null;
		try {
			File file = new File("data/Java.gexf");
			container = importController.importFile(file);
		} catch (Exception ex) {
			logger.error(ex);
		}
		processNewContainer(container);
	}

	public void processNewContainer(Container container) {
		if (container == null) {
			logger.info("Got emtpy container to display!");
			return;
		}
		importController.process(container, new DefaultProcessor(), workspace);
		graphPanel.prepareModel(previewController.getModel());
		previewController.refreshPreview();
		RenderTarget rt = previewController
				.getRenderTarget(RenderTarget.PROCESSING_TARGET);
		graphPanel.setTarget((ProcessingTarget) rt);

	}

	public GraphJPanel getGraphPanel() {
		return graphPanel;
	}

	public void setGraphPanel(GraphJPanel graphPanel) {
		this.graphPanel = graphPanel;
	}

	public void render(ProcessingTarget target) {
		previewController.render(target);
	}
}
