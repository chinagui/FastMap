package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;


/** 
* @ClassName:  IxSamepoiPart 
* @author code generator
* @date 2016-11-18 11:33:17 
* @Description: TODO
*/
public class IxSamepoiPart extends BasicRow {
	protected long groupId ;
	protected long poiPid ;
	
	public IxSamepoiPart (long objPid){
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
	public long getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(long poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}

	@Override
	public String tableName() {
		return "IX_SAMEPOI_PART";
	}
	
	public static final String GROUP_ID = "GROUP_ID";
	public static final String POI_PID = "POI_PID";

}
