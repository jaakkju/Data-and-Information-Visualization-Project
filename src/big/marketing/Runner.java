package big.marketing;

import java.io.IOException;
import java.util.ArrayList;

import big.marketing.data.Node;
import big.marketing.readers.NetwordDescriptionReader;
import big.marketing.ui.NetworkMonitorFrame;

public class Runner {
	public static final String FILE_FOLDER = "./data/";
	public static final String FILE_DESCRIPTION = "BigMktNetwork.txt";

	public static void main(String[] args) {

		// THIS IS ALSO JUST A TEST TO SEE THAT THINGS WORK - READS UNZIPPED DESCRIPTION FILE TO Arraylist<Node>
		NetwordDescriptionReader nReader = new NetwordDescriptionReader();
		try {
			ArrayList<Node> network = nReader.readNetworkDescription(Runner.FILE_FOLDER + Runner.FILE_DESCRIPTION);
			
			for (Node node : network) {
	         System.out.println(node.toString());
         }
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		NetworkMonitorFrame monitor = new NetworkMonitorFrame();
	}
}
