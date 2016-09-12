package com.navinfo.dataservice.api.man.model.subtask;

import java.sql.Timestamp;
import java.util.List;


/** 
 * @ClassName: SubtaskList
 * @author songdongyan
 * @date 2016年8月3日
 * @Description: SubtaskList.java
 */
public class SubtaskList extends SubtaskListByUser {

	/**
	 * 
	 */
	public SubtaskList() {
		// TODO Auto-generated constructor stub
	}
	
	private Integer createUserId ;
	private Integer exeUserId ;
	private String createDate;
	private Integer groupId;

	private Integer blockId;
	private String blockName;
	
	private Integer taskId ;
	private String taskName;
	
	public SubtaskList (Integer subtaskId ,
			String name,
			Integer status,
			String descp,
			Integer stage,
			Integer type,
			List<Integer> gridIds,
			String geometry,
			Integer createUserId,
			Integer exeUserId,
			String createDate,
			String planStartDate,
			String planEndDate,
			Integer dbId,
			Integer groupId,
			Integer blockId,
			String blockName,
			Integer taskId,
			String taskName
			){
		super(subtaskId, name, stage, type, status, descp, dbId,gridIds,geometry,planStartDate,planEndDate);

		this.createUserId=createUserId ;
		this.exeUserId=exeUserId ;
		this.createDate=createDate ;
		this.groupId = groupId;

		this.blockId=blockId ;
		this.blockName = blockName;
		
		this.taskId=taskId ;
		this.taskName = taskName;

	}
	public int getGroupId(){
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
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
	public Integer getCreateUserId() {
		if(null==createUserId){return 0;}
		return createUserId;
	}
	public void setCreateUserId(Integer createUserId) {
		this.createUserId = createUserId;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public Integer getExeUserId() {
		if(null==exeUserId){return 0;}
		return exeUserId;
	}
	public void setExeUserId(Integer exeUserId) {
		this.exeUserId = exeUserId;
	}

}
