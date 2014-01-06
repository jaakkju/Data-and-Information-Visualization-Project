package big.marketing.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JFrame;

import big.marketing.Settings;

public class WindowFrame extends JFrame {
   private static final long serialVersionUID = -8346810238547214403L;
	private static int FRAME_WIDTH = 1200;
	private static int FRAME_HEIGHT = 600;
	private static final String FRAME_TITLE = "eyeNet - Network Monitor";
	
	
	public WindowFrame(JComponent graphPanel, JComponent pCoordinatesPanel, JComponent controlsPanel) {
		loadSettings();
		this.setTitle(FRAME_TITLE);
		this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		addComponent(getContentPane(), gbl, graphPanel, 0, 0, 1, 1, 0.5, 0.5);
		addComponent(getContentPane(), gbl, pCoordinatesPanel, 0, 1, 1, 1, 0.5, 0.5);
		addComponent(getContentPane(), gbl, controlsPanel, 0, 2, 1, 1, 0, 0);
		
		this.pack();
		this.setVisible(true);
   }
	
	private void loadSettings() {
		FRAME_HEIGHT= Settings.getInt("view.frame.height");
		FRAME_WIDTH= Settings.getInt("view.frame.width");
	}
	
	static void addComponent(Container cont, GridBagLayout gbl, Component c,
			int x, int y, int width, int height, double weightx, double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		int border = 2;
		gbc.insets = new Insets(border / 2, border, border / 2, border);
		gbl.setConstraints(c, gbc);
		cont.add(c);
	}
}
