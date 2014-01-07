package big.marketing.test;

import java.util.ArrayList;
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
		MongoController mc = MongoController.getInstance();
		// for (DataType t : DataType.values()){
		// if (t != DataType.FLOW)
		// testPerformance(t, mc, 10,10,1000);
		// }
		testPerformance(DataType.FLOW, mc, 10, 3600 * 12, 3600 * 12);

	}

	public static void testPerformance(DataType t, MongoController mc, int times, int intervalMin, int intervalMax) {

		int minTime = 1364830798, maxTime = 1366012800;
		long turnTime = 0;
		int objects = 0;
		List<Long> vals = new ArrayList<Long>(times);
		for (int i = 0; i < times; i++) {
			// How many time units should be queried
			int intervalLength = (int) Math.ceil(Math.random() * (intervalMax - intervalMin) + intervalMin);

			// choose random start for time interval
			int start = (int) Math.ceil(Math.random() * (maxTime - minTime - intervalLength) + minTime);
			int end = start + intervalLength;

			long s = System.currentTimeMillis();
			// logger.info("Starting Time-Query: " + start + " to " + end);
			List<Object> test;
			test = mc.getConstrainedEntries(t, "time", start, end);

			turnTime = System.currentTimeMillis() - s;
			vals.add(turnTime);
			objects += test.size();
			logger.info("Finish querying " + intervalLength + " time units in " + t.name() + ". Objects: " + test.size()
			      + " Duration: " + turnTime + " ms");

		}
		long totalTime = 0;
		for (Long val : vals) {
			totalTime += val;
		}
		long avgTime = totalTime / times;
		long stddev = 0;
		for (Long val : vals) {
			stddev += (val - avgTime) * (val - avgTime);
		}
		stddev = (int) Math.sqrt(stddev);
		logger.info(t.name() + " total time: " + totalTime + "ms Objects:" + objects + " obj/ms: " + objects / totalTime);
		logger.info("Avg Time: " + (totalTime / times) + " ms Stddev: " + stddev + "ms");
	}
}
