package big.marketing.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public class QueryWindowData {

	private List<FlowMessage> flow;
	private List<HealthMessage> health;
	private List<IPSMessage> ips;
	private List<Node> network;

	public QueryWindowData() {
		this.flow = new ArrayList<FlowMessage>();
		this.health = new ArrayList<HealthMessage>();
		this.ips = new ArrayList<IPSMessage>();
		this.network = new ArrayList<Node>();
	}

	public QueryWindowData(List<FlowMessage> flow, List<HealthMessage> health, List<IPSMessage> ips, List<Node> network) {
		this.flow = flow;
		this.health = health;
		this.ips = ips;
		this.network = network;
	}

	public List<FlowMessage> getFlowData() {
		return flow;
	}

	public List<HealthMessage> getHealthData() {
		return health;
	}

	public List<IPSMessage> getIPSData() {
		return ips;
	}

	public List<Node> getNetwork() {
		return network;
	}

	public void setFlow(List<Object> flow) {
		if (flow == null || flow.size() == 0)
			this.flow = Collections.emptyList();
		else if (!(flow.get(0) instanceof FlowMessage))
			throw new IllegalArgumentException(flow.get(0).getClass() + " is not of type FLOW");
		else
			this.flow = (List<FlowMessage>) (List<?>) flow;
	}

	public void setHealth(List<Object> health) {
		if (health == null || health.size() == 0)
			this.health = Collections.emptyList();
		else if (!(health.get(0) instanceof HealthMessage))
			throw new IllegalArgumentException(health.get(0).getClass() + " is not of type HEALTH");
		else
			this.health = (List<HealthMessage>) (List<?>) health;
	}

	public void setIps(List<Object> ips) {
		if (ips == null || ips.size() == 0)
			this.ips = Collections.emptyList();
		else if (!(ips.get(0) instanceof IPSMessage))
			throw new IllegalArgumentException(ips.get(0).getClass() + " is not of type IPS");
		else
			this.ips = (List<IPSMessage>) (List<?>) ips;
	}

	public void setNetwork(List<Node> network) {
		this.network = network;
	}

	public boolean isEmpty() {
		return flow.isEmpty() && ips.isEmpty() && health.isEmpty() & network.isEmpty();
	}
}
