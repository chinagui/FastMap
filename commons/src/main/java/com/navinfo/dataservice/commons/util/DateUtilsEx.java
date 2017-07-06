package com.navinfo.dataservice.commons.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-4-26
 */

public final class DateUtilsEx {
    private final static long TimeOfOneDay = 86400000;

    /**
     * 获得当前的日期Oracle文本串.
     *
     * @return 文本串
     */
    public static String getOracleCurDate() {
        DateFormat df = new java.text.SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        StringBuffer sb = new StringBuffer("TO_DATE('");
        sb.append(df.format(Calendar.getInstance().getTime()));
        sb.append("', 'YYYY/MM/DD-HH24:MI:SS')");
        return sb.toString();
    }

    /**
     * 获得当前的日期
     *
     * @return java.sql.Date
     */
    public final static java.sql.Date getCurDate() {
        return getDate(getCurTime());
    }


    public final static Timestamp getCurTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 把yyyy-MM-dd格式的字符串转换成Timestamp
     *
     * @param dateStr
     * @return Timestamp
     */
    public final static Timestamp getTimeOfDateStr(String dateStr) {
        DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.sql.Timestamp time = null;
        try {
            java.util.Date da = df.parse(dateStr);
            time = new java.sql.Timestamp(da.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
        /*
           * Date date = Date.valueOf(dateStr);
           */
    }

    public final static String formateTimeMillis(long timemin) {
        DateFormat df = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String time = null;
        try {
            time = df.format(new Date(timemin));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
        /*
           * Date date = Date.valueOf(dateStr); 60 * 60 * 1000 - 1);
           */
    }

    /**
     * 把yyyy-MM-dd格式的字符串转换成Timestamp，且该时间是该天的最后时间
     *
     * @param dateStr
     * @return Timestamp
     */
    public final static Timestamp getTimeEndOfDateStr(String dateStr) {
        DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.sql.Timestamp time = null;
        try {
            java.util.Date da = df.parse(dateStr);
            time = new java.sql.Timestamp(
                    (da.getTime() + (24 * 60 * 60 * 1000)) - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
        /*
           * Date date = Date.valueOf(dateStr); 60 * 60 * 1000 - 1);
           */
    }

    /**
     * 把yyyy-MM-dd HH:mm:ss.S格式的字符串转换成Timestamp
     *
     * @param timeStr
     * @return Timestamp
     */
    public final static Timestamp getTimeOfTimeStr(String timeStr) {
        DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        java.sql.Timestamp time = null;
        try {
            java.util.Date da = df.parse(timeStr);
            time = new java.sql.Timestamp(da.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
        /*
           * Timestamp time = Timestamp.valueOf(timeStr);
           */
    }

    /**
     * 把pattern格式的字符串转换成Timestamp
     *
     * @param timeStr
     * @return Timestamp
     */
    public final static Timestamp getTimeOfTimeStr(String timeStr,
                                                   String pattern) {
        DateFormat df = new java.text.SimpleDateFormat(pattern);
        java.sql.Timestamp time = null;
        try {
            java.util.Date da = df.parse(timeStr);
            time = new java.sql.Timestamp(da.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    /**
     * 根据Timestamp获得日期
     *
     * @param time
     * @return java.sql.Date
     */
    public final static java.sql.Date getDate(Timestamp time) {
        return (new Date(time.getTime()));
    }

    /**
     * 获得对应时间time的相应field的值。 如获得当前时间的分钟，则调用方式如下 getTimeFieldStr(getCurTime(),
     * Calendar.MINUTE)
     *
     * @param time
     * @param field
     * @return String
     */
    public final static String getTimeFieldStr(Timestamp time, int field) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new java.util.Date(time.getTime()));
        int fieldValue = calendar.get(field);
        if (field == Calendar.MONTH) {
            fieldValue++;
        }
        return String.valueOf(fieldValue);
    }

    public final static String getTimeStr(Timestamp time, String pattern) {
        SimpleDateFormat df = new java.text.SimpleDateFormat(pattern);
        if (null == time) return "";//modified by dengfasheng 2006-08-01
        return df.format(getDate(time));
    }

    /**
     * 获得时间time对应的中文日期的字符串
     *
     * @param time
     * @return String -- 如 2003年5月12日
     */
    public final static String getDateCn(Timestamp time) {
        String dateCn = getTimeFieldStr(time, Calendar.YEAR) + "年"
                + getTimeFieldStr(time, Calendar.MONTH) + "月"
                + getTimeFieldStr(time, Calendar.DATE) + "日";
        return dateCn;
    }

    /**
     * 日期转换 接收String类型的时间，返回中文时间
     * eg.
     * 将  2005-12-14   转换成   2005年12月14日
     * create by dengfasheng
     *
     * @param NTime 如2003-05-12
     * @return String  如 2003年5月12日
     */
    public final static String getDateCn(String NTime) {
        if (null == NTime || NTime.length() < 10)
            return NTime;
        else
            NTime = NTime.substring(0, 10);//去除时分秒

        String dateCn =
                NTime.substring(0, 4)
                        + "年"
                        + cutStartZero(NTime.substring(5, 7))
                        + "月"
                        + cutStartZero(NTime.substring(8, 10))
                        + "日";

        return dateCn;
    }

    private static String cutStartZero(String s) {
        while (s.startsWith("0")) {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * 获得日期dateStr是星期几，日期格式是“yyyy-MM-dd”
     *
     * @param dateStr
     * @return String -- 如 星期日
     */
    public final static String getDayOfWeekCn(String dateStr) {
        Calendar cal = Calendar.getInstance();
        String weekCn = null;
        Timestamp time = getTimeOfDateStr(dateStr);
        if (time != null) {
            cal.setTime(time);
            int day = cal.get(Calendar.DAY_OF_WEEK);
            switch (day) {
                case 1:
                    weekCn = "星期日";
                    break;
                case 2:
                    weekCn = "星期一";
                    break;
                case 3:
                    weekCn = "星期二";
                    break;
                case 4:
                    weekCn = "星期三";
                    break;
                case 5:
                    weekCn = "星期四";
                    break;
                case 6:
                    weekCn = "星期五";
                    break;
                case 7:
                    weekCn = "星期六";
                    break;
                default:
                    weekCn = "";
                    break;
            }
        }
        return weekCn;
    }

    /**
     * 判断是否是有效日期格式
     *
     * @param yyyy 年
     * @param mm   月
     * @param dd   日
     * @return 是否正确
     */
    public static boolean isValidDate(String yyyy, String mm, String dd) {
        int year = 0;
        int month = 0;
        int day = 0;
        if (yyyy == null || yyyy.trim().length() != 4)
            return false;
        if (!checkNumric(yyyy))
            return false;
        year = Integer.parseInt(yyyy);
        if (mm == null || mm.trim().length() > 2)
            return false;
        if (!checkNumric(mm))
            return false;
        month = Integer.parseInt(mm);
        if (month < 0 || month > 12)
            return false;
        if (dd == null || dd.trim().length() > 2)
            return false;
        if (!checkNumric(dd))
            return false;
        day = Integer.parseInt(dd);
        if (day < 0 || day > 31)
            return false;
        if (month == 2) {
            boolean leap = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0));
            if (day > 29 || (day == 29 && !leap))
                return false;
        }
        return true;
    }

    /**
     * 判断是否是数字
     *
     * @param num
     * @return
     */
    private static boolean checkNumric(String num) {
        for (int i = 0; i < num.length(); i++) {
            switch (num.charAt(i)) {
                case '0':
                    break;
                case '1':
                    break;
                case '2':
                    break;
                case '3':
                    break;
                case '4':
                    break;
                case '5':
                    break;
                case '6':
                    break;
                case '7':
                    break;
                case '8':
                    break;
                case '9':
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    /**
     * @param curTime 数据库当前时间戳
     * @return 当前月的第一天
     */
    public static String getFirstDate(Timestamp curTime) {
        if (curTime == null)
            return null;
        String time = curTime.toString();
        String tmp = "";
        String sYear = time.substring(0, 4);
        String sMonth = time.substring(5, 7);
        String sDay = time.substring(8, 10);
        if (sMonth.length() < 2) {
            sMonth = "0" + sMonth;
        }
        if (sDay.length() < 2) {
            sDay = "0" + sDay;
        }
        tmp = sYear + "-" + sMonth + "-" + "01";
        return tmp;
    }

    /**
     * @param curTime 数据库当前时间戳
     * @return 当前月的最后一天
     */
    public static String getLastDate(Timestamp curTime) {
        if (curTime == null)
            return null;
        String time = curTime.toString();
        String tmp = "";
        String sYear = time.substring(0, 4);
        String sMonth = time.substring(5, 7);
        String sDay = time.substring(8, 10);
        if (sMonth.length() < 2) {
            sMonth = "0" + sMonth;
        }
        if (sDay.length() < 2) {
            sDay = "0" + sDay;
        }
        tmp = sYear + "-" + sMonth + "-" + "01";
        java.util.Date date = null;
        DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            date = df.parse(tmp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        return sYear + "-" + sMonth + "-" + days;
    }


    /**
     * 判断任意两个日期相差的天数
     *
     * @param subtrahend 减数
     * @param minuend    被减数
     * @return
     */
    public static int getDiffPeriod(Timestamp subtrahend, Timestamp minuend) {
        int value = 0;
        /*Calendar sub = Calendar.getInstance();
          Calendar minu = Calendar.getInstance();
          Calendar temp = Calendar.getInstance();
          temp.set(minu.get(Calendar.YEAR),1,1);
          sub.setTime(DateUtil.getDate(subtrahend));
          minu.setTime(DateUtil.getDate(minuend));
          System.out.println(minu.get(Calendar.DAY_OF_YEAR)+"-"+temp.get(Calendar.DAY_OF_YEAR)+"=");
          int i = minu.get(Calendar.DAY_OF_YEAR)-temp.get(Calendar.DAY_OF_YEAR);
          System.out.println(i);
          temp.set(minu.get(Calendar.YEAR),12,31);
          System.out.println(temp.get(Calendar.DAY_OF_YEAR)+"-"+sub.get(Calendar.DAY_OF_YEAR)+"=");
          int j = temp.get(Calendar.DAY_OF_YEAR)-sub.get(Calendar.DAY_OF_YEAR);
          System.out.println(j);
          System.out.println("final");
          System.out.println(i - j);*/
        Calendar minu = Calendar.getInstance();
        Calendar temp = Calendar.getInstance();
        long days = (DateUtilsEx.getDate(minuend).getTime() - DateUtilsEx.getDate(subtrahend).getTime()) / TimeOfOneDay;

        /*
          //判断是否跨年度
          if (minu.get(Calendar.YEAR)-sub.get(Calendar.YEAR) == 1){
              temp.set(minu.get(Calendar.YEAR),1,1);
          }else{
              sub.setTime(DateUtil.getDate(subtrahend));
              minu.setTime(DateUtil.getDate(minuend));
              value = minu.get(Calendar.DAY_OF_YEAR) - sub.get(Calendar.DAY_OF_YEAR);
          }*/
        return new Long(days).intValue();
    }

    /**
     * 根据所给的日期，获得推迟n年的日期
     *
     * @param currDay
     * @return
     */
    public static Timestamp getDayOfDelayYears(Timestamp currDay, int years) {
        return DateUtilsEx.getDayOfDelaySomeday(currDay, years, 0, 0);
    }

    /**
     * 根据所给的日期，获得推迟n个月的日期
     *
     * @param currDay
     * @return
     */
    public static Timestamp getDayOfDelayMonths(Timestamp currDay, int months) {
        return DateUtilsEx.getDayOfDelaySomeday(currDay, 0, months, 0);
    }

    /**
     * 根据所给的日期，获得推迟n天的日期
     *
     * @param currDay
     * @return
     */
    public static Timestamp getDayOfDelayDays(Timestamp currDay, int days) {
        return DateUtilsEx.getDayOfDelaySomeday(currDay, 0, 0, days);
    }

    /**
     * 根据所给的日期，获得推迟某些天
     *
     * @param currDay
     * @param years
     * @param months
     * @param days
     * @return
     */
    public static Timestamp getDayOfDelaySomeday(Timestamp currDay, int years,
                                                 int months, int days) {
        Timestamp resultDay;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtilsEx.getDate(currDay));
        calendar.add(Calendar.YEAR, years);
        calendar.add(Calendar.MONTH, months);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return new Timestamp(calendar.getTime().getTime());
    }

    /**
     * @param str
     * @return
     */
    public static Timestamp toTimestamp(String str)
    //throws ParseException
    {
        try {
            if (str == null || "".equals(str.trim()))
                return null;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
            java.util.Date dd1 = df.parse(str);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dd1);
            java.util.Date dt = cal.getTime();

            long lDtm = dt.getTime();
            return new java.sql.Timestamp(lDtm);
        } catch (ParseException e) {
            System.out.println("e: " + e + " You should pass the String like this:2001-4-5 21:11:11");
            //throw new ParseException("You should pass the String like this:2001-4-5 21:11:11", 1);
        }
        return null;
    }

    /**
     * @param str
     * @return
     */
    public static Timestamp s2Timestamp(String str, DateFormat df)
    //throws ParseException
    {
        try {
            if (str == null || "".equals(str.trim()))
                return null;
            java.util.Date dd1 = df.parse(str);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dd1);
            java.util.Date dt = cal.getTime();

            long lDtm = dt.getTime();
            return new java.sql.Timestamp(lDtm);
        } catch (ParseException e) {
            System.out.println("e: " + e + " You should pass the String like this:2001-4-5 21:11:11");
            //throw new ParseException("You should pass the String like this:2001-4-5 21:11:11", 1);
        }
        return null;
    }

    /**
     * @param str
     * @return
     */
    public static Timestamp s2Timestamp(String str)
    //throws ParseException
    {
        try {
            if (str == null || "".equals(str.trim()))
                return null;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date dd1 = df.parse(str);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dd1);
            java.util.Date dt = cal.getTime();

            long lDtm = dt.getTime();
            return new java.sql.Timestamp(lDtm);
        } catch (ParseException e) {
            System.out.println("e: " + e + " You should pass the String like this:2001-4-5 21:11:11");
            //throw new ParseException("You should pass the String like this:2001-4-5 21:11:11", 1);
        }
        return null;
    }

    /**
     * 返回自1970年1月1日0时起的毫秒数
     * @return
     */
    public static long getCurrentTimeMillis(){
    	return System.currentTimeMillis();
    }
    
    
    public static void main(String[] arg) {
    	System.out.println(getCurTime());
    	System.out.println(DateUtilsEx.getDayOfDelayMonths(DateUtilsEx.getTimeOfDateStr("2017-03-30"), -1));
       // System.out.println(DateUtilsEx.getDiffPeriod(DateUtilsEx.getTimeOfDateStr("2004-11-02"), DateUtilsEx.getTimeOfDateStr("2004-12-31")));
        //System.out.println(DateUtil.getDayOfDelaySomeday(DateUtil.getTimeOfDateStr("2004-07-01"),1,0,0));
	}
    
    public final static String getTimeStr(Date date) {
        DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        String time = null;
        try {
        	if(date!=null)
        	time = df.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }
    
    public final static String getTimeStr(Date date,String format) {
        DateFormat df = new java.text.SimpleDateFormat(format);
        String time = null;
        try {
        	if(date!=null)
        	time = df.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }
}