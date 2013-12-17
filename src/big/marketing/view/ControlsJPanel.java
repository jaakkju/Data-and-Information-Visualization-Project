package big.marketing.view;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import big.marketing.controller.DataController;

public class ControlsJPanel extends JPanel implements Observer {
   private static final long serialVersionUID = 7478563340170330453L;
	private final DataController controller;
	
	public ControlsJPanel(DataController controller) {
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
