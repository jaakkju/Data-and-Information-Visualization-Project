package big.marketing.controller;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.jfree.data.time.Minute;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import big.marketing.Application;
import big.marketing.Settings;
import big.marketing.data.DataType;
import big.marketing.data.FlowMessage;
import big.marketing.data.HealthMessage;
import big.marketing.data.IPSMessage;
import big.marketing.data.Node;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/*
 * Tutorial on Mongo with java:
 * http://docs.mongodb.org/ecosystem/tutorial/getting-started-with-java-driver/#getting-started-with-java-driver
 */
public class MongoController implements Runnable {

	static Logger logger = Logger.getLogger(MongoController.class);

	/*
	 * writerThread and buffer for storing write requests. handling read requests
	 * in a seperate thread doesn't make sense, since reads are always
	 * synchronous (as a value has to be returned)
	 */
	Thread writer;

	EnumMap<DataType, CollectionHandler> collections;

	private static MongoController instance;
	private static MongoClient mongo;
	private static DB database;

	public static String HOST_NAME = "localhost", DB_NAME = "eyeNet", FLOW_COLLECTION_NAME = "flow", IPS_COLLECTION_NAME = "ips",
	      HEALTH_COLLECTION_NAME = "health", DESCRIPTION_COLLECTION_NAME = "nodes";
	public static int BUFFER_SIZE = 1000;
	public static int MAX_TRIES = 3;

	public static MongoController getInstance() {
		if (instance == null)
			instance = new MongoController();
		return instance;
	}

	private MongoController() {
		loadSettings();

		boolean isConnected = connectToDatabase();
		for (int i = 1; i <= MAX_TRIES && !isConnected; i++) {
			logger.warn("Could not connect to MongoDB in try " + i + " of " + MAX_TRIES);
			MongoExecutor.startMongoProcess();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.warn("Interrupted: " + e.getLocalizedMessage());
			}
			isConnected = connectToDatabase();
		}

		if (!isConnected) {
			logger.fatal("Failed to start MongoDB! Exiting now ...");
			Application.quit();
		}

		collections = new EnumMap<DataType, CollectionHandler>(DataType.class);

		for (DataType t : DataType.values()) {
			collections.put(t, new CollectionHandler(t));
		}

		writer = new Thread(this, "DB-Writer");
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
		MAX_TRIES = Settings.getInt("mongo.exe.maxtries");

	}

	public boolean connectToDatabase() {

		try {
			mongo = new MongoClient(HOST_NAME);
			mongo.getConnector().getDBPortPool(mongo.getAddress()).get().ensureOpen();
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

	private BlockingQueue<DBObject> getBuffer(DataType t) {
		return collections.get(t).buffer;
	}

	DBCollection getCollection(DataType t) {
		return collections.get(t).collection;
	}

	public void setCollection(DataType t, String newCollectionName, boolean dropOld) {

		// stop the writer thread and wait for it to finish
		int permits = sem.drainPermits();
		while (!sem.hasQueuedThreads()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error(e.getLocalizedMessage());
			}
		}

		collections.get(t).flushBuffer();
		DBCollection old = getCollection(t);
		DBCollection newColl = database.getCollection(newCollectionName);

		if (dropOld) {
			newColl.rename(getCollectionName(t), true);
		} else {
			old.rename("old" + getCollectionName(t));
			newColl.rename(getCollectionName(t));
		}
		collections.put(t, new CollectionHandler(t));
		sem.release(permits);
	}

	public void flushBuffers() {
		for (CollectionHandler handler : collections.values()) {
			handler.flushBuffer();
		}
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

	private Iterable<DBObject> query(DataType t, String key, int min, int max) {
		BasicDBObject query = new BasicDBObject(key, new BasicDBObject("$lt", max).append("$gt", min));
		return getCollection(t).find(query);
	}

	private Iterable<DBObject> aggregate(DataType t, String key, int min, int max) {
		ProcessingWorker pw = new ProcessingWorker(this, t);
		return pw.getAggregationQuery(key, min, max);
	}

	public List<Object> getConstrainedEntries(DataType t, String key, int min, int max) {
		return getConstrainedEntries(t, key, min, max, false);
	}

	/**
	 * Queries all entries of type t with an value for key between min and max. Aggregates the result with
	 * the aggregation parameters specified in ProcessingWorker (which are used for processing the source data).
	 * <br>
	 * <b> WARNING: </b> Aggregation result must not be bigger than 16MB due to size restrictions of mongodb!
	 * @param t type of data to query
	 * @param key field for query restriction
	 * @param min minimum value on field key
	 * @param max maximum value on field kay
	 * @param aggregate aggregate the results with default aggregation parameters
	 * @return a list containing the result of the query, converted to the *Message objects according to given type
	 */
	public List<Object> getConstrainedEntries(DataType t, String key, int min, int max, boolean aggregate) {
		Iterable<DBObject> queryResult = null;
		if (aggregate)
			queryResult = aggregate(t, key, min, max);
		else
			queryResult = query(t, key, min, max);
		ArrayList<Object> result = new ArrayList<Object>();
		try {
			for (DBObject dbo : queryResult) {
				result.add(convert(t, dbo));
			}
		} catch (Exception e) {
			logger.error("Error when reading from database: " + e.getLocalizedMessage());
		}
		return result;
	}

	private Object convert(DataType t, DBObject dbo) {
		switch (t) {
		case IPS:
			return new IPSMessage(dbo);
		case FLOW:
			return new FlowMessage(dbo);
		case HEALTH:
			return new HealthMessage(dbo);
		case DESCRIPTION:
			return new Node(dbo);
		default:
			return null;
		}
	}

	/**
	 * Aggregate all occuring values of the given field into the set. Useful for
	 * analyzing the data.
	 * 
	 * @param t DataType to look in
	 * @param fieldName the values of this field are aggregated into the returned
	 *           set.
	 * @return a set of String naming all occuring values in the given field
	 */
	@SuppressWarnings("unchecked")
	public Collection<Object> getDomainOf(DataType t, String fieldName) {
		return getCollection(t).distinct(fieldName);
	}

	public void clearCollection(DataType t) {
		String name = getCollectionName(t);
		if (database.getCollectionNames().contains(name)) {
			getCollection(t).drop();
			logger.info("Dropped data of Type " + t.name());
		} else {
			logger.warn("Dropping Collection failed! No Collection with name " + name + " (for Type " + t.name() + ")");
		}
	}

	static DB getDatabase() {
		return database;
	}

	public Map<String, Node> getNetwork() {
		List<Node> result = new ArrayList<Node>();
		Map<String, Node> ipNodeMap = new HashMap<String, Node>();
		DBCollection names = database.getCollection(DESCRIPTION_COLLECTION_NAME);
		for (DBObject obj : names.find()) {
			Node n = (Node) convert(DataType.DESCRIPTION, obj);
			result.add(n);
			ipNodeMap.put(n.getAddress(), n);
		}
		return ipNodeMap;
	}

	public void storeEntry(DataType t, DBObject object) {
		if (object == null)
			return;
		if (getBuffer(t).size() >= BUFFER_SIZE && sem.availablePermits() <= 0) {
			sem.release();
		}
		try {
			getBuffer(t).put(object);
		} catch (InterruptedException e) {
			logger.error("Interrupted: " + e.getLocalizedMessage());
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
				logger.error("Interrupted: " + e.getLocalizedMessage());
			}
			for (CollectionHandler mc : collections.values()) {
				if (!mc.buffer.isEmpty())
					mc.flushBuffer();
			}
		}
	}

	public TimeSeriesCollection getHistogramTCollection(DataType t) {
		TimeSeriesCollection tSeriesCollection = new TimeSeriesCollection();

		switch (t) {
		case FLOW:
			BasicDBObject groupFieldsFlow = new BasicDBObject("_id", "$time");
			groupFieldsFlow.append("y", new BasicDBObject("$sum", "$packetCount"));
			DBObject groupOpFlow = new BasicDBObject("$group", groupFieldsFlow);

			tSeriesCollection.addSeries(getTimeSerie(DataType.FLOW, groupOpFlow, "_id", "y", DataType.FLOW.toString(), true));
			return tSeriesCollection;

		case IPS:
			BasicDBObject groupFieldsIPS = new BasicDBObject("_id", "$time");
			groupFieldsIPS.append("y", new BasicDBObject("$sum", 1));
			DBObject groupOpIPS = new BasicDBObject("$group", groupFieldsIPS);

			tSeriesCollection.addSeries(getTimeSerie(DataType.IPS, groupOpIPS, "_id", "y", DataType.IPS.toString(), false));
			return tSeriesCollection;

		case HEALTH:
			//			BasicDBObject groupFieldsHealth = new BasicDBObject("_id", new BasicDBObject("time", "$time").append("status", "$statusVal"));
			BasicDBObject groupFieldsHealth = new BasicDBObject("_id", "$time");
			groupFieldsHealth.append("y", new BasicDBObject("$sum", 1));
			DBObject groupOpIPSHealth = new BasicDBObject("$group", groupFieldsHealth);

			tSeriesCollection.addSeries(getTimeSerie(DataType.HEALTH, groupOpIPSHealth, "_id", "y", DataType.HEALTH.toString(), false));
			return tSeriesCollection;

		default:
			return null;
		}
	}

	public TimeSeries getTimeSerie(DataType t, DBObject groupOp, String groupNameX, String groupNameY, String cName, Boolean useMovingAverage) {
		long begin = new Date().getTime();

		// Query fetches collection t
		DBCollection c = getCollection(t);
		AggregationOutput ao = c.aggregate(groupOp);
		TimeSeries timeserie = new TimeSeries(cName);

		HashMap<Integer, Integer> valueMap = new HashMap<Integer, Integer>();
		List<Integer> xVals = new ArrayList<Integer>();
		for (DBObject dbo : ao.results()) {
			int x = (Integer) dbo.get(groupNameX);
			int y = (Integer) dbo.get(groupNameY);
			xVals.add(x);
			valueMap.put(x, y);
		}

		int min = Collections.min(xVals);
		int max = Collections.max(xVals);
		for (int i = min; i <= max; i += 60) {
			timeserie.add(new Minute(new Date(i * 1000L)), valueMap.get(i));
		}

		if (useMovingAverage) {
			timeserie = MovingAverage.createMovingAverage(timeserie, "", 50, 100);
		}

		logger.info("Fetched slider backgroud data, DataType:" + t.toString() + ", query took " + (new Date().getTime() - begin) + " ms");

		return timeserie;
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
