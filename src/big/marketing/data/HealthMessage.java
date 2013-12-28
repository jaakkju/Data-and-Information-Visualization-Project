package big.marketing.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Class stores the information from a single network health message from a node
 * 
 * @author jaakkju
 */
public class HealthMessage implements DBWritable {

	public static final int STATUS_GOOD = 1;
	public static final int STATUS_WARNING = 2;
	public static final int STATUS_PROBLEM = 3;

	public static final int CONN_OK = 1;
	public static final int CONN_NOK = 0;

	private final int id;
	private final String hostname;
	private final String serviceName;
	private final int statusVal;
	private final String receivedFrom;
	private final int currentTime;
	private final int diskUsage;
	private final int pageFileUsage;
	private final int numProcs;
	private final int loadAverage;
	private final int physicalMemoryUsage;
	private final int connMade;

	@Override
	public String toString() {
		return "HealthMessage [id=" + id + ", hostname=" + hostname + ", serviceName=" + serviceName + ", statusVal=" + statusVal
		      + ", receivedFrom=" + receivedFrom + ", currentTime=" + currentTime + ", diskUsage=" + diskUsage + ", pageFileUsage="
		      + pageFileUsage + ", numProcs=" + numProcs + ", loadAverage=" + loadAverage + ", physicalMemoryUsage="
		      + physicalMemoryUsage + ", connMade=" + connMade + "]";
	}

	/**
	 * 
	 * @param args
	 *           [id, hostname, serviceName, currentTime, statusVal,
	 *           receivedFrom, diskUsage, pageFileusage, numProcs, loadAverage,
	 *           physicalMemoryUsage, connMade]
	 */
	public HealthMessage(String[] args) {

		this.id = argsToInt(args, 0);
		this.hostname = argsToString(args, 1);
		this.serviceName = argsToString(args, 2);
		this.currentTime = argsToInt(args, 3);
		this.statusVal = argsToInt(args, 4);
		// args[5] is the unparsed message content
		this.receivedFrom = argsToString(args, 6);
		this.diskUsage = argsToInt(args, 7);
		this.pageFileUsage = argsToInt(args, 8);
		this.numProcs = argsToInt(args, 9);
		this.loadAverage = argsToInt(args, 10);
		this.physicalMemoryUsage = argsToInt(args, 11);
		this.connMade = argsToInt(args, 12);
		// args[13] is parsedDate from message content
	}

	private int argsToInt(String args[], int index) {
		if (args.length - 1 >= index && !args[index].isEmpty()) {
			return Integer.valueOf(args[index]);
		}
		return 0;
	}

	private String argsToString(String args[], int index) {
		if (args.length - 1 >= index && !args[index].isEmpty()) {
			return args[index];
		}
		return "";
	}

	public int getId() {
		return this.id;
	}

	public String getHostname() {
		return this.hostname;
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public int getStatusVal() {
		return this.statusVal;
	}

	public String getReceivedFrom() {
		return this.receivedFrom;
	}

	public int getCurrentTime() {
		return this.currentTime;
	}

	public int getDiskUsage() {
		return this.diskUsage;
	}

	public int getPageFileUsage() {
		return this.pageFileUsage;
	}

	public int getNumProcs() {
		return this.numProcs;
	}

	public int getLoadAverage() {
		return this.loadAverage;
	}

	public int getPhysicalMemoryUsage() {
		return this.physicalMemoryUsage;
	}

	public int getConnMade() {
		return this.connMade;
	}

	public DBObject asDBObject() {
		BasicDBObject bdbo = new BasicDBObject();
		bdbo.append("id", this.id);
		bdbo.append("hostname", this.hostname);
		bdbo.append("serviceName", this.serviceName);
		bdbo.append("statusVal", this.statusVal);
		bdbo.append("receivedFrom", this.receivedFrom);
		bdbo.append("currentTime", this.currentTime);
		bdbo.append("diskUsage", this.diskUsage);
		bdbo.append("pageFileUsage", this.pageFileUsage);
		bdbo.append("numProcs", this.numProcs);
		bdbo.append("loadAverage", this.loadAverage);
		bdbo.append("physicalMemoryUsage", this.physicalMemoryUsage);
		bdbo.append("connMade", this.connMade);
		return bdbo;
	}
}
