package ir.util.fieldDetection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationPublishNumberDetection {
	
	public final static Pattern p=Pattern.compile("CN[0-9]{7,9}[A|B|C|U|Y|S]?");

	public static boolean isApplicationPublishNumber(String word) {
		Matcher m=p.matcher(word);
		return m.matches();
	}
	
}
