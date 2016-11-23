package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiIcon 
* @author code generator
* @date 2016-11-18 11:27:59 
* @Description: TODO
*/
public class IxPoiIcon extends BasicRow {
	protected long relId ;
	protected long poiPid ;
	protected String iconName ;
	protected Object geometry ;
	protected String manageCode ;
	protected String clientFlag ;
	protected String memo ;
	
	public IxPoiIcon (long objPid){
		super(objPid);
	}
	
	public long getRelId() {
		return relId;
	}
	public void setRelId(long relId) {
		if(this.checkValue("REL_ID",this.relId,relId)){
			this.relId = relId;
		}
	}
	public long getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(long poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}
	public String getIconName() {
		return iconName;
	}
	public void setIconName(String iconName) {
		if(this.checkValue("ICON_NAME",this.iconName,iconName)){
			this.iconName = iconName;
		}
	}
	public Object getGeometry() {
		return geometry;
	}
	public void setGeometry(Object geometry) {
		if(this.checkValue("GEOMETRY",this.geometry,geometry)){
			this.geometry = geometry;
		}
	}
	public String getManageCode() {
		return manageCode;
	}
	public void setManageCode(String manageCode) {
		if(this.checkValue("MANAGE_CODE",this.manageCode,manageCode)){
			this.manageCode = manageCode;
		}
	}
	public String getClientFlag() {
		return clientFlag;
	}
	public void setClientFlag(String clientFlag) {
		if(this.checkValue("CLIENT_FLAG",this.clientFlag,clientFlag)){
			this.clientFlag = clientFlag;
		}
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		if(this.checkValue("MEMO",this.memo,memo)){
			this.memo = memo;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_ICON";
	}
}
