package big.marketing;

import java.util.List;

import big.marketing.controller.DataController;
import big.marketing.controller.MongoController;
import big.marketing.data.Node;
import big.marketing.view.ControlsJPanel;
import big.marketing.view.GraphJPanel;
import big.marketing.view.ParallelCoordinatesJPanel;

public class Application {
	public static final String FILE_FOLDER = "./data/";
	public static final String FILE_DESCRIPTION = "BigMktNetwork.txt";

	public static void main(String[] args) {

		MongoController mongo = new MongoController();
		DataController test = new DataController(mongo);
		List<Node> network = test.getNetwork();
		
		// DataController implements observer pattern and passes changes in data to JPanels

		test.addObserver(new ParallelCoordinatesJPanel(test));
		test.addObserver(new ControlsJPanel(test));
		test.addObserver(new GraphJPanel(test));

		System.out.println(test.getClass() + " Observers: " + test.countObservers());
		
		Node[] highlightedNodes = {network.get(0), network.get(1)};
		
		test.setHighlightedNodes(highlightedNodes);
		test.notifyObservers();

		// TEST TO SEE THAT THE FRAME WORKS NetworkMonitorFrame monitor = new
		// NetworkMonitorFrame();
	}

}
