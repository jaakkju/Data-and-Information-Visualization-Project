package big.marketing.data;

public class node {
	private final String address;
	private final String hostName;
	private final String comment;

	public node(String address, String hostName, String comment) {
		this.address = address;
		this.hostName = hostName;
		this.comment = comment;
	}

	public String getAddress() {
		return address;
	}

	public String getHostName() {
		return hostName;
	}

	public String getComment() {
		return comment;
	}
}
