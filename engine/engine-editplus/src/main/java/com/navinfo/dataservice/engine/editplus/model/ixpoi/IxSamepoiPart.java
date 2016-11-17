package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxSamepoiPart 
* @author code generator
* @date 2016-11-16 01:55:03 
* @Description: TODO
*/
public class IxSamepoiPart extends BasicRow {
	protected long groupId ;
	protected long poiPid ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxSamepoiPart (long objPid){
		super(objPid);
	}
	
	public long getGroupId() {
		return groupId;
	}
	protected void setGroupId(long groupId) {
		this.groupId = groupId;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
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
