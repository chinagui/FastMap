package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiNameFlag 
* @author code generator
* @date 2016-11-18 11:26:21 
* @Description: TODO
*/
public class IxPoiNameFlag extends BasicRow {
	protected long nameId ;
	protected String flagCode ;
	
	public IxPoiNameFlag (long objPid){
		super(objPid);
	}
	
	public long getNameId() {
		return nameId;
	}
	public void setNameId(long nameId) {
		if(this.checkValue("NAME_ID",this.nameId,nameId)){
			this.nameId = nameId;
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
		return "IX_POI_NAME_FLAG";
	}
}
