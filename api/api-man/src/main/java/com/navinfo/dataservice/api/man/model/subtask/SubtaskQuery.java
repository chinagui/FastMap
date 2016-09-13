
package com.navinfo.dataservice.api.man.model.subtask;

import java.util.List;


/** 
 * @ClassName: SubtaskList
 * @author songdongyan
 * @date 2016年8月3日
 * @Description: SubtaskList.java
 */
public class SubtaskQuery extends SubtaskListByUser {

	/**
	 * 
	 */
	public SubtaskQuery() {
		// TODO Auto-generated constructor stub
	}
	
	private String  executer;

	private Integer blockId;
	private String blockName;
	
	private Integer taskId ;
	private String taskName;
	
	private String version;
	private int percent;
	
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
			String blockName,
			Integer taskId,
			String taskName,
			String executer,
			Integer percent,
			String version
			){
		super(subtaskId, name, stage, type, status, descp, dbId,gridIds,geometry,planStartDate,planEndDate);

		this.executer = executer;

		this.blockId=blockId ;
		this.blockName = blockName;
		
		this.taskId=taskId ;
		this.taskName = taskName;

		this.percent=percent ;
		this.version = version;
	}
	public String getBlockName(){
		return blockName;
	}
	public void setBlockName(String blockName) {
		this.blockName = blockName;
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


}
