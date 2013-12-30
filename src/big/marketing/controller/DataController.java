package big.marketing.controller;

import java.io.IOException;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.Logger;

import big.marketing.data.DataType;
import big.marketing.data.Node;
import big.marketing.reader.NetworkReader;
import big.marketing.reader.ZipReader;

public class DataController extends Observable {
	// http://docs.oracle.com/javase/7/docs/api/java/util/Observable.html
	
	static Logger logger = Logger.getLogger(DataController.class);
	private Node[] highlightedNodes = null;
	private Node selectedNode = null;
	private MongoController mongoController;

	private List<Node> network;

	public DataController() {
		this.mongoController = new MongoController();

		NetworkReader nReader = new NetworkReader(this.mongoController);
		ZipReader zReader = new ZipReader(this.mongoController);
		
		try {
			// TODO Catch all reading error in DataController
			network = nReader.readNetwork();
			for (int week = 1; week <= 2; week++) {
				zReader.read(DataType.FLOW, week);
				zReader.read(DataType.HEALTH, week);
				zReader.read(DataType.IPS, week);
			}			
			
		} catch (IOException err) {
			logger.error("Error while loading network data.", err);
		}
	}

	public List<Node> getNetwork() {
		return network;
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
