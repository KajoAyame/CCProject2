

import java.util.regex.Pattern;

public class StringTokenizer {

	public static String[] TokenizeTheScores(String pattern, String inputString) {

		Pattern p = Pattern.compile(pattern);
		String [] splitedString = p.split(inputString);
		return splitedString;
	}	
}