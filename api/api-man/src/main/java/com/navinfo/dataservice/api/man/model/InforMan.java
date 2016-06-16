package com.navinfo.dataservice.api.man.model;

import java.sql.Timestamp;

/** 
* @ClassName:  InforMan 
* @author code generator
* @date 2016-06-15 02:27:03 
* @Description: TODO
*/
public class InforMan  {
	private String inforId ;
	private Integer inforStatus ;
	private String descp ;
	private Integer createUserId ;
	private Timestamp createDate ;
	private Timestamp collectPlanStartDate ;
	private Timestamp collectPlanEndDate ;
	private Integer collectGroupId ;
	private Timestamp dayEditPlanStartDate ;
	private Timestamp dayEditPlanEndDate ;
	private Integer dayEditGroupId ;
	private Timestamp monthEditPlanStartDate ;
	private Timestamp monthEditPlanEndDate ;
	private Integer monthEditGroupId ;
	private Timestamp dayProducePlanStartDate ;
	private Timestamp dayProducePlanEndDate ;
	private Timestamp monthProducePlanStartDate ;
	private Timestamp monthProducePlanEndDate ;
	
	public InforMan (){
	}
	
	public InforMan (String inforId ,Integer inforStatus,String descp,Integer createUserId,Timestamp createDate,Timestamp collectPlanStartDate,Timestamp collectPlanEndDate,Integer collectGroupId,Timestamp dayEditPlanStartDate,Timestamp dayEditPlanEndDate,Integer dayEditGroupId,Timestamp monthEditPlanStartDate,Timestamp monthEditPlanEndDate,Integer monthEditGroupId,Timestamp dayProducePlanStartDate,Timestamp dayProducePlanEndDate,Timestamp monthProducePlanStartDate,Timestamp monthProducePlanEndDate){
		this.inforId=inforId ;
		this.inforStatus=inforStatus ;
		this.descp=descp ;
		this.createUserId=createUserId ;
		this.createDate=createDate ;
		this.collectPlanStartDate=collectPlanStartDate ;
		this.collectPlanEndDate=collectPlanEndDate ;
		this.collectGroupId=collectGroupId ;
		this.dayEditPlanStartDate=dayEditPlanStartDate ;
		this.dayEditPlanEndDate=dayEditPlanEndDate ;
		this.dayEditGroupId=dayEditGroupId ;
		this.monthEditPlanStartDate=monthEditPlanStartDate ;
		this.monthEditPlanEndDate=monthEditPlanEndDate ;
		this.monthEditGroupId=monthEditGroupId ;
		this.dayProducePlanStartDate=dayProducePlanStartDate ;
		this.dayProducePlanEndDate=dayProducePlanEndDate ;
		this.monthProducePlanStartDate=monthProducePlanStartDate ;
		this.monthProducePlanEndDate=monthProducePlanEndDate ;
	}
	public String getInforId() {
		return inforId;
	}
	public void setInforId(String inforId) {
		this.inforId = inforId;
	}
	public Integer getInforStatus() {
		return inforStatus;
	}
	public void setInforStatus(Integer inforStatus) {
		this.inforStatus = inforStatus;
	}
	public String getDescp() {
		return descp;
	}
	public void setDescp(String descp) {
		this.descp = descp;
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
	public Timestamp getCollectPlanStartDate() {
		return collectPlanStartDate;
	}
	public void setCollectPlanStartDate(Timestamp collectPlanStartDate) {
		this.collectPlanStartDate = collectPlanStartDate;
	}
	public Timestamp getCollectPlanEndDate() {
		return collectPlanEndDate;
	}
	public void setCollectPlanEndDate(Timestamp collectPlanEndDate) {
		this.collectPlanEndDate = collectPlanEndDate;
	}
	public Integer getCollectGroupId() {
		return collectGroupId;
	}
	public void setCollectGroupId(Integer collectGroupId) {
		this.collectGroupId = collectGroupId;
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
	public Integer getDayEditGroupId() {
		return dayEditGroupId;
	}
	public void setDayEditGroupId(Integer dayEditGroupId) {
		this.dayEditGroupId = dayEditGroupId;
	}
	public Timestamp getMonthEditPlanStartDate() {
		return monthEditPlanStartDate;
	}
	public void setMonthEditPlanStartDate(Timestamp monthEditPlanStartDate) {
		this.monthEditPlanStartDate = monthEditPlanStartDate;
	}
	public Timestamp getMonthEditPlanEndDate() {
		return monthEditPlanEndDate;
	}
	public void setMonthEditPlanEndDate(Timestamp monthEditPlanEndDate) {
		this.monthEditPlanEndDate = monthEditPlanEndDate;
	}
	public Integer getMonthEditGroupId() {
		return monthEditGroupId;
	}
	public void setMonthEditGroupId(Integer monthEditGroupId) {
		this.monthEditGroupId = monthEditGroupId;
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "InforMan [inforId=" + inforId +",inforStatus="+inforStatus+",descp="+descp+",createUserId="+createUserId+",createDate="+createDate+",collectPlanStartDate="+collectPlanStartDate+",collectPlanEndDate="+collectPlanEndDate+",collectGroupId="+collectGroupId+",dayEditPlanStartDate="+dayEditPlanStartDate+",dayEditPlanEndDate="+dayEditPlanEndDate+",dayEditGroupId="+dayEditGroupId+",monthEditPlanStartDate="+monthEditPlanStartDate+",monthEditPlanEndDate="+monthEditPlanEndDate+",monthEditGroupId="+monthEditGroupId+",dayProducePlanStartDate="+dayProducePlanStartDate+",dayProducePlanEndDate="+dayProducePlanEndDate+",monthProducePlanStartDate="+monthProducePlanStartDate+",monthProducePlanEndDate="+monthProducePlanEndDate+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inforId == null) ? 0 : inforId.hashCode());
		result = prime * result + ((inforStatus == null) ? 0 : inforStatus.hashCode());
		result = prime * result + ((descp == null) ? 0 : descp.hashCode());
		result = prime * result + ((createUserId == null) ? 0 : createUserId.hashCode());
		result = prime * result + ((createDate == null) ? 0 : createDate.hashCode());
		result = prime * result + ((collectPlanStartDate == null) ? 0 : collectPlanStartDate.hashCode());
		result = prime * result + ((collectPlanEndDate == null) ? 0 : collectPlanEndDate.hashCode());
		result = prime * result + ((collectGroupId == null) ? 0 : collectGroupId.hashCode());
		result = prime * result + ((dayEditPlanStartDate == null) ? 0 : dayEditPlanStartDate.hashCode());
		result = prime * result + ((dayEditPlanEndDate == null) ? 0 : dayEditPlanEndDate.hashCode());
		result = prime * result + ((dayEditGroupId == null) ? 0 : dayEditGroupId.hashCode());
		result = prime * result + ((monthEditPlanStartDate == null) ? 0 : monthEditPlanStartDate.hashCode());
		result = prime * result + ((monthEditPlanEndDate == null) ? 0 : monthEditPlanEndDate.hashCode());
		result = prime * result + ((monthEditGroupId == null) ? 0 : monthEditGroupId.hashCode());
		result = prime * result + ((dayProducePlanStartDate == null) ? 0 : dayProducePlanStartDate.hashCode());
		result = prime * result + ((dayProducePlanEndDate == null) ? 0 : dayProducePlanEndDate.hashCode());
		result = prime * result + ((monthProducePlanStartDate == null) ? 0 : monthProducePlanStartDate.hashCode());
		result = prime * result + ((monthProducePlanEndDate == null) ? 0 : monthProducePlanEndDate.hashCode());
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
		InforMan other = (InforMan) obj;
		if (inforId == null) {
			if (other.inforId != null)
				return false;
		} else if (!inforId.equals(other.inforId))
			return false;
		if (inforStatus == null) {
			if (other.inforStatus != null)
				return false;
		} else if (!inforStatus.equals(other.inforStatus))
			return false;
		if (descp == null) {
			if (other.descp != null)
				return false;
		} else if (!descp.equals(other.descp))
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
		if (collectGroupId == null) {
			if (other.collectGroupId != null)
				return false;
		} else if (!collectGroupId.equals(other.collectGroupId))
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
		if (dayEditGroupId == null) {
			if (other.dayEditGroupId != null)
				return false;
		} else if (!dayEditGroupId.equals(other.dayEditGroupId))
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
		return true;
	}
	
	
	
}
