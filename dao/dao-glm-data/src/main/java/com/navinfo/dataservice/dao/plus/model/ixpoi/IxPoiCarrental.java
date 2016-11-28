package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiCarrental 
* @author code generator
* @date 2016-11-18 11:37:13 
* @Description: TODO
*/
public class IxPoiCarrental extends BasicRow {
	protected long poiPid ;
	protected String openHour ;
	protected String address ;
	protected String howToGo ;
	protected String phone400 ;
	protected String webSite ;
//	protected int uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiCarrental (long objPid){
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
	public String getOpenHour() {
		return openHour;
	}
	public void setOpenHour(String openHour) {
		if(this.checkValue("OPEN_HOUR",this.openHour,openHour)){
			this.openHour = openHour;
		}
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		if(this.checkValue("ADDRESS",this.address,address)){
			this.address = address;
		}
	}
	public String getHowToGo() {
		return howToGo;
	}
	public void setHowToGo(String howToGo) {
		if(this.checkValue("HOW_TO_GO",this.howToGo,howToGo)){
			this.howToGo = howToGo;
		}
	}
	public String getPhone400() {
		return phone400;
	}
	public void setPhone400(String phone400) {
		if(this.checkValue("PHONE_400",this.phone400,phone400)){
			this.phone400 = phone400;
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
//	public int getURecord() {
//		return uRecord;
//	}
//	public void setURecord(int uRecord) {
//		if(this.checkValue("U_RECORD",this.uRecord,uRecord)){
//			this.uRecord = uRecord;
//		}
//	}
//	public String getUFields() {
//		return uFields;
//	}
//	public void setUFields(String uFields) {
//		if(this.checkValue("U_FIELDS",this.uFields,uFields)){
//			this.uFields = uFields;
//		}
//	}
//	public String getUDate() {
//		return uDate;
//	}
//	public void setUDate(String uDate) {
//		if(this.checkValue("U_DATE",this.uDate,uDate)){
//			this.uDate = uDate;
//		}
//	}
	
	@Override
	public String tableName() {
		return "IX_POI_CARRENTAL";
	}
	
	public static final String POI_PID = "POI_PID";
	public static final String OPEN_HOUR = "OPEN_HOUR";
	public static final String ADDRESS = "ADDRESS";
	public static final String HOW_TO_GO = "HOW_TO_GO";
	public static final String PHONE_400 = "PHONE_400";
	public static final String WEB_SITE = "WEB_SITE";

}
