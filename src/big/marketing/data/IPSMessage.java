package big.marketing.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class IPSMessage implements DBWritable {
		
//	private final String 
//		destService,
//		flags;
	
	private final int
		time,
//		direction,
//		protocol,
//		priority,
		operation,
		srcIP,
		dstIP,
		srcPort,
		dstPort;
	
//	public String getDestService() {
//		return destService;
//	}
//
//	public String getFlags() {
//		return flags;
//	}

	public int getTime() {
		return time;
	}

//	public int getDirection() {
//		return direction;
//	}
//
//	public int getProtocol() {
//		return protocol;
//	}
//
//	public int getPriority() {
//		return priority;
//	}
//
//	public int getOperation() {
//		return operation;
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
	public static final int
		DIRECTION_IN=0,
		DIRECTION_OUT=1,
		DIRECTION_EMPTY=2,
		
		PROTOCOL_UDP=0,
		PROTOCOL_TCP=1,
		
		PRIORITY_WARNING=0,
		PRIORITY_INFO=1,
		
		OPERATION_DENY=0,
		OPERATION_TEARDOWN=1,
		OPERATION_BUILT=2;
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy kk:mm:ss"); 
	
	public IPSMessage(DBObject dbo){
		operation = OPERATION_DENY;
		time = (Integer) dbo.get("time");
		srcIP = (Integer) dbo.get("srcIP");
		dstIP= (Integer) dbo.get("dstIP");
		dstPort = (Integer) dbo.get("dstPort");
		srcPort = (Integer) dbo.get("srcPort");
	}
	public IPSMessage(String ... args) {
		super();

		Date d=null;
		try {
			d = sdf.parse(args[0]);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		time = (int) (d.getTime()/1000L);
				
//		if ("UDP".equals(args[4]))
//			this.protocol = PROTOCOL_UDP;
//		else if ("TCP".equals(args[4]))
//			this.protocol = PROTOCOL_TCP;
//		else{
//			System.out.println("Invalid Protocol: "+args[4]);
//			this.protocol = -1;
//		}
//		
//		if ("Warning".equals(args[1]))
//			this.priority = PRIORITY_WARNING;
//		else if ("Info".equals(args[1]))
//			this.priority = PRIORITY_INFO;
//		else{
//			System.out.println("Invalid Priority: "+args[1]);
//			this.priority = -1;
//		}
		
		// keep only Deny messages, the others are normal network traffic,
		// according to the manual
		if ("Deny".equals(args[2]))
			this.operation = OPERATION_DENY;
		else if ("Teardown".equals(args[2]))
			this.operation = OPERATION_TEARDOWN;
		else if ("Built".equals(args[2])){
			this.operation = OPERATION_BUILT;		
		}else{
			System.out.println("Invalid Operation: "+args[2]);
			this.operation = -1;
		}
		
		// args[3] contains the message code, which contains only redundant information
		// so dont store the message code
		
		// important message codes:
		// "ASA-6-106015" -> deny tcp packet without connection
		// "ASA-4-106023" -> deny footprinting or port scanning
		
		this.srcIP = encodeIP(args[5]);					
		this.dstIP = encodeIP(args[6]);				
		this.srcPort = Integer.parseInt(args[7]);		
		this.dstPort = Integer.parseInt(args[8]);	
		
//		this.destService = args[9];					
//		
//		if (args[10].startsWith("in")){			
//			this.direction = DIRECTION_IN;
//		}else if (args[10].startsWith("out"))
//			this.direction = DIRECTION_OUT;
//		else if (args[10].contains("empty"))
//			this.direction = DIRECTION_EMPTY;
//		else{
//			System.out.println("Invalid Direction: "+args[10]);
//			this.direction = -1;
//		}
//		
//		this.flags = args[11];								
	}

	@Override
	public DBObject asDBObject() {
		
		if (operation != OPERATION_DENY)
			return null;
		
		BasicDBObject dbo = new BasicDBObject();
		// maybe remove some more features here?
		dbo.append("time", (time/60)*60);
		dbo.append("srcIP", srcIP);
		dbo.append("dstIP", dstIP);
//		dbo.append("MessageCode", messageCode);
//		dbo.append("Protocol", protocol);
		dbo.append("srcPort", srcPort);
		dbo.append("dstPort", dstPort);
//		dbo.append("Priority", priority); // 9000000 vs 7500000
//		dbo.append("Operation", operation);  // this is always 2 , operation deny
//		dbo.append("DestinationService", destService);
//		dbo.append("Direction", direction); // this is always (empty)...
//		dbo.append("Flags", flags);
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
