package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiChargingplotPh 
* @author code generator
* @date 2016-11-16 06:02:38 
* @Description: TODO
*/
public class IxPoiChargingplotPh extends BasicRow {
	protected long poiPid ;
	protected String photoName ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiChargingplotPh (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getPhotoName() {
		return photoName;
	}
	protected void setPhotoName(String photoName) {
		this.photoName = photoName;
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
		return "IX_POI_CHARGINGPLOT_PH";
	}
}
