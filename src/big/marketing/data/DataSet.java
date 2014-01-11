package big.marketing.data;

import java.util.List;

public class DataSet {

	private List<FlowMessage> flow;
	private List<HealthMessage> health;
	private List<IPSMessage> ips;
	private List<Node> network;

	public DataSet(List<FlowMessage> flow, List<HealthMessage> health, List<IPSMessage> ips, List<Node> network) {
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

}
