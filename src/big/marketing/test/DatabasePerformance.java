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
		for (DataType t : DataType.values()){
			testPerformance(t, mc,10,10,1000);
		}
		
	}
	
	public static void testPerformance(DataType t, MongoController mc, int times, int intervalMin, int intervalMax){
		
		int minTime = 1364830798, maxTime = 1366012800;
		long totalTime=0;
		long turnTime=0;
		for (int i = 0; i < times; i++) {
			
			// How many time units should be queried
			int intervalLength = (int) Math.ceil(Math.random() * (intervalMax-intervalMin) + intervalMin);
			
			// choose random start for time interval
			int start = (int) Math.ceil(Math.random() * (maxTime - minTime - intervalLength) + minTime);
			int end = start + intervalLength;
			
			long s = System.currentTimeMillis();
			logger.info("Starting Time-Query: " + start + " to " + end);
			List<DBObject> test;
			test = mc.getConstrainedEntries(t, "Time", start, end);

			turnTime = System.currentTimeMillis() - s;
			totalTime += turnTime;
//			logger.info("Finish querying " + intervalLength + " time units in "
//					+ t.name() + ". Objects: " + test.size() + " Duration: "
//					+ turnTime + " ms");

		}
		logger.info(t.name()+" total time: "+totalTime+"ms  Avg Time: "+(totalTime/times)+" ms");
	}
}
