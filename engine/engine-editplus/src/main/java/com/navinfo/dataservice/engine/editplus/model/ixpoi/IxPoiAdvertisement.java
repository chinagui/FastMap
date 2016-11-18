package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiAdvertisement 
* @author code generator
* @date 2016-11-16 06:01:17 
* @Description: TODO
*/
public class IxPoiAdvertisement extends BasicRow {
	protected long advertiseId ;
	protected long poiPid ;
	protected String labelText ;
	protected String type ;
	protected Integer priority ;
	protected String startTime ;
	protected String endTime ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiAdvertisement (long objPid){
		super(objPid);
	}
	
	public long getAdvertiseId() {
		return advertiseId;
	}
	protected void setAdvertiseId(long advertiseId) {
		this.advertiseId = advertiseId;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getLabelText() {
		return labelText;
	}
	protected void setLabelText(String labelText) {
		this.labelText = labelText;
	}
	public String getType() {
		return type;
	}
	protected void setType(String type) {
		this.type = type;
	}
	public Integer getPriority() {
		return priority;
	}
	protected void setPriority(Integer priority) {
		this.priority = priority;
	}
	public String getStartTime() {
		return startTime;
	}
	protected void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	protected void setEndTime(String endTime) {
		this.endTime = endTime;
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
		return "IX_POI_ADVERTISEMENT";
	}
}
