package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiEvent 
* @author code generator
* @date 2016-11-18 11:36:55 
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
	
	public IxPoiEvent (long objPid){
		super(objPid);
	}
	
	public long getEventId() {
		return eventId;
	}
	public void setEventId(long eventId) {
		if(this.checkValue("EVENT_ID",this.eventId,eventId)){
			this.eventId = eventId;
		}
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		if(this.checkValue("EVENT_NAME",this.eventName,eventName)){
			this.eventName = eventName;
		}
	}
	public String getEventNameEng() {
		return eventNameEng;
	}
	public void setEventNameEng(String eventNameEng) {
		if(this.checkValue("EVENT_NAME_ENG",this.eventNameEng,eventNameEng)){
			this.eventNameEng = eventNameEng;
		}
	}
	public String getEventKind() {
		return eventKind;
	}
	public void setEventKind(String eventKind) {
		if(this.checkValue("EVENT_KIND",this.eventKind,eventKind)){
			this.eventKind = eventKind;
		}
	}
	public String getEventKindEng() {
		return eventKindEng;
	}
	public void setEventKindEng(String eventKindEng) {
		if(this.checkValue("EVENT_KIND_ENG",this.eventKindEng,eventKindEng)){
			this.eventKindEng = eventKindEng;
		}
	}
	public String getEventDesc() {
		return eventDesc;
	}
	public void setEventDesc(String eventDesc) {
		if(this.checkValue("EVENT_DESC",this.eventDesc,eventDesc)){
			this.eventDesc = eventDesc;
		}
	}
	public String getEventDescEng() {
		return eventDescEng;
	}
	public void setEventDescEng(String eventDescEng) {
		if(this.checkValue("EVENT_DESC_ENG",this.eventDescEng,eventDescEng)){
			this.eventDescEng = eventDescEng;
		}
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		if(this.checkValue("START_DATE",this.startDate,startDate)){
			this.startDate = startDate;
		}
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		if(this.checkValue("END_DATE",this.endDate,endDate)){
			this.endDate = endDate;
		}
	}
	public String getDetailTime() {
		return detailTime;
	}
	public void setDetailTime(String detailTime) {
		if(this.checkValue("DETAIL_TIME",this.detailTime,detailTime)){
			this.detailTime = detailTime;
		}
	}
	public String getDetailTimeEng() {
		return detailTimeEng;
	}
	public void setDetailTimeEng(String detailTimeEng) {
		if(this.checkValue("DETAIL_TIME_ENG",this.detailTimeEng,detailTimeEng)){
			this.detailTimeEng = detailTimeEng;
		}
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		if(this.checkValue("CITY",this.city,city)){
			this.city = city;
		}
	}
	public String getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(String poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}
	public String getPhotoName() {
		return photoName;
	}
	public void setPhotoName(String photoName) {
		if(this.checkValue("PHOTO_NAME",this.photoName,photoName)){
			this.photoName = photoName;
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
		return "IX_POI_EVENT";
	}
	
	public static final String EVENT_ID = "EVENT_ID";
	public static final String EVENT_NAME = "EVENT_NAME";
	public static final String EVENT_NAME_ENG = "EVENT_NAME_ENG";
	public static final String EVENT_KIND = "EVENT_KIND";
	public static final String EVENT_KIND_ENG = "EVENT_KIND_ENG";
	public static final String EVENT_DESC = "EVENT_DESC";
	public static final String EVENT_DESC_ENG = "EVENT_DESC_ENG";
	public static final String START_DATE = "START_DATE";
	public static final String END_DATE = "END_DATE";
	public static final String DETAIL_TIME = "DETAIL_TIME";
	public static final String DETAIL_TIME_ENG = "DETAIL_TIME_ENG";
	public static final String CITY = "CITY";
	public static final String POI_PID = "POI_PID";
	public static final String PHOTO_NAME = "PHOTO_NAME";
	public static final String RESERVED = "RESERVED";
	public static final String MEMO = "MEMO";

}
