package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ISerializable;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxPoiChargingplot 
* @author code generator
* @date 2016-11-18 11:35:20 
* @Description: TODO
*/
public class IxPoiChargingplot extends BasicRow implements ISerializable{
	protected long poiPid ;
	protected int groupId = 1;
	protected int count =1;
	protected int acdc ;
	protected String plugType = "9";
	protected String power ;
	protected String voltage ;
	protected String current ;
	protected int mode ;
	protected String memo ;
	protected int plugNum  =1;
	protected String prices ;
	protected String openType = "1";
	protected int availableState ;
	protected String manufacturer ;
	protected String factoryNum ;
	protected String plotNum ;
	protected String productNum ;
	protected String parkingNum ;
	protected int floor =1;
	protected int locationType ;
	protected String payment ="4";
	
	public IxPoiChargingplot (long objPid){
		super(objPid);
		setPoiPid(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(long poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		if(this.checkValue("GROUP_ID",this.groupId,groupId)){
			this.groupId = groupId;
		}
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		if(this.checkValue("COUNT",this.count,count)){
			this.count = count;
		}
	}
	public int getAcdc() {
		return acdc;
	}
	public void setAcdc(int acdc) {
		if(this.checkValue("ACDC",this.acdc,acdc)){
			this.acdc = acdc;
		}
	}
	public String getPlugType() {
		return plugType;
	}
	public void setPlugType(String plugType) {
		if(this.checkValue("PLUG_TYPE",this.plugType,plugType)){
			this.plugType = plugType;
		}
	}
	public String getPower() {
		return power;
	}
	public void setPower(String power) {
		if(this.checkValue("POWER",this.power,power)){
			this.power = power;
		}
	}
	public String getVoltage() {
		return voltage;
	}
	public void setVoltage(String voltage) {
		if(this.checkValue("VOLTAGE",this.voltage,voltage)){
			this.voltage = voltage;
		}
	}
	public String getCurrent() {
		return current;
	}
	public void setCurrent(String current) {
		if(this.checkValue("CURRENT",this.current,current)){
			this.current = current;
		}
	}
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		if(this.checkValue("MODE",this.mode,mode)){
			this.mode = mode;
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
	public int getPlugNum() {
		return plugNum;
	}
	public void setPlugNum(int plugNum) {
		if(this.checkValue("PLUG_NUM",this.plugNum,plugNum)){
			this.plugNum = plugNum;
		}
	}
	public String getPrices() {
		return prices;
	}
	public void setPrices(String prices) {
		if(this.checkValue("PRICES",this.prices,prices)){
			this.prices = prices;
		}
	}
	public String getOpenType() {
		return openType;
	}
	public void setOpenType(String openType) {
		if(this.checkValue("OPEN_TYPE",this.openType,openType)){
			this.openType = openType;
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
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		if(this.checkValue("MANUFACTURER",this.manufacturer,manufacturer)){
			this.manufacturer = manufacturer;
		}
	}
	public String getFactoryNum() {
		return factoryNum;
	}
	public void setFactoryNum(String factoryNum) {
		if(this.checkValue("FACTORY_NUM",this.factoryNum,factoryNum)){
			this.factoryNum = factoryNum;
		}
	}
	public String getPlotNum() {
		return plotNum;
	}
	public void setPlotNum(String plotNum) {
		if(this.checkValue("PLOT_NUM",this.plotNum,plotNum)){
			this.plotNum = plotNum;
		}
	}
	public String getProductNum() {
		return productNum;
	}
	public void setProductNum(String productNum) {
		if(this.checkValue("PRODUCT_NUM",this.productNum,productNum)){
			this.productNum = productNum;
		}
	}
	public String getParkingNum() {
		return parkingNum;
	}
	public void setParkingNum(String parkingNum) {
		if(this.checkValue("PARKING_NUM",this.parkingNum,parkingNum)){
			this.parkingNum = parkingNum;
		}
	}
	public int getFloor() {
		return floor;
	}
	public void setFloor(int floor) {
		if(this.checkValue("FLOOR",this.floor,floor)){
			this.floor = floor;
		}
	}
	public int getLocationType() {
		return locationType;
	}
	public void setLocationType(int locationType) {
		if(this.checkValue("LOCATION_TYPE",this.locationType,locationType)){
			this.locationType = locationType;
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
	
	@Override
	public String tableName() {
		return "IX_POI_CHARGINGPLOT";
	}
	
	public static final String POI_PID = "POI_PID";
	public static final String GROUP_ID = "GROUP_ID";
	public static final String COUNT = "COUNT";
	public static final String ACDC = "ACDC";
	public static final String PLUG_TYPE = "PLUG_TYPE";
	public static final String POWER = "POWER";
	public static final String VOLTAGE = "VOLTAGE";
	public static final String CURRENT = "CURRENT";
	public static final String MODE = "MODE";
	public static final String MEMO = "MEMO";
	public static final String PLUG_NUM = "PLUG_NUM";
	public static final String PRICES = "PRICES";
	public static final String OPEN_TYPE = "OPEN_TYPE";
	public static final String AVAILABLE_STATE = "AVAILABLE_STATE";
	public static final String MANUFACTURER = "MANUFACTURER";
	public static final String FACTORY_NUM = "FACTORY_NUM";
	public static final String PLOT_NUM = "PLOT_NUM";
	public static final String PRODUCT_NUM = "PRODUCT_NUM";
	public static final String PARKING_NUM = "PARKING_NUM";
	public static final String FLOOR = "FLOOR";
	public static final String LOCATION_TYPE = "LOCATION_TYPE";
	public static final String PAYMENT = "PAYMENT";

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
