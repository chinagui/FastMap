package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiBusinesstime 
* @author code generator
* @date 2016-11-18 11:33:55 
* @Description: TODO
*/
public class IxPoiBusinesstime extends BasicRow {
	protected long poiPid ;
	protected String monSrt ;
	protected String monEnd ;
	protected String weekInYearSrt ;
	protected String weekInYearEnd ;
	protected String weekInMonthSrt ;
	protected String weekInMonthEnd ;
	protected String validWeek ;
	protected String daySrt ;
	protected String dayEnd ;
	protected String timeSrt ;
	protected String timeDur ;
	protected String reserved ;
	protected String memo ;
	
	public IxPoiBusinesstime (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(long poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}
	public String getMonSrt() {
		return monSrt;
	}
	public void setMonSrt(String monSrt) {
		if(this.checkValue("MON_SRT",this.monSrt,monSrt)){
			this.monSrt = monSrt;
		}
	}
	public String getMonEnd() {
		return monEnd;
	}
	public void setMonEnd(String monEnd) {
		if(this.checkValue("MON_END",this.monEnd,monEnd)){
			this.monEnd = monEnd;
		}
	}
	public String getWeekInYearSrt() {
		return weekInYearSrt;
	}
	public void setWeekInYearSrt(String weekInYearSrt) {
		if(this.checkValue("WEEK_IN_YEAR_SRT",this.weekInYearSrt,weekInYearSrt)){
			this.weekInYearSrt = weekInYearSrt;
		}
	}
	public String getWeekInYearEnd() {
		return weekInYearEnd;
	}
	public void setWeekInYearEnd(String weekInYearEnd) {
		if(this.checkValue("WEEK_IN_YEAR_END",this.weekInYearEnd,weekInYearEnd)){
			this.weekInYearEnd = weekInYearEnd;
		}
	}
	public String getWeekInMonthSrt() {
		return weekInMonthSrt;
	}
	public void setWeekInMonthSrt(String weekInMonthSrt) {
		if(this.checkValue("WEEK_IN_MONTH_SRT",this.weekInMonthSrt,weekInMonthSrt)){
			this.weekInMonthSrt = weekInMonthSrt;
		}
	}
	public String getWeekInMonthEnd() {
		return weekInMonthEnd;
	}
	public void setWeekInMonthEnd(String weekInMonthEnd) {
		if(this.checkValue("WEEK_IN_MONTH_END",this.weekInMonthEnd,weekInMonthEnd)){
			this.weekInMonthEnd = weekInMonthEnd;
		}
	}
	public String getValidWeek() {
		return validWeek;
	}
	public void setValidWeek(String validWeek) {
		if(this.checkValue("VALID_WEEK",this.validWeek,validWeek)){
			this.validWeek = validWeek;
		}
	}
	public String getDaySrt() {
		return daySrt;
	}
	public void setDaySrt(String daySrt) {
		if(this.checkValue("DAY_SRT",this.daySrt,daySrt)){
			this.daySrt = daySrt;
		}
	}
	public String getDayEnd() {
		return dayEnd;
	}
	public void setDayEnd(String dayEnd) {
		if(this.checkValue("DAY_END",this.dayEnd,dayEnd)){
			this.dayEnd = dayEnd;
		}
	}
	public String getTimeSrt() {
		return timeSrt;
	}
	public void setTimeSrt(String timeSrt) {
		if(this.checkValue("TIME_SRT",this.timeSrt,timeSrt)){
			this.timeSrt = timeSrt;
		}
	}
	public String getTimeDur() {
		return timeDur;
	}
	public void setTimeDur(String timeDur) {
		if(this.checkValue("TIME_DUR",this.timeDur,timeDur)){
			this.timeDur = timeDur;
		}
	}
	public String getReserved() {
		return reserved;
	}
	public void setReserved(String reserved) {
		if(this.checkValue("RESERVED",this.reserved,reserved)){
			this.reserved = reserved;
		}
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		if(this.checkValue("MEMO",this.memo,memo)){
			this.memo = memo;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_BUSINESSTIME";
	}
	
	public static final String POI_PID = "POI_PID";
	public static final String MON_SRT = "MON_SRT";
	public static final String MON_END = "MON_END";
	public static final String WEEK_IN_YEAR_SRT = "WEEK_IN_YEAR_SRT";
	public static final String WEEK_IN_YEAR_END = "WEEK_IN_YEAR_END";
	public static final String WEEK_IN_MONTH_SRT = "WEEK_IN_MONTH_SRT";
	public static final String WEEK_IN_MONTH_END = "WEEK_IN_MONTH_END";
	public static final String VALID_WEEK = "VALID_WEEK";
	public static final String DAY_SRT = "DAY_SRT";
	public static final String DAY_END = "DAY_END";
	public static final String TIME_SRT = "TIME_SRT";
	public static final String TIME_DUR = "TIME_DUR";
	public static final String RESERVED = "RESERVED";
	public static final String MEMO = "MEMO";

}
