package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

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
	
	public static final String NAME_ID = "NAME_ID";
	public static final String FLAG_CODE = "FLAG_CODE";

}
