package big.marketing.readers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import big.marketing.data.SingleFlow;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

public class ZipReader {

	public static final String FILE_FOLDER = "./data/";
	public static final String FILE_BIGBROTHER = "VAST2013MC3_BigBrother.zip";
	public static final String FILE_NETWORKFLOW = "VAST2013MC3_NetworkFlow.zip";
	public static final String FILE_WEEK2DATA = "week2data_fixed.zip";

	public static final int BIG_BROTHER = 0, FLOW = 1, IPS = 2,
	// for production a value of 25 000 000 is sufficient
	// for testing change this value to read only ROWS many rows
			ROWS = 25000000;

	/**
	 * Read a csv table from the given input stream.
	 * 
	 * @param is
	 *            stream to read from
	 * @param row
	 *            amount of rows to scan
	 * @return
	 */
	String[][] readCsvTable(InputStream is, int rows) {
		long start = System.currentTimeMillis();

		Scanner sc = new Scanner(is);
		sc.useDelimiter("\\r");
		String headings = sc.next().trim();
		int columns = (headings.split("\"").length + 1) / 2;
		String[][] out = null;
		if (rows == Integer.MAX_VALUE) {
			ArrayList<String[]> tmpList = new ArrayList<>(2000000);
			int i = 0;
			while (sc.hasNext()) {
				// tmpList.add(splitLine(sc.next().trim(), columns));
				sc.next();
				if (i++ % 100000 == 0)
					System.out.println(i);
			}
			System.out.println(i);
			System.out.println("found " + tmpList.size());
			out = (String[][]) tmpList.toArray(new String[tmpList.size()][]);
		} else {

		}
		sc.close();
		System.out.println((System.currentTimeMillis() - start));
		return out;
	}

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
					System.out.println("Found " + entry.getName());
					long start = System.currentTimeMillis();
					readCSVStream(is);
					// result = readCsvTable(is, ROWS);
					System.out.println(System.currentTimeMillis() - start);
				} catch (IOException e) {
					e.printStackTrace();
				}
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
	 *            line of the table to split
	 * @param columns
	 *            amount of columns in this entry
	 * @return array that contains one string for each cell
	 */
	String[] splitLine(String entry, int columns) {
		if (columns != 14)
			return entry.split(",");

		String[] cleanedSplit = new String[columns];
		String[] rawSplit = entry.split("\"");
		for (int i = 0; i < cleanedSplit.length; i++) {
			cleanedSplit[i] = rawSplit[i * 2 + 1];
		}

		return cleanedSplit;
	}

	public static void main(String[] args) {
		ZipReader r = new ZipReader();
		// some testing
		String[][] test = null;
		// test = r.read(BIG_BROTHER, 1); // 3407968 lines
		// test = r.read(BIG_BROTHER, 2); // 2165508 lines
		test = r.read(FLOW, 0); // chunk1 15172768 lines
		// // chunk2 21526139 lines
		// // chunk3 9439406 lines
		test = r.read(FLOW, 2); // 23258686 lines
		// test = r.read(IPS, 2); // 16600932 lines

	}

	// TODO Here is a nicer way to read CSV files with a opencsv reader

	/**
	 * reading inputstream with opencsv reader that returns a array of values
	 * http://opencsv.sourceforge.net/
	 * 
	 * @param in
	 *            input stream to read
	 * @throws IOException
	 */
	public void readCSVStream(InputStream in) throws IOException {
		boolean STOP = false;
		CSVReader reader = new CSVReader(new InputStreamReader(in));
		String[] nextLine;
		int i = 0;

		// discard first line with descriptions
		reader.readNext();

		// String [][] out = new String[ROWS][];
		while ((nextLine = reader.readNext()) != null && !STOP) {
			// nextLine[] is an array of values from the line
			// Before passing an array to HealthMessage constructor we hae to
			// take some indexes away
			// Removing element with ArrayUtils.removeElement(array, element)
			// System.out.println(Arrays.toString(nextLine));
			// System.out.println(nextLine.length);
			handleRow(nextLine);
			if (++i % 100000 == 0)
				System.out.println(i);
			// if (i>=ROWS)
			// STOP=true;
		}
		reader.close();
	}

	// ArrayList<SingleFlow> list = new ArrayList<>(20000000);
	private void handleRow(String[] nextLine) {

		// boolean mode=false;
		// modify and fill data structures here
		// list.add(new SingleFlow(nextLine));
		// if (!mode)
		// mode = nextLine[9].equals("1");
		// if (mode &&
		// nextLine[5].equals("172.10.1.109")
		//
		// && nextLine[6].equals("10.199.250.2"))
		// System.out.println(Arrays.toString(nextLine));
		// if (mode)
		// mode = nextLine[9].equals("1");

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
