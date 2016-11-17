package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiIntroduction 
* @author code generator
* @date 2016-11-16 06:00:43 
* @Description: TODO
*/
public class IxPoiIntroduction extends BasicRow {
	protected long introductionId ;
	protected long poiPid ;
	protected String introduction ;
	protected String introductionEng ;
	protected String website ;
	protected String neighbor ;
	protected String neighborEng ;
	protected String traffic ;
	protected String trafficEng ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiIntroduction (long objPid){
		super(objPid);
	}
	
	public long getIntroductionId() {
		return introductionId;
	}
	protected void setIntroductionId(long introductionId) {
		this.introductionId = introductionId;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getIntroduction() {
		return introduction;
	}
	protected void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	public String getIntroductionEng() {
		return introductionEng;
	}
	protected void setIntroductionEng(String introductionEng) {
		this.introductionEng = introductionEng;
	}
	public String getWebsite() {
		return website;
	}
	protected void setWebsite(String website) {
		this.website = website;
	}
	public String getNeighbor() {
		return neighbor;
	}
	protected void setNeighbor(String neighbor) {
		this.neighbor = neighbor;
	}
	public String getNeighborEng() {
		return neighborEng;
	}
	protected void setNeighborEng(String neighborEng) {
		this.neighborEng = neighborEng;
	}
	public String getTraffic() {
		return traffic;
	}
	protected void setTraffic(String traffic) {
		this.traffic = traffic;
	}
	public String getTrafficEng() {
		return trafficEng;
	}
	protected void setTrafficEng(String trafficEng) {
		this.trafficEng = trafficEng;
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
		return "IX_POI_INTRODUCTION";
	}
}
