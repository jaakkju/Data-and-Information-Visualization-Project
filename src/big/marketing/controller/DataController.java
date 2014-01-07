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
	static int QUERYWINDOW_SIZE = 1000 * 60 * 60;

	// qWindow variables store the data returned from mongo
	private List<HealthMessage> qWindowHealth = null;
	private List<IPSMessage> qWindowIPS = null;
	private List<FlowMessage> qWindowFlow = null;

	private List<Node> network = null;

	private Node[] highlightedNodes = null;
	private Node selectedNode = null;

	private Thread readingThread;

	public DataController() {
		loadSettings();
		this.mc = MongoController.getInstance();
		this.gc = new GephiController();
	}

	public void readData() {
		readingThread = new Thread(this, "DataReader");
		readingThread.start();
	}

	public void run() {

		// TODO These things would be better to do directly in readers
		// The readers should independently handle reading and storing to the
		// database when started from the interface. Every reader should handle it's own errors
		
		NetworkReader nReader = new NetworkReader(this.mc);
		ZipReader zReader = new ZipReader(this.mc);

		try {
			network = nReader.readNetwork();

			EnumMap<DataType, Boolean> presentInDatabase = new EnumMap<DataType, Boolean>(DataType.class);
			for (DataType t : DataType.values()) {
				presentInDatabase.put(t, mc.isDataInDatabase(t));
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
	 * @param date in milliseconds marking the center point of the query
	 * @return true if data queried successfully from mongo, false otherwise
	 */
	@SuppressWarnings("unchecked")
	public void moveQueryWindow(int msdate) {
		int start = msdate - QUERYWINDOW_SIZE / 2, end = msdate + QUERYWINDOW_SIZE / 2;

		qWindowHealth = (List<HealthMessage>) (List<?>) mc.getConstrainedEntries(DataType.HEALTH, "Time", start, end);
		qWindowIPS = (List<IPSMessage>) (List<?>) mc.getConstrainedEntries(DataType.IPS, "Time", start, end);
		qWindowFlow = (List<FlowMessage>) (List<?>) mc.getConstrainedEntries(DataType.FLOW, "Time", start, end);
		
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
