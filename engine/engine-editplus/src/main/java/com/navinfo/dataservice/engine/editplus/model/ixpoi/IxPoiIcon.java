package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiIcon 
* @author code generator
* @date 2016-11-16 01:52:34 
* @Description: TODO
*/
public class IxPoiIcon extends BasicRow {
	protected long relId ;
	protected long poiPid ;
	protected String iconName ;
	protected Object geometry ;
	protected String manageCode ;
	protected String clientFlag ;
	protected String memo ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiIcon (long objPid){
		super(objPid);
	}
	
	public long getRelId() {
		return relId;
	}
	protected void setRelId(long relId) {
		this.relId = relId;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getIconName() {
		return iconName;
	}
	protected void setIconName(String iconName) {
		this.iconName = iconName;
	}
	public Object getGeometry() {
		return geometry;
	}
	protected void setGeometry(Object geometry) {
		this.geometry = geometry;
	}
	public String getManageCode() {
		return manageCode;
	}
	protected void setManageCode(String manageCode) {
		this.manageCode = manageCode;
	}
	public String getClientFlag() {
		return clientFlag;
	}
	protected void setClientFlag(String clientFlag) {
		this.clientFlag = clientFlag;
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
		return "IX_POI";
	}
}
