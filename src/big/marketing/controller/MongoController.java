package big.marketing.controller;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import big.marketing.data.DataType;
import big.marketing.reader.ZipReader;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
/*
 * Tutorial on Mongo with java:
 * http://docs.mongodb.org/ecosystem/tutorial/getting-started-with-java-driver/#getting-started-with-java-driver
 */
public class MongoController implements Runnable{
	/*
	 * writerThread and buffer for storing write requests.
	 * handling read requests in a seperate thread doesn't make sense, since reads are always synchronous (as a value has to be returned)
	 */
	Thread writer;
	BlockingQueue<DBObject> flowBuffer;
	BlockingQueue<DBObject> healthBuffer;
	BlockingQueue<DBObject> ipsBuffer;
	BlockingQueue<DBObject> descBuffer;
	private volatile boolean writingEnabled=true;
	/*
	 * MongoDB should be started on the default port.
	 * TODO: start MongoDB automatically, if no connection to MongoDB possible.
	 */
	private static MongoClient mongo;
	private static DB database;

	public static final String 	HOST_NAME="localhost",
								DB_NAME  ="eyeNet",
								FLOW_COLLECTION_NAME = "flow",
								IPS_COLLECTION_NAME = "ips",
								HEALTH_COLLECTION_NAME = "health",
								DESCRIPTION_COLLECTION_NAME = "nodes";
	
	private final DBCollection flowCollection, healthCollection, ipsCollection, descriptionCollection;
	
	public MongoController() {
		connectToDatabase();
		flowCollection = database.getCollection(FLOW_COLLECTION_NAME);
		healthCollection = database.getCollection(HEALTH_COLLECTION_NAME);
		ipsCollection = database.getCollection(IPS_COLLECTION_NAME);
		descriptionCollection = database.getCollection(DESCRIPTION_COLLECTION_NAME);
		flowBuffer = new ArrayBlockingQueue<>(1000);
		ipsBuffer = new ArrayBlockingQueue<>(1000);
		healthBuffer = new ArrayBlockingQueue<>(1000);
		descBuffer = new ArrayBlockingQueue<>(1000);
		
		writer = new Thread(this);
		writer.start();
	}
	
	public void connectToDatabase(){
		
		try {
			mongo = new MongoClient(HOST_NAME);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		database = mongo.getDB(DB_NAME);
	}
	
	public void getConstrainedEntries(DataType t, String key, int min, int max){
		
		BasicDBObject query = new BasicDBObject(key,
				new BasicDBObject("$lt", max).append("$gt", min)
				);
		DBCursor cursor = getCollection(t).find(query);
		for (DBObject dbo : cursor){
			System.out.println(dbo);
		}
	}
	
	public void printAllEntries(DataType t){
		DBCursor c = getCollection(t).find();
		for (int i=0;i<ZipReader.ROWS;i++){
			System.out.println(c.next());
		}
		
	}
	private BlockingQueue<DBObject> getBuffer(DataType t){
		switch(t){
		case FLOW:
			return flowBuffer;
		case HEALTH:
			return healthBuffer;
		case IPS:
			return ipsBuffer;
		case DESCRIPTION:
			return descBuffer;
		default:
			return null;
		}
	}
	
	/**
	 * Aggregate all occuring values of the given field into the set. Useful for analyzing the data.
	 * @param t DataType to look in
	 * @param fieldName the values of this field are aggregated into the returned set.
	 * @return a set of String naming all occuring values in the given field
	 */
	public Set<String> getDomainOf(DataType t, String fieldName){
		
		Set<String> result = new HashSet<>();
		DBObject fields = new BasicDBObject(fieldName, 1);
		fields.put("_id", 0);
		DBObject project = new BasicDBObject("$project", fields);
		
		DBObject groupFields = new BasicDBObject("_id","$"+fieldName);
		DBObject group = new BasicDBObject("$group",groupFields);
		AggregationOutput output = getCollection(t).aggregate(project, group);
		
		for (DBObject dbo : output.results()){
			result.add(dbo.get("_id").toString());
		}
		
		return result;
	}
	
	private DBCollection getCollection(DataType t){
		switch(t){
		case FLOW:
			return flowCollection;
		case HEALTH:
			return healthCollection;
		case IPS:
			return ipsCollection;
		case DESCRIPTION:
			return descriptionCollection;
		default:
			return null;
		}
	}
	public void getSingleFlowEntry(){
		// return the first flow entry
		System.out.println(flowCollection.findOne());
	}
	
	public void storeEntry(DataType t, DBObject object){
		if (object == null)
			return;
		try {
			getBuffer(t).put(object);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
//		MongoController m = new MongoController();
//		String[] bla = {"Time","SourceIP","DestIP","Protocol","sourcePort","destinationPort","Priority","Operation","MessageCode","DestinationService","Direction","Flags"};
//		String[] bla = {"Protocol","Priority","Operation","MessageCode","Direction","Flags"};
//		String [] bla = {
//			"Time",
//			"SourceIP",
//			"DestIP",
//			"Protocol",
//			"sourcePort",
//			"destinationPort",
//			"Duration",
//			"srcPayload",
//			"destPayload",
//			"srcTotal",
//			"destTotal",
//			"sourcePackets",
//			"destinationPackets"
//		};
//		for (String field : bla){
//			Set<String> result = m.getDomainOf(DataType.FLOW, field);
//			System.out.println(field + ": "+result.size());
//			if (result.size() < 20)
//				System.out.println(field + ": "+result.toString());
//						
//		}
//		
//		m.writingEnabled=false;
	}

	@Override
	public void run() {
		DBCollection [] colls = {flowCollection, healthCollection,ipsCollection, descriptionCollection};
		BlockingQueue<?> [] buffers = {flowBuffer, healthBuffer, ipsBuffer,descBuffer};
		while (true){
			if (writingEnabled){
				for (int i=0;i<colls.length;i++){
					BlockingQueue<DBObject> buffer = (BlockingQueue<DBObject>) buffers[i];
					ArrayList<DBObject> bufferList = new ArrayList<>(buffer.size());
					buffer.drainTo(bufferList);
					colls[i].insert(bufferList);
				}
			} else{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
