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

	private final String hostname;
	private final int statusVal;
	private final int time;
	private final int diskUsage;
	private final int pageFileUsage;
	private final int numProcs;
	private final int loadAverage;
	private final int physicalMemoryUsage;

	@Override
	public String toString() {
		return "HealthMessage [hostname=" + hostname + ", statusVal=" + statusVal + ", time=" + time + ", diskUsage=" + diskUsage
		      + ", pageFileUsage=" + pageFileUsage + ", numProcs=" + numProcs + ", loadAverage=" + loadAverage + ", physicalMemoryUsage="
		      + physicalMemoryUsage + "]";
	}

	public HealthMessage(DBObject dbo) {
		this.hostname = (String) dbo.get("hostname");
		this.statusVal = (Integer) dbo.get("statusVal");
		this.time = (Integer) dbo.get("time");
		this.diskUsage = (Integer) dbo.get("diskUsage");
		this.pageFileUsage = (Integer) dbo.get("pageFileUsage");
		this.numProcs = (Integer) dbo.get("numProcs");
		this.loadAverage = (Integer) dbo.get("loadAverage");
		this.physicalMemoryUsage = (Integer) dbo.get("physicalMemoryUsage");
	}

	/**
	 * @param args [id, hostname, serviceName, currentTime, statusVal,
	 *           receivedFrom, diskUsage, pageFileusage, numProcs, loadAverage,
	 *           physicalMemoryUsage, connMade]
	 */
	public HealthMessage(String[] args) {

		//		this.id = argsToInt(args, 0);
		this.hostname = argsToString(args, 1);
<<<<<<< HEAD
		//		this.serviceName = argsToString(args, 2);

=======
		this.serviceName = argsToString(args, 2);
		
>>>>>>> 30d046a4ba66c6ff5a784361eeddd557a2552a27
		int timeSeconds = (int) Double.parseDouble(args[3]);
		this.time = (timeSeconds / 60) * 60;

		this.statusVal = argsToInt(args, 4);
		//		args[5] is the unparsed message content
		//		this.receivedFrom = argsToString(args, 6);
		this.diskUsage = argsToInt(args, 7);
		this.pageFileUsage = argsToInt(args, 8);
		this.numProcs = argsToInt(args, 9);
		this.loadAverage = argsToInt(args, 10);
		this.physicalMemoryUsage = argsToInt(args, 11);
		//		this.connMade = argsToInt(args, 12);
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

	public String getHostname() {
		return hostname;
	}

	public int getStatusVal() {
		return statusVal;
	}

	public int getTime() {
		return time;
	}

	public int getDiskUsage() {
		return diskUsage;
	}

	public int getPageFileUsage() {
		return pageFileUsage;
	}

	public int getNumProcs() {
		return numProcs;
	}

	public int getLoadAverage() {
		return loadAverage;
	}

	public int getPhysicalMemoryUsage() {
		return physicalMemoryUsage;
	}

	public DBObject asDBObject() {
		BasicDBObject bdbo = new BasicDBObject();
		bdbo.append("hostname", hostname);
		bdbo.append("statusVal", statusVal);
		bdbo.append("time", time);
		bdbo.append("diskUsage", diskUsage);
		bdbo.append("pageFileUsage", pageFileUsage);
		bdbo.append("numProcs", numProcs);
		bdbo.append("loadAverage", loadAverage);
		bdbo.append("physicalMemoryUsage", physicalMemoryUsage);
		return bdbo;
	}
}
