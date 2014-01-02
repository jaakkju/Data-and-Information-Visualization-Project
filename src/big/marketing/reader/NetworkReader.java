package big.marketing.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import big.marketing.Settings;
import big.marketing.controller.MongoController;
import big.marketing.data.DataType;
import big.marketing.data.Node;

/**
 * @author jaakkju
 */
public class NetworkReader {
	static Logger logger = Logger.getLogger(NetworkReader.class);
	
	public static String FILE_FOLDER = "data/";
	public static String FILE_DESCRIPTION = "BigMktNetwork.txt";
	
	private static final String regex = "\\s";
	private static final int regLimit = 3;
	private MongoController mongo;

	public NetworkReader(MongoController mongo) {
	   loadSettings();
	   this.mongo = mongo;
   }
	private void loadSettings() {
		FILE_FOLDER = Settings.get("reader.folder");
		FILE_DESCRIPTION= Settings.get("reader.files.description");
	}
	/**
	 * reads network description file to arraylist<Node>
	 * @return network as arraylist<Node> or null if no nodes were read from the file
	 * @throws IOException
	 */
	public ArrayList<Node> readNetwork() throws IOException {
		ArrayList<Node> network = new ArrayList<Node>();

		File file = new File(FILE_FOLDER + FILE_DESCRIPTION);
		
		logger.info("Loading network description file from " + file.getAbsolutePath());
		
		mongo.clearCollection(DataType.DESCRIPTION);
		
		FileInputStream fileIn = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fileIn));

		String strLine;
		int count = 0;
		while ((strLine = br.readLine()) != null) {
			if (!strLine.startsWith("#") && strLine.length() > 0) {
				Node node = new Node(strLine.split(regex, regLimit));
				mongo.storeEntry(DataType.DESCRIPTION, node.asDBObject());
				network.add(node);
				count++;
			}
		}

		fileIn.close();
		logger.info("Network description file " + file.getName() + " with " + count + " network nodes successfully read and stored to mongo");
		return network.isEmpty() ? null : network;
	}
}
