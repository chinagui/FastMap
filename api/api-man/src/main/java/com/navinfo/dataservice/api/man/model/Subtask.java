package com.navinfo.dataservice.api.man.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/** 
* @ClassName:  Subtask 
* @author code generator
 * @param <JSONObject>
* @date 2016-06-06 07:40:15 
* @Description: TODO
*/
/**
 * (修改)增加质检子任务字段(第七迭代)
 * @author zhangli5174
 *
 */
public class Subtask implements Serializable{
	private Integer subtaskId ;
	private String name ;
//	private Integer blockId;
//	private Integer blockManId;
//	private Integer cityId ;
	private Integer taskId ;
	private String geometry ;
	private int stage ;
	private Integer type ;
	private Integer createUserId ;
	private Timestamp createDate ;
	private int exeUserId ;
	private Integer exeGroupId ;
	private Integer status ;
	private Timestamp planStartDate ;
	private Timestamp planEndDate;
	private String descp ;
	private Map<Integer,Integer> gridIds = new HashMap<Integer,Integer>();
	private Integer dbId ;
	private int groupId;
//	private String blockManName;
//	private String taskName;
	private String version;
	private Integer executerId;
	private String executer;
	private int percent;
	private JSONObject geometryJSON;

	private Integer qualitySubtaskId ;
	private Integer isQuality;
	private Integer qualityExeUserId ;
	private String qualityExeUserName;
	private Timestamp qualityPlanStartDate ;
	private Timestamp qualityPlanEndDate;
	private Integer qualityTaskStatus;
	//外业不规则子任务圈
	private String referGeometry;
	private JSONObject referGeometryJSON;
	private JSONArray referSubtasks;
	private int referId;
	private int workKind;//0无1外业采集，2众包，3情报矢量，4多源
	//快线中线标识，4快线，1中线
	private int subType;
	//添加质检方式字段
	private int qualityMethod;
	
	private Map<String, Object> changeFields=new HashMap<String, Object>();
	
	public Map<String, Object> getChangeFields() {
		return changeFields;
	}

	public int getSubType() {
		return subType;
	}
	public void setSubType(int subType) {
		//changeFields.put("WORK_KIND", workKind);
		this.subType = subType;
	}
	public Subtask (){
	}
	public int getGroupId(){
		return groupId;
	}
	public void setGroupId(int groupId) {
		//changeFields.put("GROUP_ID", groupId);
		this.groupId = groupId;
	}
	public String getName(){
		if(null==name){return "";}
		return name;
	}
	public void setName(String name) {
		changeFields.put("NAME", name);
		this.name = name;
	}
	public int getDbId(){
		return dbId;
	}
	public void setDbId(int dbId) {
		this.dbId = dbId;
	}
	public Map<Integer,Integer> gridIdMap(){
		return gridIds;
	}
	public List<Integer> getGridIds(){
		List<Integer> list = new ArrayList<Integer> (); 
		if(gridIds!=null&&gridIds.size()!=0){
			list.addAll(gridIds.keySet());
		}
		return list;
	}
	public void setGridIds(Map<Integer,Integer> list) {
		this.gridIds = list;
	}
	
	public Integer getSubtaskId() {
		if(null==subtaskId){return 0;}
		return subtaskId;
	}
	public void setSubtaskId(Integer subtaskId) {
		changeFields.put("SUBTASK_ID", subtaskId);
		this.subtaskId = subtaskId;
	}
//	public Integer getBlockId() {
//		if(null==blockId){return 0;}
//		return blockId;
//	}
//	public void setBlockId(Integer blockId) {
//		this.blockId = blockId;
//	}
	public Integer getTaskId() {
		if(null==taskId){return 0;}
		return taskId;
	}
	public void setTaskId(Integer taskId) {
		changeFields.put("TASK_ID", taskId);
		this.taskId = taskId;
	}
	public String getGeometry() {
		if(null==geometry){return "";}
		return geometry;
	}
	public void setGeometry(String geometry) {
		changeFields.put("GEOMETRY", geometry);
		this.geometry = geometry;
	}
	public int getStage() {
		return stage;
	}
	public void setStage(int stage) {
		changeFields.put("STAGE", stage);
		this.stage = stage;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		changeFields.put("TYPE", type);
		this.type = type;
	}
	public Integer getCreateUserId() {
		if(null==createUserId){return 0;}
		return createUserId;
	}
	public void setCreateUserId(Integer createUserId) {
		changeFields.put("CREATE_USER_ID", createUserId);
		this.createUserId = createUserId;
	}
	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
		changeFields.put("CREATE_DATE", createDate);
		this.createDate = createDate;
	}
	public int getExeUserId() {
		return exeUserId;
	}
	public void setExeUserId(int exeUserId) {
		changeFields.put("EXE_USER_ID", exeUserId);
		this.exeUserId = exeUserId;
	}
	public Integer getExeGroupId() {
		if(null==exeGroupId){return 0;}
		return exeGroupId;
	}
	public void setExeGroupId(Integer exeGroupId) {
		changeFields.put("EXE_GROUP_ID", exeGroupId);
		this.exeGroupId = exeGroupId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		changeFields.put("STATUS", status);
		this.status = status;
	}
	public Timestamp getPlanStartDate() {
		return planStartDate;
	}
	public void setPlanStartDate(Timestamp planStartDate) {
		changeFields.put("PLAN_START_DATE", planStartDate);
		this.planStartDate = planStartDate;
	}
	public Timestamp getPlanEndDate() {
		return planEndDate;
	}
	public void setPlanEndDate(Timestamp planEndDate) {
		changeFields.put("PLAN_END_DATE", planEndDate);
		this.planEndDate = planEndDate;
	}
	public String getDescp() {
		if(descp==null){return "";}
		return descp;
	}
	public void setDescp(String descp) {
		changeFields.put("DESCP", descp);
		this.descp = descp;
	}
	//***************zl 2016.11.03*****************
	//新增质检子任务字段
	public Integer getQualitySubtaskId() {
		return qualitySubtaskId;
	}

	public void setQualitySubtaskId(Integer qualitySubtaskId) {
		changeFields.put("QUALITY_SUBTASK_ID", qualitySubtaskId);
		this.qualitySubtaskId = qualitySubtaskId;
	}
	//是否是质检子任务
	public Integer getIsQuality() {
		return isQuality;
	}

	public void setIsQuality(Integer isQuality) {
		changeFields.put("IS_QUALITY", isQuality);
		this.isQuality = isQuality;
	}
	//增质检子任务执行人字段(只在实体中使用,数据库表中无此字段)
	public Integer getQualityExeUserId() {
		return qualityExeUserId;
	}
	public void setQualityExeUserId(Integer qualityExeUserId) {
		this.qualityExeUserId = qualityExeUserId;
	}
	public String getQualityExeUserName() {
		return qualityExeUserName;
	}
	public void setQualityExeUserName(String qualityExeUserName) {
		this.qualityExeUserName = qualityExeUserName;
	}
	//增质检子任务计划开始时间字段(只在实体中使用,数据库表中无此字段)
	public Timestamp getQualityPlanStartDate() {
		return qualityPlanStartDate;
	}
	public void setQualityPlanStartDate(Timestamp qualityPlanStartDate) {
		this.qualityPlanStartDate = qualityPlanStartDate;
	}
	//增质检子任务计划结束时间字段(只在实体中使用,数据库表中无此字段)
	public Timestamp getQualityPlanEndDate() {
		return qualityPlanEndDate;
	}
	public void setQualityPlanEndDate(Timestamp qualityPlanEndDate) {
		this.qualityPlanEndDate = qualityPlanEndDate;
	}
	//增加质检子任务任务状态
	public Integer getQualityTaskStatus() {
		return qualityTaskStatus;
	}
	public void setQualityTaskStatus(Integer qualityTaskStatus) {
		this.qualityTaskStatus = qualityTaskStatus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
//	@Override
//	public String toString() {
//		return "Subtask ["
//				+ "subtaskId=" + subtaskId
//				+ ",name=" + this.getName() 
//				+",blockId="+this.getBlockId()
//				+",taskId="+this.getTaskId()
//				+",geometry="+geometry
//				+",stage="+stage
//				+",type="+type
//				+",createUserId="+createUserId
//				+",createDate="+createDate
//				+",exeUserId="+exeUserId
//				+",status="+status
//				+ ",dbId=" + dbId
//				+ ",groupId=" + groupId
//				+",descp="+descp+"]";
//	}


//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((subtaskId == null) ? 0 : subtaskId.hashCode());
//		result = prime * result + ((name == null) ? 0 : name.hashCode());
//		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
//		result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
//		result = prime * result + ((geometry == null) ? 0 : geometry.hashCode());
//		result = prime * result + ((stage == null) ? 0 : stage.hashCode());
//		result = prime * result + ((type == null) ? 0 : type.hashCode());
//		result = prime * result + ((createUserId == null) ? 0 : createUserId.hashCode());
//		result = prime * result + ((createDate == null) ? 0 : createDate.hashCode());
//		result = prime * result + ((exeUserId == null) ? 0 : exeUserId.hashCode());
//		result = prime * result + ((status == null) ? 0 : status.hashCode());
//		result = prime * result + ((planStartDate == null) ? 0 : planStartDate.hashCode());
//		result = prime * result + ((planEndDate == null) ? 0 : planEndDate.hashCode());
//		result = prime * result + ((dbId == null) ? 0 : dbId.hashCode());
//		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
//		result = prime * result + ((gridIds == null) ? 0 : gridIds.hashCode());
//		result = prime * result + ((descp == null) ? 0 : descp.hashCode());
//		return result;
//	}
//
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Subtask other = (Subtask) obj;
//		if (subtaskId == null) {
//			if (other.subtaskId != null)
//				return false;
//		} else if (!subtaskId.equals(other.subtaskId))
//			return false;
//		if (name == null) {
//			if (other.name != null)
//				return false;
//		} else if (!name.equals(other.name))
//			return false;
//		if (blockId == null) {
//			if (other.blockId != null)
//				return false;
//		} else if (!blockId.equals(other.blockId))
//			return false;
//		if (taskId == null) {
//			if (other.taskId != null)
//				return false;
//		} else if (!taskId.equals(other.taskId))
//			return false;
//		if (geometry == null) {
//			if (other.geometry != null)
//				return false;
//		} else if (!geometry.equals(other.geometry))
//			return false;
//		if (stage == null) {
//			if (other.stage != null)
//				return false;
//		} else if (!stage.equals(other.stage))
//			return false;
//		if (type == null) {
//			if (other.type != null)
//				return false;
//		} else if (!type.equals(other.type))
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
//		if (exeUserId == null) {
//			if (other.exeUserId != null)
//				return false;
//		} else if (!exeUserId.equals(other.exeUserId))
//			return false;
//		if (status == null) {
//			if (other.status != null)
//				return false;
//		} else if (!status.equals(other.status))
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
//		if (dbId == null) {
//			if (other.dbId != null)
//				return false;
//		} else if (!dbId.equals(other.dbId))
//			return false;
//		if (groupId == null) {
//			if (other.groupId != null)
//				return false;
//		} else if (!groupId.equals(other.groupId))
//			return false;
//		if (gridIds == null) {
//			if (other.gridIds != null)
//				return false;
//		} else if (!gridIds.equals(other.gridIds))
//			return false;
//		if (descp == null) {
//			if (other.descp != null)
//				return false;
//		} else if (!descp.equals(other.descp))
//			return false;
//		return true;
//	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the executer
	 */
	public String getExecuter() {
		return executer;
	}

	/**
	 * @param executer the executer to set
	 */
	public void setExecuter(String executer) {
		this.executer = executer;
	}

	/**
	 * @return the percent
	 */
	public int getPercent() {
		return percent;
	}

	/**
	 * @param percent the percent to set
	 */
	public void setPercent(int percent) {
		this.percent = percent;
	}

	/**
	 * @return the executerId
	 */
	public Integer getExecuterId() {
		return executerId;
	}

	/**
	 * @param executerId the executerId to set
	 */
	public void setExecuterId(Integer executerId) {
		this.executerId = executerId;
	}

//	/**
//	 * @return the blockManId
//	 */
//	public Integer getBlockManId() {
//		return blockManId;
//	}
//
//	/**
//	 * @param blockManId the blockManId to set
//	 */
//	public void setBlockManId(Integer blockManId) {
//		this.blockManId = blockManId;
//	}
//
//	/**
//	 * @return the blockManName
//	 */
//	public String getBlockManName() {
//		return blockManName;
//	}
//
//	/**
//	 * @param blockManName the blockManName to set
//	 */
//	public void setBlockManName(String blockManName) {
//		this.blockManName = blockManName;
//	}
//
//	/**
//	 * @return the cityId
//	 */
//	public Integer getCityId() {
//		return cityId;
//	}
//
//	/**
//	 * @param cityId the cityId to set
//	 */
//	public void setCityId(Integer cityId) {
//		this.cityId = cityId;
//	}

	/**
	 * @return the geometryJSON
	 */
	public JSONObject getGeometryJSON() {
		return geometryJSON;
	}

	/**
	 * @param geometryJSON the geometryJSON to set
	 */
	public void setGeometryJSON(JSONObject geometryJSON) {
		this.geometryJSON = geometryJSON;
	}

	/**
	 * @return the referGeometryJSON
	 */
	public JSONObject getReferGeometryJSON() {
		return referGeometryJSON;
	}
	/**
	 * @param referGeometryJSON the referGeometryJSON to set
	 */
	public void setReferGeometryJSON(JSONObject referGeometryJSON) {
		this.referGeometryJSON = referGeometryJSON;
	}
	/**
	 * @return the referSubtasks
	 */
	public JSONArray getReferSubtasks() {
		return referSubtasks;
	}
	/**
	 * @param referSubtasks the referSubtasks to set
	 */
	public void setReferSubtasks(JSONArray referSubtasks) {
		this.referSubtasks = referSubtasks;
	}
	public int getReferId() {
		return referId;
	}
	public void setReferId(int referId) {
		changeFields.put("REFER_ID", referId);
		this.referId = referId;
	}
	public int getWorkKind() {
		return workKind;
	}
	public void setWorkKind(int workKind) {
		changeFields.put("WORK_KIND", workKind);
		this.workKind = workKind;
	}
	
	public int getQualityMethod() {
		return qualityMethod;
	}
	public void setQualityMethod(int qualityMethod) {
		changeFields.put("QUALITY_METHOD", qualityMethod);
		this.qualityMethod = qualityMethod;
	}
}