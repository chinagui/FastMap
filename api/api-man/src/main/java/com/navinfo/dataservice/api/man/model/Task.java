package com.navinfo.dataservice.api.man.model;

import java.sql.Timestamp;


/** 
* @ClassName:  Task 
* @author code generator
* @date 2016-06-06 06:12:30 
* @Description: TODO
*/
public class Task{
	private Integer taskId ;
	private String name;
	private Integer cityId ;
	private Integer createUserId ;
	private Timestamp createDate ;
	private Integer status ;
	private String descp ;
	private Timestamp collectPlanStartDate ;
	private Timestamp collectPlanEndDate ;
	private Timestamp dayEditPlanStartDate ;
	private Timestamp dayEditPlanEndDate ;
	private Timestamp bMonthEditPlanStartDate ;
	private Timestamp bMonthEditPlanEndDate ;
	private Timestamp cMonthEditPlanStartDate ;
	private Timestamp cMonthEditPlanEndDate ;
	private Timestamp dayProducePlanStartDate ;
	private Timestamp dayProducePlanEndDate ;
	private Timestamp monthProducePlanStartDate ;
	private Timestamp monthProducePlanEndDate ;
	private Integer latest ;
	
	public Task (){
	}
	public Task (Integer taskId ,String name,Integer cityId,Integer createUserId,Timestamp createDate,Integer status,String descp,Timestamp collectPlanStartDate,Timestamp collectPlanEndDate,Timestamp dayEditPlanStartDate,Timestamp dayEditPlanEndDate,Timestamp bMonthEditPlanStartDate,Timestamp bMonthEditPlanEndDate,Timestamp cMonthEditPlanStartDate,Timestamp cMonthEditPlanEndDate,Timestamp dayProducePlanStartDate,Timestamp dayProducePlanEndDate,Timestamp monthProducePlanStartDate,Timestamp monthProducePlanEndDate,Integer latest){
		this.taskId=taskId ;
		this.name=name;
		this.cityId=cityId ;
		this.createUserId=createUserId ;
		this.createDate=createDate ;
		this.status=status ;
		this.descp=descp ;
		this.collectPlanStartDate=collectPlanStartDate ;
		this.collectPlanEndDate=collectPlanEndDate ;
		this.dayEditPlanStartDate=dayEditPlanStartDate ;
		this.dayEditPlanEndDate=dayEditPlanEndDate ;
		this.bMonthEditPlanStartDate=bMonthEditPlanStartDate ;
		this.bMonthEditPlanEndDate=bMonthEditPlanEndDate ;
		this.cMonthEditPlanStartDate=cMonthEditPlanStartDate ;
		this.cMonthEditPlanEndDate=cMonthEditPlanEndDate ;
		this.dayProducePlanStartDate=dayProducePlanStartDate ;
		this.dayProducePlanEndDate=dayProducePlanEndDate ;
		this.monthProducePlanStartDate=monthProducePlanStartDate ;
		this.monthProducePlanEndDate=monthProducePlanEndDate ;
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
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getDescp() {
		return descp;
	}
	public void setDescp(String descp) {
		this.descp = descp;
	}
	public Timestamp getCollectPlanStartDate() {
		return collectPlanStartDate;
	}
	
	public void setCollectPlanStartDate(Timestamp collectPlanStartDate) {
		this.collectPlanStartDate = collectPlanStartDate;//String.valueOf(collectPlanStartDate);
	}
	public Timestamp getCollectPlanEndDate() {
		return collectPlanEndDate;
	}
	public void setCollectPlanEndDate(Timestamp collectPlanEndDate) {
		this.collectPlanEndDate = collectPlanEndDate;
	}
	public Timestamp getDayEditPlanStartDate() {
		return dayEditPlanStartDate;
	}
	public void setDayEditPlanStartDate(Timestamp dayEditPlanStartDate) {
		this.dayEditPlanStartDate = dayEditPlanStartDate;
	}
	public Timestamp getDayEditPlanEndDate() {
		return dayEditPlanEndDate;
	}
	public void setDayEditPlanEndDate(Timestamp dayEditPlanEndDate) {
		this.dayEditPlanEndDate = dayEditPlanEndDate;
	}
	public Timestamp getBMonthEditPlanStartDate() {
		return bMonthEditPlanStartDate;
	}
	public void setBMonthEditPlanStartDate(Timestamp bMonthEditPlanStartDate) {
		this.bMonthEditPlanStartDate =bMonthEditPlanStartDate;
	}
	public Timestamp getBMonthEditPlanEndDate() {
		return bMonthEditPlanEndDate;
	}
	public void setBMonthEditPlanEndDate(Timestamp bMonthEditPlanEndDate) {
		this.bMonthEditPlanEndDate = bMonthEditPlanEndDate;
	}
	public Timestamp getCMonthEditPlanStartDate() {
		return cMonthEditPlanStartDate;
	}
	public void setCMonthEditPlanStartDate(Timestamp cMonthEditPlanStartDate) {
		this.cMonthEditPlanStartDate = cMonthEditPlanStartDate;
	}
	public Timestamp getCMonthEditPlanEndDate() {
		return cMonthEditPlanEndDate;
	}
	public void setCMonthEditPlanEndDate(Timestamp cMonthEditPlanEndDate) {
		this.cMonthEditPlanEndDate = cMonthEditPlanEndDate;
	}
	public Timestamp getDayProducePlanStartDate() {
		return dayProducePlanStartDate;
	}
	public void setDayProducePlanStartDate(Timestamp dayProducePlanStartDate) {
		this.dayProducePlanStartDate = dayProducePlanStartDate;
	}
	public Timestamp getDayProducePlanEndDate() {
		return dayProducePlanEndDate;
	}
	public void setDayProducePlanEndDate(Timestamp dayProducePlanEndDate) {
		this.dayProducePlanEndDate = dayProducePlanEndDate;
	}
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
		return "Task [taskId=" + taskId +",cityId="+cityId+",createUserId="+createUserId+",createDate="+createDate+",status="+status+",descp="+descp+",collectPlanStartDate="+collectPlanStartDate+",collectPlanEndDate="+collectPlanEndDate+",dayEditPlanStartDate="+dayEditPlanStartDate+",dayEditPlanEndDate="+dayEditPlanEndDate+",bMonthEditPlanStartDate="+bMonthEditPlanStartDate+",bMonthEditPlanEndDate="+bMonthEditPlanEndDate+",cMonthEditPlanStartDate="+cMonthEditPlanStartDate+",cMonthEditPlanEndDate="+cMonthEditPlanEndDate+",dayProducePlanStartDate="+dayProducePlanStartDate+",dayProducePlanEndDate="+dayProducePlanEndDate+",monthProducePlanStartDate="+monthProducePlanStartDate+",monthProducePlanEndDate="+monthProducePlanEndDate+",latest="+latest+"]";
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
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((descp == null) ? 0 : descp.hashCode());
		result = prime * result + ((collectPlanStartDate == null) ? 0 : collectPlanStartDate.hashCode());
		result = prime * result + ((collectPlanEndDate == null) ? 0 : collectPlanEndDate.hashCode());
		result = prime * result + ((dayEditPlanStartDate == null) ? 0 : dayEditPlanStartDate.hashCode());
		result = prime * result + ((dayEditPlanEndDate == null) ? 0 : dayEditPlanEndDate.hashCode());
		result = prime * result + ((bMonthEditPlanStartDate == null) ? 0 : bMonthEditPlanStartDate.hashCode());
		result = prime * result + ((bMonthEditPlanEndDate == null) ? 0 : bMonthEditPlanEndDate.hashCode());
		result = prime * result + ((cMonthEditPlanStartDate == null) ? 0 : cMonthEditPlanStartDate.hashCode());
		result = prime * result + ((cMonthEditPlanEndDate == null) ? 0 : cMonthEditPlanEndDate.hashCode());
		result = prime * result + ((dayProducePlanStartDate == null) ? 0 : dayProducePlanStartDate.hashCode());
		result = prime * result + ((dayProducePlanEndDate == null) ? 0 : dayProducePlanEndDate.hashCode());
		result = prime * result + ((monthProducePlanStartDate == null) ? 0 : monthProducePlanStartDate.hashCode());
		result = prime * result + ((monthProducePlanEndDate == null) ? 0 : monthProducePlanEndDate.hashCode());
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
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (descp == null) {
			if (other.descp != null)
				return false;
		} else if (!descp.equals(other.descp))
			return false;
		if (collectPlanStartDate == null) {
			if (other.collectPlanStartDate != null)
				return false;
		} else if (!collectPlanStartDate.equals(other.collectPlanStartDate))
			return false;
		if (collectPlanEndDate == null) {
			if (other.collectPlanEndDate != null)
				return false;
		} else if (!collectPlanEndDate.equals(other.collectPlanEndDate))
			return false;
		if (dayEditPlanStartDate == null) {
			if (other.dayEditPlanStartDate != null)
				return false;
		} else if (!dayEditPlanStartDate.equals(other.dayEditPlanStartDate))
			return false;
		if (dayEditPlanEndDate == null) {
			if (other.dayEditPlanEndDate != null)
				return false;
		} else if (!dayEditPlanEndDate.equals(other.dayEditPlanEndDate))
			return false;
		if (bMonthEditPlanStartDate == null) {
			if (other.bMonthEditPlanStartDate != null)
				return false;
		} else if (!bMonthEditPlanStartDate.equals(other.bMonthEditPlanStartDate))
			return false;
		if (bMonthEditPlanEndDate == null) {
			if (other.bMonthEditPlanEndDate != null)
				return false;
		} else if (!bMonthEditPlanEndDate.equals(other.bMonthEditPlanEndDate))
			return false;
		if (cMonthEditPlanStartDate == null) {
			if (other.cMonthEditPlanStartDate != null)
				return false;
		} else if (!cMonthEditPlanStartDate.equals(other.cMonthEditPlanStartDate))
			return false;
		if (cMonthEditPlanEndDate == null) {
			if (other.cMonthEditPlanEndDate != null)
				return false;
		} else if (!cMonthEditPlanEndDate.equals(other.cMonthEditPlanEndDate))
			return false;
		if (dayProducePlanStartDate == null) {
			if (other.dayProducePlanStartDate != null)
				return false;
		} else if (!dayProducePlanStartDate.equals(other.dayProducePlanStartDate))
			return false;
		if (dayProducePlanEndDate == null) {
			if (other.dayProducePlanEndDate != null)
				return false;
		} else if (!dayProducePlanEndDate.equals(other.dayProducePlanEndDate))
			return false;
		if (monthProducePlanStartDate == null) {
			if (other.monthProducePlanStartDate != null)
				return false;
		} else if (!monthProducePlanStartDate.equals(other.monthProducePlanStartDate))
			return false;
		if (monthProducePlanEndDate == null) {
			if (other.monthProducePlanEndDate != null)
				return false;
		} else if (!monthProducePlanEndDate.equals(other.monthProducePlanEndDate))
			return false;
		if (latest == null) {
			if (other.latest != null)
				return false;
		} else if (!latest.equals(other.latest))
			return false;
		return true;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
}
