package big.marketing.view;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import big.marketing.controller.DataController;
import big.marketing.data.HealthMessage;
import big.marketing.data.Node;
import big.marketing.data.QueryWindowData;
import big.marketing.xdat.DataSheet;
import big.marketing.xdat.ParallelCoordinatesChart;
import big.marketing.xdat.ParallelCoordinatesChartPanel;

public class PCoordinatesJPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 7723958207563842249L;
	static Logger logger = Logger.getLogger(PCoordinatesJPanel.class);

	private final DataController controller;
	private ParallelCoordinatesChartPanel panel;

	public PCoordinatesJPanel(final DataController controller) {
		this.setLayout(new BorderLayout());
		this.controller = controller;

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// TODO: many events are fired, don't update everytime, very costly
				// FIX: update only on last event... (or maybe find a setting to fire events only at the end of resizing...)
				update(null, controller.getCurrentQueryWindow());
			}
		});
	}

	@Override
	public void update(Observable o, Object obj) {
		if (obj instanceof QueryWindowData) {
			QueryWindowData qWindow = (QueryWindowData) obj;
			List<HealthMessage> healthMessages = qWindow.getHealthData();
			updateParallelCoordinatesPanel(healthMessages);

		} else if (obj instanceof Node[]) {
			Node[] selectedNodes = (Node[]) obj;
			List<HealthMessage> healthMessages = controller.getCurrentQueryWindow().getHealthData();

			ArrayList<HealthMessage> selection = new ArrayList<>();
			for (Node node : selectedNodes) {
				for (HealthMessage healthMessage : healthMessages) {
					if (node.getHostName().equalsIgnoreCase(healthMessage.getHostname())) {
						selection.add(healthMessage);
					}
				}
			}
			updateParallelCoordinatesPanel(selection);
		}
	}

	private void updateParallelCoordinatesPanel(List<HealthMessage> healthMessages) {
		// Do refreshing of the parallel coordinates here

		removeAll();
		DataSheet dataSheet = new DataSheet(healthMessages);
		ParallelCoordinatesChart chart = new ParallelCoordinatesChart(dataSheet, this.getSize());
		panel = new ParallelCoordinatesChartPanel(chart, dataSheet, controller);
		add(panel);
		revalidate();
	}
}
