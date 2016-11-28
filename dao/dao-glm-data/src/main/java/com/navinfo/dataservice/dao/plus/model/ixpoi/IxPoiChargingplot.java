package com.navinfo.dataservice.dao.plus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxPoiChargingplot 
* @author code generator
* @date 2016-11-18 11:35:20 
* @Description: TODO
*/
public class IxPoiChargingplot extends BasicRow {
	protected long poiPid ;
	protected int groupId ;
	protected int count ;
	protected int acdc ;
	protected String plugType ;
	protected String power ;
	protected String voltage ;
	protected String current ;
	protected int mode ;
	protected String memo ;
	protected int plugNum ;
	protected String prices ;
	protected String openType ;
	protected int availableState ;
	protected String manufacturer ;
	protected String factoryNum ;
	protected String plotNum ;
	protected String productNum ;
	protected String parkingNum ;
	protected int floor ;
	protected int locationType ;
	protected String payment ;
	
	public IxPoiChargingplot (long objPid){
		super(objPid);
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
}
