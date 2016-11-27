package com.navinfo.dataservice.dao.plus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxPoiChargingstation 
* @author code generator
* @date 2016-11-18 11:35:05 
* @Description: TODO
*/
public class IxPoiChargingstation extends BasicRow {
	protected long chargingId ;
	protected long poiPid ;
	protected int chargingType ;
	protected String changeBrands ;
	protected String changeOpenType ;
	protected int chargingNum ;
	protected String serviceProv ;
	protected String memo ;
	protected String photoName ;
	protected String openHour ;
	protected int parkingFees ;
	protected String parkingInfo ;
	protected int availableState ;
	
	public IxPoiChargingstation (long objPid){
		super(objPid);
	}
	
	public long getChargingId() {
		return chargingId;
	}
	public void setChargingId(long chargingId) {
		if(this.checkValue("CHARGING_ID",this.chargingId,chargingId)){
			this.chargingId = chargingId;
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
	public int getChargingType() {
		return chargingType;
	}
	public void setChargingType(int chargingType) {
		if(this.checkValue("CHARGING_TYPE",this.chargingType,chargingType)){
			this.chargingType = chargingType;
		}
	}
	public String getChangeBrands() {
		return changeBrands;
	}
	public void setChangeBrands(String changeBrands) {
		if(this.checkValue("CHANGE_BRANDS",this.changeBrands,changeBrands)){
			this.changeBrands = changeBrands;
		}
	}
	public String getChangeOpenType() {
		return changeOpenType;
	}
	public void setChangeOpenType(String changeOpenType) {
		if(this.checkValue("CHANGE_OPEN_TYPE",this.changeOpenType,changeOpenType)){
			this.changeOpenType = changeOpenType;
		}
	}
	public int getChargingNum() {
		return chargingNum;
	}
	public void setChargingNum(int chargingNum) {
		if(this.checkValue("CHARGING_NUM",this.chargingNum,chargingNum)){
			this.chargingNum = chargingNum;
		}
	}
	public String getServiceProv() {
		return serviceProv;
	}
	public void setServiceProv(String serviceProv) {
		if(this.checkValue("SERVICE_PROV",this.serviceProv,serviceProv)){
			this.serviceProv = serviceProv;
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
	public String getPhotoName() {
		return photoName;
	}
	public void setPhotoName(String photoName) {
		if(this.checkValue("PHOTO_NAME",this.photoName,photoName)){
			this.photoName = photoName;
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
	public int getParkingFees() {
		return parkingFees;
	}
	public void setParkingFees(int parkingFees) {
		if(this.checkValue("PARKING_FEES",this.parkingFees,parkingFees)){
			this.parkingFees = parkingFees;
		}
	}
	public String getParkingInfo() {
		return parkingInfo;
	}
	public void setParkingInfo(String parkingInfo) {
		if(this.checkValue("PARKING_INFO",this.parkingInfo,parkingInfo)){
			this.parkingInfo = parkingInfo;
		}
	}
	public int getAvailableState() {
		return availableState;
	}
	public void setAvailableState(int availableState) {
		if(this.checkValue("AVAILABLE_STATE",this.availableState,availableState)){
			this.availableState = availableState;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_CHARGINGSTATION";
	}
}
