package big.marketing.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class IPSMessage implements DBWritable {
	static Logger logger = Logger.getLogger(IPSMessage.class);

	private final int time;
	private final int operation;
	private final int srcIP;
	private final int dstIP;
	private final int srcPort;
	private final int dstPort;

	public static final int OPERATION_DENY = 0;
	public static final int OPERATION_TEARDOWN = 1;
	public static final int OPERATION_BUILT = 2;

	private static SimpleDateFormat dateParser = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss", Locale.ENGLISH);

	public IPSMessage(String... args) throws ParseException {
		super();

		Date d = null;
		d = dateParser.parse(args[0]);

		int timeSeconds = (int) (d.getTime() / 1000L);
		this.time = (timeSeconds / 60) * 60;

		// keep only Deny messages, the others are normal network traffic,
		// according to the manual
		if ("Deny".equals(args[2]))
			this.operation = OPERATION_DENY;
		else if ("Teardown".equals(args[2]))
			this.operation = OPERATION_TEARDOWN;
		else if ("Built".equals(args[2])) {
			this.operation = OPERATION_BUILT;
		} else {
			System.out.println("Invalid Operation: " + args[2]);
			this.operation = -1;
		}

		this.srcIP = encodeIP(args[5]);
		this.dstIP = encodeIP(args[6]);
		this.srcPort = Integer.parseInt(args[7]);
		this.dstPort = Integer.parseInt(args[8]);

	}

	public IPSMessage(DBObject dbo) {
		operation = OPERATION_DENY;
		time = convertToInt(dbo.get("time"));
		srcIP = convertToInt(dbo.get("srcIP"));
		dstIP = convertToInt(dbo.get("dstIP"));
		dstPort = convertToInt(dbo.get("dstPort"));
		srcPort = convertToInt(dbo.get("srcPort"));
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
				logger.warn("Could not parse Object as Integer",e);
				return -1;
			}
	}

	@Override
	public DBObject asDBObject() {

		if (operation != OPERATION_DENY)
			return null;
		BasicDBObject dbo = new BasicDBObject();
		dbo.append("time", (time / 60) * 60);
		dbo.append("srcIP", srcIP);
		dbo.append("dstIP", dstIP);
		dbo.append("srcPort", srcPort);
		dbo.append("dstPort", dstPort);
		return dbo;
	}

	public int getTime() {
		return time;
	}

	public int getOperation() {
		return operation;
	}

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
