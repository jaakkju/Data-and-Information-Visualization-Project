package big.marketing.data;

public class SingleFlow {
	private final String dateTime;
	private final int protocol;
	private final String sourceIP;
	private final String destinationIP;
	private final int sourcePort;
	private final int destinationPort;
	private final boolean last;
	private final boolean first;
	private final int duration;
	private final int sourcePayloadBytes;
	private final int destinationPayloadBytes;
	private final int sourceTotalBytes;
	private final int destinationTotalBytes;
	private final int sourcePacketCount;
	private final int destinationPacketCount;
	private final boolean recordForceOut;
	public SingleFlow(String [] args) {
		super();
		this.dateTime = args[1];
		this.protocol = Integer.parseInt(args[4]);
		this.sourceIP = args[5]; // split in bytes
		this.destinationIP = args[6]; // split in bytes
		this.sourcePort = Integer.parseInt(args[7]);
		this.destinationPort = Integer.parseInt(args[8]);
		this.last = "1".equals(args[9]); //0 or 1
		this.first = "1".equals(args[10]); //0 or 1
		this.duration = Integer.parseInt(args[11]);
		this.sourcePayloadBytes = Integer.parseInt(args[12]);
		this.destinationPayloadBytes = Integer.parseInt(args[13]);
		this.sourceTotalBytes = Integer.parseInt(args[14]);
		this.destinationTotalBytes = Integer.parseInt(args[15]);
		this.sourcePacketCount = Integer.parseInt(args[16]);
		this.destinationPacketCount = Integer.parseInt(args[17]);
		this.recordForceOut = "1".equals(args[18]); //0 or 1
	}
	public String getDateTime() {
		return dateTime;
	}
	public int getProtocol() {
		return protocol;
	}
	public String getSourceIP() {
		return sourceIP;
	}
	public String getDestinationIP() {
		return destinationIP;
	}
	public int getSourcePort() {
		return sourcePort;
	}
	public int getDestinationPort() {
		return destinationPort;
	}
	public boolean isLast() {
		return last;
	}
	public boolean isFirst() {
		return first;
	}
	public int getDuration() {
		return duration;
	}
	public int getSourcePayloadBytes() {
		return sourcePayloadBytes;
	}
	public int getDestinationPayloadBytes() {
		return destinationPayloadBytes;
	}
	public int getSourceTotalBytes() {
		return sourceTotalBytes;
	}
	public int getDestinationTotalBytes() {
		return destinationTotalBytes;
	}
	public int getSourcePacketCount() {
		return sourcePacketCount;
	}
	public int getDestinationPacketCount() {
		return destinationPacketCount;
	}
	public boolean getRecordForceOut() {
		return recordForceOut;
	}
	
	
	public static void main(String[] args) {
		
	}
}
