package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiCarrental 
* @author code generator
* @date 2016-11-16 06:05:32 
* @Description: TODO
*/
public class IxPoiCarrental extends BasicRow {
	protected long poiPid ;
	protected String openHour ;
	protected String address ;
	protected String howToGo ;
	protected String phone400 ;
	protected String webSite ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiCarrental (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getOpenHour() {
		return openHour;
	}
	protected void setOpenHour(String openHour) {
		this.openHour = openHour;
	}
	public String getAddress() {
		return address;
	}
	protected void setAddress(String address) {
		this.address = address;
	}
	public String getHowToGo() {
		return howToGo;
	}
	protected void setHowToGo(String howToGo) {
		this.howToGo = howToGo;
	}
	public String getPhone400() {
		return phone400;
	}
	protected void setPhone400(String phone400) {
		this.phone400 = phone400;
	}
	public String getWebSite() {
		return webSite;
	}
	protected void setWebSite(String webSite) {
		this.webSite = webSite;
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
		return "IX_POI_CARRENTAL";
	}
}
