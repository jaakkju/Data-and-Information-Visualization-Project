package big.marketing;

import big.marketing.data.node;

public class Runner {
	
	private static final String FILE_DESCRIPTION 	= "2013MC3AnswerSheetandDataDescriptions.zip";
	private static final String FILE_SUPLEMENTARY 	= "Supplementary Data Descriptions for Week 2.zip";
	private static final String FILE_BIGBROTHER 		= "VAST2013MC3_BigBrother.zip";
	private static final String FILE_NETWORKFLOW 	= "VAST2013MC3_NetworkFlow.zip";
	private static final String FILE_WEEK2DATA 		= "week2data.zip";

	public static void main(String[] args) {

		
		// THIS IS JUST A TEST TO SEE IF WORKS, DELETE THNX
		System.out.println("\nServer");
		node test = new node("172.10.0.2 DC01.BIGMKT1.COM	Domain controller");
		
		System.out.println(test.getAddress());
		System.out.println(test.getHostName());
		System.out.println(test.getComment());
		System.out.println(test.isServer());
		
		System.out.println("\nWorkstation");		
		
		node test2 = new node("172.20.1.4 WSS2-04.BIGMKT2.COM");
		
		System.out.println(test2.getAddress());
		System.out.println(test2.getHostName());
		System.out.println(test2.isWorkstation());
		System.out.println(test2.getComment());
		
		System.out.println("\nAdministrator");
		
		node test3 = new node("172.10.0.40 Administrator.BIGMKT1.COM");
		
		System.out.println(test3.getAddress());
		System.out.println(test3.getHostName());
		System.out.println(test3.isAdministator());
		System.out.println(test3.getComment());
		// TEST END

	}

}
