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
	private Integer groupId ;
	private String groupName ;
	private Integer groupType ;
	private Integer leaderId ;
	
	public UserGroup (){
	}
	
	public UserGroup (Integer groupId ,String groupName,Integer groupType,Integer leaderId){
		this.groupId=groupId ;
		this.groupName=groupName ;
		this.groupType=groupType ;
		this.leaderId=leaderId ;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
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


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((groupType == null) ? 0 : groupType.hashCode());
		result = prime * result + ((leaderId == null) ? 0 : leaderId.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserGroup other = (UserGroup) obj;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (groupType == null) {
			if (other.groupType != null)
				return false;
		} else if (!groupType.equals(other.groupType))
			return false;
		if (leaderId == null) {
			if (other.leaderId != null)
				return false;
		} else if (!leaderId.equals(other.leaderId))
			return false;
		return true;
	}
	
	
	
}
