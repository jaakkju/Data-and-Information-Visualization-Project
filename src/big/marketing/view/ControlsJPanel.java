package big.marketing.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import big.marketing.Settings;
import big.marketing.controller.DataController;

public class ControlsJPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 7478563340170330453L;
	private final DataController controller;
	private JSlider qWindowSlider;

	static Logger logger = Logger.getLogger(ControlsJPanel.class);
	private static int QW_MIN = 0, QW_MAX = 1217384;

	public ControlsJPanel(final DataController controller) {
		loadSettings();
		this.controller = controller;
		this.setLayout(new BorderLayout());

		qWindowSlider = new JSlider(JSlider.HORIZONTAL, QW_MIN, QW_MAX, QW_MAX / 2);
		qWindowSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					logger.info("Slider moved to " + (int) source.getValue());
				}
			}
		});

		Font font = new Font("Serif", Font.ITALIC, 15);
		qWindowSlider.setFont(font);

		add(qWindowSlider);
	}

	@Override
	public void update(Observable o, Object arg) {

	}

	private void loadSettings() {
		QW_MIN = Settings.getInt("qwindow.data.min");
		QW_MAX = Settings.getInt("qwindow.data.max");
	}

}