package big.marketing.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class IPSMessage implements DBWritable {
	
	public static final int DIRECTION_IN=0,DIRECTION_OUT=1,DIRECTION_EMPTY=2;
	
	private final String dateTime,
		priority,
		operation,
		messageCode,
		protocol,
		destinationService,
		flags;
	
	private final int direction,
		sourceIP,
		destinationIP,
		sourcePort,
		destinationPort;
	
	
	
	public IPSMessage(String ... args) {
		super();
		this.dateTime = args[0];
		this.priority = args[1];
		this.operation = args[2];
		this.messageCode = args[3];
		this.protocol = args[4];
		this.sourceIP = encodeIP(args[5]);
		this.destinationIP = encodeIP(args[6]);
		this.sourcePort = Integer.parseInt(args[7]);
		this.destinationPort = Integer.parseInt(args[8]);
		this.destinationService = args[9];
		
		if ("in".compareToIgnoreCase(args[10])==0){
			this.direction = DIRECTION_IN;
		}else if ("out".compareToIgnoreCase(args[10])==0)
			this.direction = DIRECTION_OUT;
		else
			this.direction = DIRECTION_EMPTY;
		
		this.flags = args[11];
	}

	@Override
	public DBObject asDBObject() {
		BasicDBObject dbo = new BasicDBObject();
		// TODO: remove unused features
		dbo.append("Time", dateTime);
		dbo.append("SourceIP", sourceIP);
		dbo.append("DestIP", destinationIP);
		dbo.append("Protocol", protocol);
		dbo.append("sourcePort", sourcePort);
		dbo.append("destinationPort", destinationPort);
		dbo.append("Priority", priority);
		dbo.append("Operation", operation);
		dbo.append("MessageCode", messageCode);
		dbo.append("DestinationService", destinationService);
		dbo.append("Direction", direction);
		dbo.append("Flags", flags);
		return dbo;
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
}
