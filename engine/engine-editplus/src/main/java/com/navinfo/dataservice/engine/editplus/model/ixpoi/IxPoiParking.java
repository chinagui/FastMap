package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiParking 
* @author code generator
* @date 2016-11-18 11:35:49 
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
	protected int certificate ;
	protected int vehicle ;
	protected String photoName ;
	protected String haveSpecialplace ;
	protected int womenNum ;
	protected int handicapNum ;
	protected int miniNum ;
	protected int vipNum ;
	
	public IxPoiParking (long objPid){
		super(objPid);
	}
	
	public long getParkingId() {
		return parkingId;
	}
	public void setParkingId(long parkingId) {
		if(this.checkValue("PARKING_ID",this.parkingId,parkingId)){
			this.parkingId = parkingId;
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
	public String getParkingType() {
		return parkingType;
	}
	public void setParkingType(String parkingType) {
		if(this.checkValue("PARKING_TYPE",this.parkingType,parkingType)){
			this.parkingType = parkingType;
		}
	}
	public String getTollStd() {
		return tollStd;
	}
	public void setTollStd(String tollStd) {
		if(this.checkValue("TOLL_STD",this.tollStd,tollStd)){
			this.tollStd = tollStd;
		}
	}
	public String getTollDes() {
		return tollDes;
	}
	public void setTollDes(String tollDes) {
		if(this.checkValue("TOLL_DES",this.tollDes,tollDes)){
			this.tollDes = tollDes;
		}
	}
	public String getTollWay() {
		return tollWay;
	}
	public void setTollWay(String tollWay) {
		if(this.checkValue("TOLL_WAY",this.tollWay,tollWay)){
			this.tollWay = tollWay;
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
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		if(this.checkValue("REMARK",this.remark,remark)){
			this.remark = remark;
		}
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		if(this.checkValue("SOURCE",this.source,source)){
			this.source = source;
		}
	}
	public String getOpenTiime() {
		return openTiime;
	}
	public void setOpenTiime(String openTiime) {
		if(this.checkValue("OPEN_TIIME",this.openTiime,openTiime)){
			this.openTiime = openTiime;
		}
	}
	public long getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(long totalNum) {
		if(this.checkValue("TOTAL_NUM",this.totalNum,totalNum)){
			this.totalNum = totalNum;
		}
	}
	public String getWorkTime() {
		return workTime;
	}
	public void setWorkTime(String workTime) {
		if(this.checkValue("WORK_TIME",this.workTime,workTime)){
			this.workTime = workTime;
		}
	}
	public double getResHigh() {
		return resHigh;
	}
	public void setResHigh(double resHigh) {
		if(this.checkValue("RES_HIGH",this.resHigh,resHigh)){
			this.resHigh = resHigh;
		}
	}
	public double getResWidth() {
		return resWidth;
	}
	public void setResWidth(double resWidth) {
		if(this.checkValue("RES_WIDTH",this.resWidth,resWidth)){
			this.resWidth = resWidth;
		}
	}
	public double getResWeigh() {
		return resWeigh;
	}
	public void setResWeigh(double resWeigh) {
		if(this.checkValue("RES_WEIGH",this.resWeigh,resWeigh)){
			this.resWeigh = resWeigh;
		}
	}
	public int getCertificate() {
		return certificate;
	}
	public void setCertificate(int certificate) {
		if(this.checkValue("CERTIFICATE",this.certificate,certificate)){
			this.certificate = certificate;
		}
	}
	public int getVehicle() {
		return vehicle;
	}
	public void setVehicle(int vehicle) {
		if(this.checkValue("VEHICLE",this.vehicle,vehicle)){
			this.vehicle = vehicle;
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
	public String getHaveSpecialplace() {
		return haveSpecialplace;
	}
	public void setHaveSpecialplace(String haveSpecialplace) {
		if(this.checkValue("HAVE_SPECIALPLACE",this.haveSpecialplace,haveSpecialplace)){
			this.haveSpecialplace = haveSpecialplace;
		}
	}
	public int getWomenNum() {
		return womenNum;
	}
	public void setWomenNum(int womenNum) {
		if(this.checkValue("WOMEN_NUM",this.womenNum,womenNum)){
			this.womenNum = womenNum;
		}
	}
	public int getHandicapNum() {
		return handicapNum;
	}
	public void setHandicapNum(int handicapNum) {
		if(this.checkValue("HANDICAP_NUM",this.handicapNum,handicapNum)){
			this.handicapNum = handicapNum;
		}
	}
	public int getMiniNum() {
		return miniNum;
	}
	public void setMiniNum(int miniNum) {
		if(this.checkValue("MINI_NUM",this.miniNum,miniNum)){
			this.miniNum = miniNum;
		}
	}
	public int getVipNum() {
		return vipNum;
	}
	public void setVipNum(int vipNum) {
		if(this.checkValue("VIP_NUM",this.vipNum,vipNum)){
			this.vipNum = vipNum;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_PARKING";
	}
}
