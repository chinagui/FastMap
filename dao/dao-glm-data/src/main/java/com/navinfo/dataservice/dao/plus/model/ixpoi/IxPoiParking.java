package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ISerializable;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxPoiParking 
* @author code generator
* @date 2016-11-18 11:35:49 
* @Description: TODO
*/
public class IxPoiParking extends BasicRow implements ISerializable{
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
		setPoiPid(objPid);
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
	
	public static final String PARKING_ID = "PARKING_ID";
	public static final String POI_PID = "POI_PID";
	public static final String PARKING_TYPE = "PARKING_TYPE";
	public static final String TOLL_STD = "TOLL_STD";
	public static final String TOLL_DES = "TOLL_DES";
	public static final String TOLL_WAY = "TOLL_WAY";
	public static final String PAYMENT = "PAYMENT";
	public static final String REMARK = "REMARK";
	public static final String SOURCE = "SOURCE";
	public static final String OPEN_TIIME = "OPEN_TIIME";
	public static final String TOTAL_NUM = "TOTAL_NUM";
	public static final String WORK_TIME = "WORK_TIME";
	public static final String RES_HIGH = "RES_HIGH";
	public static final String RES_WIDTH = "RES_WIDTH";
	public static final String RES_WEIGH = "RES_WEIGH";
	public static final String CERTIFICATE = "CERTIFICATE";
	public static final String VEHICLE = "VEHICLE";
	public static final String PHOTO_NAME = "PHOTO_NAME";
	public static final String HAVE_SPECIALPLACE = "HAVE_SPECIALPLACE";
	public static final String WOMEN_NUM = "WOMEN_NUM";
	public static final String HANDICAP_NUM = "HANDICAP_NUM";
	public static final String MINI_NUM = "MINI_NUM";
	public static final String VIP_NUM = "VIP_NUM";

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
