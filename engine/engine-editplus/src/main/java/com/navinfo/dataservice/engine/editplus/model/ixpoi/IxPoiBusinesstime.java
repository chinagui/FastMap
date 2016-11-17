package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiBusinesstime 
* @author code generator
* @date 2016-11-16 06:00:15 
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
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiBusinesstime (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getMonSrt() {
		return monSrt;
	}
	protected void setMonSrt(String monSrt) {
		this.monSrt = monSrt;
	}
	public String getMonEnd() {
		return monEnd;
	}
	protected void setMonEnd(String monEnd) {
		this.monEnd = monEnd;
	}
	public String getWeekInYearSrt() {
		return weekInYearSrt;
	}
	protected void setWeekInYearSrt(String weekInYearSrt) {
		this.weekInYearSrt = weekInYearSrt;
	}
	public String getWeekInYearEnd() {
		return weekInYearEnd;
	}
	protected void setWeekInYearEnd(String weekInYearEnd) {
		this.weekInYearEnd = weekInYearEnd;
	}
	public String getWeekInMonthSrt() {
		return weekInMonthSrt;
	}
	protected void setWeekInMonthSrt(String weekInMonthSrt) {
		this.weekInMonthSrt = weekInMonthSrt;
	}
	public String getWeekInMonthEnd() {
		return weekInMonthEnd;
	}
	protected void setWeekInMonthEnd(String weekInMonthEnd) {
		this.weekInMonthEnd = weekInMonthEnd;
	}
	public String getValidWeek() {
		return validWeek;
	}
	protected void setValidWeek(String validWeek) {
		this.validWeek = validWeek;
	}
	public String getDaySrt() {
		return daySrt;
	}
	protected void setDaySrt(String daySrt) {
		this.daySrt = daySrt;
	}
	public String getDayEnd() {
		return dayEnd;
	}
	protected void setDayEnd(String dayEnd) {
		this.dayEnd = dayEnd;
	}
	public String getTimeSrt() {
		return timeSrt;
	}
	protected void setTimeSrt(String timeSrt) {
		this.timeSrt = timeSrt;
	}
	public String getTimeDur() {
		return timeDur;
	}
	protected void setTimeDur(String timeDur) {
		this.timeDur = timeDur;
	}
	public String getReserved() {
		return reserved;
	}
	protected void setReserved(String reserved) {
		this.reserved = reserved;
	}
	public String getMemo() {
		return memo;
	}
	protected void setMemo(String memo) {
		this.memo = memo;
	}
//	public Integer getURecord() {
//		return uRecord;
//	}
//	protected void setURecord(Integer uRecord) {
//		this.uRecord = uRecord;
//	}
//	public String getUFields() {
//		return uFields;
//	}
//	protected void setUFields(String uFields) {
//		this.uFields = uFields;
//	}
//	public String getUDate() {
//		return uDate;
//	}
//	protected void setUDate(String uDate) {
//		this.uDate = uDate;
//	}
	
	@Override
	public String tableName() {
		return "IX_POI_BUSINESSTIME";
	}
}
