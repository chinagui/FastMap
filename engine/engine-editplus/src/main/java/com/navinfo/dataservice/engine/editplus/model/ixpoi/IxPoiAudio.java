package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiAudio 
* @author code generator
* @date 2016-11-16 01:53:33 
* @Description: TODO
*/
public class IxPoiAudio extends BasicRow {
	protected long poiPid ;
	protected long audioId ;
	protected String pid ;
	protected String status ;
	protected String memo ;
	protected Integer tag ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiAudio (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public long getAudioId() {
		return audioId;
	}
	protected void setAudioId(long audioId) {
		this.audioId = audioId;
	}
	public String getPid() {
		return pid;
	}
	protected void setPid(String pid) {
		this.pid = pid;
	}
	public String getStatus() {
		return status;
	}
	protected void setStatus(String status) {
		this.status = status;
	}
	public String getMemo() {
		return memo;
	}
	protected void setMemo(String memo) {
		this.memo = memo;
	}
	public Integer getTag() {
		return tag;
	}
	protected void setTag(Integer tag) {
		this.tag = tag;
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
		return "IX_POI_AUDIO";
	}
}
