package big.marketing.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;

import big.marketing.Application;
import big.marketing.Settings;
import big.marketing.controller.DataController;

public class WindowFrame extends JFrame {
	private static final long serialVersionUID = -8346810238547214403L;
	static Logger logger = Logger.getLogger(WindowFrame.class);

	static int FRAME_WIDTH = 1200;
	static int FRAME_HEIGHT = 600;
	private static final String FRAME_TITLE = "eyeNet - Network Monitor";

	public WindowFrame(DataController controller, JComponent graphPanel, JComponent pCoordinatesPanel, JComponent controlsPanel) {
		loadSettings();

		this.setTitle(FRAME_TITLE);
		this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Application.quit();
				super.windowClosing(e);
			}
		});

		this.setLayout(new BorderLayout());
		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphPanel, pCoordinatesPanel);
		add(splitter, BorderLayout.CENTER);
		add(controlsPanel, BorderLayout.SOUTH);

		this.pack();
		splitter.setDividerLocation(0.5);
		this.pack();
		this.setVisible(true);
	}

	private void loadSettings() {
		FRAME_HEIGHT = Settings.getInt("view.frame.height");
		FRAME_WIDTH = Settings.getInt("view.frame.width");
	}

}
