package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiChildren 
* @author code generator
* @date 2016-11-16 01:54:16 
* @Description: TODO
*/
public class IxPoiChildren extends BasicRow {
	protected long groupId ;
	protected long childPoiPid ;
	protected Integer relationType ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiChildren (long objPid){
		super(objPid);
	}
	
	public long getGroupId() {
		return groupId;
	}
	protected void setGroupId(long groupId) {
		this.groupId = groupId;
	}
	public long getChildPoiPid() {
		return childPoiPid;
	}
	protected void setChildPoiPid(long childPoiPid) {
		this.childPoiPid = childPoiPid;
	}
	public Integer getRelationType() {
		return relationType;
	}
	protected void setRelationType(Integer relationType) {
		this.relationType = relationType;
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
