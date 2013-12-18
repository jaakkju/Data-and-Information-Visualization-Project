package big.marketing.controller;

import java.net.UnknownHostException;

import big.marketing.data.SingleFlow;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
/*
 * Tutorial on Mongo with java:
 * http://docs.mongodb.org/ecosystem/tutorial/getting-started-with-java-driver/#getting-started-with-java-driver
 */
public class MongoController {
	
	/*
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
	
	
	public void getSingleFlowEntry(){
		// return the first flow entry
		System.out.println(flowCollection.findOne());
	}
	
	public void storeSingleFlow(SingleFlow flow){
		flowCollection.insert(flow.asDBObject());
	}
	
	public static void main(String[] args) {
		new MongoController().getConstrainedFlowEntries("i", 30, 40);
	}
}
