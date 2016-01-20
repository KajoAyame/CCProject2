package part2;

/* Project 0
 * This source code is created by Xinghao Zhou independently.
 * Andrew id: xinghaoz
 * Name: Xinghao Zhou
 * Email: xinghaoz@andrew.cmu.edu
 * 
*/

public class Adding {

	
	public static void addInstance(String LGDNS, String DCDNS) {
		
		String url = new String("http://"
								+ LGDNS
								+ "/test/horizontal/add?dns="
								+ DCDNS);
			
		try {
			URLConnectionReader.connectURL(url);
			System.out.println("Adding new data center to load generator");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
