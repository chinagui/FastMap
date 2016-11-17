package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiNameFlag 
* @author code generator
* @date 2016-11-16 02:33:42 
* @Description: TODO
*/
public class IxPoiNameFlag extends BasicRow {
	protected long nameId ;
	protected String flagCode ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiNameFlag (long objPid){
		super(objPid);
	}
	
	public long getNameId() {
		return nameId;
	}
	protected void setNameId(long nameId) {
		this.nameId = nameId;
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
		return "IX_POI_NAME_FLAG";
	}
}
