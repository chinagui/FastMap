package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxSamepoi 
* @author code generator
* @date 2016-11-18 11:33:04 
* @Description: TODO
*/
public class IxSamepoi extends BasicRow {
	protected long groupId ;
	protected int relationType = 1 ;
	
	public IxSamepoi (long objPid){
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
		return "IX_SAMEPOI";
	}
	public static final String GROUP_ID = "GROUP_ID";
	public static final String RELATION_TYPE = "RELATION_TYPE";

}
