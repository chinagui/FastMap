package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiEntryimage 
* @author code generator
* @date 2016-11-16 02:59:19 
* @Description: TODO
*/
public class IxPoiEntryimage extends BasicRow {
	protected long poiPid ;
	protected String imageCode ;
	protected Integer xPixelR4 ;
	protected Integer yPixelR4 ;
	protected Integer xPixelR5 ;
	protected Integer yPixelR5 ;
	protected Integer xPixel35 ;
	protected Integer yPixel35 ;
	protected String memo ;
	protected long mainPoiPid ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiEntryimage (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getImageCode() {
		return imageCode;
	}
	protected void setImageCode(String imageCode) {
		this.imageCode = imageCode;
	}
	public Integer getXPixelR4() {
		return xPixelR4;
	}
	protected void setXPixelR4(Integer xPixelR4) {
		this.xPixelR4 = xPixelR4;
	}
	public Integer getYPixelR4() {
		return yPixelR4;
	}
	protected void setYPixelR4(Integer yPixelR4) {
		this.yPixelR4 = yPixelR4;
	}
	public Integer getXPixelR5() {
		return xPixelR5;
	}
	protected void setXPixelR5(Integer xPixelR5) {
		this.xPixelR5 = xPixelR5;
	}
	public Integer getYPixelR5() {
		return yPixelR5;
	}
	protected void setYPixelR5(Integer yPixelR5) {
		this.yPixelR5 = yPixelR5;
	}
	public Integer getXPixel35() {
		return xPixel35;
	}
	protected void setXPixel35(Integer xPixel35) {
		this.xPixel35 = xPixel35;
	}
	public Integer getYPixel35() {
		return yPixel35;
	}
	protected void setYPixel35(Integer yPixel35) {
		this.yPixel35 = yPixel35;
	}
	public String getMemo() {
		return memo;
	}
	protected void setMemo(String memo) {
		this.memo = memo;
	}
	public long getMainPoiPid() {
		return mainPoiPid;
	}
	protected void setMainPoiPid(long mainPoiPid) {
		this.mainPoiPid = mainPoiPid;
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
		return "IX_POI_ENTRYIMAGE";
	}
}
