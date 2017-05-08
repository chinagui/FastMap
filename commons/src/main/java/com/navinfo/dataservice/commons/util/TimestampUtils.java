package com.navinfo.dataservice.commons.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class TimestampUtils {
	
	private static Long dayToMiliseconds(int days){
	    Long result = Long.valueOf(days * 24 * 60 * 60 * 1000);
	    return result;
	}

	public static String tsToStr(Timestamp ts){
		
		if(ts == null){
			return null;
		}
		
		DateFormat sdf = new SimpleDateFormat("yyyyMMdd"); 
		
		return sdf.format(ts);
	}
	
	public static long getDiff(Timestamp date1, Timestamp date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}
	
	public static Timestamp addDays(Timestamp ts, int days){
		if(ts==null){
			return ts;
		}
		return new Timestamp(ts.getTime()+ dayToMiliseconds(days));
	}
}
