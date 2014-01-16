package big.marketing.view;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import big.marketing.controller.DataController;
import big.marketing.data.HealthMessage;
import big.marketing.data.QueryWindowData;
import big.marketing.xdat.DataSheet;
import big.marketing.xdat.ParallelCoordinatesChart;
import big.marketing.xdat.ParallelCoordinatesChartPanel;

public class PCoordinatesJPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 7723958207563842249L;
	static Logger logger = Logger.getLogger(PCoordinatesJPanel.class);

	private final DataController controller;

	public PCoordinatesJPanel(DataController controller) {
		this.setLayout(new BorderLayout());
		this.controller = controller;
	}

	@Override
	public void update(Observable o, Object obj) {
		if (obj instanceof QueryWindowData) {
			QueryWindowData newData = (QueryWindowData) obj;
			List<HealthMessage> healthMessages = newData.getHealthData();

			// Do refreshing of the parallel coordinates here
			removeAll();
			DataSheet dataSheet = new DataSheet(healthMessages);
			ParallelCoordinatesChart chart = new ParallelCoordinatesChart(dataSheet, this.getSize());
			ParallelCoordinatesChartPanel panel = new ParallelCoordinatesChartPanel(chart, dataSheet);
			add(panel);
			revalidate();
		}
	}
}
