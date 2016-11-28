package com.navinfo.dataservice.dao.plus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxPoiFlag 
* @author code generator
* @date 2016-11-18 11:27:34 
* @Description: TODO
*/
public class IxPoiFlag extends BasicRow {
	protected long poiPid ;
	protected String flagCode ;
	
	public IxPoiFlag (long objPid){
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
	public String getFlagCode() {
		return flagCode;
	}
	public void setFlagCode(String flagCode) {
		if(this.checkValue("FLAG_CODE",this.flagCode,flagCode)){
			this.flagCode = flagCode;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_FLAG";
	}
}
