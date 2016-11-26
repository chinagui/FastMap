package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiChildren 
* @author code generator
* @date 2016-11-18 11:32:51 
* @Description: TODO
*/
public class IxPoiChildren extends BasicRow {
	protected long groupId ;
	protected long childPoiPid ;
	protected int relationType ;
	
	

	public IxPoiChildren() {
		super();
		// TODO Auto-generated constructor stub
	}

	public IxPoiChildren (long objPid){
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
	public long getChildPoiPid() {
		return childPoiPid;
	}
	public void setChildPoiPid(long childPoiPid) {
		if(this.checkValue("CHILD_POI_PID",this.childPoiPid,childPoiPid)){
			this.childPoiPid = childPoiPid;
		}
	}
	public int getRelationType() {
		return relationType;
	}
	public void setRelationType(int relationType) {
		if(this.checkValue("RELATION_TYPE",this.relationType,relationType)){
			this.relationType = relationType;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_CHILDREN";
	}
}
