package big.marketing.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.SpigotImporter;

import big.marketing.data.FlowMessage;

public class GephiImporter implements SpigotImporter {
	private ContainerLoader container;
	private Report report;
	private List<FlowMessage> currentFlow;
	private Map<String, String> ipHostMap;
	Map<String, NodeDraft> nodes;

	public GephiImporter(List<FlowMessage> flows, Map<String, String> ipHostMap) {
		this.currentFlow = flows;
		nodes = new HashMap<String, NodeDraft>();
		this.ipHostMap = ipHostMap;
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
		for (FlowMessage message : currentFlow) {

			NodeDraft src = nodes.get(message.getSourceIP());
			if (src == null) {
				src = fact.newNodeDraft();
				String hostName = ipHostMap.get(message.getSourceIP());
				src.setLabel(hostName == null ? "extern" : hostName);
				nodes.put(message.getSourceIP(), src);
				loader.addNode(src);
			}

			NodeDraft dst = nodes.get(message.getDestinationIP());
			if (dst == null) {
				dst = fact.newNodeDraft();
				String hostName = ipHostMap.get(message.getDestinationIP());
				dst.setLabel(hostName == null ? "extern" : hostName);
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

	@Override
	public ContainerLoader getContainer() {
		return container;
	}

	@Override
	public Report getReport() {
		return report;
	}

}
