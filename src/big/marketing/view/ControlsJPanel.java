package big.marketing.view;

import java.awt.BorderLayout;
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
import org.jfree.data.xy.IntervalXYDataset;

import big.marketing.Settings;
import big.marketing.controller.DataController;

public class ControlsJPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 7478563340170330453L;
	private final DataController controller;
	private JSlider qWindowSlider;
	private JPanel buttonPanel;
	private JButton playButton, stopButton;

	static Logger logger = Logger.getLogger(ControlsJPanel.class);
	public static int QW_MIN = 0, QW_MAX = 1217384;

	public ControlsJPanel(final DataController controller) {
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

		qWindowSlider = new JSlider(JSlider.HORIZONTAL, QW_MIN, QW_MAX, QW_MIN);
		qWindowSlider.setUI(new QuerySliderUI(qWindowSlider, QW_MAX - QW_MIN));
		qWindowSlider.setMajorTickSpacing(100000);
		qWindowSlider.setMinorTickSpacing(10000);
		qWindowSlider.setPaintTicks(true);
		qWindowSlider.setPaintLabels(true);
		qWindowSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					controller.moveQueryWindow((int) source.getValue());
				}
			}
		});
		add(qWindowSlider);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof IntervalXYDataset) {
			QuerySliderUI ui = (QuerySliderUI) qWindowSlider.getUI();
			ui.showChart((IntervalXYDataset) arg);
		}
	}

	private void loadSettings() {
		QW_MIN = Settings.getInt("qwindow.data.min");
		QW_MAX = Settings.getInt("qwindow.data.max");
	}

}