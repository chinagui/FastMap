package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ISerializable;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxPoiGasstation 
* @author code generator
* @date 2016-11-18 11:34:52 
* @Description: TODO
*/
public class IxPoiGasstation extends BasicRow implements ISerializable{
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
		setPoiPid(objPid);
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
	
	public static final String GASSTATION_ID = "GASSTATION_ID";
	public static final String POI_PID = "POI_PID";
	public static final String SERVICE_PROV = "SERVICE_PROV";
	public static final String FUEL_TYPE = "FUEL_TYPE";
	public static final String OIL_TYPE = "OIL_TYPE";
	public static final String EG_TYPE = "EG_TYPE";
	public static final String MG_TYPE = "MG_TYPE";
	public static final String PAYMENT = "PAYMENT";
	public static final String SERVICE = "SERVICE";
	public static final String MEMO = "MEMO";
	public static final String OPEN_HOUR = "OPEN_HOUR";
	public static final String PHOTO_NAME = "PHOTO_NAME";

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
