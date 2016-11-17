package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiChargingplot 
* @author code generator
* @date 2016-11-16 06:02:18 
* @Description: TODO
*/
public class IxPoiChargingplot extends BasicRow {
	protected long poiPid ;
	protected Integer groupId ;
	protected Integer count ;
	protected Integer acdc ;
	protected String plugType ;
	protected String power ;
	protected String voltage ;
	protected String current ;
	protected Integer mode ;
	protected String memo ;
	protected Integer plugNum ;
	protected String prices ;
	protected String openType ;
	protected Integer availableState ;
	protected String manufacturer ;
	protected String factoryNum ;
	protected String plotNum ;
	protected String productNum ;
	protected String parkingNum ;
	protected Integer floor ;
	protected Integer locationType ;
	protected String payment ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;

	public IxPoiChargingplot (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public Integer getGroupId() {
		return groupId;
	}
	protected void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public Integer getCount() {
		return count;
	}
	protected void setCount(Integer count) {
		this.count = count;
	}
	public Integer getAcdc() {
		return acdc;
	}
	protected void setAcdc(Integer acdc) {
		this.acdc = acdc;
	}
	public String getPlugType() {
		return plugType;
	}
	protected void setPlugType(String plugType) {
		this.plugType = plugType;
	}
	public String getPower() {
		return power;
	}
	protected void setPower(String power) {
		this.power = power;
	}
	public String getVoltage() {
		return voltage;
	}
	protected void setVoltage(String voltage) {
		this.voltage = voltage;
	}
	public String getCurrent() {
		return current;
	}
	protected void setCurrent(String current) {
		this.current = current;
	}
	public Integer getMode() {
		return mode;
	}
	protected void setMode(Integer mode) {
		this.mode = mode;
	}
	public String getMemo() {
		return memo;
	}
	protected void setMemo(String memo) {
		this.memo = memo;
	}
	public Integer getPlugNum() {
		return plugNum;
	}
	protected void setPlugNum(Integer plugNum) {
		this.plugNum = plugNum;
	}
	public String getPrices() {
		return prices;
	}
	protected void setPrices(String prices) {
		this.prices = prices;
	}
	public String getOpenType() {
		return openType;
	}
	protected void setOpenType(String openType) {
		this.openType = openType;
	}
	public Integer getAvailableState() {
		return availableState;
	}
	protected void setAvailableState(Integer availableState) {
		this.availableState = availableState;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	protected void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getFactoryNum() {
		return factoryNum;
	}
	protected void setFactoryNum(String factoryNum) {
		this.factoryNum = factoryNum;
	}
	public String getPlotNum() {
		return plotNum;
	}
	protected void setPlotNum(String plotNum) {
		this.plotNum = plotNum;
	}
	public String getProductNum() {
		return productNum;
	}
	protected void setProductNum(String productNum) {
		this.productNum = productNum;
	}
	public String getParkingNum() {
		return parkingNum;
	}
	protected void setParkingNum(String parkingNum) {
		this.parkingNum = parkingNum;
	}
	public Integer getFloor() {
		return floor;
	}
	protected void setFloor(Integer floor) {
		this.floor = floor;
	}
	public Integer getLocationType() {
		return locationType;
	}
	protected void setLocationType(Integer locationType) {
		this.locationType = locationType;
	}
	public String getPayment() {
		return payment;
	}
	protected void setPayment(String payment) {
		this.payment = payment;
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
		return "IX_POI_CHARGINGPLOT";
	}
}
