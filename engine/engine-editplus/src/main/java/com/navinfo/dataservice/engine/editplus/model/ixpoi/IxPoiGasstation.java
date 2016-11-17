package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiGasstation 
* @author code generator
* @date 2016-11-16 06:01:42 
* @Description: TODO
*/
public class IxPoiGasstation extends BasicRow {
	protected long gasstationId ;
	protected long poiPid ;
	protected String serviceProv ;
	protected String fuelType ;
	protected String oilType ;
	protected String egType ;
	protected String mgType ;
	protected String payment ;
	protected String service ;
	protected String memo ;
	protected String openHour ;
	protected String photoName ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiGasstation (long objPid){
		super(objPid);
	}
	
	public long getGasstationId() {
		return gasstationId;
	}
	protected void setGasstationId(long gasstationId) {
		this.gasstationId = gasstationId;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getServiceProv() {
		return serviceProv;
	}
	protected void setServiceProv(String serviceProv) {
		this.serviceProv = serviceProv;
	}
	public String getFuelType() {
		return fuelType;
	}
	protected void setFuelType(String fuelType) {
		this.fuelType = fuelType;
	}
	public String getOilType() {
		return oilType;
	}
	protected void setOilType(String oilType) {
		this.oilType = oilType;
	}
	public String getEgType() {
		return egType;
	}
	protected void setEgType(String egType) {
		this.egType = egType;
	}
	public String getMgType() {
		return mgType;
	}
	protected void setMgType(String mgType) {
		this.mgType = mgType;
	}
	public String getPayment() {
		return payment;
	}
	protected void setPayment(String payment) {
		this.payment = payment;
	}
	public String getService() {
		return service;
	}
	protected void setService(String service) {
		this.service = service;
	}
	public String getMemo() {
		return memo;
	}
	protected void setMemo(String memo) {
		this.memo = memo;
	}
	public String getOpenHour() {
		return openHour;
	}
	protected void setOpenHour(String openHour) {
		this.openHour = openHour;
	}
	public String getPhotoName() {
		return photoName;
	}
	protected void setPhotoName(String photoName) {
		this.photoName = photoName;
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
		return "IX_POI_GASSTATION";
	}
}
