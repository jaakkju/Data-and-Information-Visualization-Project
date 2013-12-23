package big.marketing.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import big.marketing.Application;
import big.marketing.data.DataType;
import big.marketing.data.Node;
import big.marketing.reader.NetwordDescriptionReader;
import big.marketing.reader.ZipReader;

public class DataController extends Observable {
	// http://docs.oracle.com/javase/7/docs/api/java/util/Observable.html
	
	private MongoController mongoController;
	
	private List<Node> network;
	
	public DataController(MongoController mc) {
		super();
		this.mongoController = mc;

		// read in the data:
		NetwordDescriptionReader nReader = new NetwordDescriptionReader(mc);
		try {
			network = nReader.readNetworkDescription(Application.FILE_FOLDER + Application.FILE_DESCRIPTION);
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
	
	// Controller stores selected nodes that are updated from the views
	private Node[] selectedNodes = null;

	public boolean setSelectedNodes(Node[] nodes) {
		this.selectedNodes = nodes;
		setChanged();
		
		return true;
	}
	
	public Node[] getSelectedNodes() {
		return this.selectedNodes;
	}
}
