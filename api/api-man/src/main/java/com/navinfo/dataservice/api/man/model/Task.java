package com.navinfo.dataservice.api.man.model;

import java.io.Serializable;
import java.sql.Timestamp;


/** 
* @ClassName:  Task 
* @author code generator
* @date 2016-06-06 06:12:30 
* @Description: TODO
*/
public class Task implements Serializable{
	private Integer taskId ;
	private String taskName;
	private Integer cityId ;
	private String cityName;
	private String version;
	private Integer createUserId ;
	private String createUserName;
	private Timestamp createDate ;
	private Integer taskStatus ;
	private String taskDescp ;
	private Timestamp planStartDate ;
	private Timestamp planEndDate ;
	private Timestamp monthEditPlanStartDate ;
	private Timestamp monthEditPlanEndDate ;
	private Integer monthEditGroupId ;
	private String monthEditGroupName;
	private Timestamp monthProducePlanStartDate ;
	private Timestamp monthProducePlanEndDate ;
	//1常规，2多源，3代理店，4情报
	private Integer TaskType;
	private Integer latest ;
	
	public Timestamp getMonthProducePlanStartDate() {
		return monthProducePlanStartDate;
	}
	public void setMonthProducePlanStartDate(Timestamp monthProducePlanStartDate) {
		this.monthProducePlanStartDate = monthProducePlanStartDate;
	}
	public Timestamp getMonthProducePlanEndDate() {
		return monthProducePlanEndDate;
	}
	public void setMonthProducePlanEndDate(Timestamp monthProducePlanEndDate) {
		this.monthProducePlanEndDate = monthProducePlanEndDate;
	}
	public Integer getTaskType() {
		return TaskType;
	}
	public void setTaskType(Integer taskType) {
		TaskType = taskType;
	}
		
	public Task (){
	}
	public Task (Integer taskId ,String name,Integer cityId,Integer createUserId,Timestamp createDate,Integer status,String descp,Timestamp planStartDate,Timestamp planEndDate,Timestamp monthEditPlanStartDate,Timestamp monthEditPlanEndDate,Integer monthEditGroupId,Integer latest){
		this.taskId=taskId ;
		this.taskName=name;
		this.cityId=cityId ;
		this.createUserId=createUserId ;
		this.createDate=createDate ;
		this.taskStatus=status ;
		this.setTaskDescp(descp) ;
		this.planStartDate=planStartDate ;
		this.planEndDate=planEndDate ;
		this.monthEditPlanStartDate=monthEditPlanStartDate ;
		this.monthEditPlanEndDate=monthEditPlanEndDate ;
		this.monthEditGroupId=monthEditGroupId ;
		this.latest=latest ;
	}
	
	public Integer getTaskId() {
		return taskId;
	}
	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	public Integer getCityId() {
		return cityId;
	}
	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}
	public Integer getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(Integer createUserId) {
		this.createUserId = createUserId;
	}
	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	public Integer getTaskStatus() {
		return taskStatus;
	}
	public void setTaskStatus(Integer status) {
		this.taskStatus = status;
	}
	public String getTaskDescp() {
		if(null==taskDescp){return "";}
		return taskDescp;
	}
	public void setTaskDescp(String descp) {
		if(null==descp){this.taskDescp="";}
		this.taskDescp = descp;
	}
	public Timestamp getPlanEndDate() {
		return planEndDate;
	}
	public void setPlanEndDate(Timestamp collectPlanEndDate) {
		this.planEndDate = collectPlanEndDate;
	}
	public Timestamp getMonthEditPlanStartDate() {
		return monthEditPlanStartDate;
	}
	public void setMonthEditPlanStartDate(Timestamp MonthEditPlanStartDate) {
		this.monthEditPlanStartDate =MonthEditPlanStartDate;
	}
	public Timestamp getMonthEditPlanEndDate() {
		return monthEditPlanEndDate;
	}
	public void setMonthEditPlanEndDate(Timestamp MonthEditPlanEndDate) {
		this.monthEditPlanEndDate = MonthEditPlanEndDate;
	}
	public Integer getLatest() {
		return latest;
	}
	public void setLatest(Integer latest) {
		this.latest = latest;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Task [taskId=" + taskId +",cityId="+cityId+",createUserId="+createUserId+",createDate="+createDate+",status="+taskStatus+",descp="+taskDescp+",planStartDate="+planStartDate+",planEndDate="+planEndDate+",monthEditPlanStartDate="+monthEditPlanStartDate+",monthEditPlanEndDate="+monthEditPlanEndDate+",monthEditGroupId="+monthEditGroupId+",latest="+latest+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
		result = prime * result + ((cityId == null) ? 0 : cityId.hashCode());
		result = prime * result + ((createUserId == null) ? 0 : createUserId.hashCode());
		result = prime * result + ((createDate == null) ? 0 : createDate.hashCode());
		result = prime * result + ((taskStatus == null) ? 0 : taskStatus.hashCode());
		result = prime * result + ((taskDescp == null) ? 0 : taskDescp.hashCode());
		result = prime * result + ((planStartDate == null) ? 0 : planStartDate.hashCode());
		result = prime * result + ((planEndDate == null) ? 0 : planEndDate.hashCode());
		result = prime * result + ((monthEditPlanStartDate == null) ? 0 : monthEditPlanStartDate.hashCode());
		result = prime * result + ((monthEditPlanEndDate == null) ? 0 : monthEditPlanEndDate.hashCode());
		result = prime * result + ((monthEditGroupId == null) ? 0 : monthEditGroupId.hashCode());
		result = prime * result + ((latest == null) ? 0 : latest.hashCode());
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
		Task other = (Task) obj;
		if (taskId == null) {
			if (other.taskId != null)
				return false;
		} else if (!taskId.equals(other.taskId))
			return false;
		if (cityId == null) {
			if (other.cityId != null)
				return false;
		} else if (!cityId.equals(other.cityId))
			return false;
		if (createUserId == null) {
			if (other.createUserId != null)
				return false;
		} else if (!createUserId.equals(other.createUserId))
			return false;
		if (createDate == null) {
			if (other.createDate != null)
				return false;
		} else if (!createDate.equals(other.createDate))
			return false;
		if (taskStatus == null) {
			if (other.taskStatus != null)
				return false;
		} else if (!taskStatus.equals(other.taskStatus))
			return false;
		if (taskDescp == null) {
			if (other.taskDescp != null)
				return false;
		} else if (!taskDescp.equals(other.taskDescp))
			return false;
		if (planStartDate == null) {
			if (other.planStartDate != null)
				return false;
		} else if (!planStartDate.equals(other.planStartDate))
			return false;
		if (planEndDate == null) {
			if (other.planEndDate != null)
				return false;
		} else if (!planEndDate.equals(other.planEndDate))
			return false;
		
		if (monthEditPlanStartDate == null) {
			if (other.monthEditPlanStartDate != null)
				return false;
		} else if (!monthEditPlanStartDate.equals(other.monthEditPlanStartDate))
			return false;
		if (monthEditPlanEndDate == null) {
			if (other.monthEditPlanEndDate != null)
				return false;
		} else if (!monthEditPlanEndDate.equals(other.monthEditPlanEndDate))
			return false;
		if (monthEditGroupId == null) {
			if (other.monthEditGroupId != null)
				return false;
		} else if (!monthEditGroupId.equals(other.monthEditGroupId))
			return false;
		if (latest == null) {
			if (other.latest != null)
				return false;
		} else if (!latest.equals(other.latest))
			return false;
		return true;
	}
	public String getTaskName() {
		if(null==taskName){return "";}
		return taskName;
	}
	public void setTaskName(String name) {
		if(null==name){this.taskName="";}
		this.taskName = name;
	}
	public Timestamp getPlanStartDate() {
		return planStartDate;
	}
	public void setPlanStartDate(Timestamp planStartDate) {
		this.planStartDate = planStartDate;
	}
	public Integer getMonthEditGroupId() {
		if(null==monthEditGroupId){return 0;}
		return monthEditGroupId;
	}
	public void setMonthEditGroupId(Integer monthEditGroupId) {
		if(null==monthEditGroupId){this.monthEditGroupId=0;}
		this.monthEditGroupId = monthEditGroupId;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getCreateUserName() {
		return createUserName;
	}
	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}
	public String getMonthEditGroupName() {
		return monthEditGroupName;
	}
	public void setMonthEditGroupName(String monthEditGroupName) {
		this.monthEditGroupName = monthEditGroupName;
	}	
}
