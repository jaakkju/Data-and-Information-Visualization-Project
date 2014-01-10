package big.marketing.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.types.DependantOriginalColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.xy.IntervalXYDataset;

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
		removeAll();
		add(applet, BorderLayout.CENTER);
		controller.getGephiController().render(target);
	}

	public GraphJPanel(DataController controller) {
		this.controller = controller;
		setLayout(new BorderLayout());
		this.controller.getGephiController().setGraphPanel(this);
	}

	
	public void showChart(IntervalXYDataset dataset){
		JFreeChart chart = ChartFactory.createHistogram("Title", "time", "traffic", dataset, PlotOrientation.VERTICAL, false, false, false);
		chart.setBackgroundPaint(Color.white);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.white);
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoTickUnitSelection(true);
		rangeAxis.setAutoRangeIncludesZero(true);
		plot.setRenderer(new XYAreaRenderer());
		// TODO: make the chart visible by placing a ChartPanel somewhere
	}
	
	
	public void setupModel(PreviewModel previewModel) {
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.WHITE));
		previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 10f);
		previewModel.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);
	}

	// TODO: use update(...), not prepareModel and setTarget
	@Override
	public void update(Observable o, Object arg) {

		if (arg instanceof IntervalXYDataset){
			showChart((IntervalXYDataset)arg);
		}
		if (target != null) {
			target.refresh();
			target.resetZoom();
		}
	}

}
