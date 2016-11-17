package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxSamepoi 
* @author code generator
* @date 2016-11-16 01:54:53 
* @Description: TODO
*/
public class IxSamepoi extends BasicRow {
	protected long groupId ;
	protected Integer relationType ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxSamepoi (long objPid){
		super(objPid);
	}
	
	public long getGroupId() {
		return groupId;
	}
	protected void setGroupId(long groupId) {
		this.groupId = groupId;
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
