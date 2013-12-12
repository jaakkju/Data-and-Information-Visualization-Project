package big.marketing.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileReader {

	private static final String FILE_FOLDER 			= "./data/";
	public static final String FILE_DESCRIPTION 		= "2013MC3AnswerSheetandDataDescriptions.zip";
	public static final String FILE_SUPLEMENTARY 	= "Supplementary Data Descriptions for Week 2.zip";
	public static final String FILE_BIGBROTHER 		= "VAST2013MC3_BigBrother.zip";
	public static final String FILE_NETWORKFLOW 		= "VAST2013MC3_NetworkFlow.zip";
	public static final String FILE_WEEK2DATA 		= "week2data.zip";

	public static final int BIG_BROTHER = 0, FLOW = 1, IPS = 2, ROWS = 10;

	/**
	 * Read a csv table from the given input stream.
	 * 
	 * @param is stream to read from
	 * @param row amount of rows to scan
	 * @return
	 */
	String[][] readCsvTable(InputStream is, int rows) {
		
		if (rows == Integer.MAX_VALUE) {
			// TODO: use an ArrayList for unbounded input reading...
		}

		String[][] out = new String[rows][];
		Scanner sc = new Scanner(is);
		sc.useDelimiter("\\r");
		String headings = sc.next().trim();
		int columns = (headings.split("\"").length + 1) / 2;

		for (int i = 0; i < out.length; i++) {
			out[i] = splitLine(sc.next().trim(), columns);
		}
		sc.close();
		return out;
	}

	/**
	 * Reads in some csv tables from a zip file. The files inside the zipFile
	 * that are read in are specified by the regex streamName. This may be a
	 * single filename or a regex matching several files.
	 * 
	 * @param zipFile
	 *           name of the actual zip file
	 * @param streamName
	 *           path of the file inside the zip file (may be a regex for several
	 *           files)
	 * @return read rows of the
	 */
	String[][] readFromZip(String zipFile, String streamName) {
		System.out.println("Loading " + FILE_FOLDER + zipFile);
		ZipFile zip = null;

		try {
			zip = new ZipFile(FILE_FOLDER + zipFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Enumeration<? extends ZipEntry> entries = zip.entries();
		String[][] result = null;
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().matches(streamName)) {
				InputStream is = null;
				try {
					is = zip.getInputStream(entry);
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Found " + entry.getName());
				result = readCsvTable(is, ROWS);

			}
		}

		try {
			zip.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * handles different splitting behaviour. The big brother data contains
	 * additional quotes, that have to be removed. All other data can be simply
	 * split at each ",".
	 * 
	 * @param entry
	 *           line of the table to split
	 * @param columns
	 *           amount of columns in this entry
	 * @return array that contains one string for each cell
	 */
	String[] splitLine(String entry, int columns) {
		if (columns != 18)
			return entry.split(",");
		String[] splitted = new String[columns];
		int idx = 0;
		for (int j = 0; j < splitted.length; j++) {
			int nextidx = entry.indexOf('"', idx + 1);
			splitted[j] = entry.substring(idx + 1, nextidx);
			idx = entry.indexOf('"', nextidx + 1);

		}
		return splitted;
	}

	public static void main(String[] args) {
		// Reader r = new Reader(null);
		// some testing
		// String [][] test = null;
		// test = r.read(BIG_BROTHER, 1);
		// test = r.read(BIG_BROTHER, 2); // NOT WORKING YET because of invalid
		// header exception ???
		// test = r.read(FLOW, 0);
		// test = r.read(FLOW, 2 ); // invalid header...
		// test = r.read(IPS, 2); // invalid header...

	}

	public String[][] read(int type, int week) {
		String zipFile = null, streamName = null;
		if (week == 2) {
			zipFile = FILE_WEEK2DATA;
			switch (type) {
			case BIG_BROTHER:
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
			if (type == BIG_BROTHER) {
				streamName = "bbexport-wiz2 - Copy.csv";
				zipFile = FILE_BIGBROTHER;
			} else if (type == FLOW) {
				zipFile = FILE_NETWORKFLOW;
				streamName = "nf/nf-chunk\\d\\.csv";
			}
		}
		if (zipFile == null || streamName == null)
			throw new IllegalArgumentException("invalid type or week");
		return readFromZip(zipFile, streamName);

	}
}
