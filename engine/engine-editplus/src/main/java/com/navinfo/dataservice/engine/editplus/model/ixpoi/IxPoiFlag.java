package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxPoiFlag 
* @author code generator
* @date 2016-11-15 01:12:20 
* @Description: TODO
*/
public class IxPoiFlag extends BasicRow {
	protected long poiPid ;
	protected String flagCode ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiFlag (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getFlagCode() {
		return flagCode;
	}
	protected void setFlagCode(String flagCode) {
		this.flagCode = flagCode;
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
