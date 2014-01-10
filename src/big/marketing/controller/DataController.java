package big.marketing.controller;

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
	public static int QUERYWINDOW_SIZE = 3600;

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
		this.gc = new GephiController(this);
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

		// Handling all reader errors here
		try {
			network = nReader.readNetwork();
			zReader.read(DataType.FLOW, DataType.IPS, DataType.HEALTH);
			mc.flushBuffers();
			logger.info("Finished Reading Data");
		} catch (Exception err) {
			logger.error("Cannot read data", err);
		}
	}

	/**
	 * Moves QueryWindow to certain position in time and queries data to qWindow
	 * variables from mongo Hides mongo implementation details from views
	 * 
	 * @param date in milliseconds marking the center point of the query
	 * @return true if data was stored into queryWindow variables otherwise false
	 */
	@SuppressWarnings("unchecked")
	public boolean moveQueryWindow(int time) {
		int start = time - QUERYWINDOW_SIZE / 2, end = time + QUERYWINDOW_SIZE / 2;
		long startTime = System.currentTimeMillis();

		qWindowHealth = (List<HealthMessage>) (List<?>) mc.getConstrainedEntries(DataType.HEALTH, "time", start, end);
		qWindowIPS = (List<IPSMessage>) (List<?>) mc.getConstrainedEntries(DataType.IPS, "time", start, end);
		qWindowFlow = (List<FlowMessage>) (List<?>) mc.getConstrainedEntries(DataType.FLOW, "time", start, end);

		gc.load(qWindowFlow);

		logger.info("Moved qWindow to " + time + ", Query took " + (System.currentTimeMillis() - startTime) + " ms,  Window size: "
		      + QUERYWINDOW_SIZE + " sec, Flow: " + qWindowFlow.size() + " objects, Health: " + qWindowHealth.size() + " objects, IPS: "
		      + qWindowIPS.size() + " objects");

		return !qWindowFlow.isEmpty() & !qWindowHealth.isEmpty() & !qWindowHealth.isEmpty();
	}

	/**
	 * This is the same as moveQueryWindow, but queries only certain type of data
	 * @param time date in milliseconds marking the center point of the query
	 * @param date in milliseconds marking the center point of the query
	 * @return true if data was stored into queryWindow variables otherwise false
	 */
	@SuppressWarnings("unchecked")
	public boolean moveQueryWindow(int time, DataType t) {
		int start = time - QUERYWINDOW_SIZE / 2, end = time + QUERYWINDOW_SIZE / 2;
		long startTime = System.currentTimeMillis();

		switch (t) {
		case FLOW:
			qWindowFlow = (List<FlowMessage>) (List<?>) mc.getConstrainedEntries(DataType.FLOW, "time", start, end);
			logger.info("Moved qWindow to " + time + ", Query took " + (System.currentTimeMillis() - startTime) + " ms,  Window size: "
			      + QUERYWINDOW_SIZE + " sec, Flow: " + qWindowFlow.size() + " objects");
			break;

		case HEALTH:
			qWindowHealth = (List<HealthMessage>) (List<?>) mc.getConstrainedEntries(DataType.HEALTH, "time", start, end);
			logger.info("Moved qWindow to " + time + ", Query took " + (System.currentTimeMillis() - startTime) + " ms,  Window size: "
			      + QUERYWINDOW_SIZE + " sec, Health: " + qWindowHealth.size() + " objects");
			break;

		case IPS:
			qWindowIPS = (List<IPSMessage>) (List<?>) mc.getConstrainedEntries(DataType.IPS, "time", start, end);
			logger.info("Moved qWindow to " + time + ", Query took " + (System.currentTimeMillis() - startTime) + " ms,  Window size: "
			      + QUERYWINDOW_SIZE + " sec, IPS: " + qWindowIPS.size() + " objects");
			break;

		case DESCRIPTION:
			break;

		default:
			break;
		}

		return !qWindowFlow.isEmpty() || !qWindowHealth.isEmpty() || !qWindowHealth.isEmpty();
	}

	private void loadSettings() {
		QUERYWINDOW_SIZE = Settings.getInt("controller.querywindow.size");
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
