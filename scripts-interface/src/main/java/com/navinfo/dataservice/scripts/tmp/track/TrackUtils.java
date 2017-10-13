package com.navinfo.dataservice.scripts.tmp.track;

/**
 * Created by zhangjunfang on 2017/9/30.
 */
public class TrackUtils {
    public static int leapSecondeFrom19800101ToNow = 18;

    /**
     * 根据gps时间计算周秒
     * @param utc
     * @return
     */
    public static double zoneUTCTime2GPSTime(String utc){
        if(utc.length() < 14)
        {
            return -1;
        }

        for (int e=0; e<utc.length(); e++)
        {
            if (utc.charAt(e)<'0' || utc.charAt(e)>'9')
            {
                return -1;
            }
        }

        int year_past=0, month_cur=0,day_past=0;
        int i=0,temp=0;

        year_past=(utc.charAt(0)-'1')*1000+(utc.charAt(1)-'9')*100+(utc.charAt(2)-'8')*10+utc.charAt(3)-'0';
        month_cur=(utc.charAt(4)-'0')*10+utc.charAt(5)-'0';
        day_past=(utc.charAt(6)-'0')*10+utc.charAt(7)-'1';

        for (i=1; i<month_cur; i++)
        {
            if (i==1||i==3||i==5||i==7||i==8||i==10)
            {
                day_past+=31;
            }
            else if(i==2)
            {
                day_past+=28;
            }
            else
            {
                day_past+=30;
            }
        }

        temp=year_past*365+year_past/4+day_past-4;

        if (month_cur<3)
        {
            if (year_past%4==0) {
                temp--;
            }
        }
        double weeksecond = (temp%7)*86400
                + ((utc.charAt(8)-'0')*10+utc.charAt(9)-'0')*3600
                + ((utc.charAt(10)-'0')*10+utc.charAt(11)-'0')*60
                +((utc.charAt(12)-'0')*10+utc.charAt(13)-'0') + leapSecondeFrom19800101ToNow;

        return weeksecond;
    }

    public static void main(String[] args) {
        double weekSeconds = TrackUtils.zoneUTCTime2GPSTime("20170930235959");
        System.out.println(String.valueOf(weekSeconds));
    }
}
