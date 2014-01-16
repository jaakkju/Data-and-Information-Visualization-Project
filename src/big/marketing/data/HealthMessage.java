package big.marketing.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Class stores the information from a single network health message from a node
 * 
 * @author jaakkju
 */
public class HealthMessage implements DBWritable {

	public static String[] FIELDS = { "Hostname", "statusVal", "DiskUsage", "PageFileUsage", "NumProcs", "LoadAverage",
	      "PhysicalMemoryUsage", "ConnMade" };

	public enum STATUS {
		GOOD, WARNING, PROBLEM;
	}

	public enum CONN {
		OK, NOK;
	}

	public static final int STATUS_GOOD = 1, STATUS_WARNING = 2, STATUS_PROBLEM = 3;
	public static final int CONN_OK = 1, CONN_NOK = 0;

	//	private final int id;
	private final String hostname;
	//	private final String serviceName;
	private final int time, statusVal;
	//	private final String receivedFrom;
	private final int diskUsage, pageFileUsage, numProcs, loadAverage, physicalMemoryUsage, connMade;

	@Override
	public String toString() {
		return "HealthMessage [hostname=" + hostname + ", statusVal=" + statusVal + ", time=" + time + ", diskUsage=" + diskUsage
		      + ", pageFileUsage=" + pageFileUsage + ", numProcs=" + numProcs + ", loadAverage=" + loadAverage + ", physicalMemoryUsage="
		      + physicalMemoryUsage + ", connMade=" + connMade + "]";
	}

	public HealthMessage(DBObject dbo) {
		this.hostname = (String) dbo.get("hostname");
		// this.serviceName = (String) dbo.get("serviceName");
		this.statusVal = convertToInt(dbo.get("statusVal"));
		// this.receivedFrom = (String) dbo.get("receivedFrom");
		this.time = convertToInt(dbo.get("time"));
		this.diskUsage = convertToInt(dbo.get("diskUsage"));
		this.pageFileUsage = convertToInt(dbo.get("pageFileUsage"));
		this.numProcs = convertToInt(dbo.get("numProcs"));
		this.loadAverage = convertToInt(dbo.get("loadAverage"));
		this.physicalMemoryUsage = convertToInt(dbo.get("physicalMemoryUsage"));
		this.connMade = convertToInt(dbo.get("connMade"));
	}

	private int convertToInt(Object o) {
		if (o instanceof Double) {
			return ((Double) o).intValue();
		} else if (o instanceof Integer) {
			return ((Integer) o).intValue();
		} else
			try {
				return Integer.parseInt((String) o);
			} catch (Exception e) {
				return 0;
			}
	}

	/**
	 * @param args [id, hostname, serviceName, currentTime, statusVal,
	 *           receivedFrom, diskUsage, pageFileusage, numProcs, loadAverage,
	 *           physicalMemoryUsage, connMade]
	 */
	public HealthMessage(String[] args) {

		//		this.id = argsToInt(args, 0);
		this.hostname = argsToString(args, 1);
		//		this.serviceName = argsToString(args, 2);
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

	public String getHostname() {
		return hostname;
	}

	//	public String getServiceName() {
	//		return serviceName;
	//	}

	public int getStatusVal() {
		return statusVal;
	}

	//	public String getReceivedFrom() {
	//		return receivedFrom;
	//	}

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

	public int getConnMade() {
		return connMade;
	}

	public String statusValToString() {
		switch (statusVal) {
		case STATUS_GOOD:
			return "GOOD";
		case STATUS_WARNING:
			return "WARNING";
		case STATUS_PROBLEM:
			return "PROBLEM";
		default:
			return "EMPTY";
		}
	}

	public String connMadeToString() {
		return connMade > 0 ? "OK" : "NOK";
	}

	public DBObject asDBObject() {
		BasicDBObject bdbo = new BasicDBObject();
		bdbo.append("hostname", hostname);
		//		bdbo.append("serviceName", serviceName);
		bdbo.append("statusVal", statusVal);
		//		bdbo.append("receivedFrom", receivedFrom);
		bdbo.append("time", time);
		bdbo.append("diskUsage", diskUsage);
		bdbo.append("pageFileUsage", pageFileUsage);
		bdbo.append("numProcs", numProcs);
		bdbo.append("loadAverage", loadAverage);
		bdbo.append("physicalMemoryUsage", physicalMemoryUsage);
		bdbo.append("connMade", connMade);
		return bdbo;
	}
}
