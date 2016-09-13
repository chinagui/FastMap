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
			String executer
			){
		super(subtaskId, name, stage, type, status, descp, dbId,gridIds,geometry,planStartDate,planEndDate);

		this.executer = executer;

		this.blockId=blockId ;
		this.blockName = blockName;
		
		this.taskId=taskId ;
		this.taskName = taskName;

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


}
