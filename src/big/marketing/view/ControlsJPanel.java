package big.marketing.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;

import big.marketing.controller.DataController;
import big.marketing.data.DataType;
import big.marketing.test.DatabasePerformance;

public class ControlsJPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 7478563340170330453L;
	private final DataController controller;
	private JButton startReadingButton, resetDatabaseButton, perfTestButton, qWindowButton;

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

				for (DataType t : DataType.values()) {
					controller.getMongoController().clearCollection(t);
				}
			}

		});
		add(resetDatabaseButton);

		perfTestButton = new JButton("performance test");
		perfTestButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						DatabasePerformance.main(null);

					}
				}, "PerformanceTester").start();
			}
		});

		add(perfTestButton);

		qWindowButton = new JButton("Test qWindow");
		qWindowButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.moveQueryWindow(1364830798);
			}

		});
		add(qWindowButton);
	}

	@Override
	public void update(Observable o, Object arg) {

	}
}