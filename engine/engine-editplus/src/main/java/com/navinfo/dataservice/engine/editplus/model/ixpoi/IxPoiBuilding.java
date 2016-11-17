package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiBuilding 
* @author code generator
* @date 2016-11-16 05:59:28 
* @Description: TODO
*/
public class IxPoiBuilding extends BasicRow {
	protected long poiPid ;
	protected String floorUsed ;
	protected String floorEmpty ;
	protected String memo ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiBuilding (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getFloorUsed() {
		return floorUsed;
	}
	protected void setFloorUsed(String floorUsed) {
		this.floorUsed = floorUsed;
	}
	public String getFloorEmpty() {
		return floorEmpty;
	}
	protected void setFloorEmpty(String floorEmpty) {
		this.floorEmpty = floorEmpty;
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
		return "IX_POI_BUILDING";
	}
}
