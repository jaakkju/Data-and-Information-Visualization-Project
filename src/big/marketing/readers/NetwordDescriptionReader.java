package big.marketing.readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import big.marketing.data.Node;

/**
 * @author jaakkju
 */
public class NetwordDescriptionReader {
	
	private static final String regex = "\\s";
	private static final int regLimit = 3;
	
	/**
	 * reads network description file to arraylist<Node>
	 * @param filePath file location
	 * @return network as arraylist<Node> or null if no nodes were read from the file
	 * @throws IOException
	 */
	public ArrayList<Node> readNetworkDescription(String filePath) throws IOException {
		ArrayList<Node> network = new ArrayList<>();

		File file = new File(filePath);
		FileInputStream fileIn = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fileIn));

		String strLine;
		while ((strLine = br.readLine()) != null) {
			if (!strLine.startsWith("#") && strLine.length() > 0) {
				network.add(new Node(strLine.split(regex, regLimit)));
			}
		}

		fileIn.close();
		return network.isEmpty() ? null : network;
	}
}
