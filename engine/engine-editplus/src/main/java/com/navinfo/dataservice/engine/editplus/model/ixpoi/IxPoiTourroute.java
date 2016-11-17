package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiTourroute 
* @author code generator
* @date 2016-11-16 06:04:53 
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
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiTourroute (long objPid){
		super(objPid);
	}
	
	public long getTourId() {
		return tourId;
	}
	protected void setTourId(long tourId) {
		this.tourId = tourId;
	}
	public String getTourName() {
		return tourName;
	}
	protected void setTourName(String tourName) {
		this.tourName = tourName;
	}
	public String getTourNameEng() {
		return tourNameEng;
	}
	protected void setTourNameEng(String tourNameEng) {
		this.tourNameEng = tourNameEng;
	}
	public String getTourIntr() {
		return tourIntr;
	}
	protected void setTourIntr(String tourIntr) {
		this.tourIntr = tourIntr;
	}
	public String getTourIntrEng() {
		return tourIntrEng;
	}
	protected void setTourIntrEng(String tourIntrEng) {
		this.tourIntrEng = tourIntrEng;
	}
	public String getTourType() {
		return tourType;
	}
	protected void setTourType(String tourType) {
		this.tourType = tourType;
	}
	public String getTourTypeEng() {
		return tourTypeEng;
	}
	protected void setTourTypeEng(String tourTypeEng) {
		this.tourTypeEng = tourTypeEng;
	}
	public double getTourX() {
		return tourX;
	}
	protected void setTourX(double tourX) {
		this.tourX = tourX;
	}
	public double getTourY() {
		return tourY;
	}
	protected void setTourY(double tourY) {
		this.tourY = tourY;
	}
	public double getTourLen() {
		return tourLen;
	}
	protected void setTourLen(double tourLen) {
		this.tourLen = tourLen;
	}
	public String getTrailTime() {
		return trailTime;
	}
	protected void setTrailTime(String trailTime) {
		this.trailTime = trailTime;
	}
	public String getVisitTime() {
		return visitTime;
	}
	protected void setVisitTime(String visitTime) {
		this.visitTime = visitTime;
	}
	public String getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(String poiPid) {
		this.poiPid = poiPid;
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
		return "IX_POI_TOURROUTE";
	}
}
