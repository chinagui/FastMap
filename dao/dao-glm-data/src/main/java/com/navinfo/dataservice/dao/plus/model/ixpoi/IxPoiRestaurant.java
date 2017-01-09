package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ISerializable;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxPoiRestaurant 
* @author code generator
* @date 2016-11-18 11:36:29 
* @Description: TODO
*/
public class IxPoiRestaurant extends BasicRow implements ISerializable{
	protected long restaurantId ;
	protected long poiPid ;
	protected String foodType ;
	protected String creditCard ;
	protected int avgCost ;
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
	
	public IxPoiRestaurant (long objPid){
		super(objPid);
		setPoiPid(objPid);
	}
	
	public long getRestaurantId() {
		return restaurantId;
	}
	public void setRestaurantId(long restaurantId) {
		if(this.checkValue("RESTAURANT_ID",this.restaurantId,restaurantId)){
			this.restaurantId = restaurantId;
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
	public String getFoodType() {
		return foodType;
	}
	public void setFoodType(String foodType) {
		if(this.checkValue("FOOD_TYPE",this.foodType,foodType)){
			this.foodType = foodType;
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
	public int getAvgCost() {
		return avgCost;
	}
	public void setAvgCost(int avgCost) {
		if(this.checkValue("AVG_COST",this.avgCost,avgCost)){
			this.avgCost = avgCost;
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
		return "IX_POI_RESTAURANT";
	}
	
	public static final String RESTAURANT_ID = "RESTAURANT_ID";
	public static final String POI_PID = "POI_PID";
	public static final String FOOD_TYPE = "FOOD_TYPE";
	public static final String CREDIT_CARD = "CREDIT_CARD";
	public static final String AVG_COST = "AVG_COST";
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
