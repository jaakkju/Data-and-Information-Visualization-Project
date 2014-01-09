package big.marketing.data;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class FlowMessage implements DBWritable {
	static Logger logger = Logger.getLogger(FlowMessage.class);

	private final int time;
	// private final int protocol;
	private final int srcIP;
	private final int dstIP;
	private final int srcPort;
	private final int dstPort;
	private final int payloadBytes;
	private final int totalBytes;
	private final int packetCount;
	private final int srcPortCount;
	private final int dstPortCount;
	private final int duration;

	public FlowMessage(String[] args) {
		super();
		int timeSeconds = (int) Double.parseDouble(args[0]);
		this.time = (timeSeconds / 60) * 60;
		// this.protocol = Integer.parseInt(args[3]);
		this.srcIP = encodeIP(args[5]);
		this.dstIP = encodeIP(args[6]);
		this.srcPort = Integer.parseInt(args[7]);
		this.dstPort = Integer.parseInt(args[8]);
		this.srcPortCount = 1;
		this.dstPortCount = 1;

		this.duration = Integer.parseInt(args[11]);
		this.payloadBytes = Integer.parseInt(args[12]) + Integer.parseInt(args[13]);
		this.totalBytes = Integer.parseInt(args[14]) + Integer.parseInt(args[15]);
		this.packetCount = Integer.parseInt(args[16]) + Integer.parseInt(args[17]);

	}

	public FlowMessage(DBObject dbo) {
		this.time = convertToInt(dbo.get("time"));
		this.srcIP = convertToInt(dbo.get("srcIP"));
		this.dstIP = convertToInt(dbo.get("dstIP"));
		this.srcPort = convertToInt(dbo.get("srcPort"));
		this.dstPort = convertToInt(dbo.get("dstPort"));
		this.srcPortCount = arrayToInt(dbo.get("srcPort"));
		this.dstPortCount = arrayToInt(dbo.get("dstPort"));
		this.duration = convertToInt(dbo.get("duration"));
		this.payloadBytes = convertToInt(dbo.get("payloadBytes"));
		this.totalBytes = convertToInt(dbo.get("totalBytes"));
		this.packetCount = convertToInt(dbo.get("packetCount"));
	}

	private int arrayToInt(Object o) {
		if (o instanceof BasicDBList) {
			BasicDBList list = (BasicDBList) o;
			return list.size();
		} else {
			throw new IllegalArgumentException();
		}
	}

	private int convertToInt(Object o) {
		if (o instanceof Double) {
			return ((Double) o).intValue();
		} else if (o instanceof Integer) {
			return ((Integer) o).intValue();
		} else if (o instanceof BasicDBList) {
			BasicDBList list = (BasicDBList) o;
			if (list.size() > 0) {
				return ((Integer) list.get(0)).intValue();
			} else
				return 0;
		} else
			try {
				return Integer.parseInt((String) o);
			} catch (Exception e) {
				logger.warn("Could not parse Object as Integer",e);
				return -1;
			}
	}

	public DBObject asDBObject() {
		BasicDBObject dbo = new BasicDBObject();
		dbo.append("time", time); // reduce time resolution to minute
		dbo.append("srcIP", srcIP);
		dbo.append("dstIP", dstIP);
		// dbo.append("Protocol", protocol);
		dbo.append("srcPort", srcPort);
		dbo.append("dstPort", dstPort);
		dbo.append("duration", duration);

		dbo.append("payloadBytes", payloadBytes);
		dbo.append("totalBytes", totalBytes);
		dbo.append("packetCount", packetCount);

		return dbo;
	}

	public int getTime() {
		return time;
	}

	// public int getProtocol() {
	// return protocol;
	// }

	public String getSourceIP() {
		return decodeIP(srcIP);
	}

	public String getDestinationIP() {
		return decodeIP(dstIP);
	}

	public int getSourcePort() {
		return srcPort;
	}

	public int getSrcPortCount() {
		return srcPortCount;
	}

	public int getDestinationPort() {
		return dstPort;
	}

	public int getDestinationPortCount() {
		return dstPortCount;
	}

	public int getDuration() {
		return duration;
	}

	public int getPayloadBytes() {
		return payloadBytes;
	}

	public int getTotalBytes() {
		return totalBytes;
	}

	public int getPacketCount() {
		return packetCount;
	}

	private static String decodeIP(int ip) {
		String out = "";
		for (int i = 3; i >= 0; i--) {
			out += ((ip >> (i * 8)) & 255) + ".";
		}
		return out.substring(0, out.length() - 1);
	}

	private static int encodeIP(String strIP) {
		int ipNum = 0;
		String[] split = strIP.split("\\.");
		for (int i = 0; i < split.length; i++) {
			ipNum += Integer.parseInt(split[split.length - 1 - i]) << (i * 8);
		}
		return ipNum;
	}
}
