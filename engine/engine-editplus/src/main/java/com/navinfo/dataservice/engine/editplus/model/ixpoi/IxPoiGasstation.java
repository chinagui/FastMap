package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiGasstation 
* @author code generator
* @date 2016-11-18 11:34:52 
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
	
	public IxPoiGasstation (long objPid){
		super(objPid);
	}
	
	public long getGasstationId() {
		return gasstationId;
	}
	public void setGasstationId(long gasstationId) {
		if(this.checkValue("GASSTATION_ID",this.gasstationId,gasstationId)){
			this.gasstationId = gasstationId;
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
	public String getServiceProv() {
		return serviceProv;
	}
	public void setServiceProv(String serviceProv) {
		if(this.checkValue("SERVICE_PROV",this.serviceProv,serviceProv)){
			this.serviceProv = serviceProv;
		}
	}
	public String getFuelType() {
		return fuelType;
	}
	public void setFuelType(String fuelType) {
		if(this.checkValue("FUEL_TYPE",this.fuelType,fuelType)){
			this.fuelType = fuelType;
		}
	}
	public String getOilType() {
		return oilType;
	}
	public void setOilType(String oilType) {
		if(this.checkValue("OIL_TYPE",this.oilType,oilType)){
			this.oilType = oilType;
		}
	}
	public String getEgType() {
		return egType;
	}
	public void setEgType(String egType) {
		if(this.checkValue("EG_TYPE",this.egType,egType)){
			this.egType = egType;
		}
	}
	public String getMgType() {
		return mgType;
	}
	public void setMgType(String mgType) {
		if(this.checkValue("MG_TYPE",this.mgType,mgType)){
			this.mgType = mgType;
		}
	}
	public String getPayment() {
		return payment;
	}
	public void setPayment(String payment) {
		if(this.checkValue("PAYMENT",this.payment,payment)){
			this.payment = payment;
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
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		if(this.checkValue("MEMO",this.memo,memo)){
			this.memo = memo;
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
	public String getPhotoName() {
		return photoName;
	}
	public void setPhotoName(String photoName) {
		if(this.checkValue("PHOTO_NAME",this.photoName,photoName)){
			this.photoName = photoName;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_GASSTATION";
	}
}
