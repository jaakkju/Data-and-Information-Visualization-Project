package big.marketing.controller;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.Logger;

import big.marketing.Settings;
import big.marketing.data.DataType;
import big.marketing.data.FlowMessage;
import big.marketing.data.HealthMessage;
import big.marketing.data.IPSMessage;
import big.marketing.data.Node;
import big.marketing.reader.NetworkReader;
import big.marketing.reader.ZipReader;

public class DataController extends Observable implements Runnable {
	// http://docs.oracle.com/javase/7/docs/api/java/util/Observable.html

	static Logger logger = Logger.getLogger(DataController.class);
	private GephiController gc;
	private MongoController mc;

	// qWindow size in milliseconds, default value 1 hour
	static int QUERYWINDOW_SIZE = 6000;

	// qWindow variables store the data returned from mongo
	private List<HealthMessage> qWindowHealth = null;
	private List<IPSMessage> qWindowIPS = null;
	private List<FlowMessage> qWindowFlow = null;

	private List<Node> network = null;

	private Node[] highlightedNodes = null;
	private Node selectedNode = null;

	private Thread readingThread, processingThread;

	private static DataController instance;
	
	public static DataController getInstance() {
		if (instance == null)
			instance = new DataController();
	   return instance;
   }
	
	private DataController() {
		loadSettings();
		this.mc = MongoController.getInstance();
		this.gc = new GephiController();
	}

	public void readData() {
		readingThread = new Thread(this, "DataReader");
		readingThread.start();
	}

	public void processData() {
		DataProcessor dp = new DataProcessor(this.mc, DataType.FLOW, DataType.IPS, DataType.HEALTH);
		processingThread = new Thread(dp, "ProcessingThread");
		processingThread.start();
	}
	
	public void run() {

		NetworkReader nReader = new NetworkReader(this.mc);
		ZipReader zReader = new ZipReader(this.mc);
		
		network = nReader.readNetwork();
		zReader.read(DataType.FLOW, DataType.IPS, DataType.HEALTH);
	}

	/**
	 * Moves QueryWindow to certain position in time and queries data to qWindow
	 * variables from mongo Hides mongo implementation details from views
	 * 
	 * @param date in milliseconds marking the center point of the query
	 * @return TODO return some info
	 */
	@SuppressWarnings("unchecked")
	public void moveQueryWindow(int msdate) {
		int start = msdate - QUERYWINDOW_SIZE / 2, end = msdate + QUERYWINDOW_SIZE / 2;
		long startTime = System.currentTimeMillis();

		qWindowHealth = (List<HealthMessage>) (List<?>) mc.getConstrainedEntries(DataType.HEALTH, "Time", start, end);
		qWindowIPS = (List<IPSMessage>) (List<?>) mc.getConstrainedEntries(DataType.IPS, "Time", start, end);
		qWindowFlow = (List<FlowMessage>) (List<?>) mc.getConstrainedEntries(DataType.FLOW, "Time", start, end);

		logger.info("Moved qWindow to " + msdate + ", Query took " + (System.currentTimeMillis() - startTime) + " ms,  Window size: "
		      + QUERYWINDOW_SIZE + " ms, Flow: " + qWindowFlow.size() + " objects, Health: " + qWindowHealth.size() + " objects, IPS: "
		      + qWindowIPS.size() + " objects");
		// TODO moveQueryWindow should return some info about the success of the database query
	}

	private void loadSettings() {
		try {
			QUERYWINDOW_SIZE = Integer.valueOf(Settings.get("controller.querywindow.size"));
		} catch (NumberFormatException err) {
			logger.error("Loading settings failed, number conversion error", err);
		}
	}

	public List<Node> getNetwork() {
		return network;
	}

	public List<HealthMessage> getqWindowHealth() {
		return qWindowHealth;
	}

	public List<IPSMessage> getqWindowIPS() {
		return qWindowIPS;
	}

	public List<FlowMessage> getqWindowFlow() {
		return qWindowFlow;
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

	public MongoController getMongoController() {
		return mc;
	}

	public GephiController getGephiController() {
		return gc;
	}
}
