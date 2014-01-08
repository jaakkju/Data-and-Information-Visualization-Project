package big.marketing.controller;

import org.apache.log4j.Logger;

import big.marketing.data.DataType;

public class DataProcessor implements Runnable {
	static Logger logger = Logger.getLogger(DataProcessor.class);
	private MongoController mc;
	private DataType[] typesToProcess;

	public DataProcessor(MongoController mc, DataType... typesToProcess) {
		this.mc = mc;
		this.typesToProcess = typesToProcess;
	}

	public void run() {

		for (DataType type : typesToProcess) {
			ProcessingWorker pw = new ProcessingWorker(mc, type);
			pw.process();
		}
		
		logger.info("Finished data processing");

	}
}
