package big.marketing.view;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;

import big.marketing.Settings;

public class WindowFrame extends JFrame {
   private static final long serialVersionUID = -8346810238547214403L;
	private static int FRAME_WIDTH = 1200;
	private static int FRAME_HEIGHT = 600;
	private static final String FRAME_TITLE = "eyeNet - Network Monitor";
	private static final int rows = 2;
	private static final int cols = 2;
	
	
	public WindowFrame() {
		loadSettings();
		this.setTitle(FRAME_TITLE);
		this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new GridLayout(rows, cols));
		this.pack();
		this.setVisible(true);
   }
	
	private void loadSettings() {
		FRAME_HEIGHT= Settings.getInt("view.frame.height");
		FRAME_WIDTH= Settings.getInt("view.frame.width");
	}
	
}
