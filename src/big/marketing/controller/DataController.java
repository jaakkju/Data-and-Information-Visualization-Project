package big.marketing.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.Logger;

import big.marketing.data.DataType;
import big.marketing.data.HealthMessage;
import big.marketing.data.Node;
import big.marketing.reader.NetworkReader;
import big.marketing.reader.ZipReader;

public class DataController extends Observable implements Runnable {
	// http://docs.oracle.com/javase/7/docs/api/java/util/Observable.html

	static Logger logger = Logger.getLogger(DataController.class);
	private MongoController mongoController;
	private GephiController gephiController;

	// qWindow size in milliseconds
	static final int QUERYWINDOW_SIZE = 1000 * 60 * 60;

	// qWindow variables store the data returned from mongo
	private ArrayList<HealthMessage> qWindowHealth = null;
	private ArrayList<HealthMessage> qWindowIPS = null;
	private ArrayList<HealthMessage> qWindowFlow = null;

	private List<Node> network = null;

	private Node[] highlightedNodes = null;
	private Node selectedNode = null;

	private Thread readingThread;

	public DataController() {
		this.mongoController = new MongoController();
		this.gephiController = new GephiController();
	}

	public void readData() {
		readingThread = new Thread(this, "DataReader");
		readingThread.start();
	}

	public void run() {

		NetworkReader nReader = new NetworkReader(this.mongoController);
		ZipReader zReader = new ZipReader(this.mongoController);

		try {
			// TODO Catch all reading error in DataController
			network = nReader.readNetwork();

			EnumMap<DataType, Boolean> presentInDatabase = new EnumMap<DataType, Boolean>(
					DataType.class);
			for (DataType t : DataType.values()) {
				presentInDatabase.put(t, mongoController.isDataInDatabase(t));
			}

			for (int week = 1; week <= 2; week++) {
				for (DataType t : DataType.values()) {
					if (!presentInDatabase.get(t))
						zReader.read(t, week);
				}
			}
		} catch (IOException err) {
			logger.error("Error while loading network data.", err);
		}
	}

	/**
	 * Moves QueryWindow to certain position in time and queries data to qWindow
	 * variables from mongo Hides mongo implementation details from views
	 * 
	 * @param date
	 *            in milliseconds
	 * @return true if data queried successfully from mongo, false otherwise
	 */
	public boolean moveQueryWindow(int msdate) {
		// TODO implement moveQueryWindow

		// TODO fetch health data

		// TODO fetch flow data

		// TODO fetch IPS data

		return false;
	}

	public List<Node> getNetwork() {
		return network;
	}

	public ArrayList<HealthMessage> getqWindowHealth() {
		return qWindowHealth;
	}

	public ArrayList<HealthMessage> getqWindowIPS() {
		return qWindowIPS;
	}

	public ArrayList<HealthMessage> getqWindowFlow() {
		return qWindowFlow;
	}

	public GephiController getGephiController() {
		return gephiController;
	}

	public void setMongoController(MongoController mongoController) {
		this.mongoController = mongoController;
	}

	public MongoController getMongoController() {
		return mongoController;
	}

	public void setHighlightedNodes(Node[] highlightedNodes) {
		this.highlightedNodes = highlightedNodes;
		setChanged();
	}

	public void setSelectedNode(Node selectedNode) {
		this.selectedNode = selectedNode;
		setChanged();
	}

	public Node[] getHighlightedNodes() {
		return highlightedNodes;
	}

	public Node getSelectedNode() {
		return selectedNode;
	}
}
