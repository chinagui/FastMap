package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiChargingplotPh 
* @author code generator
* @date 2016-11-18 11:35:37 
* @Description: TODO
*/
public class IxPoiChargingplotPh extends BasicRow {
	protected long poiPid ;
	protected String photoName ;

	
	public IxPoiChargingplotPh (long objPid){
		super(objPid);
		setPoiPid(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(long poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}
	public String getPhotoName() {
		return photoName;
	}
	public void setPhotoName(String photoName) {
		if(this.checkValue("PHOTO_NAME",this.photoName,photoName)){
			this.photoName = photoName;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_CHARGINGPLOT_PH";
	}
	
	public static final String POI_PID = "POI_PID";
	public static final String PHOTO_NAME = "PHOTO_NAME";

}
