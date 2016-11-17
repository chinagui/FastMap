package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiParking 
* @author code generator
* @date 2016-11-16 06:03:04 
* @Description: TODO
*/
public class IxPoiParking extends BasicRow {
	protected long parkingId ;
	protected long poiPid ;
	protected String parkingType ;
	protected String tollStd ;
	protected String tollDes ;
	protected String tollWay ;
	protected String payment ;
	protected String remark ;
	protected String source ;
	protected String openTiime ;
	protected long totalNum ;
	protected String workTime ;
	protected double resHigh ;
	protected double resWidth ;
	protected double resWeigh ;
	protected Integer certificate ;
	protected Integer vehicle ;
	protected String photoName ;
	protected String haveSpecialplace ;
	protected Integer womenNum ;
	protected Integer handicapNum ;
	protected Integer miniNum ;
	protected Integer vipNum ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiParking (long objPid){
		super(objPid);
	}
	
	public long getParkingId() {
		return parkingId;
	}
	protected void setParkingId(long parkingId) {
		this.parkingId = parkingId;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getParkingType() {
		return parkingType;
	}
	protected void setParkingType(String parkingType) {
		this.parkingType = parkingType;
	}
	public String getTollStd() {
		return tollStd;
	}
	protected void setTollStd(String tollStd) {
		this.tollStd = tollStd;
	}
	public String getTollDes() {
		return tollDes;
	}
	protected void setTollDes(String tollDes) {
		this.tollDes = tollDes;
	}
	public String getTollWay() {
		return tollWay;
	}
	protected void setTollWay(String tollWay) {
		this.tollWay = tollWay;
	}
	public String getPayment() {
		return payment;
	}
	protected void setPayment(String payment) {
		this.payment = payment;
	}
	public String getRemark() {
		return remark;
	}
	protected void setRemark(String remark) {
		this.remark = remark;
	}
	public String getSource() {
		return source;
	}
	protected void setSource(String source) {
		this.source = source;
	}
	public String getOpenTiime() {
		return openTiime;
	}
	protected void setOpenTiime(String openTiime) {
		this.openTiime = openTiime;
	}
	public long getTotalNum() {
		return totalNum;
	}
	protected void setTotalNum(long totalNum) {
		this.totalNum = totalNum;
	}
	public String getWorkTime() {
		return workTime;
	}
	protected void setWorkTime(String workTime) {
		this.workTime = workTime;
	}
	public double getResHigh() {
		return resHigh;
	}
	protected void setResHigh(double resHigh) {
		this.resHigh = resHigh;
	}
	public double getResWidth() {
		return resWidth;
	}
	protected void setResWidth(double resWidth) {
		this.resWidth = resWidth;
	}
	public double getResWeigh() {
		return resWeigh;
	}
	protected void setResWeigh(double resWeigh) {
		this.resWeigh = resWeigh;
	}
	public Integer getCertificate() {
		return certificate;
	}
	protected void setCertificate(Integer certificate) {
		this.certificate = certificate;
	}
	public Integer getVehicle() {
		return vehicle;
	}
	protected void setVehicle(Integer vehicle) {
		this.vehicle = vehicle;
	}
	public String getPhotoName() {
		return photoName;
	}
	protected void setPhotoName(String photoName) {
		this.photoName = photoName;
	}
	public String getHaveSpecialplace() {
		return haveSpecialplace;
	}
	protected void setHaveSpecialplace(String haveSpecialplace) {
		this.haveSpecialplace = haveSpecialplace;
	}
	public Integer getWomenNum() {
		return womenNum;
	}
	protected void setWomenNum(Integer womenNum) {
		this.womenNum = womenNum;
	}
	public Integer getHandicapNum() {
		return handicapNum;
	}
	protected void setHandicapNum(Integer handicapNum) {
		this.handicapNum = handicapNum;
	}
	public Integer getMiniNum() {
		return miniNum;
	}
	protected void setMiniNum(Integer miniNum) {
		this.miniNum = miniNum;
	}
	public Integer getVipNum() {
		return vipNum;
	}
	protected void setVipNum(Integer vipNum) {
		this.vipNum = vipNum;
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
		return "IX_POI_PARKING";
	}
}
