package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiDetail 
* @author code generator
* @date 2016-11-18 11:33:43 
* @Description: TODO
*/
public class IxPoiDetail extends BasicRow {
	protected long poiPid ;
	protected String webSite ;
	protected String fax ;
	protected String starHotel ;
	protected String briefDesc ;
	protected int adverFlag ;
	protected String photoName ;
	protected String reserved ;
	protected String memo ;
	protected int hwEntryexit ;
	protected int paycard ;
	protected String cardtype ;
	protected int hospitalClass ;
	
	public IxPoiDetail (long objPid){
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
	public String getWebSite() {
		return webSite;
	}
	public void setWebSite(String webSite) {
		if(this.checkValue("WEB_SITE",this.webSite,webSite)){
			this.webSite = webSite;
		}
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		if(this.checkValue("FAX",this.fax,fax)){
			this.fax = fax;
		}
	}
	public String getStarHotel() {
		return starHotel;
	}
	public void setStarHotel(String starHotel) {
		if(this.checkValue("STAR_HOTEL",this.starHotel,starHotel)){
			this.starHotel = starHotel;
		}
	}
	public String getBriefDesc() {
		return briefDesc;
	}
	public void setBriefDesc(String briefDesc) {
		if(this.checkValue("BRIEF_DESC",this.briefDesc,briefDesc)){
			this.briefDesc = briefDesc;
		}
	}
	public int getAdverFlag() {
		return adverFlag;
	}
	public void setAdverFlag(int adverFlag) {
		if(this.checkValue("ADVER_FLAG",this.adverFlag,adverFlag)){
			this.adverFlag = adverFlag;
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
	public String getReserved() {
		return reserved;
	}
	public void setReserved(String reserved) {
		if(this.checkValue("RESERVED",this.reserved,reserved)){
			this.reserved = reserved;
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
	public int getHwEntryexit() {
		return hwEntryexit;
	}
	public void setHwEntryexit(int hwEntryexit) {
		if(this.checkValue("HW_ENTRYEXIT",this.hwEntryexit,hwEntryexit)){
			this.hwEntryexit = hwEntryexit;
		}
	}
	public int getPaycard() {
		return paycard;
	}
	public void setPaycard(int paycard) {
		if(this.checkValue("PAYCARD",this.paycard,paycard)){
			this.paycard = paycard;
		}
	}
	public String getCardtype() {
		return cardtype;
	}
	public void setCardtype(String cardtype) {
		if(this.checkValue("CARDTYPE",this.cardtype,cardtype)){
			this.cardtype = cardtype;
		}
	}
	public int getHospitalClass() {
		return hospitalClass;
	}
	public void setHospitalClass(int hospitalClass) {
		if(this.checkValue("HOSPITAL_CLASS",this.hospitalClass,hospitalClass)){
			this.hospitalClass = hospitalClass;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_DETAIL";
	}
	
	public static final String POI_PID = "POI_PID";
	public static final String WEB_SITE = "WEB_SITE";
	public static final String FAX = "FAX";
	public static final String STAR_HOTEL = "STAR_HOTEL";
	public static final String BRIEF_DESC = "BRIEF_DESC";
	public static final String ADVER_FLAG = "ADVER_FLAG";
	public static final String PHOTO_NAME = "PHOTO_NAME";
	public static final String RESERVED = "RESERVED";
	public static final String MEMO = "MEMO";
	public static final String HW_ENTRYEXIT = "HW_ENTRYEXIT";
	public static final String PAYCARD = "PAYCARD";
	public static final String CARDTYPE = "CARDTYPE";
	public static final String HOSPITAL_CLASS = "HOSPITAL_CLASS";

}
