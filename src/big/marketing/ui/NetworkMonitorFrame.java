package big.marketing.ui;

import java.awt.Dimension;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class NetworkMonitorFrame extends JFrame {
	private static final int FRAME_WIDTH = 1200;
	private static final int FRAME_HEIGHT = 600;
	private static final String FRAME_TITLE = "Big Marketing Network Monitor";
	
	
	public NetworkMonitorFrame() {
		this.setTitle(FRAME_TITLE);
		this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
   }
}
