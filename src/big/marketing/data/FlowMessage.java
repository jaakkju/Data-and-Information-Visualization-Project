package big.marketing.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class FlowMessage implements DBWritable{

	
	/**
	 * time in Unixtime
	 * range: [1364802616,1366020000]
	 */
	private final int time;
	
	
	private final int protocol;
	private final int sourceIP;
	private final int destinationIP;
	private final int sourcePort;
	private final int destinationPort;
	private final boolean hasMoreFragments;
	private final boolean hasSubsequentFragments;
	private final int duration;
	private final int sourcePayloadBytes;
	private final int destinationPayloadBytes;
	private final int sourceTotalBytes;
	private final int destinationTotalBytes;
	private final int sourcePacketCount;
	private final int destinationPacketCount;

	public FlowMessage(String[] args) {
		super();
		// TODO: remove unused features
		this.time = (int) Double.parseDouble(args[0]);
		this.protocol = Integer.parseInt(args[3]);
		this.sourceIP = encodeIP(args[5]); 
		this.destinationIP = encodeIP(args[6]); 
		this.sourcePort = Integer.parseInt(args[7]);
		this.destinationPort = Integer.parseInt(args[8]);
		
		// 0 0 -> flow with just one fragment
		// 0 1 -> last fragment
		// 1 0 -> first fragment
		// 1 1 -> intermediate fragment (not first nor last fragment)
		this.hasMoreFragments = "1".equals(args[9]); // 0 or 1
		this.hasSubsequentFragments = "1".equals(args[10]); // 0 or 1
		this.duration = Integer.parseInt(args[11]);
		this.sourcePayloadBytes = Integer.parseInt(args[12]);
		this.destinationPayloadBytes = Integer.parseInt(args[13]);
		this.sourceTotalBytes = Integer.parseInt(args[14]);
		this.destinationTotalBytes = Integer.parseInt(args[15]);
		this.sourcePacketCount = Integer.parseInt(args[16]);
		this.destinationPacketCount = Integer.parseInt(args[17]);
	}

	public int getDateTime() {
		return time;
	}

	public int getProtocol() {
		return protocol;
	}

	public String getSourceIP() {
		return decodeIP(sourceIP);
	}

	public String getDestinationIP() {
		return decodeIP(destinationIP);
	}

	public int getSourcePort() {
		return sourcePort;
	}

	public int getDestinationPort() {
		return destinationPort;
	}

	public boolean hasMoreFragments() {
		return hasMoreFragments;
	}

	public boolean hasSubsequentFragments() {
		return hasSubsequentFragments;
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

	private static String decodeIP(int ip){
		String out ="";
		for (int i=3;i>=0;i--){
			 out += ((ip >> (i*8)) & 255) + ".";
		}
		return out.substring(0, out.length()-1);
	}
	private static int encodeIP(String strIP) {
		int ipNum = 0;
		String[] split = strIP.split("\\.");
		for (int i = 0; i < split.length; i++){
			ipNum += Integer.parseInt(split[split.length-1-i]) << (i*8);
		}
		return ipNum;
	}
	public DBObject asDBObject(){
		BasicDBObject dbo = new BasicDBObject();
		// TODO: remove unused features
		dbo.append("Time", (time/60)*60); // reduce time resolution to minute
		dbo.append("SourceIP", sourceIP);
		dbo.append("DestIP", destinationIP);
		dbo.append("Protocol", protocol);
//		dbo.append("sourcePort", sourcePort);
		dbo.append("destinationPort", destinationPort);
		dbo.append("Duration", duration);
//		dbo.append("hasSubsequentFragments", hasSubsequentFragments);
//		dbo.append("hasMoreFragments", hasMoreFragments);
		
		dbo.append("payloadBytes", sourcePayloadBytes+destinationPayloadBytes);
		dbo.append("totalBytes", sourceTotalBytes+destinationTotalBytes);
		dbo.append("packetCount", sourcePacketCount+destinationPacketCount);
		
//		dbo.append("srcPayload", sourcePayloadBytes);
//		dbo.append("destPayload", destinationPayloadBytes);
//		dbo.append("srcTotal", sourceTotalBytes);
//		dbo.append("destTotal", destinationTotalBytes);
//		dbo.append("sourcePackets", sourcePacketCount);
//		dbo.append("destinationPackets", destinationPacketCount);
		return dbo;
	}
}
