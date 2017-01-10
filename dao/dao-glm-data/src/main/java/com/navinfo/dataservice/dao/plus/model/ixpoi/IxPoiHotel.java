package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ISerializable;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxPoiHotel 
* @author code generator
* @date 2016-11-18 11:36:16 
* @Description: TODO
*/
public class IxPoiHotel extends BasicRow implements ISerializable{
	protected long hotelId ;
	protected long poiPid ;
	protected String creditCard ;
	protected int rating ;
	protected String checkinTime ;
	protected String checkoutTime ;
	protected int roomCount ;
	protected String roomType ;
	protected String roomPrice ;
	protected int breakfast ;
	protected String service ;
	protected int parking ;
	protected String longDescription ;
	protected String longDescripEng ;
	protected String openHour ;
	protected String openHourEng ;
	protected String telephone ;
	protected String address ;
	protected String city ;
	protected String photoName ;
	protected int travelguideFlag ;
	
	public IxPoiHotel (long objPid){
		super(objPid);
		setPoiPid(objPid);
	}
	
	public long getHotelId() {
		return hotelId;
	}
	public void setHotelId(long hotelId) {
		if(this.checkValue("HOTEL_ID",this.hotelId,hotelId)){
			this.hotelId = hotelId;
		}
	}
	public long getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(long poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}
	public String getCreditCard() {
		return creditCard;
	}
	public void setCreditCard(String creditCard) {
		if(this.checkValue("CREDIT_CARD",this.creditCard,creditCard)){
			this.creditCard = creditCard;
		}
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		if(this.checkValue("RATING",this.rating,rating)){
			this.rating = rating;
		}
	}
	public String getCheckinTime() {
		return checkinTime;
	}
	public void setCheckinTime(String checkinTime) {
		if(this.checkValue("CHECKIN_TIME",this.checkinTime,checkinTime)){
			this.checkinTime = checkinTime;
		}
	}
	public String getCheckoutTime() {
		return checkoutTime;
	}
	public void setCheckoutTime(String checkoutTime) {
		if(this.checkValue("CHECKOUT_TIME",this.checkoutTime,checkoutTime)){
			this.checkoutTime = checkoutTime;
		}
	}
	public int getRoomCount() {
		return roomCount;
	}
	public void setRoomCount(int roomCount) {
		if(this.checkValue("ROOM_COUNT",this.roomCount,roomCount)){
			this.roomCount = roomCount;
		}
	}
	public String getRoomType() {
		return roomType;
	}
	public void setRoomType(String roomType) {
		if(this.checkValue("ROOM_TYPE",this.roomType,roomType)){
			this.roomType = roomType;
		}
	}
	public String getRoomPrice() {
		return roomPrice;
	}
	public void setRoomPrice(String roomPrice) {
		if(this.checkValue("ROOM_PRICE",this.roomPrice,roomPrice)){
			this.roomPrice = roomPrice;
		}
	}
	public int getBreakfast() {
		return breakfast;
	}
	public void setBreakfast(int breakfast) {
		if(this.checkValue("BREAKFAST",this.breakfast,breakfast)){
			this.breakfast = breakfast;
		}
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		if(this.checkValue("SERVICE",this.service,service)){
			this.service = service;
		}
	}
	public int getParking() {
		return parking;
	}
	public void setParking(int parking) {
		if(this.checkValue("PARKING",this.parking,parking)){
			this.parking = parking;
		}
	}
	public String getLongDescription() {
		return longDescription;
	}
	public void setLongDescription(String longDescription) {
		if(this.checkValue("LONG_DESCRIPTION",this.longDescription,longDescription)){
			this.longDescription = longDescription;
		}
	}
	public String getLongDescripEng() {
		return longDescripEng;
	}
	public void setLongDescripEng(String longDescripEng) {
		if(this.checkValue("LONG_DESCRIP_ENG",this.longDescripEng,longDescripEng)){
			this.longDescripEng = longDescripEng;
		}
	}
	public String getOpenHour() {
		return openHour;
	}
	public void setOpenHour(String openHour) {
		if(this.checkValue("OPEN_HOUR",this.openHour,openHour)){
			this.openHour = openHour;
		}
	}
	public String getOpenHourEng() {
		return openHourEng;
	}
	public void setOpenHourEng(String openHourEng) {
		if(this.checkValue("OPEN_HOUR_ENG",this.openHourEng,openHourEng)){
			this.openHourEng = openHourEng;
		}
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		if(this.checkValue("TELEPHONE",this.telephone,telephone)){
			this.telephone = telephone;
		}
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		if(this.checkValue("ADDRESS",this.address,address)){
			this.address = address;
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
	public String getPhotoName() {
		return photoName;
	}
	public void setPhotoName(String photoName) {
		if(this.checkValue("PHOTO_NAME",this.photoName,photoName)){
			this.photoName = photoName;
		}
	}
	public int getTravelguideFlag() {
		return travelguideFlag;
	}
	public void setTravelguideFlag(int travelguideFlag) {
		if(this.checkValue("TRAVELGUIDE_FLAG",this.travelguideFlag,travelguideFlag)){
			this.travelguideFlag = travelguideFlag;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_HOTEL";
	}
	
	public static final String HOTEL_ID = "HOTEL_ID";
	public static final String POI_PID = "POI_PID";
	public static final String CREDIT_CARD = "CREDIT_CARD";
	public static final String RATING = "RATING";
	public static final String CHECKIN_TIME = "CHECKIN_TIME";
	public static final String CHECKOUT_TIME = "CHECKOUT_TIME";
	public static final String ROOM_COUNT = "ROOM_COUNT";
	public static final String ROOM_TYPE = "ROOM_TYPE";
	public static final String ROOM_PRICE = "ROOM_PRICE";
	public static final String BREAKFAST = "BREAKFAST";
	public static final String SERVICE = "SERVICE";
	public static final String PARKING = "PARKING";
	public static final String LONG_DESCRIPTION = "LONG_DESCRIPTION";
	public static final String LONG_DESCRIP_ENG = "LONG_DESCRIP_ENG";
	public static final String OPEN_HOUR = "OPEN_HOUR";
	public static final String OPEN_HOUR_ENG = "OPEN_HOUR_ENG";
	public static final String TELEPHONE = "TELEPHONE";
	public static final String ADDRESS = "ADDRESS";
	public static final String CITY = "CITY";
	public static final String PHOTO_NAME = "PHOTO_NAME";
	public static final String TRAVELGUIDE_FLAG = "TRAVELGUIDE_FLAG";

	//*********zl 2017.01.05 ***********
	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}
