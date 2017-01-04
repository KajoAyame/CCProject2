/* Project 2.1
 * This source code is created by Xinghao Zhou independently.
 * Andrew id: xinghaoz
 * Name: Xinghao Zhou
 * Email: xinghaoz@andrew.cmu.edu
 * 
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class URLConnectionReader {

	// Get the web page by the input argument.
	public static void connectURL(String url) throws Exception {
		URL newurl = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) newurl.openConnection();
		connection.getContent();
		
	}
	
	// Get the log page return by starting the test.
	public static String getLog(String url) throws Exception {
		URL newurl = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) newurl.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		String line[];
		inputLine = br.readLine();
		line = StringTokenizer.TokenizeTheScores("'", inputLine);
		String logUrlpart = new String(line[1]); 
		br.close();
		System.out.println(logUrlpart);
		return logUrlpart;
	}
	
}
