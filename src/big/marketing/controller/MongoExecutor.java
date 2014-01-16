package big.marketing.controller;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import big.marketing.Settings;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;

public class MongoExecutor {
	static Logger logger = Logger.getLogger(MongoExecutor.class);

	private static Process mongoProcess;
	public static String MONGOD_PATH = "data/mongo/mongod.exe";
	public static String DB_PATH = "data/db";
	public static String MONGO_LOG_FILE = "data/mongo/mongo.log";
	public static String MONGO_OPTIONS = "--noprealloc --nojournal";

	private static void loadSettings() {
		MONGOD_PATH = Settings.get("mongo.exe.path");
		DB_PATH = Settings.get("mongo.exe.dbpath");
		MONGO_LOG_FILE = Settings.get("mongo.exe.log");
		MONGO_OPTIONS = Settings.get("mongo.exe.options");
	}

	public static void startMongoProcess() {
		loadSettings();
		if (mongoProcess != null) {
			logger.info("Database already started!");
			return;
		}
		try {
			String executable = MONGOD_PATH + "/mongod";
			if (System.getProperty("os.name").toLowerCase().contains("win"))
				executable += ".exe";
			String canPath = new File(executable).getCanonicalPath();
			String mongoCommand = canPath + " --dbpath=" + DB_PATH + " --logpath " + MONGO_LOG_FILE;
			if (MONGO_OPTIONS != null)
				mongoCommand = mongoCommand + " " + MONGO_OPTIONS;

			// remove lockFile of mongoDB
			try {
				File lockFile = new File(DB_PATH, "mongod.lock");
				if (lockFile.exists())
					lockFile.delete();
				logger.info("Removed mongo lock file");
			} catch (Exception e) {
				logger.error("Could not remove mongo lock file! Are you sure monge is not running?");
			}

			logger.info("Starting mongoDB, command: " + mongoCommand);

			mongoProcess = Runtime.getRuntime().exec(mongoCommand);

			logger.info("Sucessfully started MongoDB");
		} catch (IOException e) {
			logger.error("Failed to start MongoDB: " + e.getMessage());
		}
	}

	public static void killMongoProcess() {
		// Check for open queries and cancel them
		DB database = MongoController.getDatabase();
		DBObject dbObject = null;
		try {
			dbObject = database.getCollection("$cmd.sys.inprog").findOne();
		} catch (Exception e) {
			logger.warn("Failed to get query status, no database connected!");
			return;
		}
		if (dbObject != null) {
			logger.info("Killing running queries");
			BasicDBList currentOps = (BasicDBList) dbObject.get("inprog");
			for (Object o : currentOps) {
				DBObject operation = (BasicDBObject) o;
				String opType = (String) operation.get("op");
				String opNamespace = (String) operation.get("ns");
				if (opType.equals("query") && opNamespace.contains(MongoController.DB_NAME)) {
					int opid = (Integer) operation.get("opid");
					database.eval("db.killOp(" + opid + ")");
					logger.info("Killed active query on " + MongoController.DB_NAME + " with opcode " + opid);
				}
			}

		}
		if (mongoProcess != null) {
			logger.info("Shutting down database...");
			mongoProcess.destroy();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mongoProcess = null;
		}
	}

}
