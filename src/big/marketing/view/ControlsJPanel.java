package big.marketing.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.ui.RectangleInsets;

import big.marketing.Settings;
import big.marketing.controller.DataController;

public class ControlsJPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 7478563340170330453L;
	private final DataController controller;
	private JSlider qWindowSlider;
	private JPanel buttonPanel;
	private JButton playButton, stopButton;
	private ChartPanel chartPanel;
	static Logger logger = Logger.getLogger(ControlsJPanel.class);
	public static int QW_MIN = 0, QW_MAX = 1217384;

	public ControlsJPanel(final DataController controller, IntervalXYDataset sliderBackgroundData) {
		loadSettings();
		this.controller = controller;
		this.setLayout(new BorderLayout());

		playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("Play button press");
			}
		});
		add(playButton, BorderLayout.LINE_START);

		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("Stop button press");
			}
		});
		add(stopButton, BorderLayout.LINE_START);

		buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(playButton);
		buttonPanel.add(stopButton);
		add(buttonPanel, BorderLayout.LINE_START);

		//				chartPanel = new ChartPanel(showChart(sliderBackgroundData), WindowFrame.FRAME_WIDTH, 50, 0, 0, 1920, 1080, false, false, false,
		//				      false, false, false);
		chartPanel = new ChartPanel(showChart(sliderBackgroundData));
		chartPanel.setLayout(new BorderLayout());
		qWindowSlider = new JSlider(JSlider.HORIZONTAL, QW_MIN, QW_MAX, QW_MIN);
		qWindowSlider.setOpaque(false);
		qWindowSlider.setUI(new QuerySliderUI(qWindowSlider));

		qWindowSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					controller.moveQueryWindow((int) source.getValue());
				}
			}
		});
		chartPanel.add(qWindowSlider, BorderLayout.CENTER);
		add(chartPanel);
		//		chartPanel.setBorder(null);
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.info("Update ControlsPanel");
		if (arg instanceof IntervalXYDataset) {
			logger.info("Got Dataset");
			QuerySliderUI ui = (QuerySliderUI) qWindowSlider.getUI();
			showChart((IntervalXYDataset) arg);
		}
	}

	public JFreeChart showChart(IntervalXYDataset dataset) {

		JFreeChart chart = ChartFactory.createHistogram("", "", "", dataset, PlotOrientation.VERTICAL, false, false, false);

		chart.setBackgroundPaint(Color.white);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);

		plot.setRangeAxis(new LogarithmicAxis("123"));

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(false);

		rangeAxis.setAutoTickUnitSelection(true);
		rangeAxis.setAutoRangeIncludesZero(true);
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setRange(ControlsJPanel.QW_MIN, ControlsJPanel.QW_MAX);
		//		domainAxis.setVisible(false);
		plot.setRenderer(new XYAreaRenderer());
		//		int length = (int) (qWindowSlider.getWidth() * DataController.QUERYWINDOW_SIZE / (QW_MAX - QW_MIN));
		//		plot.setAxisOffset(new RectangleInsets(0, length / 4, 0, length / 4));
		plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
		chart.setPadding(new RectangleInsets(0, 0, 0, 0));

		return chart;
	}

	private void loadSettings() {
		QW_MIN = Settings.getInt("qwindow.data.min");
		QW_MAX = Settings.getInt("qwindow.data.max");
	}

}