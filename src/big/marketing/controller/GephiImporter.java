package big.marketing.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		System.out.println(selected.size());
		for (FlowMessage message : data.getFlowData()) {

			// dont use nodes that are not selected
			Node srcNetworkNode = ipMap.get(message.getSourceIP());
			Node destNetworkNode = ipMap.get(message.getDestinationIP());

			if (!isVisibleNode(srcNetworkNode, selected) && !isVisibleNode(destNetworkNode, selected)) {
				continue;
			}

			NodeDraft src = nodes.get(message.getSourceIP());
			if (src == null) {
				src = fact.newNodeDraft();
				Node networkNode = ipMap.get(message.getSourceIP());
				String label = "extern";
				if (networkNode != null)
					label = networkNode.getHostName();
				src.setLabel(label.substring(0, 1));
				nodes.put(message.getSourceIP(), src);
				loader.addNode(src);
			}

			NodeDraft dst = nodes.get(message.getDestinationIP());
			if (dst == null) {
				dst = fact.newNodeDraft();
				Node networkNode = ipMap.get(message.getDestinationIP());
				String label = "extern";
				if (networkNode != null)
					label = networkNode.getHostName();
				dst.setLabel(label.substring(0, 1));
				nodes.put(message.getDestinationIP(), dst);
				loader.addNode(dst);
			}
			if (!loader.edgeExists(src, dst)) {
				EdgeDraft edge = fact.newEdgeDraft();
				edge.setSource(src);
				edge.setTarget(dst);
				loader.addEdge(edge);
			}
		}
		return true;
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
