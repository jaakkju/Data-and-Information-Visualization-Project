package big.marketing.controller;

import java.util.List;
import java.util.Observable;

import org.apache.log4j.Logger;

import big.marketing.Settings;
import big.marketing.data.QueryWindowData;
import big.marketing.data.DataType;
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

	// currentQueryWindow stores the data returned from mongo
	private List<Node> network;
	
	private Node[] highlightedNodes = null;
	private Node selectedNode = null;

	private Thread readingThread, processingThread;
	private Player player;

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
	
	public void setTime(int newTime){
		setChanged();
		notifyObservers(newTime);
	}
	
	public void playStopButtonPressed(int startTime, int stepSize){
		if (player != null && player.isAlive()){
			// actually playing
			player.stopPlaying();
			logger.info("Waiting for current query to finish, then stopping playing");
		}
		else{
			// no player yet or playing finished
			player = new Player(this,startTime,stepSize,100);
			player.startPlaying();
			logger.info("Started playing at "+startTime+" with stepSize "+stepSize );
		}
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
	public boolean moveQueryWindow(int time) {
		int start = time - QUERYWINDOW_SIZE / 2, end = time + QUERYWINDOW_SIZE / 2;
		long startTime = System.currentTimeMillis();
		QueryWindowData currentQueryWindow = new QueryWindowData(null, null, null, network);
		currentQueryWindow.setFlow(mc.getConstrainedEntries(DataType.FLOW, "time", start, end));
		currentQueryWindow.setIps(mc.getConstrainedEntries(DataType.IPS, "time", start, end));
		currentQueryWindow.setHealth(mc.getConstrainedEntries(DataType.HEALTH, "time", start, end));

		gc.load(currentQueryWindow);

		logger.info("Moved qWindow to " + time + ", Query took " + (System.currentTimeMillis() - startTime) + " ms,  Window size: "
		      + QUERYWINDOW_SIZE + " sec, Flow: " + currentQueryWindow.getFlowData().size() + " objects, Health: "
		      + currentQueryWindow.getHealthData().size() + " objects, IPS: " + currentQueryWindow.getIPSData().size() + " objects");

		setChanged();
		notifyObservers(currentQueryWindow);
		boolean returnValue =!currentQueryWindow.isEmpty(); 
		currentQueryWindow = null;
		System.gc();

		return returnValue;
	}

	/**
	 * This is the same as moveQueryWindow, but queries only certain type of data
	 * @param time date in milliseconds marking the center point of the query
	 * @param date in milliseconds marking the center point of the query
	 * @return true if data was stored into queryWindow variables otherwise false
	 */
	public boolean moveQueryWindow(int time, DataType t) {
		int start = time - QUERYWINDOW_SIZE / 2, end = time + QUERYWINDOW_SIZE / 2;
		long startTime = System.currentTimeMillis();
		QueryWindowData currentQueryWindow = new QueryWindowData(null, null, null, network);
		List<Object> newEntries = mc.getConstrainedEntries(t, "time", start, end);
		switch (t) {
		case FLOW:
			currentQueryWindow.setFlow(newEntries);
			gc.load(currentQueryWindow);
			break;

		case HEALTH:
			currentQueryWindow.setHealth(newEntries);
			break;

		case IPS:
			currentQueryWindow.setIps(newEntries);
			break;

		case DESCRIPTION:
			return false;
		}

		setChanged();
		logger.info("Moved qWindow to " + time + ", Query took " + (System.currentTimeMillis() - startTime) + " ms,  Window size: "
		      + QUERYWINDOW_SIZE + " sec, " + t.name() + ": " + newEntries.size() + " objects");
		notifyObservers(currentQueryWindow);
		return !currentQueryWindow.isEmpty();
	}

	private void loadSettings() {
		QUERYWINDOW_SIZE = Settings.getInt("controller.querywindow.size");
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
