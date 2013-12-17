package big.marketing.controller;

import java.util.Observable;

import big.marketing.data.Node;

public class DataController extends Observable {
	// http://docs.oracle.com/javase/7/docs/api/java/util/Observable.html
	
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
