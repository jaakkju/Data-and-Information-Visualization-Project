package big.marketing.view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.xy.IntervalXYDataset;

import big.marketing.controller.DataController;

public class QuerySliderUI extends BasicSliderUI {
	private final int range;

	public QuerySliderUI(JSlider aSlider, int range) {
		super(aSlider);
		this.range = range;
	}

	@Override
	public void paintThumb(Graphics g) {
		int QW_LENGTH = (int) (slider.getWidth() * DataController.QUERYWINDOW_SIZE / range);
		g.setColor(new Color(0, 0, 255, 120));
		g.fillRect(thumbRect.width / 2 + thumbRect.x - QW_LENGTH / 2, thumbRect.y, QW_LENGTH, thumbRect.height);
		super.paintThumb(g);
	}

	public void showChart(IntervalXYDataset dataset) {
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
}