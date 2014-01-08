package big.marketing.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class FlowMessage implements DBWritable{

	
	/**
	 * time in Unixtime
	 * range: [1364802616,1366020000]
	 */
	private final int time;
	
	
//	private final int protocol;
	private final int srcIP;
	private final int dstIP;
	private final int srcPort;
	private final int dstPort;
	private final int payloadBytes;
	private final int totalBytes;
	private final int packetCount;
	
//	private final boolean hasMoreFragments;
//	private final boolean hasSubsequentFragments;
	private final int duration;
//	private final int sourcePayloadBytes;
//	private final int destinationPayloadBytes;
//	private final int sourceTotalBytes;
//	private final int destinationTotalBytes;
//	private final int sourcePacketCount;
//	private final int destinationPacketCount;

	public FlowMessage(String[] args) {
		super();
		// TODO: remove unused features
		int timeSeconds = (int) Double.parseDouble(args[0]);
		this.time = (timeSeconds / 60) * 60;
		// this.protocol = Integer.parseInt(args[3]);
		this.srcIP = encodeIP(args[5]); 
		this.dstIP = encodeIP(args[6]); 
		this.srcPort = Integer.parseInt(args[7]);
		this.dstPort = Integer.parseInt(args[8]);
		
		// 0 0 -> flow with just one fragment
		// 0 1 -> last fragment
		// 1 0 -> first fragment
		// 1 1 -> intermediate fragment (not first nor last fragment)
//		this.hasMoreFragments = "1".equals(args[9]); // 0 or 1
//		this.hasSubsequentFragments = "1".equals(args[10]); // 0 or 1
		this.duration = Integer.parseInt(args[11]);
//		this.sourcePayloadBytes = Integer.parseInt(args[12]);
//		this.destinationPayloadBytes = Integer.parseInt(args[13]);
//		this.sourceTotalBytes = Integer.parseInt(args[14]);
//		this.destinationTotalBytes = Integer.parseInt(args[15]);
//		this.sourcePacketCount = Integer.parseInt(args[16]);
//		this.destinationPacketCount = Integer.parseInt(args[17]);
		this.payloadBytes = Integer.parseInt(args[12]) +Integer.parseInt(args[13]);
		this.totalBytes = Integer.parseInt(args[14]) +Integer.parseInt(args[15]);
		this.packetCount = Integer.parseInt(args[16]) +Integer.parseInt(args[17]);
		
	}
	
	public FlowMessage(DBObject dbo){
		this.time = (Integer) dbo.get("time");
		this.srcIP = (Integer) dbo.get("srcIP");
		this.dstIP = (Integer) dbo.get("dstIP");
		this.srcPort = (Integer) dbo.get("srcPort");
		this.dstPort = (Integer) dbo.get("dstPort");
		this.duration = (Integer) dbo.get("duration");
		this.payloadBytes = (Integer) dbo.get("payloadBytes");
		this.totalBytes = (Integer) dbo.get("totalBytes");
		this.packetCount = (Integer) dbo.get("packetCount");
	}
	
	public int getTime() {
		return time;
	}

//	public int getProtocol() {
//		return protocol;
//	}

	public String getSourceIP() {
		return decodeIP(srcIP);
	}

	public String getDestinationIP() {
		return decodeIP(dstIP);
	}

	public int getSourcePort() {
		return srcPort;
	}

	public int getDestinationPort() {
		return dstPort;
	}

//	public boolean hasMoreFragments() {
//		return hasMoreFragments;
//	}
//
//	public boolean hasSubsequentFragments() {
//		return hasSubsequentFragments;
//	}

	public int getDuration() {
		return duration;
	}

//	public int getSourcePayloadBytes() {
//		return sourcePayloadBytes;
//	}
//
//	public int getDestinationPayloadBytes() {
//		return destinationPayloadBytes;
//	}
//
//	public int getSourceTotalBytes() {
//		return sourceTotalBytes;
//	}
//
//	public int getDestinationTotalBytes() {
//		return destinationTotalBytes;
//	}
//
//	public int getSourcePacketCount() {
//		return sourcePacketCount;
//	}
//
//	public int getDestinationPacketCount() {
//		return destinationPacketCount;
//	}

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
		dbo.append("time", time); // reduce time resolution to minute
		dbo.append("srcIP", srcIP);
		dbo.append("dstIP", dstIP);
//		dbo.append("Protocol", protocol);
//		dbo.append("sourcePort", sourcePort);
		dbo.append("dstPort", dstPort);
		dbo.append("duration", duration);
//		dbo.append("hasSubsequentFragments", hasSubsequentFragments);
//		dbo.append("hasMoreFragments", hasMoreFragments);
		
		dbo.append("payloadBytes", payloadBytes);
		dbo.append("totalBytes", totalBytes);
		dbo.append("packetCount", packetCount);
		
//		dbo.append("payloadBytes", sourcePayloadBytes+destinationPayloadBytes);
//		dbo.append("totalBytes", sourceTotalBytes+destinationTotalBytes);
//		dbo.append("packetCount", sourcePacketCount+destinationPacketCount);
		
//		dbo.append("srcPayload", sourcePayloadBytes);
//		dbo.append("destPayload", destinationPayloadBytes);
//		dbo.append("srcTotal", sourceTotalBytes);
//		dbo.append("destTotal", destinationTotalBytes);
//		dbo.append("sourcePackets", sourcePacketCount);
//		dbo.append("destinationPackets", destinationPacketCount);
		return dbo;
	}
}
