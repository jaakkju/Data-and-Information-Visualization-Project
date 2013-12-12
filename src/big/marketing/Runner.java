package big.marketing;

import java.io.IOException;
import java.util.ArrayList;

import big.marketing.data.FileReader;
import big.marketing.data.Node;
import big.marketing.ui.NetworkMonitorFrame;

public class Runner {

	public static void main(String[] args) {

		// THIS IS ALSO JUST A TEST TO SEE THAT THINGS WORK - READS UNZIPPED DESCRIPTION FILE TO Arraylist<Node>
		FileReader reader = new FileReader();
		try {
			ArrayList<Node> network = reader.readNetworkDescription(FileReader.FILE_FOLDER + FileReader.FILE_DESCRIPTION);
			
			for (Node node : network) {
	         System.out.println(node.toString());
         }
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		NetworkMonitorFrame monitor = new NetworkMonitorFrame();
	}
	
	

}
