package com.navinfo.dataservice.engine.statics.tools;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.navinfo.dataservice.commons.util.DateUtils;

public class StatUtil {

	public static double formatDouble(double d) {

		BigDecimal b = new BigDecimal(d);

		return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	public static String formatDate(Timestamp date) {
		if(date==null){return "";}
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddkkmmss");
		return df.format(date);
	}
	
	public static int daysOfTwo(Date fDate, Date oDate) {
		if(fDate==null||oDate==null){return 0;}
		Calendar aCalendar = Calendar.getInstance();

		aCalendar.setTime(fDate);
		long day1 = aCalendar.getTimeInMillis();

		aCalendar.setTime(oDate);
		long day2 = aCalendar.getTimeInMillis();

		return Integer.parseInt(String.valueOf((day2 - day1)/(24*60*60*1000)));
	}
	public static int daysOfTwo(String startDateStr, String endDateStr) {
		if(startDateStr==null||endDateStr==null){return 0;}
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = DateUtils.stringToDate(startDateStr, "yyyyMMddHHmmss");
			endDate = DateUtils.stringToDate(endDateStr, "yyyyMMddHHmmss");
		} catch (ParseException e) {
			return 0;
		}
		Calendar aCalendar = Calendar.getInstance();

		aCalendar.setTime(startDate);
		long day1 = aCalendar.getTimeInMillis();

		aCalendar.setTime(endDate);
		long day2 = aCalendar.getTimeInMillis();

		return Integer.parseInt(String.valueOf((day2 - day1)/(24*60*60*1000) + 1));
	}

	public static void main(String[] args) {
//		System.out.println(formatDouble(123456789.12345));
	       Date fDate = new GregorianCalendar(2016, 10, 20,23,13,0).getTime();

	       Date oDate = new GregorianCalendar(2016, 10, 19,0,0,0).getTime();

	       System.out.println("使用 daysOfTwo 相差天数 = " + daysOfTwo(fDate, oDate));

	}
}
