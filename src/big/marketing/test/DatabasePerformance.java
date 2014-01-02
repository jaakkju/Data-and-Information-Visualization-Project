package big.marketing.test;

import java.util.List;

import org.apache.log4j.Logger;

import big.marketing.Settings;
import big.marketing.controller.MongoController;
import big.marketing.data.DataType;

import com.mongodb.DBObject;

public class DatabasePerformance {
	static Logger logger = Logger.getLogger(DatabasePerformance.class);

	public static void main(String[] args) {
		// Testing performance of Queries
		Settings.loadConfig();
		MongoController mc = new MongoController();
		int maxTries = 10;
		int minTime = 1364802616, maxTime = 1366020000;
		for (int i = 0; i < maxTries; i++) {
			
			// How many time units should be queried
			int intervalLength = (int) Math.ceil(Math.random() * 1000 + 10);
			
			// choose random start for time interval
			int start = (int) Math.ceil(Math.random() * (maxTime - minTime - intervalLength) + minTime);
			int end = start + intervalLength;
			
			long s = System.currentTimeMillis();
			logger.info("Starting Time-Query: " + start + " to " + end);
			List<DBObject> test;
			try {
				test = mc.getConstrainedEntries(DataType.FLOW,
						"Time", start, end);
				logger.info("Finish querying " + intervalLength
						+ " time units in flow. Objects: " + test.size()
						+ " Duration: " + (System.currentTimeMillis() - s) + " ms");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
