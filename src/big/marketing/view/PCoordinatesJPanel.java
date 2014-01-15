package big.marketing.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import big.marketing.controller.DataController;
import big.marketing.data.HealthMessage;
import big.marketing.data.QueryWindowData;
import big.marketing.xdat.DataSheet;
import big.marketing.xdat.ParallelCoordinatesChart;
import big.marketing.xdat.ParallelCoordinatesChartPanel;

public class PCoordinatesJPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 7723958207563842249L;
	private static final int WITDH = 200;
	private static final int HEIGHT = 200;

	private final DataController controller;

	private DataSheet data;
	private ParallelCoordinatesChart chart;
	private ParallelCoordinatesChartPanel chartPanel;

	public PCoordinatesJPanel(DataController controller) {
		this.setPreferredSize(new Dimension(WITDH, HEIGHT));
		this.setLayout(new BorderLayout());
		this.controller = controller;
	}

	@Override
	public void update(Observable o, Object obj) {
		if (obj instanceof QueryWindowData) {
			QueryWindowData newData = (QueryWindowData) obj;
			List<HealthMessage> healthMessages = newData.getHealthData();

			// Do refreshing of the parallel coordinates here
			data = new DataSheet(healthMessages);
			chart = new ParallelCoordinatesChart(data, this.getSize());
			chartPanel = new ParallelCoordinatesChartPanel(chart, data);
			this.add(chartPanel);
		}
	}
}
