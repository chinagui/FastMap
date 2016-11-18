package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiTourroute 
* @author code generator
* @date 2016-11-18 11:36:41 
* @Description: TODO
*/
public class IxPoiTourroute extends BasicRow {
	protected long tourId ;
	protected String tourName ;
	protected String tourNameEng ;
	protected String tourIntr ;
	protected String tourIntrEng ;
	protected String tourType ;
	protected String tourTypeEng ;
	protected double tourX ;
	protected double tourY ;
	protected double tourLen ;
	protected String trailTime ;
	protected String visitTime ;
	protected String poiPid ;
	protected String reserved ;
	protected String memo ;
	
	public IxPoiTourroute (long objPid){
		super(objPid);
	}
	
	public long getTourId() {
		return tourId;
	}
	public void setTourId(long tourId) {
		if(this.checkValue("TOUR_ID",this.tourId,tourId)){
			this.tourId = tourId;
		}
	}
	public String getTourName() {
		return tourName;
	}
	public void setTourName(String tourName) {
		if(this.checkValue("TOUR_NAME",this.tourName,tourName)){
			this.tourName = tourName;
		}
	}
	public String getTourNameEng() {
		return tourNameEng;
	}
	public void setTourNameEng(String tourNameEng) {
		if(this.checkValue("TOUR_NAME_ENG",this.tourNameEng,tourNameEng)){
			this.tourNameEng = tourNameEng;
		}
	}
	public String getTourIntr() {
		return tourIntr;
	}
	public void setTourIntr(String tourIntr) {
		if(this.checkValue("TOUR_INTR",this.tourIntr,tourIntr)){
			this.tourIntr = tourIntr;
		}
	}
	public String getTourIntrEng() {
		return tourIntrEng;
	}
	public void setTourIntrEng(String tourIntrEng) {
		if(this.checkValue("TOUR_INTR_ENG",this.tourIntrEng,tourIntrEng)){
			this.tourIntrEng = tourIntrEng;
		}
	}
	public String getTourType() {
		return tourType;
	}
	public void setTourType(String tourType) {
		if(this.checkValue("TOUR_TYPE",this.tourType,tourType)){
			this.tourType = tourType;
		}
	}
	public String getTourTypeEng() {
		return tourTypeEng;
	}
	public void setTourTypeEng(String tourTypeEng) {
		if(this.checkValue("TOUR_TYPE_ENG",this.tourTypeEng,tourTypeEng)){
			this.tourTypeEng = tourTypeEng;
		}
	}
	public double getTourX() {
		return tourX;
	}
	public void setTourX(double tourX) {
		if(this.checkValue("TOUR_X",this.tourX,tourX)){
			this.tourX = tourX;
		}
	}
	public double getTourY() {
		return tourY;
	}
	public void setTourY(double tourY) {
		if(this.checkValue("TOUR_Y",this.tourY,tourY)){
			this.tourY = tourY;
		}
	}
	public double getTourLen() {
		return tourLen;
	}
	public void setTourLen(double tourLen) {
		if(this.checkValue("TOUR_LEN",this.tourLen,tourLen)){
			this.tourLen = tourLen;
		}
	}
	public String getTrailTime() {
		return trailTime;
	}
	public void setTrailTime(String trailTime) {
		if(this.checkValue("TRAIL_TIME",this.trailTime,trailTime)){
			this.trailTime = trailTime;
		}
	}
	public String getVisitTime() {
		return visitTime;
	}
	public void setVisitTime(String visitTime) {
		if(this.checkValue("VISIT_TIME",this.visitTime,visitTime)){
			this.visitTime = visitTime;
		}
	}
	public String getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(String poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
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
	
	
	@Override
	public String tableName() {
		return "IX_POI_TOURROUTE";
	}
}
