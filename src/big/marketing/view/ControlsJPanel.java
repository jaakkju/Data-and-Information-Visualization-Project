package big.marketing.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
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
	private JButton playPauseButton;
	private ChartPanel chartPanel;
	private JLabel currentTimeLabel;
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM HH:mm", Locale.ENGLISH);
	static Logger logger = Logger.getLogger(ControlsJPanel.class);
	public static int QW_MIN = 0, QW_MAX = 1217384;

	public ControlsJPanel(final DataController controller, IntervalXYDataset sliderBackgroundData) {
		loadSettings();
		this.controller = controller;
		this.setLayout(new BorderLayout());

		playPauseButton = new JButton("Play");
		playPauseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info(playPauseButton.getText() + " button press");
				controller.playStopButtonPressed(1364802600, 3600);
			}
		});
		add(playPauseButton, BorderLayout.LINE_START);

		buttonPanel = new JPanel();
		BoxLayout bl = new BoxLayout(buttonPanel, BoxLayout.Y_AXIS);
		buttonPanel.setLayout(bl);
		buttonPanel.add(playPauseButton);
		currentTimeLabel = new JLabel();
		buttonPanel.add(currentTimeLabel);
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
				int newTime = source.getValue();
				Date date = new Date(newTime * 1000L);
				currentTimeLabel.setText(formatter.format(date));
				if (!source.getValueIsAdjusting()) {
					controller.moveQueryWindow((int) source.getValue());
				}
			}
		});
		chartPanel.add(qWindowSlider, BorderLayout.CENTER);
		add(chartPanel);
		//		chartPanel.setBorder(null);
		setPreferredSize(new Dimension(WindowFrame.FRAME_WIDTH, (int) (WindowFrame.FRAME_HEIGHT * 0.3)));
		qWindowSlider.setValue(QW_MIN);
	}

	public void setCurrentTime(int i) {
		// clamp time between QW_MIN and QW_MAX
		i = Math.min(QW_MAX, Math.max(QW_MIN, i));
		qWindowSlider.setValue(i);
	}

	private void switchPlayButtonName() {
		playPauseButton.setText(playPauseButton.getText().equals("Play") ? "Pause" : "Play");
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof IntervalXYDataset) {
			showChart((IntervalXYDataset) arg);
		} else if (arg instanceof Integer) {
			int newTime = (Integer) arg;
			setCurrentTime(newTime);
		} else if ("PlayStateChanged".equals(arg)) {
			switchPlayButtonName();
		}
	}

	public JFreeChart showChart(IntervalXYDataset dataset) {

		JFreeChart chart = ChartFactory.createHistogram("", "", "", dataset, PlotOrientation.VERTICAL, false, false, false);

		chart.setBackgroundPaint(Color.white);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);
		plot.setRangeAxis(new LogarithmicAxis("123"));

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(false);

		rangeAxis.setAutoTickUnitSelection(true);
		rangeAxis.setAutoRangeIncludesZero(true);
		DateAxis dAxis = new DateAxis();
		dAxis.setRange((long) ControlsJPanel.QW_MIN * 1000L, (long) ControlsJPanel.QW_MAX * 1000L);
		plot.setDomainAxis(dAxis);
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