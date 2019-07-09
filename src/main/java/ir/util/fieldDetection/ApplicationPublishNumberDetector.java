package ir.util.fieldDetection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 * 专利文献号的检测
 * 
 * 用于检测出全域查询中的专利号
 * 
 * @author 余定邦
 *
 */
@Service
public class ApplicationPublishNumberDetector {
	
	public final static Pattern p=Pattern.compile("CN[0-9]{7,9}[A|B|C|U|Y|S]?");

	public boolean isApplicationPublishNumber(String word) {
		Matcher m=p.matcher(word);
		return m.matches();
	}
	
}
