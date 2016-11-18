package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxSamepoi 
* @author code generator
* @date 2016-11-18 11:33:04 
* @Description: TODO
*/
public class IxSamepoi extends BasicRow {
	protected long groupId ;
	protected int relationType ;
	
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
}
