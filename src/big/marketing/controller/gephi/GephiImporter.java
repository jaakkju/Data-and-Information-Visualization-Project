package big.marketing.controller.gephi;

import java.util.HashMap;
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
	private Map<Node, Boolean> selectionMap;
	Map<String, NodeDraft> nodes;
	private AttributeColumn ipColumn, typeColumn;

	public GephiImporter(QueryWindowData dataset, Map<String, Node> ipMap, Node[] selectedNodes) {
		this.data = dataset;
		this.ipMap = ipMap;
		nodes = new HashMap<String, NodeDraft>();
		selectionMap = new HashMap<>();
		for (Node n : selectedNodes) {
			selectionMap.put(n, true);
		}
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

		AttributeModel am = container.getAttributeModel();
		AttributeTable nt = am.getNodeTable();
		ipColumn = nt.getColumn("ip");
		if (ipColumn == null)
			ipColumn = nt.addColumn("ip", AttributeType.STRING);
		typeColumn = nt.getColumn("hostType");
		if (typeColumn == null) {
			typeColumn = nt.addColumn("hostType", AttributeType.SHORT);
		}

		for (FlowMessage message : data.getFlowData()) {

			// dont use nodes that are not selected
			Node srcNetworkNode = ipMap.get(message.getSourceIP());
			Node destNetworkNode = ipMap.get(message.getDestinationIP());

			NodeDraft src = createNode(message.getSourceIP(), srcNetworkNode, loader);
			NodeDraft dst = createNode(message.getDestinationIP(), destNetworkNode, loader);

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

		return true;
	}

	private NodeDraft createNode(String name, Node node, ContainerLoader loader) {
		NodeDraft draft = nodes.get(name);
		if (draft == null) {
			draft = loader.factory().newNodeDraft();
			String label = "extern";
			draft.addAttributeValue(ipColumn, name);
			if (node != null) {
				label = node.getHostName();
				draft.addAttributeValue(typeColumn, node.getType());
			}
			draft.setLabel(label);
			nodes.put(name, draft);
			loader.addNode(draft);
		}
		return draft;
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
