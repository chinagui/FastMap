package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiRestaurant 
* @author code generator
* @date 2016-11-16 06:04:22 
* @Description: TODO
*/
public class IxPoiRestaurant extends BasicRow {
	protected long restaurantId ;
	protected long poiPid ;
	protected String foodType ;
	protected String creditCard ;
	protected Integer avgCost ;
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
	
	public IxPoiRestaurant (long objPid){
		super(objPid);
	}
	
	public long getRestaurantId() {
		return restaurantId;
	}
	protected void setRestaurantId(long restaurantId) {
		this.restaurantId = restaurantId;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getFoodType() {
		return foodType;
	}
	protected void setFoodType(String foodType) {
		this.foodType = foodType;
	}
	public String getCreditCard() {
		return creditCard;
	}
	protected void setCreditCard(String creditCard) {
		this.creditCard = creditCard;
	}
	public Integer getAvgCost() {
		return avgCost;
	}
	protected void setAvgCost(Integer avgCost) {
		this.avgCost = avgCost;
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
		return "IX_POI_RESTAURANT";
	}
}
