package big.marketing.view.gephi;

import java.util.HashMap;
import java.util.Map;

import org.gephi.filters.plugin.AbstractFilter;
import org.gephi.filters.spi.ComplexFilter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

public class SelectionFilter extends AbstractFilter implements ComplexFilter {

	Map<String, Boolean> map = new HashMap<>();
	Map<Node, Boolean> map2 = new HashMap<>();

	public SelectionFilter(big.marketing.data.Node[] selected) {
		super("SelectionFilterBla");
		for (big.marketing.data.Node n : selected) {
			map.put(n.getAddress(), true);
		}
	}

	@Override
	public Graph filter(Graph graph) {
		for (Edge e : graph.getEdges().toArray()) {
			Node src = e.getSource(), dst = e.getTarget();
			String srcIp = (String) src.getAttributes().getValue("ip"), dstIp = (String) dst.getAttributes().getValue("ip");

			if (map.get(srcIp) != Boolean.TRUE && map.get(dstIp) != Boolean.TRUE) {
				graph.removeEdge(e);
			}
		}

		for (Node n : graph.getNodes().toArray()) {
			if (graph.getDegree(n) == 0)
				graph.removeNode(n);
		}

		return graph;
	}
}
