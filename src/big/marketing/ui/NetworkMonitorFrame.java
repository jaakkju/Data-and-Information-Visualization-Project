package big.marketing.ui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;

public class NetworkMonitorFrame extends JFrame {
   private static final long serialVersionUID = -8346810238547214403L;
	private static final int FRAME_WIDTH = 1200;
	private static final int FRAME_HEIGHT = 600;
	private static final String FRAME_TITLE = "Big Marketing Network Monitor";
	private static final int rows = 2;
	private static final int cols = 2;
	
	
	public NetworkMonitorFrame() {
		addPanels();
		
		this.setTitle(FRAME_TITLE);
		this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new GridLayout(rows, cols));
		this.pack();
		this.setVisible(true);
   }
	
	private void addPanels() {
		this.add(new ControlsJPanel());
		this.add(new GraphJPanel());
		this.add(new ParallerCoordinatesJPanel());
	}
}
