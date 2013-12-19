package big.marketing;

import java.io.IOException;
import java.util.ArrayList;

import big.marketing.controller.DataController;
import big.marketing.data.Node;
import big.marketing.reader.NetworkReader;
import big.marketing.view.ControlsJPanel;
import big.marketing.view.GraphJPanel;
import big.marketing.view.ParallelCoordinatesJPanel;

public class Application {
	public static final String FILE_FOLDER = "./data/";
	public static final String FILE_DESCRIPTION = "BigMktNetwork.txt";

	public static void main(String[] args) {

		// THIS IS ALSO JUST A TEST TO SEE THAT THINGS WORK - READS UNZIPPED
		// DESCRIPTION FILE TO Arraylist<Node>
		NetworkReader nReader = new NetworkReader();
		ArrayList<Node> network = null;
		try {
			network = nReader.readNetworkDescription(Application.FILE_FOLDER + Application.FILE_DESCRIPTION);

			for (Node node : network) {
				// TEST System.out.println(node.toString());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// DataController implements observer pattern and passes changes in data to JPanels
		DataController test = new DataController();

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
