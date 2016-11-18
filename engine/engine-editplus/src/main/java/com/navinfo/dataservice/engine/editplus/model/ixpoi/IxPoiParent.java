package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiParent 
* @author code generator
* @date 2016-11-16 01:54:02 
* @Description: TODO
*/
public class IxPoiParent extends BasicRow {
	protected long groupId ;
	protected long parentPoiPid ;
	protected Integer tenantFlag ;
	protected String memo ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiParent (long objPid){
		super(objPid);
	}
	
	public long getGroupId() {
		return groupId;
	}
	protected void setGroupId(long groupId) {
		this.groupId = groupId;
	}
	public long getParentPoiPid() {
		return parentPoiPid;
	}
	protected void setParentPoiPid(long parentPoiPid) {
		this.parentPoiPid = parentPoiPid;
	}
	public Integer getTenantFlag() {
		return tenantFlag;
	}
	protected void setTenantFlag(Integer tenantFlag) {
		this.tenantFlag = tenantFlag;
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
