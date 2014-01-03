package big.marketing.controller;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import big.marketing.Settings;
import big.marketing.data.DataType;

import com.mongodb.BasicDBList;
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

	private static Process mongoProcess;
	private static MongoClient mongo;
	private static DB database;

	public static String HOST_NAME = "localhost", DB_NAME = "eyeNet",
			FLOW_COLLECTION_NAME = "flow", IPS_COLLECTION_NAME = "ips",
			HEALTH_COLLECTION_NAME = "health",
			DESCRIPTION_COLLECTION_NAME = "nodes";
	public static int BUFFER_SIZE = 1000;
	public static int MAX_TRIES = 3;

	public static String MONGOD_PATH = "data/mongo/mongod.exe";
	public static String DB_PATH = "data/db";
	public static String MONGO_LOG_FILE = "mongo.log";

	public MongoController() {
		loadSettings();

		boolean isConnected = connectToDatabase();
		for (int i = 1; i <= MAX_TRIES && !isConnected; i++) {
			logger.warn("Could not connect to MongoDB in try " + i + " of "
					+ MAX_TRIES);
			startMongoDBProcess();
			isConnected = connectToDatabase();
		}

		if (!isConnected) {
			logger.fatal("Failed to start MongoDB! Exiting now ...");
			System.exit(1);
		}

		collections = new EnumMap<DataType,CollectionHandler>(DataType.class);

		for (DataType t : DataType.values()) {
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
		BUFFER_SIZE = Settings.getInt("mongo.writeBuffer.size");

		MONGOD_PATH = Settings.get("mongo.exe.path");
		DB_PATH = Settings.get("mongo.exe.dbpath");
		MONGO_LOG_FILE = Settings.get("mongo.exe.log");
		MAX_TRIES = Settings.getInt("mongo.exe.maxtries");

	}

	public boolean connectToDatabase() {

		try {
			mongo = new MongoClient(HOST_NAME);
			mongo.getConnector().getDBPortPool(mongo.getAddress()).get()
					.ensureOpen();
			database = mongo.getDB(DB_NAME);
			logger.info("Connection to MongoDB established");
			return true;
		} catch (UnknownHostException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.warn("No MongoDB server running!");
		}
		return false;
	}

	public void startMongoDBProcess() {
		try {
			String canPath = new File(MONGOD_PATH).getCanonicalPath();
			mongoProcess = Runtime.getRuntime().exec(
					canPath + " --dbpath=" + DB_PATH + " --logpath "
							+ MONGO_LOG_FILE);

			// ensure that mongoDB is closed on shutdown of the VM
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					shutDown();
				}
			}));
			logger.info("Sucessfully started MongoDB");
		} catch (IOException e) {
			logger.error("Failed to start MongoDB: " + e.getMessage());
		}
	}
	
	public void shutDown(){
		logger.info("Shutting down database...");
		// Check for open queries and cancel them
		DBObject dbObject = database.getCollection("$cmd.sys.inprog").findOne();
		if (mongoProcess != null && dbObject != null){
			
			BasicDBList currentOps = (BasicDBList) dbObject.get("inprog");
			for (Object o : currentOps) {
				DBObject operation = (BasicDBObject ) o;
				String opType =(String) operation.get("op");
				String opNamespace = (String) operation.get("ns");
				if (opType.equals("query") && opNamespace.contains(DB_NAME)){
					int opid = (Integer) operation.get("opid");
					database.eval("db.killOp(" + opid + ")");
					logger.info("Killed active query on "+DB_NAME+" with opcode "+opid);
				}
			}

			mongoProcess.destroy();
		}
	}
	
	private BlockingQueue<DBObject> getBuffer(DataType t) {
		return collections.get(t).buffer;
	}

	private DBCollection getCollection(DataType t) {
		return collections.get(t).collection;
	}

	private String getCollectionName(DataType t) {
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

	public boolean isDataInDatabase(DataType t) {
		return database.collectionExists(getCollectionName(t));
	}

	public List<DBObject> getConstrainedEntries(DataType t, String key,
			int min, int max) {

		BasicDBObject query = new BasicDBObject(key, new BasicDBObject("$lt",
				max).append("$gt", min));
		DBCursor cursor = getCollection(t).find(query);
		ArrayList<DBObject> result = new ArrayList<DBObject>();
		try{
			for (DBObject dbo : cursor) {
				result.add(dbo);
			}
		} catch (Exception e){
			logger.error("Error when reading from database: "+e.getLocalizedMessage());
		}
		return result;
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
	public Collection<Object> getDomainOf(DataType t, String fieldName) {
		return getCollection(t).distinct(fieldName);
	}

	public void clearCollection(DataType t) {
		String name = getCollectionName(t);
		if (database.getCollectionNames().contains(name)) {
			getCollection(t).drop();
			logger.info("Dropped data of Type " + t.name());
		} else {
			logger.warn("Dropping Collection failed! No Collection with name "
					+ name + " (for Type " + t.name() + ")");
		}
	}

	public void storeEntry(DataType t, DBObject object) {
		if (object == null)
			return;
		if (getBuffer(t).size() >= BUFFER_SIZE && sem.availablePermits() <= 0){
			sem.release();
		}
		try {
			getBuffer(t).put(object);
		} catch (InterruptedException e) {
			logger.error("Interrupted: "
					+ e.getLocalizedMessage());
		}
	}
	private Semaphore sem;

	@Override
	public void run() {
		sem = new Semaphore(1);
		while (true) {
			try {
				sem.acquire();
			} catch (InterruptedException e) {
				logger.error("Interrupted: "
						+ e.getLocalizedMessage());
			}
			for (CollectionHandler mc : collections.values()) {
				if (!mc.buffer.isEmpty())
					mc.flushBuffer();
			}
		}
	}

	private class CollectionHandler {
		DBCollection collection;
		BlockingQueue<DBObject> buffer;

		public CollectionHandler(DataType t) {
			this.collection = database.getCollection(getCollectionName(t));
			this.buffer = new ArrayBlockingQueue<DBObject>(BUFFER_SIZE);
		}

		public void flushBuffer() {
			ArrayList<DBObject> tmpBuffer = new ArrayList<DBObject>(buffer.size());
			buffer.drainTo(tmpBuffer);
			collection.insert(tmpBuffer);
		}
	}
}
