package big.marketing.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import big.marketing.Settings;
import big.marketing.controller.MongoController;
import big.marketing.data.DBWritable;
import big.marketing.data.DataType;
import big.marketing.data.HealthMessage;
import big.marketing.data.IPSMessage;
import big.marketing.data.FlowMessage;

public class ZipReader {

	static Logger logger = Logger.getLogger(ZipReader.class);
	public static String FILE_FOLDER = "data/";
	public static String FILE_BIGBROTHER = "VAST2013MC3_BigBrother.zip";
	public static String FILE_NETWORKFLOW = "VAST2013MC3_NetworkFlow.zip";
	public static String FILE_WEEK2DATA = "week2data_fixed.zip";

	// for production a value of 25 000 000 should be sufficient
	// for testing change this value to read only ROWS many rows
	public static int ROWS = 500000;
//	public static final int ROWS = 50000000;
	
	
	/**
	 * current open zipFile, needs to be remembered to close it after usage.
	 */
	private ZipFile openZIP;
	
	
	/**
	 * Mongo instance to store all entries into. 
	 */
	private MongoController mongo;

	
	/**
	 * Look into the zipFile and collections InputStreams for all files, that are matching the Regex <b>streamName</b>
	 * @param zipFile Path of the ZipFile to open
	 * @param streamName Regex for files within the zipFile
	 * @return InputStreams for all matched files in the ZipFile
	 */
	private List<InputStream> getZipInputStreams(String zipFile, String streamName) {
		List<InputStream> matchedStreams = new ArrayList<InputStream>(5);
		try {
			openZIP = new ZipFile(FILE_FOLDER + zipFile);
			logger.info("Loading file " + openZIP.getName());
		} catch (IOException err) {
			// TODO errors should be thrown to upper level -> controller try-catch-block
			logger.error("Error while reading stream " + streamName + " from zipfile " + zipFile, err);
		}

		Enumeration<? extends ZipEntry> entries = openZIP.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().matches(streamName)) {
				try {
					matchedStreams.add(openZIP.getInputStream(entry));
					logger.info("Found entry " + entry.getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return matchedStreams;
	}
	
	/**
	 * @param mongo database where the entries will be stored.
	 */
	public ZipReader(MongoController mongo) {
		loadSettings();
		this.mongo = mongo;
	}
	
	private void loadSettings() {
		FILE_FOLDER = Settings.get("reader.folder");
		FILE_BIGBROTHER= Settings.get("reader.files.health");
		FILE_NETWORKFLOW= Settings.get("reader.files.flow");
		FILE_WEEK2DATA= Settings.get("reader.files.week2");
		ROWS= Settings.getInt("reader.maxrows");
	}
	
	/**
	 * Reading InputStream <b>in </b> and store each entry as <b>type</b> into the database.
	 * Uses CSV-Reader to parse the InputStream See: http://opencsv.sourceforge.net/
	 * @param in InputStream to read from
	 * @param type Found entries are handled as this type and thus stored in the database with this type.
	 * @throws IOException is thrown when IOException occurs within the Inputstream
	 */
	public int readCSVStream(InputStream in, DataType type) throws IOException {
		CSVReader reader = new CSVReader(new InputStreamReader(in));
		
		String[] nextLine;
		int i = 0;

		// discard first line with descriptions
		reader.readNext();

		while ((nextLine = reader.readNext()) != null && i<ROWS) {
			DBWritable dbw = createDataStructure(nextLine,type);
			mongo.storeEntry(type, dbw.asDBObject());
			if (++i % 100000 == 0)
				logger.info("Stored " + i + " objects"); // Informing GUI (progress bar or something like that) would be better
		}
		reader.close();
		return i;
	}
	
	
	/**
	 * Create an Object according to <b>type</b>. This object is not inserted in the database here.
	 * @param entry entry from the CSV-table to create the object from.
	 * @param type the type of the object that will be created
	 * @return the created object
	 */
	private DBWritable createDataStructure(String[] entry, DataType type) {
		DBWritable out = null;
		// modify and fill data structures here
		switch (type) {
		case FLOW:
			out = new FlowMessage(entry);
			break;
		case HEALTH:
			out = new HealthMessage(entry);
			break;
		case IPS:
			out = new IPSMessage(entry);
		default:
		}
		return out;
		
	}
	
	public void read(DataType ... types){
		for (DataType type : types){
			if (mongo.isDataInDatabase(type)){
				logger.info("Skipped reading "+type.name()+" because there is alreay some data in the database");
			}else{
				for (int week=1;week<=2;week++){
					read(type,week);
				}
			}
		}
	}
	
	
	/**
	 * Read data of type <b>type</b> of week <b>week</b> into the database.
	 * @param type which type of data to read
	 * @param week which week to read
	 */
	public void read(DataType type, int week) {
		String[] streamLocation = getFileNames(type, week);
		if (streamLocation[0] == null || streamLocation[1]==null){
			logger.warn("No data for combination " + type.toString() + " Week "+week);
			return;
		}
		List<InputStream> streams = getZipInputStreams(streamLocation[0], streamLocation[1]);
		int count=0;
		// Do two try-catch blocks independently to ensure that openZIP is really getting closed.
		try {
			for (InputStream is : streams)
				count += readCSVStream(is, type);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			openZIP.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("Finished reading "+count +" data entries for "+type.toString()+" Week "+week);
		
	}
	/** 
	 * Gives the filename of the zipFile and the path of the file within the zipFile
	 * @param type 
	 * @param week
	 * @return an array of length 2, <br>
	 * [0] is the path to the zipFile<br>
	 * [1] is the path (or regex) within the zipFile.
	 */
	private String [] getFileNames(DataType type, int week){
		String zipFile = null, streamName = null;
		if (week == 2) {
			zipFile = FILE_WEEK2DATA;
			switch (type) {
			case HEALTH:
				streamName = "bb-week2.csv";
				break;
			case FLOW:
				streamName = "nf-week2.csv";
				break;
			case IPS:
				streamName = "IPS-syslog-week2.csv";
				break;
			default:
			}
		} else {
			switch(type){
			case HEALTH:
				streamName = "bbexport-wiz2 - Copy.csv";
				zipFile = FILE_BIGBROTHER;
				break;
			case FLOW:
				zipFile = FILE_NETWORKFLOW;
				streamName = "nf/nf-chunk\\d\\.csv";
			default:
			}
		}
		return new String [] {zipFile,streamName};
	}
}
