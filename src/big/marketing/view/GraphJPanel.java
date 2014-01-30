package big.marketing.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
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
		this.target = target;
		applet = target.getApplet();
		applet.init();
		//		removeAll();
		add(applet, BorderLayout.CENTER);
		controller.getGephiController().render(target);
	}

	public GraphJPanel(DataController controller) {
		this.controller = controller;
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// TODO: many events are fired, don't update everytime, very costly
				// FIX: update only on last event... (or maybe find a setting to fire events only at the end of resizing...)
				update(null, null);
			}
		});
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

			// Calculate ranking vor Nodes & Edges and color them 
			RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
			Ranking degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.INDEGREE_RANKING);
			if (degreeRanking == null)
				return;
			AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(
			      Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
			colorTransformer.setColorPositions(new float[] { 0, 0.5f, 1 });
			colorTransformer.setColors(new Color[] { new Color(0x00FF00), new Color(0xFFFF00), new Color(0xFF0000) });
			rankingController.transform(degreeRanking, colorTransformer);
			AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(
			      Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
			sizeTransformer.setMinSize(5);
			sizeTransformer.setMaxSize(20);
			rankingController.transform(degreeRanking, sizeTransformer);

			PreviewController previewController = (PreviewController) arg;
			PreviewProperties props = previewController.getModel().getProperties();

			// Dimensions are different every run... thats bad
			// topleft & dimensions are only updated during refreshPreview...
			// how to create a mapping from graph coordinates to screen coords???
			// screen dimensions via previewapplet...
			// initial graph dimensions via previewModel
			// track changes (pan, zoom)

			props.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.FALSE);
			//			props.putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.WHITE));
			props.putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
			props.putValue(PreviewProperty.EDGE_OPACITY, 50);
			props.putValue(PreviewProperty.EDGE_RADIUS, 0f);
			props.putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);
			props.putValue(PreviewProperty.ARROW_SIZE, 1);
			props.putValue(PreviewProperty.EDGE_THICKNESS, 20);
			props.putValue(PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.TRUE);
			previewController.refreshPreview();
		}

		if (target != null) {
			target.refresh();
			target.resetZoom();
		}
	}

}
