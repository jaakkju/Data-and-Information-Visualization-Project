package big.marketing.controller;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import big.marketing.Settings;
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
public class MongoController implements Runnable {
	
	static Logger logger = Logger.getLogger(MongoController.class);

	/*
	 * writerThread and buffer for storing write requests. handling read
	 * requests in a seperate thread doesn't make sense, since reads are always
	 * synchronous (as a value has to be returned)
	 */
	Thread writer;

	EnumMap<DataType, CollectionHandler> collections;

	private volatile boolean writingEnabled = true;
	
	/*
	 * MongoDB should be started on the default port. TODO: start MongoDB
	 * automatically, if no connection to MongoDB possible.
	 */
	private static MongoClient mongo;
	private static DB database;

	public static String HOST_NAME = "localhost", DB_NAME = "eyeNet",
			FLOW_COLLECTION_NAME = "flow", IPS_COLLECTION_NAME = "ips",
			HEALTH_COLLECTION_NAME = "health",
			DESCRIPTION_COLLECTION_NAME = "nodes";
	public static int BUFFER_SIZE = 1000;

	public MongoController() {
		loadSettings();
		connectToDatabase();
		collections = new EnumMap<>(DataType.class);
		
		for (DataType t : DataType.values()){
			collections.put(t, new CollectionHandler(t));
		}

		writer = new Thread(this);
		writer.start();
	}

	private void loadSettings() {
		HOST_NAME = Settings.get("mongo.hostname");
		DB_NAME = Settings.get("mongo.databasename");
		FLOW_COLLECTION_NAME = Settings.get("mongo.collections.flow");
		IPS_COLLECTION_NAME = Settings.get("mongo.collections.ips");
		HEALTH_COLLECTION_NAME = Settings.get("mongo.collections.health");
		DESCRIPTION_COLLECTION_NAME = Settings.get("mongo.collections.network");
		BUFFER_SIZE= Settings.getInt("mongo.writeBuffer.size");
	}

	public void connectToDatabase() {

		try {
			mongo = new MongoClient(HOST_NAME);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		database = mongo.getDB(DB_NAME);
	}

	public List<DBObject> getConstrainedEntries(DataType t, String key, int min, int max) {

		BasicDBObject query = new BasicDBObject(key, new BasicDBObject("$lt",
				max).append("$gt", min));
		DBCursor cursor = getCollection(t).find(query);
		ArrayList<DBObject> result = new ArrayList<>();
		for (DBObject dbo : cursor) {
			result.add(dbo);
		}
		return result;
	}

	public void printAllEntries(DataType t) {
		DBCursor c = getCollection(t).find();
		for (int i = 0; i < ZipReader.ROWS; i++) {
			System.out.println(c.next());
		}

	}

	private BlockingQueue<DBObject> getBuffer(DataType t) {
		return collections.get(t).buffer;
	}

	/**
	 * Aggregate all occuring values of the given field into the set. Useful for
	 * analyzing the data.
	 * 
	 * @param t
	 *            DataType to look in
	 * @param fieldName
	 *            the values of this field are aggregated into the returned set.
	 * @return a set of String naming all occuring values in the given field
	 */
	public Set<String> getDomainOf(DataType t, String fieldName) {

		Set<String> result = new HashSet<>();
		DBObject fields = new BasicDBObject(fieldName, 1);
		fields.put("_id", 0);
		DBObject project = new BasicDBObject("$project", fields);

		DBObject groupFields = new BasicDBObject("_id", "$" + fieldName);
		DBObject group = new BasicDBObject("$group", groupFields);
		AggregationOutput output = getCollection(t).aggregate(project, group);

		for (DBObject dbo : output.results()) {
			result.add(dbo.get("_id").toString());
		}

		return result;
	}

	private DBCollection getCollection(DataType t) {
		return collections.get(t).collection;
	}

	public void storeEntry(DataType t, DBObject object) {
		if (object == null)
			return;
		try {
			getBuffer(t).put(object);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private String getCollectionName(DataType t){
		switch (t) {
		case FLOW:
			return FLOW_COLLECTION_NAME;
		case HEALTH:
			return HEALTH_COLLECTION_NAME;
		case IPS:
			return IPS_COLLECTION_NAME;
		case DESCRIPTION:
			return DESCRIPTION_COLLECTION_NAME;
		}
		return null;
	}

	public void clearCollection(DataType t) {
		getCollection(t).drop();
		logger.info("Dropped data of Type "+t.name());
	}

	public boolean isDataInDatabase(DataType t){
		return database.collectionExists(getCollectionName(t));
	}
	
	@Override
	public void run() {

		while (true) {
			if (writingEnabled) {
				for (CollectionHandler mc : collections.values()) {
					mc.flushBuffer();
				}
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class CollectionHandler {
		DBCollection collection;
		BlockingQueue<DBObject> buffer;

		public CollectionHandler(DataType t) {
			this.collection = database.getCollection(getCollectionName(t));
			this.buffer = new ArrayBlockingQueue<>(BUFFER_SIZE);
		}

		public void flushBuffer() {
			ArrayList<DBObject> tmpBuffer = new ArrayList<>(buffer.size());
			buffer.drainTo(tmpBuffer);
			collection.insert(tmpBuffer);
		}
	}
	public static void main(String[] args) {
//		[1364802616,1366020000]
		Settings.loadConfig();
		MongoController mc = new MongoController();
		int minCount = 1, maxCount=100;
		int min=1364802616,max=1366020000;
		for (int count=minCount;count<maxCount;count++){
			int range = max-min-count;
			int start = (int)Math.ceil(Math.random()*range+min);
			int end = start+count;
			long s = System.currentTimeMillis();
			logger.info("Starting Time-Query: "+start+" to "+end);
			List<DBObject> test = mc.getConstrainedEntries(DataType.FLOW, "Time", start, end);
			logger.info("Finish querying "+count+" time unit in flow. Objects: "+test.size()+" Duration: "+(System.currentTimeMillis()-s)+" ms"); 
		}
		
	}
}
