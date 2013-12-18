package big.marketing.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class SingleFlow implements DBWritable{

	private final int time;
	private final int protocol;
	private final int sourceIP;
	private final int destinationIP;
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

	public SingleFlow(String[] args) {
		super();
		this.time = (int) Double.parseDouble(args[0]);
		this.protocol = Integer.parseInt(args[3]);
		this.sourceIP = encodeIP(args[5]); 
		this.destinationIP = encodeIP(args[6]); 
		this.sourcePort = Integer.parseInt(args[7]);
		this.destinationPort = Integer.parseInt(args[8]);
		this.last = "1".equals(args[9]); // 0 or 1
		this.first = "1".equals(args[10]); // 0 or 1
		this.duration = Integer.parseInt(args[11]);
		this.sourcePayloadBytes = Integer.parseInt(args[12]);
		this.destinationPayloadBytes = Integer.parseInt(args[13]);
		this.sourceTotalBytes = Integer.parseInt(args[14]);
		this.destinationTotalBytes = Integer.parseInt(args[15]);
		this.sourcePacketCount = Integer.parseInt(args[16]);
		this.destinationPacketCount = Integer.parseInt(args[17]);
		this.recordForceOut = "1".equals(args[18]); // 0 or 1
	}

	public int getDateTime() {
		return time;
	}

	public int getProtocol() {
		return protocol;
	}

	public int getSourceIP() {
		return sourceIP;
	}

	public int getDestinationIP() {
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
		dbo.append("Time", time);
		dbo.append("SourceIP", sourceIP);
		dbo.append("DestIP", destinationIP);
		dbo.append("Protocol", protocol);
		dbo.append("sourcePort", sourcePort);
		dbo.append("destinationPort", destinationPort);
		dbo.append("Duration", duration);
		dbo.append("srcPayload", sourcePayloadBytes);
		dbo.append("destPayload", destinationPayloadBytes);
		dbo.append("srcTotal", sourceTotalBytes);
		dbo.append("destTotal", destinationTotalBytes);
		dbo.append("sourcePackets", sourcePacketCount);
		dbo.append("destinationPackets", destinationPacketCount);
		dbo.append("forceOut", recordForceOut);
		return dbo;
	}
	public static void main(String[] args) {
	}
}
