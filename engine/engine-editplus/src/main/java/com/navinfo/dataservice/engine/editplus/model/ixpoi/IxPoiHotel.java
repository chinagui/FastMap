package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiHotel 
* @author code generator
* @date 2016-11-16 06:03:44 
* @Description: TODO
*/
public class IxPoiHotel extends BasicRow {
	protected long hotelId ;
	protected long poiPid ;
	protected String creditCard ;
	protected Integer rating ;
	protected String checkinTime ;
	protected String checkoutTime ;
	protected Integer roomCount ;
	protected String roomType ;
	protected String roomPrice ;
	protected Integer breakfast ;
	protected String service ;
	protected Integer parking ;
	protected String longDescription ;
	protected String longDescripEng ;
	protected String openHour ;
	protected String openHourEng ;
	protected String telephone ;
	protected String address ;
	protected String city ;
	protected String photoName ;
	protected Integer travelguideFlag ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiHotel (long objPid){
		super(objPid);
	}
	
	public long getHotelId() {
		return hotelId;
	}
	protected void setHotelId(long hotelId) {
		this.hotelId = hotelId;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getCreditCard() {
		return creditCard;
	}
	protected void setCreditCard(String creditCard) {
		this.creditCard = creditCard;
	}
	public Integer getRating() {
		return rating;
	}
	protected void setRating(Integer rating) {
		this.rating = rating;
	}
	public String getCheckinTime() {
		return checkinTime;
	}
	protected void setCheckinTime(String checkinTime) {
		this.checkinTime = checkinTime;
	}
	public String getCheckoutTime() {
		return checkoutTime;
	}
	protected void setCheckoutTime(String checkoutTime) {
		this.checkoutTime = checkoutTime;
	}
	public Integer getRoomCount() {
		return roomCount;
	}
	protected void setRoomCount(Integer roomCount) {
		this.roomCount = roomCount;
	}
	public String getRoomType() {
		return roomType;
	}
	protected void setRoomType(String roomType) {
		this.roomType = roomType;
	}
	public String getRoomPrice() {
		return roomPrice;
	}
	protected void setRoomPrice(String roomPrice) {
		this.roomPrice = roomPrice;
	}
	public Integer getBreakfast() {
		return breakfast;
	}
	protected void setBreakfast(Integer breakfast) {
		this.breakfast = breakfast;
	}
	public String getService() {
		return service;
	}
	protected void setService(String service) {
		this.service = service;
	}
	public Integer getParking() {
		return parking;
	}
	protected void setParking(Integer parking) {
		this.parking = parking;
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
		return "IX_POI_HOTEL";
	}
}
