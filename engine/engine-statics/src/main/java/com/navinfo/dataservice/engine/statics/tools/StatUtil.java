package com.navinfo.dataservice.engine.statics.tools;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class StatUtil {

	public static double formatDouble(double d) {

		BigDecimal b = new BigDecimal(d);

		return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	
	public static int daysOfTwo(Date fDate, Date oDate) {

	       Calendar aCalendar = Calendar.getInstance();

	       aCalendar.setTime(fDate);
	       int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);

	       aCalendar.setTime(oDate);
	       int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);

	       return day2 - day1;

	    }

	public static void main(String[] args) {
//		System.out.println(formatDouble(123456789.12345));
	       Date fDate = new GregorianCalendar(2016, 10, 20,23,13,0).getTime();

	       Date oDate = new GregorianCalendar(2016, 10, 19,0,0,0).getTime();

	       System.out.println("使用 daysOfTwo 相差天数 = " + daysOfTwo(fDate, oDate));

	}
}
