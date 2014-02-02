package big.marketing.view.gephi;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.EdgeDraft.EdgeType;
import org.gephi.io.importer.api.EdgeDraftGetter;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.gephi.io.processor.plugin.AbstractProcessor;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

public class CustomProcessor extends AbstractProcessor implements Processor {

	static Logger logger = Logger.getLogger(CustomProcessor.class);

	@Override
	public String getDisplayName() {
		return "CustomProcessor";
	}

	public void process() {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		//Workspace
		if (workspace == null) {
			workspace = pc.getCurrentWorkspace();
			if (workspace == null) {
				//Append mode but no workspace
				workspace = pc.newWorkspace(pc.getCurrentProject());
				pc.openWorkspace(workspace);
			}
		}
		if (container.getSource() != null) {
			pc.setSource(workspace, container.getSource());
		}

		//Architecture
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

		HierarchicalGraph graph = null;
		switch (container.getEdgeDefault()) {
		case DIRECTED:
			graph = graphModel.getHierarchicalDirectedGraph();
			break;
		case UNDIRECTED:
			graph = graphModel.getHierarchicalUndirectedGraph();
			break;
		case MIXED:
			graph = graphModel.getHierarchicalMixedGraph();
			break;
		default:
			graph = graphModel.getHierarchicalMixedGraph();
			break;
		}
		GraphFactory factory = graphModel.factory();

		//Attributes - Creates columns for properties
		attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		attributeModel.mergeModel(container.getAttributeModel());

		//Dynamic
		if (container.getTimeFormat() != null) {
			DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
			dynamicController.setTimeFormat(container.getTimeFormat());
		}

		//Index existing graph
		Map<String, Node> map = new HashMap<String, Node>();
		for (Node n : graph.getNodes()) {
			String ip = (String) n.getNodeData().getAttributes().getValue("ip");
			if (ip != null) {
				map.put(ip, n);
			}
			String id = n.getNodeData().getId();
			if (id != null && !id.equalsIgnoreCase(String.valueOf(n.getId()))) {
				map.put(id, n);
			}
			if (n.getNodeData().getLabel() != null && !n.getNodeData().getLabel().isEmpty()) {
				map.put(n.getNodeData().getLabel(), n);
			}
		}

		int nodeCount = 0;
		int totalNodes = 0;
		//Create all nodes
		for (NodeDraftGetter draftNode : container.getNodes()) {
			totalNodes++;
			Node n;
			String id = draftNode.getId();
			String label = draftNode.getLabel();
			String ip = (String) draftNode.getAttributeRow().getValue("ip");
			boolean keepOldNode = true;
			if (ip != null && map.get(ip) != null) {
				n = map.get(ip);
			} else if (!draftNode.isAutoId() && id != null && map.get(id) != null) {
				n = map.get(id);
			} else if (label != null && map.get(label) != null) {
				n = map.get(label);
			} else {
				n = factory.newNode(draftNode.isAutoId() ? null : id);
				nodeCount++;
				keepOldNode = false;
			}
			float x = n.getNodeData().x();
			float y = n.getNodeData().y();
			float z = n.getNodeData().z();

			flushToNode(draftNode, n);
			if (keepOldNode) {
				n.getNodeData().setX(x);
				n.getNodeData().setY(y);
				n.getNodeData().setZ(z);
				n.getNodeData().setFixed(true);
			}

			draftNode.setNode(n);
		}

		//Push nodes in data structure
		for (NodeDraftGetter draftNode : container.getNodes()) {
			Node n = draftNode.getNode();
			NodeDraftGetter[] parents = draftNode.getParents();
			if (parents != null) {
				for (int i = 0; i < parents.length; i++) {
					Node parent = parents[i].getNode();
					graph.addNode(n, parent);
				}
			} else {
				graph.addNode(n);
			}
		}

		//Create all edges and push to data structure
		int edgeCount = 0;
		for (EdgeDraftGetter edge : container.getEdges()) {
			Node source = edge.getSource().getNode();
			Node target = edge.getTarget().getNode();

			Edge e = null;
			switch (container.getEdgeDefault()) {
			case DIRECTED:
				e = factory.newEdge(edge.isAutoId() ? null : edge.getId(), source, target, edge.getWeight(), true);
				break;
			case UNDIRECTED:
				e = factory.newEdge(edge.isAutoId() ? null : edge.getId(), source, target, edge.getWeight(), false);
				break;
			case MIXED:
				e = factory.newEdge(edge.isAutoId() ? null : edge.getId(), source, target, edge.getWeight(),
				      edge.getType().equals(EdgeType.UNDIRECTED) ? false : true);
				break;
			}

			flushToEdge(edge, e);
			edgeCount++;
			graph.addEdge(e);

		}

		System.out.println("# New Nodes appended: " + nodeCount + "\n# Total nodes appended: " + totalNodes + "\n# Edges appended: "
		      + edgeCount);
		workspace = null;
	}
}