package com.navinfo.dataservice.api.man.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/** 
* @ClassName:  UserGroup 
* @author code generator
* @date 2016-06-17 05:31:11 
* @Description: TODO
*/
public class UserGroup  {
	private int groupId ;
	private String groupName ;
	private Integer groupType ;
	private Integer leaderId ;
	private Integer parentGroupId ;
	
	public UserGroup (){
	}
	
	public UserGroup (int groupId ,String groupName,Integer groupType,Integer leaderId,Integer parentGroupId){
		this.groupId=groupId ;
		this.groupName=groupName ;
		this.groupType=groupType ;
		this.leaderId=leaderId ;
		this.parentGroupId = parentGroupId;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public Integer getGroupType() {
		return groupType;
	}
	public void setGroupType(Integer groupType) {
		this.groupType = groupType;
	}
	public Integer getLeaderId() {
		return leaderId;
	}
	public void setLeaderId(Integer leaderId) {
		this.leaderId = leaderId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserGroup [groupId=" + groupId +",groupName="+groupName+",groupType="+groupType+",leaderId="+leaderId+"]";
	}

	/**
	 * @return the parentGroupId
	 */
	public Integer getParentGroupId() {
		return parentGroupId;
	}

	/**
	 * @param parentGroupId the parentGroupId to set
	 */
	public void setParentGroupId(Integer parentGroupId) {
		this.parentGroupId = parentGroupId;
	}
	
	
	
}
