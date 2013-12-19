package big.marketing.controller;

import java.util.Observable;

import big.marketing.data.Node;

public class DataController extends Observable {
	// http://docs.oracle.com/javase/7/docs/api/java/util/Observable.html
	
	private Node[] highlightedNodes = null;
	private Node selectedNode = null;

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
