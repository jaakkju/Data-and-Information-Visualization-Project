package big.marketing.readers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

public class ZipReader {

	public static final String FILE_FOLDER = "./data/";
	public static final String FILE_BIGBROTHER = "VAST2013MC3_BigBrother.zip";
	public static final String FILE_NETWORKFLOW = "VAST2013MC3_NetworkFlow.zip";
	public static final String FILE_WEEK2DATA = "week2data_fixed.zip";

	public static final int BIG_BROTHER=0, FLOW=1, IPS=2, ROWS=Integer.MAX_VALUE;
	/**
	 * Read a csv table from the given input stream.
	 * @param is stream to read from
	 * @param row amount of rows to scan
	 * @return
	 */
	String [][] readCsvTable(InputStream is, int rows){
		long start = System.currentTimeMillis();

		Scanner sc = new Scanner(is);
		sc.useDelimiter("\\r");
		String headings = sc.next().trim();
		int columns = (headings.split("\"").length+1)/2;
		String [][] out=null;
		if (rows == Integer.MAX_VALUE){
			ArrayList<String[]> tmpList = new ArrayList<>(2000000);
			int i=0;
			while (sc.hasNext()){
//				tmpList.add(splitLine(sc.next().trim(), columns));
				sc.next();
				if (i++ % 100000 == 0)
					System.out.println(i);
			}
			System.out.println(i);
			System.out.println("found "+tmpList.size());
			out = (String[][]) tmpList.toArray(new String[tmpList.size()][]); 
		}else{
			
		}
		sc.close();
		System.out.println((System.currentTimeMillis()-start));
		return out;
	}

	/**
	 * Reads in some csv tables from a zip file. The files inside the zipFile
	 * that are read in are specified by the regex streamName. This may be a
	 * single filename or a regex matching several files.
	 * 
	 * @param zipFile name of the actual zip file
	 * @param streamName path of the file inside the zip file (may be a regex for several files)
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
//					result = readCsvTable(is, ROWS);
					System.out.println(System.currentTimeMillis()-start);
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
	 * @param entry line of the table to split
	 * @param columns amount of columns in this entry
	 * @return array that contains one string for each cell
	 */
	String[] splitLine(String entry, int columns) {
		if (columns != 14)
			return entry.split(",");
		
		String [] cleanedSplit = new String[columns];
		String[] rawSplit = entry.split("\"");
		for (int i=0;i<cleanedSplit.length;i++){
			cleanedSplit[i] = rawSplit[ i*2 +1];
		}		
		
		return cleanedSplit;
	}

	public static void main(String[] args) {
		 ZipReader r = new ZipReader();
		// some testing
		String [][] test = null;
//		test = r.read(BIG_BROTHER, 1); 	//  3407968 lines
//		test = r.read(BIG_BROTHER, 2); 	//  2165508 lines
		test = r.read(FLOW, 0); //	chunk1 15172768 lines
//								//	chunk2 21526139 lines
//								//	chunk3  9439406 lines
		test = r.read(FLOW, 2 ); 		// 23258686 lines
//		test = r.read(IPS, 2); 			// 16600932 lines
		
	}
	
	// TODO Here is a nicer way to read CSV files with a opencsv reader
	
	/**
	 * reading inputstream with opencsv reader that returns a array of values
	 * http://opencsv.sourceforge.net/
	 * @param in input stream to read
	 * @throws IOException
	 */
	public void readCSVStream(InputStream in) throws IOException {
		boolean STOP = false;
		CSVReader reader = new CSVReader(new InputStreamReader(in));
		String[] nextLine;
		int i=0;
//		CSVParser csvp = new CSVParser(',');
//		
//		Scanner sc = new Scanner(in);
//		sc.useDelimiter("\\r");
//		String [][] out = new String[ROWS][];
//		for (i=0;i<ROWS;i++){
//			nextLine =csvp.parseLine(sc.next());
//			if (!"0".equals(nextLine[9]) || !"0".equals(nextLine[10]) || !"0".equals(nextLine[18]) )
//				System.out.println(nextLine[9] + "\t"+nextLine[10]+"\t"+nextLine[18]);
//		}
		
		
//		String [][] out = new String[ROWS][];
		
		// for more than 100 000 lines, reader.readNext gives a heap error
		// we have 63 000 000 lines in total, so this doesn't work
		while ((nextLine = reader.readNext()) != null && !STOP) {
			// nextLine[] is an array of values from the line
			// Before passing an array to HealthMessage constructor we hae to take some indexes away
			// Removing element with ArrayUtils.removeElement(array, element)
//			System.out.println(Arrays.toString(nextLine));
//			System.out.println(nextLine.length);
			if (!"0".equals(nextLine[18]) && !"1".equals(nextLine[18]))
				System.out.println("18"+nextLine[18]);
			if (!"0".equals(nextLine[10]) && !"1".equals(nextLine[10]))
				System.out.println("10"+nextLine[10]);
			if (!"0".equals(nextLine[9]) && !"1".equals(nextLine[9]))
				System.out.println("9"+nextLine[9]);
//			
			//			out[i]=nextLine;
			i++;
			if (++i % 1000000 == 0)
				System.out.println(i);
//			if (i>=ROWS)
//				STOP=true;
		}
		reader.close();
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
