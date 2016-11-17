package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiEvent 
* @author code generator
* @date 2016-11-16 06:05:07 
* @Description: TODO
*/
public class IxPoiEvent extends BasicRow {
	protected long eventId ;
	protected String eventName ;
	protected String eventNameEng ;
	protected String eventKind ;
	protected String eventKindEng ;
	protected String eventDesc ;
	protected String eventDescEng ;
	protected String startDate ;
	protected String endDate ;
	protected String detailTime ;
	protected String detailTimeEng ;
	protected String city ;
	protected String poiPid ;
	protected String photoName ;
	protected String reserved ;
	protected String memo ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiEvent (long objPid){
		super(objPid);
	}
	
	public long getEventId() {
		return eventId;
	}
	protected void setEventId(long eventId) {
		this.eventId = eventId;
	}
	public String getEventName() {
		return eventName;
	}
	protected void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getEventNameEng() {
		return eventNameEng;
	}
	protected void setEventNameEng(String eventNameEng) {
		this.eventNameEng = eventNameEng;
	}
	public String getEventKind() {
		return eventKind;
	}
	protected void setEventKind(String eventKind) {
		this.eventKind = eventKind;
	}
	public String getEventKindEng() {
		return eventKindEng;
	}
	protected void setEventKindEng(String eventKindEng) {
		this.eventKindEng = eventKindEng;
	}
	public String getEventDesc() {
		return eventDesc;
	}
	protected void setEventDesc(String eventDesc) {
		this.eventDesc = eventDesc;
	}
	public String getEventDescEng() {
		return eventDescEng;
	}
	protected void setEventDescEng(String eventDescEng) {
		this.eventDescEng = eventDescEng;
	}
	public String getStartDate() {
		return startDate;
	}
	protected void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	protected void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getDetailTime() {
		return detailTime;
	}
	protected void setDetailTime(String detailTime) {
		this.detailTime = detailTime;
	}
	public String getDetailTimeEng() {
		return detailTimeEng;
	}
	protected void setDetailTimeEng(String detailTimeEng) {
		this.detailTimeEng = detailTimeEng;
	}
	public String getCity() {
		return city;
	}
	protected void setCity(String city) {
		this.city = city;
	}
	public String getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(String poiPid) {
		this.poiPid = poiPid;
	}
	public String getPhotoName() {
		return photoName;
	}
	protected void setPhotoName(String photoName) {
		this.photoName = photoName;
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
		return "IX_POI_EVENT";
	}
}
