package big.marketing.view;

import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import big.marketing.controller.DataController;

public class ParallelCoordinatesJPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 7723958207563842249L;
	private static final int WITDH = 200;
	private static final int HEIGHT = 200;
	
	private final DataController controller;
	 
	public ParallelCoordinatesJPanel(DataController controller) {
		this.setPreferredSize(new Dimension(WITDH, HEIGHT));
	   this.controller = controller;
   }

	@Override
   public void update(Observable o, Object arg) {
		System.out.println(this.getClass() + " Selected nodes: ");
		
		// THIS IS REALLY NOT NICE WAY TO CODE THIS, BUT FOR AN EXAMPLE IT WORKS
		for (int i = 0; i < controller.getSelectedNodes().length; i++) {
	      System.out.println(controller.getSelectedNodes()[i].toString());
      }
   }
}
