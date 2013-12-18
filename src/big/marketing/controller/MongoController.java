package big.marketing.controller;

import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import big.marketing.data.DBWritable;
import big.marketing.data.HealthMessage;
import big.marketing.data.SingleFlow;
import big.marketing.reader.ZipReader;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
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
	BlockingQueue<DBWritable> writeQueue;
	
	/*
	 * MongoDB should be started on the default port.
	 * TODO: start MongoDB automatically, if no connection to MongoDB possible.
	 */
	private static MongoClient mongo;
	private static DB database;
		
	public static final String 	HOST_NAME="localhost",
								DB_NAME  ="network",
								FLOW_COLLECTION_NAME = "flow",
								IPS_COLLECTION_NAME = "ips",
								HEALTH_COLLECTION_NAME = "health";
	
	private final DBCollection flowCollection, healthCollection, ipsCollection;
	
	public MongoController() {
		connectToDatabase();
		flowCollection = database.getCollection(FLOW_COLLECTION_NAME);
		healthCollection = database.getCollection(HEALTH_COLLECTION_NAME);
		ipsCollection = database.getCollection(IPS_COLLECTION_NAME);
		writeQueue = new ArrayBlockingQueue<>(100);
		
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
	
	public void getConstrainedFlowEntries(String key, int min, int max){
		
		BasicDBObject query = new BasicDBObject(key,
				new BasicDBObject("$lt", max).append("$gt", min)
				);
		DBCursor cursor = flowCollection.find(query);
		for (DBObject dbo : cursor){
			System.out.println(dbo);
		}
	}
	
	public void printAllFlowEntries(){
		DBCursor c = flowCollection.find();
		for (int i=0;i<ZipReader.ROWS;i++){
			System.out.println(c.next());
		}
		
	}
	public void getSingleFlowEntry(){
		// return the first flow entry
		System.out.println(flowCollection.findOne());
	}
	
	public void storeSingleFlow(DBWritable dbw){
		try {
			writeQueue.put(dbw);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		flowCollection.insert(dbw.asDBObject());
	}
	
	public static void main(String[] args) {
		new MongoController().getConstrainedFlowEntries("i", 30, 40);
	}

	@Override
	public void run() {
		while (true){
			DBWritable dbw=null;
			try {
				dbw = writeQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DBCollection target = ipsCollection;
			if (dbw instanceof SingleFlow){
				target = flowCollection;
			}else if (dbw instanceof HealthMessage)
				target = healthCollection;
			target.insert(dbw.asDBObject());
			// TODO: handle Node and IPS messages
				
		}
				
	}
}
