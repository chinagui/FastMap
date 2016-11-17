package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiAttraction 
* @author code generator
* @date 2016-11-16 06:03:30 
* @Description: TODO
*/
public class IxPoiAttraction extends BasicRow {
	protected long attractionId ;
	protected long poiPid ;
	protected Integer sightLevel ;
	protected String longDescription ;
	protected String longDescripEng ;
	protected String ticketPrice ;
	protected String ticketPriceEng ;
	protected String openHour ;
	protected String openHourEng ;
	protected String telephone ;
	protected String address ;
	protected String city ;
	protected String photoName ;
	protected Integer parking ;
	protected Integer travelguideFlag ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiAttraction (long objPid){
		super(objPid);
	}
	
	public long getAttractionId() {
		return attractionId;
	}
	protected void setAttractionId(long attractionId) {
		this.attractionId = attractionId;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public Integer getSightLevel() {
		return sightLevel;
	}
	protected void setSightLevel(Integer sightLevel) {
		this.sightLevel = sightLevel;
	}
	public String getLongDescription() {
		return longDescription;
	}
	protected void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}
	public String getLongDescripEng() {
		return longDescripEng;
	}
	protected void setLongDescripEng(String longDescripEng) {
		this.longDescripEng = longDescripEng;
	}
	public String getTicketPrice() {
		return ticketPrice;
	}
	protected void setTicketPrice(String ticketPrice) {
		this.ticketPrice = ticketPrice;
	}
	public String getTicketPriceEng() {
		return ticketPriceEng;
	}
	protected void setTicketPriceEng(String ticketPriceEng) {
		this.ticketPriceEng = ticketPriceEng;
	}
	public String getOpenHour() {
		return openHour;
	}
	protected void setOpenHour(String openHour) {
		this.openHour = openHour;
	}
	public String getOpenHourEng() {
		return openHourEng;
	}
	protected void setOpenHourEng(String openHourEng) {
		this.openHourEng = openHourEng;
	}
	public String getTelephone() {
		return telephone;
	}
	protected void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getAddress() {
		return address;
	}
	protected void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	protected void setCity(String city) {
		this.city = city;
	}
	public String getPhotoName() {
		return photoName;
	}
	protected void setPhotoName(String photoName) {
		this.photoName = photoName;
	}
	public Integer getParking() {
		return parking;
	}
	protected void setParking(Integer parking) {
		this.parking = parking;
	}
	public Integer getTravelguideFlag() {
		return travelguideFlag;
	}
	protected void setTravelguideFlag(Integer travelguideFlag) {
		this.travelguideFlag = travelguideFlag;
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
		return "IX_POI_ATTRACTION";
	}
}
