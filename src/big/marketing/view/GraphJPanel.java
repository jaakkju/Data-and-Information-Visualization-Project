package big.marketing.view;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import big.marketing.controller.DataController;

public class GraphJPanel extends JPanel implements Observer {
   private static final long serialVersionUID = -7417639995072699909L;
	private final DataController controller;
	
	public GraphJPanel(DataController controller) {
	   this.controller = controller;
   }
	

	@Override
   public void update(Observable o, Object arg) {
		System.out.println(this.getClass() + " Selected nodes: ");
		
		// THIS IS REALLY NOT NICE WAY TO CODE THIS, BUT FOR AN EXAMPLE IT WORKS
		for (int i = 0; i < controller.getHighlightedNodes().length; i++) {
	      System.out.println(controller.getHighlightedNodes()[i].toString());
      }
   }

}
