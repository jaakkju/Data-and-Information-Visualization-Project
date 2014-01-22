package big.marketing.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.types.DependantOriginalColor;
import org.openide.util.Lookup;

import processing.core.PApplet;
import big.marketing.controller.DataController;

public class GraphJPanel extends JPanel implements Observer {
	static Logger logger = Logger.getLogger(GraphJPanel.class);

	private static final long serialVersionUID = -7417639995072699909L;
	private final DataController controller;
	private PApplet applet;
	private ProcessingTarget target;

	public void setContent(ProcessingTarget target) {
		logger.info("Init applet");
		this.target = target;
		applet = target.getApplet();
		applet.init();
		//		removeAll();
		add(applet, BorderLayout.CENTER);
		controller.getGephiController().render(target);
	}

	public GraphJPanel(DataController controller) {
		this.controller = controller;
		setLayout(new BorderLayout());
		this.controller.getGephiController().setGraphPanel(this);
	}

	public void layoutGraph() {

		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		ForceAtlas2 layout = new ForceAtlas2(null);
		//		YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.initAlgo();
		layout.resetPropertiesValues();
		//		layout.setOptimalDistance(200f);
		layout.setEdgeWeightInfluence(0.1);
		layout.setScalingRatio(50.0);
		layout.setLinLogMode(false);

		for (int i = 0; i < 100 && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();

	}

	@Override
	public void update(Observable o, Object arg) {

		if (arg instanceof PreviewController) {

			layoutGraph();

			PreviewController previewController = (PreviewController) arg;
			PreviewProperties props = previewController.getModel().getProperties();
			props.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
			props.putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.WHITE));
			props.putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
			props.putValue(PreviewProperty.EDGE_OPACITY, 50);
			props.putValue(PreviewProperty.EDGE_RADIUS, 0f);
			props.putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);
			props.putValue(PreviewProperty.ARROW_SIZE, 1);
			props.putValue(PreviewProperty.EDGE_THICKNESS, 20);
			props.putValue(PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.TRUE);
			previewController.refreshPreview();

			if (target != null) {
				target.refresh();
				target.resetZoom();
			}
		}
	}

}
