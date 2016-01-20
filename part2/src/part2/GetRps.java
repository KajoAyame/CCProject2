package part2;
/* Project 0
 * This source code is created by Xinghao Zhou independently.
 * Andrew id: xinghaoz
 * Name: Xinghao Zhou
 * Email: xinghaoz@andrew.cmu.edu
 * 
*/

import java.net.*;
import java.io.*;

public class GetRps {

	public static float readURL(String url, String DCDNS) throws Exception {
		
		URL oracle = new URL(url);
		BufferedReader br = new BufferedReader(new InputStreamReader(oracle.openStream()));
		float currentRps = 0;
		
		String inputLine;
		while ((inputLine = br.readLine()) != null) {
			String[] line = StringTokenizer.TokenizeTheScores("=", inputLine);
			if (line[0].equals(DCDNS)) {
				currentRps = Float.parseFloat(line[1]);
				return currentRps;
			}
		}
		
		br.close();
		return 0;
	}
}