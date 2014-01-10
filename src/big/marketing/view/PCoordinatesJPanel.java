package big.marketing.view;

import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import big.marketing.controller.DataController;

public class PCoordinatesJPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 7723958207563842249L;
	private static final int WITDH = 200;
	private static final int HEIGHT = 200;

	private final DataController controller;

	public PCoordinatesJPanel(DataController controller) {
		this.setPreferredSize(new Dimension(WITDH, HEIGHT));
		this.controller = controller;
	}

	@Override
	public void update(Observable o, Object arg) {

	}
}
