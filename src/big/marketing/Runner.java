package big.marketing;

import big.marketing.data.Node;

public class Runner {
	
	public static void main(String[] args) {
		
		// THIS IS JUST A TEST TO SEE IF WORKS, DELETE THNX
		System.out.println("\nServer");
		Node test = new Node("172.10.0.2 DC01.BIGMKT1.COM	Domain controller");
		
		System.out.println(test.getAddress());
		System.out.println(test.getHostName());
		System.out.println(test.getComment());
		System.out.println(test.isServer());
		System.out.println(test.getSite());
		
		System.out.println("\nWorkstation");		
		
		Node test2 = new Node("172.20.1.4 WSS2-04.BIGMKT2.COM");
		
		System.out.println(test2.getAddress());
		System.out.println(test2.getHostName());
		System.out.println(test2.isWorkstation());
		System.out.println(test2.getComment());
		System.out.println(test2.getSite());
		
		System.out.println("\nAdministrator");
		
		Node test3 = new Node("172.10.0.40 Administrator.BIGMKT1.COM");
		
		System.out.println(test3.getAddress());
		System.out.println(test3.getHostName());
		System.out.println(test3.isAdministator());
		System.out.println(test3.getComment());
		System.out.println(test3.getSite());
		// TEST END

	}

}
