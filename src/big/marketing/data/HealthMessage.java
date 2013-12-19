package big.marketing.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Class stores the information from a single network health message from a node
 * 
 * @author jaakkju
 * 
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

	/**
	 * @param args
	 *           healthMessage values string array [id, hostname, serviceName,
	 *           statusVal, receivedFrom, currentTime, diskUsage, pageFileUsage,
	 *           loadAverage, numProcs, physicalMemomyUsage, connMade]
	 */
	public HealthMessage(String[] args) {
		this.id = Integer.valueOf(args[0]);
		this.hostname = args[1];
		this.serviceName = args[2];
		this.statusVal = Integer.valueOf(args[3]);
		this.receivedFrom = args[4];
		this.currentTime = Integer.valueOf(args[5]);
		this.diskUsage = Integer.valueOf(args[6]);
		this.pageFileUsage = Integer.valueOf(args[7]);
		this.numProcs = Integer.valueOf(args[8]);
		this.loadAverage = Integer.valueOf(args[9]);
		this.physicalMemoryUsage = Integer.valueOf(args[10]);
		this.connMade = Integer.valueOf(args[11]);
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
