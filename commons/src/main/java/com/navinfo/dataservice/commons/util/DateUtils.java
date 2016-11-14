package com.navinfo.dataservice.commons.util;

import org.apache.commons.lang.StringUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	public static String DATE_DEFAULT_FORMAT ="yyyy-MM-dd HH:mm:ss";
	public static String DATE_COMPACTED_FORMAT ="yyyyMMddHHmmss";
	public static String DATE_YMD="yyyyMMdd";
	private static SimpleDateFormat compactedSdf=new SimpleDateFormat(DATE_COMPACTED_FORMAT);
	private static SimpleDateFormat defaultSdf=new SimpleDateFormat(DATE_DEFAULT_FORMAT);
	private static SimpleDateFormat ymdSdf = new SimpleDateFormat(DATE_YMD);

	/**
	 * 
	 * @param date
	 * @return default format:"yyyy-MM-dd HH:mm:ss";
	 *  if date == null then return null;
	 */
	public static String dateToString(Date date) {
		if(date!=null){
			return defaultSdf.format(date);
		}
		return null;
	}
	// date类型转换为String类型
	// formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒等等
	// date Date类型的时间
	public static String dateToString(Date date, String formatType) {
		if(StringUtils.isEmpty(formatType)||DATE_DEFAULT_FORMAT.equals(formatType)){
			return defaultSdf.format(date);
		}else if(DATE_COMPACTED_FORMAT.equals(formatType)){
			return compactedSdf.format(date);
		}else if(DATE_YMD.equals(formatType)){
			return ymdSdf.format(date);
		}
		else{
			return new SimpleDateFormat(formatType).format(date);
		}
	}

	// long类型转换为String类型
	// currentTime要转换的long类型的时间
	// formatType要转换的string类型的时间格式
	public static String longToString(long currentTime, String formatType)
			throws ParseException {
		Date date = longToDate(currentTime, formatType); // long类型转成Date类型
		String strTime = dateToString(date, formatType); // date类型转成String
		return strTime;
	}

	// string类型转换为date类型
	// strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
	// HH时mm分ss秒，
	// strTime的时间格式必须要与formatType的时间格式相同
	public static Date stringToDate(String strTime, String formatType)
			throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(formatType);
		Date date = null;
		date = formatter.parse(strTime);
		return date;
	}

	// long转换为Date类型
	// currentTime要转换的long类型的时间
	// formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
	public static Date longToDate(long currentTime, String formatType)
			throws ParseException {
		Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
		String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
		Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
		return date;
	}

	// string类型转换为long类型
	// strTime要转换的String类型的时间
	// formatType时间格式
	// strTime的时间格式和formatType的时间格式必须相同
	public static long stringToLong(String strTime, String formatType)
			throws ParseException {
		Date date = stringToDate(strTime, formatType); // String类型转成date类型
		if (date == null) {
			return 0;
		} else {
			long currentTime = dateToLong(date); // date类型转成long类型
			return currentTime;
		}
	}

	// date类型转换为long类型
	// date要转换的date类型的时间
	public static long dateToLong(Date date) {
		return date.getTime();
	}

	public static String numToDate(int number, String formatType) {
		Date date = new Date(number);
		SimpleDateFormat sdf = new SimpleDateFormat(formatType);
		return sdf.format(date);
	}
	
	public static String getCurYmd(){
		return dateToString(new Date(),DateUtils.DATE_YMD);
	}
	
	 public static void main(String[] args) throws ParseException, java.text.ParseException {
	        //String s = du.numToDate(1350144260, "yyyy-MM-dd hh:mm:ss");
//	        long time = DateUtils.stringToLong("20160302154457", "yyyyMMddhhmmss");
//	        long time1 = DateUtils.stringToLong("2012-10-15 20:44:53", "yyyy-MM-dd hh:mm:ss")/1000;
//	        String date = DateUtils.longToString(1350470693,"yyyy-MM-dd hh:mm:ss" );
//	        System.out.println(time);
//	        System.out.println(time1);
//	        System.out.println(date);
		 System.out.println(DateUtils.dateToString(new Date(),DateUtils.DATE_YMD));
	 }
}
