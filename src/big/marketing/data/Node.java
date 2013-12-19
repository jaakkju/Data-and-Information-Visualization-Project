package big.marketing.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author jaakkju
 * 
 *         #Column 1: IP #Column 2: Host Name. Hosts with a "WSS" prefix are
 *         user workstations. "Administrator" is the administrator workstation.
 *         Others are servers. #Column 3 (Optional): Comments
 */
public class Node implements DBWritable{

	public static final int TYPE_WORKSTATION = 0;
	public static final int TYPE_ADMINISTRATOR = 1;
	public static final int TYPE_SERVER = 2;

	private static final String administrator = "Administrator";
	private static final String workstation = "WSS";

	private final String address;
	private final String hostName;
	private final int type;
	private String comment = null;

	/**
	 * Constructor takes the one line as a string array from the BigMktNetwork.txt 
	 * @param args String array ["IP address, hostname, comment(optional)"]
	 */
	public Node(String[] args) {

		this.address = args[0].trim();
		this.hostName = args[1].trim();

		if (args.length == 3) {
			this.comment = args[2].trim();
		}

		if (this.address.contains(Node.administrator)) {
			this.type = Node.TYPE_ADMINISTRATOR;
		} else if (this.address.contains(Node.workstation)) {
			this.type = Node.TYPE_WORKSTATION;
		} else {
			this.type = Node.TYPE_SERVER;
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

	@Override
	public String toString() {
		return this.comment != null ? this.address + " " + this.hostName + " " + this.comment : this.address + " " + this.hostName;
	}

	@Override
	public DBObject asDBObject() {
		BasicDBObject dbObject = new BasicDBObject();
		dbObject.append("adress", address);
		dbObject.append("hostName", hostName);
		dbObject.append("type", type);
		dbObject.append("comment", comment);
		return dbObject;
	}
}
