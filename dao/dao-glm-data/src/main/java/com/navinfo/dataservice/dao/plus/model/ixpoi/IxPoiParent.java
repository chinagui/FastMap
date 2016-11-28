package com.navinfo.dataservice.dao.plus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxPoiParent 
* @author code generator
* @date 2016-11-18 11:32:35 
* @Description: TODO
*/
public class IxPoiParent extends BasicRow {
	protected long groupId ;
	protected long parentPoiPid ;
	protected int tenantFlag ;
	protected String memo ;
	
	
	public IxPoiParent() {
		super();
		// TODO Auto-generated constructor stub
	}

	public IxPoiParent (long objPid){
		super(objPid);
	}
	
	public long getGroupId() {
		return groupId;
	}
	public void setGroupId(long groupId) {
		if(this.checkValue("GROUP_ID",this.groupId,groupId)){
			this.groupId = groupId;
		}
	}
	public long getParentPoiPid() {
		return parentPoiPid;
	}
	public void setParentPoiPid(long parentPoiPid) {
		if(this.checkValue("PARENT_POI_PID",this.parentPoiPid,parentPoiPid)){
			this.parentPoiPid = parentPoiPid;
		}
	}
	public int getTenantFlag() {
		return tenantFlag;
	}
	public void setTenantFlag(int tenantFlag) {
		if(this.checkValue("TENANT_FLAG",this.tenantFlag,tenantFlag)){
			this.tenantFlag = tenantFlag;
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
		return "IX_POI_PARENT";
	}
}
