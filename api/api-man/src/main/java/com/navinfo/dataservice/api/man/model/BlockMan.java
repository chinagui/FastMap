package com.navinfo.dataservice.api.man.model;

/** 
* @ClassName:  BlockMan 
* @author code generator
* @date 2016-06-14 09:00:16 
* @Description: TODO
*/
public class BlockMan  {
	private Integer blockManId ;
	private String blockManName;
	private Integer blockId ;
	private Integer status ;
	private Integer latest ;
	private String descp ;
	private Integer createUserId ;
	private Object createDate ;
	private Object collectPlanStartDate ;
	private Object collectPlanEndDate ;
	private Integer collectGroupId ;
	private Object dayEditPlanStartDate ;
	private Object dayEditPlanEndDate ;
	private Integer dayEditGroupId ;
	private Object monthEditPlanStartDate ;
	private Object monthEditPlanEndDate ;
	private Integer monthEditGroupId ;
	private Object dayProducePlanStartDate ;
	private Object dayProducePlanEndDate ;
	private Object monthProducePlanStartDate ;
	private Object monthProducePlanEndDate ;
	private Integer roadPlanTotal;
	private Integer poiPlanTotal; 
	
	public BlockMan (){
	}
	
	public BlockMan (Integer blockManId ,String blockManName,Integer blockId,Integer status,Integer latest,String descp,Integer createUserId,Object createDate,Object collectPlanStartDate,Object collectPlanEndDate,Integer collectGroupId,Object dayEditPlanStartDate,Object dayEditPlanEndDate,Integer dayEditGroupId,Object monthEditPlanStartDate,Object monthEditPlanEndDate,Integer monthEditGroupId,Object dayProducePlanStartDate,Object dayProducePlanEndDate,Object monthProducePlanStartDate,Object monthProducePlanEndDate){
		this.blockManId=blockManId ;
		this.blockManName=blockManName;
		this.blockId=blockId ;
		this.status=status ;
		this.latest=latest ;
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
	public Integer getBlockManId() {
		return blockManId;
	}
	public void setBlockManId(Integer blockManId) {
		this.blockManId = blockManId;
	}
	public Integer getBlockId() {
		return blockId;
	}
	public void setBlockId(Integer blockId) {
		this.blockId = blockId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getLatest() {
		return latest;
	}
	public void setLatest(Integer latest) {
		this.latest = latest;
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
	public Object getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Object createDate) {
		this.createDate = createDate;
	}
	public Object getCollectPlanStartDate() {
		return collectPlanStartDate;
	}
	public void setCollectPlanStartDate(Object collectPlanStartDate) {
		this.collectPlanStartDate = collectPlanStartDate;
	}
	public Object getCollectPlanEndDate() {
		return collectPlanEndDate;
	}
	public void setCollectPlanEndDate(Object collectPlanEndDate) {
		this.collectPlanEndDate = collectPlanEndDate;
	}
	public Integer getCollectGroupId() {
		return collectGroupId;
	}
	public void setCollectGroupId(Integer collectGroupId) {
		this.collectGroupId = collectGroupId;
	}
	public Object getDayEditPlanStartDate() {
		return dayEditPlanStartDate;
	}
	public void setDayEditPlanStartDate(Object dayEditPlanStartDate) {
		this.dayEditPlanStartDate = dayEditPlanStartDate;
	}
	public Object getDayEditPlanEndDate() {
		return dayEditPlanEndDate;
	}
	public void setDayEditPlanEndDate(Object dayEditPlanEndDate) {
		this.dayEditPlanEndDate = dayEditPlanEndDate;
	}
	public Integer getDayEditGroupId() {
		return dayEditGroupId;
	}
	public void setDayEditGroupId(Integer dayEditGroupId) {
		this.dayEditGroupId = dayEditGroupId;
	}
	public Object getMonthEditPlanStartDate() {
		return monthEditPlanStartDate;
	}
	public void setMonthEditPlanStartDate(Object monthEditPlanStartDate) {
		this.monthEditPlanStartDate = monthEditPlanStartDate;
	}
	public Object getMonthEditPlanEndDate() {
		return monthEditPlanEndDate;
	}
	public void setMonthEditPlanEndDate(Object monthEditPlanEndDate) {
		this.monthEditPlanEndDate = monthEditPlanEndDate;
	}
	public Integer getMonthEditGroupId() {
		return monthEditGroupId;
	}
	public void setMonthEditGroupId(Integer monthEditGroupId) {
		this.monthEditGroupId = monthEditGroupId;
	}
	public Object getDayProducePlanStartDate() {
		return dayProducePlanStartDate;
	}
	public void setDayProducePlanStartDate(Object dayProducePlanStartDate) {
		this.dayProducePlanStartDate = dayProducePlanStartDate;
	}
	public Object getDayProducePlanEndDate() {
		return dayProducePlanEndDate;
	}
	public void setDayProducePlanEndDate(Object dayProducePlanEndDate) {
		this.dayProducePlanEndDate = dayProducePlanEndDate;
	}
	public Object getMonthProducePlanStartDate() {
		return monthProducePlanStartDate;
	}
	public void setMonthProducePlanStartDate(Object monthProducePlanStartDate) {
		this.monthProducePlanStartDate = monthProducePlanStartDate;
	}
	public Object getMonthProducePlanEndDate() {
		return monthProducePlanEndDate;
	}
	public void setMonthProducePlanEndDate(Object monthProducePlanEndDate) {
		this.monthProducePlanEndDate = monthProducePlanEndDate;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BlockMan [blockManId=" + blockManId +",blockId="+blockId+",status="+status+",latest="+latest+",descp="+descp+",createUserId="+createUserId+",createDate="+createDate+",collectPlanStartDate="+collectPlanStartDate+",collectPlanEndDate="+collectPlanEndDate+",collectGroupId="+collectGroupId+",dayEditPlanStartDate="+dayEditPlanStartDate+",dayEditPlanEndDate="+dayEditPlanEndDate+",dayEditGroupId="+dayEditGroupId+",monthEditPlanStartDate="+monthEditPlanStartDate+",monthEditPlanEndDate="+monthEditPlanEndDate+",monthEditGroupId="+monthEditGroupId+",dayProducePlanStartDate="+dayProducePlanStartDate+",dayProducePlanEndDate="+dayProducePlanEndDate+",monthProducePlanStartDate="+monthProducePlanStartDate+",monthProducePlanEndDate="+monthProducePlanEndDate+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockManId == null) ? 0 : blockManId.hashCode());
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((latest == null) ? 0 : latest.hashCode());
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
		BlockMan other = (BlockMan) obj;
		if (blockManId == null) {
			if (other.blockManId != null)
				return false;
		} else if (!blockManId.equals(other.blockManId))
			return false;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (latest == null) {
			if (other.latest != null)
				return false;
		} else if (!latest.equals(other.latest))
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

	public String getBlockManName() {
		return blockManName;
	}

	public void setBlockManName(String blockManName) {
		this.blockManName = blockManName;
	}

	public Integer getRoadPlanTotal() {
		return roadPlanTotal;
	}

	public void setRoadPlanTotal(Integer roadPlanTotal) {
		this.roadPlanTotal = roadPlanTotal;
	}

	public Integer getPoiPlanTotal() {
		return poiPlanTotal;
	}

	public void setPoiPlanTotal(Integer poiPlanTotal) {
		this.poiPlanTotal = poiPlanTotal;
	}
	
	
	
}
