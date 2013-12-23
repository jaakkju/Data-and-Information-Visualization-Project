package big.marketing.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.csvreader.CsvReader;

import au.com.bytecode.opencsv.CSVReader;
import big.marketing.controller.MongoController;
import big.marketing.data.DBWritable;
import big.marketing.data.DataType;
import big.marketing.data.HealthMessage;
import big.marketing.data.SingleFlow;

public class ZipReader {

	public static final String FILE_FOLDER = "./data/";
	public static final String FILE_BIGBROTHER = "VAST2013MC3_BigBrother.zip";
	public static final String FILE_NETWORKFLOW = "VAST2013MC3_NetworkFlow.zip";
	public static final String FILE_WEEK2DATA = "week2data_fixed.zip";

	// for production a value of 25 000 000 is sufficient
	// for testing change this value to read only ROWS many rows
	public static final int ROWS = 500000;//25000000;
	private ZipFile openZIP;

	/**
	 * Reads in some csv tables from a zip file. The files inside the zipFile
	 * that are read in are specified by the regex streamName. This may be a
	 * single filename or a regex matching several files.
	 * 
	 * @param zipFile
	 *            name of the actual zip file
	 * @param streamName
	 *            path of the file inside the zip file (may be a regex for
	 *            several files)
	 * @return read rows of the
	 */
	private InputStream getZipInputStream(String zipFile, String streamName) {
		System.out.println("Loading " + FILE_FOLDER + zipFile);
		try {
			openZIP = new ZipFile(FILE_FOLDER + zipFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Enumeration<? extends ZipEntry> entries = openZIP.entries();
		InputStream is = null;
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().matches(streamName)) {
				try {
					is = openZIP.getInputStream(entry);
					System.out.println("Found " + entry.getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return is;
	}

	public static void main(String[] args) {
		ZipReader r = new ZipReader();
		// some testing
		String[][] test = null;
		// test = r.read(BIG_BROTHER, 1); // 3407968 lines
		// test = r.read(BIG_BROTHER, 2); // 2165508 lines
		long start = System.currentTimeMillis();
//		test = r.read(DataType.FLOW, 0); // chunk1 15172768 lines
		System.out.println(System.currentTimeMillis()-start);
		// // chunk2 21526139 lines
		// // chunk3 9439406 lines
//		test = r.read(FLOW, 2); // 23258686 lines
		// test = r.read(IPS, 2); // 16600932 lines
//		mc.printAllFlowEntries();
	}

	/**
	 * reading inputstream with opencsv reader that returns a array of values
	 * http://opencsv.sourceforge.net/
	 * 
	 * @param in
	 *            input stream to read
	 * @throws IOException
	 */
	public void readCSVStream(InputStream in, DataType type) throws IOException {
		CSVReader reader = new CSVReader(new InputStreamReader(in));
		
		String[] nextLine;
		int i = 0;

		// discard first line with descriptions
		reader.readNext();

		while ((nextLine = reader.readNext()) != null && i<ROWS) {
			DBWritable dbw = createDataStructure(nextLine,type);
			mongo.storeEntry(type, dbw.asDBObject());
			if (++i % 100000 == 0)
				System.out.println(i);
		}
		reader.close();
	}
	
	// just for testing
	private static MongoController mongo=new MongoController();
	
	private DBWritable createDataStructure(String[] nextLine, DataType type) {
		DBWritable out = null;
		// modify and fill data structures here
		switch (type) {
		case FLOW:
			out = new SingleFlow(nextLine);
			break;
		case HEALTH:
			out = new HealthMessage(nextLine);
			break;
		case IPS:
			// TODO: create datastructure for IPS-messages
//			out = new SingleFlow(nextLine);
//			break;
		default:
		}
		mongo.storeEntry(type, out.asDBObject());
		return out;
		
	}

	public void read(DataType type, int week) {
		String[] streamLocation = getFileNames(type, week);
		InputStream is = getZipInputStream(streamLocation[0], streamLocation[1]);
		
		// Do two try-catch blocks independently to ensure that openZIP is really getting closed.
		try {
			readCSVStream(is, type);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			openZIP.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
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
