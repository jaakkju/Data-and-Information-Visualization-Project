package big.marketing.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.SpigotImporter;

import big.marketing.data.FlowMessage;
import big.marketing.data.Node;
import big.marketing.data.QueryWindowData;

public class GephiImporter implements SpigotImporter {

	private ContainerLoader container;
	private Report report;
	private QueryWindowData data;
	private Map<String, Node> ipMap;
	Map<String, NodeDraft> nodes;
	private Node[] selectedNodes;
	private AttributeColumn ipColumn;

	public GephiImporter(QueryWindowData dataset, Map<String, Node> ipMap, Node[] selectedNodes) {
		this.data = dataset;
		nodes = new HashMap<String, NodeDraft>();
		this.ipMap = ipMap;
		this.selectedNodes = selectedNodes;
	}

	/**
	 * Careful, multi edges are ignored...
	 * @param loader
	 * @return
	 */
	@Override
	public boolean execute(ContainerLoader loader) {
		report = new Report();
		container = loader;
		ContainerLoader.DraftFactory fact = loader.factory();
		// import...
		// convert flow messages to gephi internal graph structure
		List<Node> selected = new ArrayList<Node>();
		for (Node n : selectedNodes) {
			selected.add(n);
		}

		AttributeModel am = container.getAttributeModel();
		AttributeTable nt = am.getNodeTable();
		ipColumn = nt.getColumn("ip");
		if (ipColumn == null)
			ipColumn = nt.addColumn("ip", AttributeType.STRING);

		for (FlowMessage message : data.getFlowData()) {

			// dont use nodes that are not selected
			Node srcNetworkNode = ipMap.get(message.getSourceIP());
			Node destNetworkNode = ipMap.get(message.getDestinationIP());

			if (isVisibleNode(srcNetworkNode, selected) || isVisibleNode(destNetworkNode, selected)) {

				NodeDraft src = createNode(message.getSourceIP(), loader);
				NodeDraft dst = createNode(message.getDestinationIP(), loader);

				if (!loader.edgeExists(src, dst)) {
					EdgeDraft edge = fact.newEdgeDraft();
					edge.setSource(src);
					edge.setTarget(dst);
					edge.setWeight(1);
					loader.addEdge(edge);
				} else {
					EdgeDraft e = loader.getEdge(src, dst);
					e.setWeight(e.getWeight() + 1);
				}

			}

		}

		return true;
	}

	private NodeDraft createNode(String name, ContainerLoader loader) {
		NodeDraft draft = nodes.get(name);
		if (draft == null) {
			draft = loader.factory().newNodeDraft();
			draft.setFixed(false);
			Node networkNode = ipMap.get(name);
			String label = "extern";
			if (networkNode != null)
				label = networkNode.getHostName();
			draft.addAttributeValue(ipColumn, name);
			draft.setLabel(label.substring(0, 1));
			nodes.put(name, draft);
			loader.addNode(draft);
		} else {
			draft.setFixed(true);
		}
		return draft;
	}

	private boolean isVisibleNode(Node n, List<Node> selected) {
		return (n != null && selected.contains(n));
	}

	@Override
	public ContainerLoader getContainer() {
		return container;
	}

	@Override
	public Report getReport() {
		return report;
	}

}
