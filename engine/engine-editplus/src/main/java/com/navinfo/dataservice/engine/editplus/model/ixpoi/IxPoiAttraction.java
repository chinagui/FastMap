package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiAttraction 
* @author code generator
* @date 2016-11-18 11:36:04 
* @Description: TODO
*/
public class IxPoiAttraction extends BasicRow {
	protected long attractionId ;
	protected long poiPid ;
	protected int sightLevel ;
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
	protected int parking ;
	protected int travelguideFlag ;
	
	public IxPoiAttraction (long objPid){
		super(objPid);
	}
	
	public long getAttractionId() {
		return attractionId;
	}
	public void setAttractionId(long attractionId) {
		if(this.checkValue("ATTRACTION_ID",this.attractionId,attractionId)){
			this.attractionId = attractionId;
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
	public int getSightLevel() {
		return sightLevel;
	}
	public void setSightLevel(int sightLevel) {
		if(this.checkValue("SIGHT_LEVEL",this.sightLevel,sightLevel)){
			this.sightLevel = sightLevel;
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
	public String getTicketPrice() {
		return ticketPrice;
	}
	public void setTicketPrice(String ticketPrice) {
		if(this.checkValue("TICKET_PRICE",this.ticketPrice,ticketPrice)){
			this.ticketPrice = ticketPrice;
		}
	}
	public String getTicketPriceEng() {
		return ticketPriceEng;
	}
	public void setTicketPriceEng(String ticketPriceEng) {
		if(this.checkValue("TICKET_PRICE_ENG",this.ticketPriceEng,ticketPriceEng)){
			this.ticketPriceEng = ticketPriceEng;
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
	public int getParking() {
		return parking;
	}
	public void setParking(int parking) {
		if(this.checkValue("PARKING",this.parking,parking)){
			this.parking = parking;
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
		return "IX_POI_ATTRACTION";
	}
}
