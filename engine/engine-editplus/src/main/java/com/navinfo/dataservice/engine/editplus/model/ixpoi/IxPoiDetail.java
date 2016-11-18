package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiDetail 
* @author code generator
* @date 2016-11-16 05:59:44 
* @Description: TODO
*/
public class IxPoiDetail extends BasicRow {
	protected long poiPid ;
	protected String webSite ;
	protected String fax ;
	protected String starHotel ;
	protected String briefDesc ;
	protected Integer adverFlag ;
	protected String photoName ;
	protected String reserved ;
	protected String memo ;
	protected Integer hwEntryexit ;
	protected Integer paycard ;
	protected String cardtype ;
	protected Integer hospitalClass ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiDetail (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getWebSite() {
		return webSite;
	}
	protected void setWebSite(String webSite) {
		this.webSite = webSite;
	}
	public String getFax() {
		return fax;
	}
	protected void setFax(String fax) {
		this.fax = fax;
	}
	public String getStarHotel() {
		return starHotel;
	}
	protected void setStarHotel(String starHotel) {
		this.starHotel = starHotel;
	}
	public String getBriefDesc() {
		return briefDesc;
	}
	protected void setBriefDesc(String briefDesc) {
		this.briefDesc = briefDesc;
	}
	public Integer getAdverFlag() {
		return adverFlag;
	}
	protected void setAdverFlag(Integer adverFlag) {
		this.adverFlag = adverFlag;
	}
	public String getPhotoName() {
		return photoName;
	}
	protected void setPhotoName(String photoName) {
		this.photoName = photoName;
	}
	public String getReserved() {
		return reserved;
	}
	protected void setReserved(String reserved) {
		this.reserved = reserved;
	}
	public String getMemo() {
		return memo;
	}
	protected void setMemo(String memo) {
		this.memo = memo;
	}
	public Integer getHwEntryexit() {
		return hwEntryexit;
	}
	protected void setHwEntryexit(Integer hwEntryexit) {
		this.hwEntryexit = hwEntryexit;
	}
	public Integer getPaycard() {
		return paycard;
	}
	protected void setPaycard(Integer paycard) {
		this.paycard = paycard;
	}
	public String getCardtype() {
		return cardtype;
	}
	protected void setCardtype(String cardtype) {
		this.cardtype = cardtype;
	}
	public Integer getHospitalClass() {
		return hospitalClass;
	}
	protected void setHospitalClass(Integer hospitalClass) {
		this.hospitalClass = hospitalClass;
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
		return "IX_POI_DETAIL";
	}
}
