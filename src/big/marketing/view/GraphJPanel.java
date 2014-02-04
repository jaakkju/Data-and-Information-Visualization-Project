package big.marketing.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.types.EdgeColor;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.openide.util.Lookup;

import processing.core.PApplet;
import big.marketing.controller.DataController;
import big.marketing.view.gephi.SelectionFilter;

public class GraphJPanel extends JPanel implements Observer {
	static Logger logger = Logger.getLogger(GraphJPanel.class);

	private static final long serialVersionUID = -7417639995072699909L;
	private final DataController controller;
	private PApplet applet;
	private ProcessingTarget target;

	public GraphJPanel(DataController controller) {
		this.controller = controller;
		this.controller.getGephiController().addObserver(this);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// TODO: many events are fired, don't update everytime, very costly
				// FIX: update only on last event... (or maybe find a setting to fire events only at the end of resizing...)
				update(null, null);
			}
		});
		setLayout(new BorderLayout());
		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
		RenderTarget rt = previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
		target = (ProcessingTarget) rt;
		applet = target.getApplet();
		applet.init();
		add(applet, BorderLayout.CENTER);
		previewController.render(target);

	}

	private void filterGraph() {
		FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

		SelectionFilter degreeFilter = new SelectionFilter(controller.getSelectedNodes());
		Query query = filterController.createQuery(degreeFilter);

		GraphView view = filterController.filter(query);
		graphModel.setVisibleView(view);

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
		int maxSteps = 100;

		for (int i = 0; i < maxSteps && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();

	}

	@Override
	public void update(Observable o, Object arg) {

		filterGraph();
		if ("SelectionOnly".equals(arg)) {
			logger.info("Short update");
		} else {
			// time changed, do full redraw
			layoutGraph();
			// Calculate ranking vor Nodes & Edges and color them 
			RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
			rankingController.setUseLocalScale(false);
			Ranking<?> degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.INDEGREE_RANKING);

			// NODE COLOR
			AbstractColorTransformer<?> colorTransformer = (AbstractColorTransformer<?>) rankingController.getModel().getTransformer(
			      Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
			colorTransformer.setColorPositions(new float[] { 0, 0.5f, 1 });
			colorTransformer.setColors(new Color[] { Color.GREEN, Color.YELLOW, Color.RED });
			rankingController.transform(degreeRanking, colorTransformer);

			// NODE SIZE
			AbstractSizeTransformer<?> sizeTransformer = (AbstractSizeTransformer<?>) rankingController.getModel().getTransformer(
			      Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
			sizeTransformer.setMinSize(5);
			sizeTransformer.setMaxSize(20);
			rankingController.transform(degreeRanking, sizeTransformer);

			// EDGE COLOR
			AbstractColorTransformer<?> edgeColorTransformer = (AbstractColorTransformer<?>) rankingController.getModel().getTransformer(
			      Ranking.EDGE_ELEMENT, Transformer.RENDERABLE_COLOR);
			edgeColorTransformer.setColorPositions(new float[] { 0, 0.5f, 1 });
			edgeColorTransformer.setColors(new Color[] { Color.blue, Color.cyan, Color.magenta });

			Ranking<?> weightRanking = rankingController.getModel().getRanking(Ranking.EDGE_ELEMENT, "weight");
			rankingController.transform(weightRanking, edgeColorTransformer);
			// EDGE_ELEMENT has Transformers: RenderableColorTransformer, LabelColorTransformer, LabelSizeTransformer

			PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
			PreviewProperties props = previewController.getModel().getProperties();
			props.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.FALSE);
			//			props.putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.WHITE));
			//			props.putValue(PreviewProperty.NODE_BORDER_COLOR, new DependantColor(Color.white));

			props.putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
			props.putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(EdgeColor.Mode.ORIGINAL));
			props.putValue(PreviewProperty.EDGE_OPACITY, 50);
			props.putValue(PreviewProperty.EDGE_RADIUS, 10f);
			props.putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);
			//			props.putValue(PreviewProperty.ARROW_SIZE, 50);
			props.putValue(PreviewProperty.EDGE_THICKNESS, 20);
			props.putValue(PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.TRUE);

			target.resetZoom();
		}
		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
		previewController.refreshPreview();

		Lookup.getDefault().lookup(PreviewController.class).refreshPreview();
		target.refresh();
	}
}
