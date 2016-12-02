package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiIntroduction 
* @author code generator
* @date 2016-11-18 11:34:10 
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
	
	public IxPoiIntroduction (long objPid){
		super(objPid);
		setPoiPid(objPid);
	}
	
	public long getIntroductionId() {
		return introductionId;
	}
	public void setIntroductionId(long introductionId) {
		if(this.checkValue("INTRODUCTION_ID",this.introductionId,introductionId)){
			this.introductionId = introductionId;
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
	public String getIntroduction() {
		return introduction;
	}
	public void setIntroduction(String introduction) {
		if(this.checkValue("INTRODUCTION",this.introduction,introduction)){
			this.introduction = introduction;
		}
	}
	public String getIntroductionEng() {
		return introductionEng;
	}
	public void setIntroductionEng(String introductionEng) {
		if(this.checkValue("INTRODUCTION_ENG",this.introductionEng,introductionEng)){
			this.introductionEng = introductionEng;
		}
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		if(this.checkValue("WEBSITE",this.website,website)){
			this.website = website;
		}
	}
	public String getNeighbor() {
		return neighbor;
	}
	public void setNeighbor(String neighbor) {
		if(this.checkValue("NEIGHBOR",this.neighbor,neighbor)){
			this.neighbor = neighbor;
		}
	}
	public String getNeighborEng() {
		return neighborEng;
	}
	public void setNeighborEng(String neighborEng) {
		if(this.checkValue("NEIGHBOR_ENG",this.neighborEng,neighborEng)){
			this.neighborEng = neighborEng;
		}
	}
	public String getTraffic() {
		return traffic;
	}
	public void setTraffic(String traffic) {
		if(this.checkValue("TRAFFIC",this.traffic,traffic)){
			this.traffic = traffic;
		}
	}
	public String getTrafficEng() {
		return trafficEng;
	}
	public void setTrafficEng(String trafficEng) {
		if(this.checkValue("TRAFFIC_ENG",this.trafficEng,trafficEng)){
			this.trafficEng = trafficEng;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_INTRODUCTION";
	}
	
	public static final String INTRODUCTION_ID = "INTRODUCTION_ID";
	public static final String POI_PID = "POI_PID";
	public static final String INTRODUCTION = "INTRODUCTION";
	public static final String INTRODUCTION_ENG = "INTRODUCTION_ENG";
	public static final String WEBSITE = "WEBSITE";
	public static final String NEIGHBOR = "NEIGHBOR";
	public static final String NEIGHBOR_ENG = "NEIGHBOR_ENG";
	public static final String TRAFFIC = "TRAFFIC";
	public static final String TRAFFIC_ENG = "TRAFFIC_ENG";

}
