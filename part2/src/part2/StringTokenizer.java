package part2;
/* Project 2.1
 * This source code is created by Xinghao Zhou independently.
 * Andrew id: xinghaoz
 * Name: Xinghao Zhou
 * Email: xinghaoz@andrew.cmu.edu
 * 
*/

import java.util.regex.Pattern;

public class StringTokenizer {

	public static String[] TokenizeTheScores(String pattern, String inputString) {

		Pattern p = Pattern.compile(pattern);
		String [] splitedString = p.split(inputString);
		return splitedString;
	}	
}