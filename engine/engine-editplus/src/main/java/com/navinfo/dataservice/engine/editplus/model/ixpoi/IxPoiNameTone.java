package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiNameTone 
* @author code generator
* @date 2016-11-16 02:36:46 
* @Description: TODO
*/
public class IxPoiNameTone extends BasicRow {
	protected long nameId ;
	protected String toneA ;
	protected String toneB ;
	protected String lhA ;
	protected String lhB ;
	protected String jyutp ;
	protected String memo ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiNameTone (long objPid){
		super(objPid);
	}
	
	public long getNameId() {
		return nameId;
	}
	protected void setNameId(long nameId) {
		this.nameId = nameId;
	}
	public String getToneA() {
		return toneA;
	}
	protected void setToneA(String toneA) {
		this.toneA = toneA;
	}
	public String getToneB() {
		return toneB;
	}
	protected void setToneB(String toneB) {
		this.toneB = toneB;
	}
	public String getLhA() {
		return lhA;
	}
	protected void setLhA(String lhA) {
		this.lhA = lhA;
	}
	public String getLhB() {
		return lhB;
	}
	protected void setLhB(String lhB) {
		this.lhB = lhB;
	}
	public String getJyutp() {
		return jyutp;
	}
	protected void setJyutp(String jyutp) {
		this.jyutp = jyutp;
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
		return "IX_POI_NAME_TONE";
	}
}
