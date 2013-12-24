package big.marketing.controller;

import java.io.IOException;
import java.util.List;
import java.util.Observable;

import big.marketing.Application;
import big.marketing.data.DataType;
import big.marketing.data.Node;
import big.marketing.reader.NetworkReader;
import big.marketing.reader.ZipReader;

public class DataController extends Observable {
	// http://docs.oracle.com/javase/7/docs/api/java/util/Observable.html
	
	private Node[] highlightedNodes = null;
	private Node selectedNode = null;
	private MongoController mongoController;
	
	private List<Node> network;
	
	public DataController(MongoController mc) {
		super();
		this.mongoController = mc;

		// read in the data:
		NetworkReader nReader = new NetworkReader(mc);
		try {
			network = nReader.readNetwork(Application.FILE_FOLDER + Application.FILE_DESCRIPTION);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ZipReader zReader = new ZipReader(mongoController);
		for (int week=1;week<=2;week++){
			zReader.read(DataType.FLOW, week);
			zReader.read(DataType.HEALTH, week);
			zReader.read(DataType.IPS, week);
			// DESCRIPTIONs have been read above with the NetworkDescriptionReader
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
