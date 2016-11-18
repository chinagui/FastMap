package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiAdvertisement 
* @author code generator
* @date 2016-11-18 11:34:39 
* @Description: TODO
*/
public class IxPoiAdvertisement extends BasicRow {
	protected long advertiseId ;
	protected long poiPid ;
	protected String labelText ;
	protected String type ;
	protected int priority ;
	protected String startTime ;
	protected String endTime ;
	
	public IxPoiAdvertisement (long objPid){
		super(objPid);
	}
	
	public long getAdvertiseId() {
		return advertiseId;
	}
	public void setAdvertiseId(long advertiseId) {
		if(this.checkValue("ADVERTISE_ID",this.advertiseId,advertiseId)){
			this.advertiseId = advertiseId;
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
	public String getLabelText() {
		return labelText;
	}
	public void setLabelText(String labelText) {
		if(this.checkValue("LABEL_TEXT",this.labelText,labelText)){
			this.labelText = labelText;
		}
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		if(this.checkValue("TYPE",this.type,type)){
			this.type = type;
		}
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		if(this.checkValue("PRIORITY",this.priority,priority)){
			this.priority = priority;
		}
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		if(this.checkValue("START_TIME",this.startTime,startTime)){
			this.startTime = startTime;
		}
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		if(this.checkValue("END_TIME",this.endTime,endTime)){
			this.endTime = endTime;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_ADVERTISEMENT";
	}
}
