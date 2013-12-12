package big.marketing.data;

/**
 * Class stores the information from a single network health message from a node
 * @author jaakkju
 *
 */
public class HealthMessage {
	
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
	 * @param args healthMessage values string array [id, hostname, serviceName, statusVal, receivedFrom, currentTime, diskUsage, pageFileUsage, physicalMemomyUsage, connMade]
	 */
	public HealthMessage(String[] args) {
	   this.id 						= Integer.valueOf(args[0]);
	   this.hostname 				= args[1];
	   this.serviceName 			= args[2];
	   this.statusVal 			= Integer.valueOf(args[3]);
	   this.receivedFrom 		= args[4];
	   this.currentTime 			= Integer.valueOf(args[5]);
	   this.diskUsage 			= Integer.valueOf(args[6]);
	   this.pageFileUsage 		= Integer.valueOf(args[7]);
	   this.numProcs 				= Integer.valueOf(args[8]);
	   this.loadAverage			= Integer.valueOf(args[9]);
	   this.physicalMemoryUsage = Integer.valueOf(args[10]);
	   this.connMade 				= Integer.valueOf(args[11]);
   }
	
	public int getId() {
		return id;
	}

	public String getHostname() {
		return hostname;
	}

	public String getServiceName() {
		return serviceName;
	}

	public int getStatusVal() {
		return statusVal;
	}

	public String getReceivedFrom() {
		return receivedFrom;
	}

	public int getCurrentTime() {
		return currentTime;
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
}
