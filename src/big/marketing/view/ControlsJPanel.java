package big.marketing.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;

import big.marketing.controller.DataController;
import big.marketing.data.DataType;

public class ControlsJPanel extends JPanel implements Observer {
   private static final long serialVersionUID = 7478563340170330453L;
	private final DataController controller;
	private JButton startReadingButton, resetDatabaseButton;
	public ControlsJPanel(final DataController controller) {
	   this.controller = controller;
	   startReadingButton = new JButton("Read Data");
	   startReadingButton.addActionListener(new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			controller.readData();
			
		}
	   });
	   add(startReadingButton);
	   
	   resetDatabaseButton = new JButton("Reset DB");
	   resetDatabaseButton.addActionListener(new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			for (DataType t : DataType.values()){
				controller.getMongoController().clearCollection(t);
			}
			
		}
		
	});
	   add(resetDatabaseButton);
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