package ir.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

public final static long YEAR_TIME=365l*24*60*60*1000;
	
	public static String timeBackPush(int yearBackPush) {
		SimpleDateFormat sdf=new SimpleDateFormat("YYYY-MM-dd");
		return sdf.format(new Date(System.currentTimeMillis()-yearBackPush*YEAR_TIME));
	}
}
