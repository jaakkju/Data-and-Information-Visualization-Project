package big.marketing.data;

/*
 *  #Column 1:  IP
 *  #Column 2:  Host Name. Hosts with a "WSS" prefix are user workstations. "Administrator" is the administrator workstation. Others are servers.
 *  #Column 3 (Optional): Comments
 */
public class Node {

	public static final int TYPE_WORKSTATION = 0;
	public static final int TYPE_ADMINISTRATOR = 1;
	public static final int TYPE_SERVER = 2;

	private static final String regex = "\\s";
	private static final int reglimit = 3;
	private static final String administrator = "Administrator";

	private final String address;
	private final String hostName;
	private final String comment;
	private final int type;

	/**
	 * Constructor takes the one line from the BigMktNetwork.txt and uses regex
	 * to split string to address, hostname, comment
	 * 
	 * @param description
	 *           line from the txt file that describes one network node
	 */
	public Node(String strLine) {

		String[] split = strLine.split(regex, reglimit);

		this.address = split[0];
		this.hostName = split[1];

		if (split.length == 3) {
			this.comment = split[2];
			this.type = Node.TYPE_SERVER;
			
		} else if (split[1].contains(Node.administrator)) {
			this.type = Node.TYPE_ADMINISTRATOR;
			this.comment = null;
			
		} else {
			this.type = Node.TYPE_WORKSTATION;
			this.comment = null;
		}
	}

	public boolean isServer() {
		return this.type == Node.TYPE_SERVER;
	}

	public boolean isWorkstation() {
		return this.type == Node.TYPE_WORKSTATION;
	}

	public boolean isAdministator() {
		return this.type == Node.TYPE_ADMINISTRATOR;
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

	public int getType() {
		return type;
	}
}
