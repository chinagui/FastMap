
package com.navinfo.dataservice.api.man.model.subtask;

import java.util.List;


/** 
 * @ClassName: SubtaskList
 * @author songdongyan
 * @param <JSONObject>
 * @date 2016年8月3日
 * @Description: SubtaskList.java
 */
public class SubtaskQuery<JSONObject> extends SubtaskListByUser {

	/**
	 * 
	 */
	public SubtaskQuery() {
		// TODO Auto-generated constructor stub
	}
	private Integer executerId;
	private String  executer;

	private Integer blockId;
	private Integer blockManId;
	private String blockManName;
	
	private Integer cityId ;
	private Integer taskId ;
	private String taskName;
	
	private String version;
	private int percent;
	
	private JSONObject geometryJSON;
	
	public SubtaskQuery (Integer subtaskId ,
			String name,
			Integer status,
			String descp,
			Integer stage,
			Integer type,
			List<Integer> gridIds,
			String geometry,
			String planStartDate,
			String planEndDate,
			Integer dbId,
			Integer blockId,
			Integer blockManId,
			String blockManName,
			Integer cityId,
			Integer taskId,
			String taskName,
			String executer,
			Integer executerId,
			Integer percent,
			String version,
			JSONObject geometryJSON
			){
		super(subtaskId, name, stage, type, status, descp, dbId,gridIds,geometry,planStartDate,planEndDate);

		this.setExecuterId(executerId);
		this.executer = executer;

		this.blockId=blockId ;
		this.blockManId=blockManId ;
		this.blockManName = blockManName;
		
		this.cityId=cityId ;
		this.taskId=taskId ;
		this.taskName = taskName;

		this.percent=percent ;
		this.version = version;
		
		this.geometryJSON = geometryJSON;
	}
	public String getTaskName(){
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Integer getBlockId() {
		if(null==blockId){return 0;}
		return blockId;
	}
	public void setBlockId(Integer blockId) {
		this.blockId = blockId;
	}
	public Integer getTaskId() {
		if(null==taskId){return 0;}
		return taskId;
	}
	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	public String getExecuter() {
		return executer;
	}
	public void setExecuter(String executer) {
		this.executer = executer;
	}
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
	/**
	 * @return the blockManId
	 */
	public Integer getBlockManId() {
		return blockManId;
	}
	/**
	 * @param blockManId the blockManId to set
	 */
	public void setBlockManId(Integer blockManId) {
		this.blockManId = blockManId;
	}
	/**
	 * @return the blockManName
	 */
	public String getBlockManName() {
		return blockManName;
	}
	/**
	 * @param blockManName the blockManName to set
	 */
	public void setBlockManName(String blockManName) {
		this.blockManName = blockManName;
	}
	/**
	 * @return the cityId
	 */
	public Integer getCityId() {
		return cityId;
	}
	/**
	 * @param cityId the cityId to set
	 */
	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}
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


}
