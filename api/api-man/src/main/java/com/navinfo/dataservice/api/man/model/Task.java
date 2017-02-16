package com.navinfo.dataservice.api.man.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;

import net.sf.json.JSONObject;


/** 
* @ClassName:  Task 
* @author code generator
* @date 2016-06-06 06:12:30 
* @Description: TODO
*/
public class Task implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Integer taskId ;
	private String name;
	private Integer blockId=0 ;
	private String blockName;
	private Integer regionId=0;
	private Integer programId=0;
	private String programName;
	private String version;
	private Integer createUserId ;
	private String createUserName;
	private Timestamp createDate ;
	private Integer status ;
	private String descp ;
	private Timestamp planStartDate ;
	private Timestamp planEndDate ;
	private Integer type;
	private Integer programType;
	private Timestamp producePlanStartDate ;
	private Timestamp producePlanEndDate ;
	private Integer lot ;
	private Integer groupId;
	private String groupName;
	private Integer roadPlanTotal ;
	private Integer poiPlanTotal ;
	private Integer latest ;
	private Integer groupLeader =0; 
	private Integer workProperty ;
	
	private JSONObject geometry;
	private Map<Integer,Integer> gridIds;
	
	public Task (){
	}

	
	public Integer getTaskId() {
		return taskId;
	}
	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
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
		if(null==descp){return "";}
		return descp;
	}
	public void setDescp(String descp) {
		if(null==descp){this.descp="";}
		this.descp = descp;
	}
	public Timestamp getPlanEndDate() {
		return planEndDate;
	}
	public void setPlanEndDate(Timestamp collectPlanEndDate) {
		this.planEndDate = collectPlanEndDate;
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
//	@Override
//	public String toString() {
//		return "Task [taskId=" + taskId +",cityId="+cityId+",createUserId="+createUserId+",createDate="+createDate+",status="+taskStatus+",descp="+taskDescp+",planStartDate="+planStartDate+",planEndDate="+planEndDate+",monthEditPlanStartDate="+monthEditPlanStartDate+",monthEditPlanEndDate="+monthEditPlanEndDate+",monthEditGroupId="+monthEditGroupId+",latest="+latest+"]";
//	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
//		result = prime * result + ((cityId == null) ? 0 : cityId.hashCode());
//		result = prime * result + ((createUserId == null) ? 0 : createUserId.hashCode());
//		result = prime * result + ((createDate == null) ? 0 : createDate.hashCode());
//		result = prime * result + ((taskStatus == null) ? 0 : taskStatus.hashCode());
//		result = prime * result + ((taskDescp == null) ? 0 : taskDescp.hashCode());
//		result = prime * result + ((planStartDate == null) ? 0 : planStartDate.hashCode());
//		result = prime * result + ((planEndDate == null) ? 0 : planEndDate.hashCode());
//		result = prime * result + ((monthEditPlanStartDate == null) ? 0 : monthEditPlanStartDate.hashCode());
//		result = prime * result + ((monthEditPlanEndDate == null) ? 0 : monthEditPlanEndDate.hashCode());
//		result = prime * result + ((monthEditGroupId == null) ? 0 : monthEditGroupId.hashCode());
//		result = prime * result + ((latest == null) ? 0 : latest.hashCode());
//		return result;
//	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Task other = (Task) obj;
//		if (taskId == null) {
//			if (other.taskId != null)
//				return false;
//		} else if (!taskId.equals(other.taskId))
//			return false;
//		if (cityId == null) {
//			if (other.cityId != null)
//				return false;
//		} else if (!cityId.equals(other.cityId))
//			return false;
//		if (createUserId == null) {
//			if (other.createUserId != null)
//				return false;
//		} else if (!createUserId.equals(other.createUserId))
//			return false;
//		if (createDate == null) {
//			if (other.createDate != null)
//				return false;
//		} else if (!createDate.equals(other.createDate))
//			return false;
//		if (taskStatus == null) {
//			if (other.taskStatus != null)
//				return false;
//		} else if (!taskStatus.equals(other.taskStatus))
//			return false;
//		if (taskDescp == null) {
//			if (other.taskDescp != null)
//				return false;
//		} else if (!taskDescp.equals(other.taskDescp))
//			return false;
//		if (planStartDate == null) {
//			if (other.planStartDate != null)
//				return false;
//		} else if (!planStartDate.equals(other.planStartDate))
//			return false;
//		if (planEndDate == null) {
//			if (other.planEndDate != null)
//				return false;
//		} else if (!planEndDate.equals(other.planEndDate))
//			return false;
//		
//		if (monthEditPlanStartDate == null) {
//			if (other.monthEditPlanStartDate != null)
//				return false;
//		} else if (!monthEditPlanStartDate.equals(other.monthEditPlanStartDate))
//			return false;
//		if (monthEditPlanEndDate == null) {
//			if (other.monthEditPlanEndDate != null)
//				return false;
//		} else if (!monthEditPlanEndDate.equals(other.monthEditPlanEndDate))
//			return false;
//		if (monthEditGroupId == null) {
//			if (other.monthEditGroupId != null)
//				return false;
//		} else if (!monthEditGroupId.equals(other.monthEditGroupId))
//			return false;
//		if (latest == null) {
//			if (other.latest != null)
//				return false;
//		} else if (!latest.equals(other.latest))
//			return false;
//		return true;
//	}
	public String getName() {
		if(null==name){return "";}
		return name;
	}
	public void setName(String name) {
		if(null==name){this.name="";}
		this.name = name;
	}
	public Timestamp getPlanStartDate() {
		return planStartDate;
	}
	public void setPlanStartDate(Timestamp planStartDate) {
		this.planStartDate = planStartDate;
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


	/**
	 * @return the blockId
	 */
	public Integer getBlockId() {
		return blockId;
	}


	/**
	 * @param blockId the blockId to set
	 */
	public void setBlockId(Integer blockId) {
		this.blockId = blockId;
	}


	/**
	 * @return the regionId
	 */
	public Integer getRegionId() {
		return regionId;
	}


	/**
	 * @param regionId the regionId to set
	 */
	public void setRegionId(Integer regionId) {
		this.regionId = regionId;
	}


	/**
	 * @return the type
	 */
	public Integer getType() {
		return type;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(Integer type) {
		this.type = type;
	}


	/**
	 * @return the producePlanStartDate
	 */
	public Timestamp getProducePlanStartDate() {
		return producePlanStartDate;
	}


	/**
	 * @param producePlanStartDate the producePlanStartDate to set
	 */
	public void setProducePlanStartDate(Timestamp producePlanStartDate) {
		this.producePlanStartDate = producePlanStartDate;
	}


	/**
	 * @return the producePlanEndDate
	 */
	public Timestamp getProducePlanEndDate() {
		return producePlanEndDate;
	}


	/**
	 * @param producePlanEndDate the producePlanEndDate to set
	 */
	public void setProducePlanEndDate(Timestamp producePlanEndDate) {
		this.producePlanEndDate = producePlanEndDate;
	}


	/**
	 * @return the lot
	 */
	public Integer getLot() {
		return lot;
	}


	/**
	 * @param lot the lot to set
	 */
	public void setLot(Integer lot) {
		this.lot = lot;
	}


	/**
	 * @return the groupId
	 */
	public Integer getGroupId() {
		return groupId;
	}


	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}


	/**
	 * @return the roadPlanTotal
	 */
	public Integer getRoadPlanTotal() {
		return roadPlanTotal;
	}


	/**
	 * @param roadPlanTotal the roadPlanTotal to set
	 */
	public void setRoadPlanTotal(Integer roadPlanTotal) {
		this.roadPlanTotal = roadPlanTotal;
	}


	/**
	 * @return the poiPlanTotal
	 */
	public Integer getPoiPlanTotal() {
		return poiPlanTotal;
	}


	/**
	 * @param poiPlanTotal the poiPlanTotal to set
	 */
	public void setPoiPlanTotal(Integer poiPlanTotal) {
		this.poiPlanTotal = poiPlanTotal;
	}


	/**
	 * @return the programId
	 */
	public Integer getProgramId() {
		return programId;
	}


	/**
	 * @param programId the programId to set
	 */
	public void setProgramId(Integer programId) {
		this.programId = programId;
	}


	/**
	 * @return the groupLeader
	 */
	public Integer getGroupLeader() {
		return groupLeader;
	}


	/**
	 * @param groupLeader the groupLeader to set
	 */
	public void setGroupLeader(Integer groupLeader) {
		this.groupLeader = groupLeader;
	}


	/**
	 * @return the blockName
	 */
	public String getBlockName() {
		return blockName;
	}


	/**
	 * @param blockName the blockName to set
	 */
	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}


	/**
	 * @return the programName
	 */
	public String getProgramName() {
		return programName;
	}


	/**
	 * @param programName the programName to set
	 */
	public void setProgramName(String programName) {
		this.programName = programName;
	}


	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}


	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}


	/**
	 * @return the workProperty
	 */
	public Integer getWorkProperty() {
		return workProperty;
	}


	/**
	 * @param workProperty the workProperty to set
	 */
	public void setWorkProperty(Integer workProperty) {
		this.workProperty = workProperty;
	}


	/**
	 * @return the programType
	 */
	public Integer getProgramType() {
		return programType;
	}


	/**
	 * @param programType the programType to set
	 */
	public void setProgramType(Integer programType) {
		this.programType = programType;
	}


	/**
	 * @return the geometry
	 */
	public JSONObject getGeometry() {
		return geometry;
	}


	/**
	 * @param geometry the geometry to set
	 */
	public void setGeometry(JSONObject geometry) {
		this.geometry = geometry;
	}


	/**
	 * @return the gridIds
	 */
	public Map<Integer,Integer> getGridIds() {
		return gridIds;
	}


	/**
	 * @param gridIds the gridIds to set
	 */
	public void setGridIds(Map<Integer,Integer> gridIds) {
		this.gridIds = gridIds;
	}

}
