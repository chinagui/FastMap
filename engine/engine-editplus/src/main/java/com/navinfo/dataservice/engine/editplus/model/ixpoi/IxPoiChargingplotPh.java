package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

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
}
