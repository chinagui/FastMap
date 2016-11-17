package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiChargingstation 
* @author code generator
* @date 2016-11-16 06:02:01 
* @Description: TODO
*/
public class IxPoiChargingstation extends BasicRow {
	protected long chargingId ;
	protected long poiPid ;
	protected Integer chargingType ;
	protected String changeBrands ;
	protected String changeOpenType ;
	protected Integer chargingNum ;
	protected String serviceProv ;
	protected String memo ;
	protected String photoName ;
	protected String openHour ;
	protected Integer parkingFees ;
	protected String parkingInfo ;
	protected Integer availableState ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiChargingstation (long objPid){
		super(objPid);
	}
	
	public long getChargingId() {
		return chargingId;
	}
	protected void setChargingId(long chargingId) {
		this.chargingId = chargingId;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public Integer getChargingType() {
		return chargingType;
	}
	protected void setChargingType(Integer chargingType) {
		this.chargingType = chargingType;
	}
	public String getChangeBrands() {
		return changeBrands;
	}
	protected void setChangeBrands(String changeBrands) {
		this.changeBrands = changeBrands;
	}
	public String getChangeOpenType() {
		return changeOpenType;
	}
	protected void setChangeOpenType(String changeOpenType) {
		this.changeOpenType = changeOpenType;
	}
	public Integer getChargingNum() {
		return chargingNum;
	}
	protected void setChargingNum(Integer chargingNum) {
		this.chargingNum = chargingNum;
	}
	public String getServiceProv() {
		return serviceProv;
	}
	protected void setServiceProv(String serviceProv) {
		this.serviceProv = serviceProv;
	}
	public String getMemo() {
		return memo;
	}
	protected void setMemo(String memo) {
		this.memo = memo;
	}
	public String getPhotoName() {
		return photoName;
	}
	protected void setPhotoName(String photoName) {
		this.photoName = photoName;
	}
	public String getOpenHour() {
		return openHour;
	}
	protected void setOpenHour(String openHour) {
		this.openHour = openHour;
	}
	public Integer getParkingFees() {
		return parkingFees;
	}
	protected void setParkingFees(Integer parkingFees) {
		this.parkingFees = parkingFees;
	}
	public String getParkingInfo() {
		return parkingInfo;
	}
	protected void setParkingInfo(String parkingInfo) {
		this.parkingInfo = parkingInfo;
	}
	public Integer getAvailableState() {
		return availableState;
	}
	protected void setAvailableState(Integer availableState) {
		this.availableState = availableState;
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
		return "IX_POI_CHARGINGSTATION";
	}
}
