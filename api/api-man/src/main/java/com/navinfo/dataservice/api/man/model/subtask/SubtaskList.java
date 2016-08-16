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
	private Timestamp createDate;
	private Integer groupId;

	private Integer blockId;
	private String blockName;
	private Timestamp blockCollectPlanStartDate;
	private Timestamp blockCollectPlanEndDate;
	private Timestamp blockDayEditPlanStartDate;
	private Timestamp blockDayEditPlanEndDate;
	private Timestamp blockCMonthEditPlanStartDate;
	private Timestamp blockCMonthEditPlanEndDate;
	
	private Integer taskId ;
	private String taskName;
	private Timestamp taskCMonthEditPlanStartDate;
	private Timestamp taskCMonthEditPlanEndDate;
	
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
			Timestamp createDate,
			Timestamp planStartDate,
			Timestamp planEndDate,
			Integer dbId,
			Integer groupId,
			Integer blockId,
			String blockName,
			Timestamp blockCollectPlanStartDate,
			Timestamp blockCollectPlanEndDate,
			Timestamp blockDayEditPlanStartDate,
			Timestamp blockDayEditPlanEndDate,
			Timestamp blockCMonthEditPlanStartDate,
			Timestamp blockCMonthEditPlanEndDate,
			Integer taskId,
			String taskName,
			Timestamp taskCMonthEditPlanStartDate,
			Timestamp taskCMonthEditPlanEndDate
			){
		super(subtaskId, name, stage, type, status, descp, dbId,gridIds,geometry,planStartDate,planEndDate);

		this.createUserId=createUserId ;
		this.exeUserId=exeUserId ;
		this.createDate=createDate ;
		this.groupId = groupId;

		this.blockId=blockId ;
		this.blockName = blockName;
		this.blockCollectPlanStartDate = blockCollectPlanStartDate;
		this.blockCollectPlanEndDate = blockCollectPlanEndDate;
		this.blockDayEditPlanStartDate = blockDayEditPlanStartDate;
		this.blockDayEditPlanEndDate = blockDayEditPlanEndDate;
		this.blockCMonthEditPlanStartDate = blockCMonthEditPlanStartDate;
		this.blockCMonthEditPlanEndDate = blockCMonthEditPlanEndDate;	
		
		this.taskId=taskId ;
		this.taskName = taskName;
		this.taskCMonthEditPlanStartDate = taskCMonthEditPlanStartDate;
		this.taskCMonthEditPlanEndDate = taskCMonthEditPlanEndDate;

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
	public Timestamp getBlockCollectPlanStartDate(){
		return blockCollectPlanStartDate;
	}
	public void setBlockCollectPlanStartDate(Timestamp blockCollectPlanStartDate) {
		this.blockCollectPlanStartDate = blockCollectPlanStartDate;
	}
	public Timestamp getBlockCollectPlanEndDate(){
		return blockCollectPlanEndDate;
	}
	public void setBlockCollectPlanEndDate(Timestamp blockCollectPlanEndDate) {
		this.blockCollectPlanEndDate = blockCollectPlanEndDate;
	}
	public Timestamp getBlockDayEditPlanStartDate(){
		return blockDayEditPlanStartDate;
	}
	public void setBlockDayEditPlanStartDate(Timestamp blockDayEditPlanStartDate) {
		this.blockDayEditPlanStartDate = blockDayEditPlanStartDate;
	}
	public Timestamp getBlockDayEditPlanEndDate(){
		return blockDayEditPlanEndDate;
	}
	public void setBlockDayEditPlanEndDate(Timestamp blockDayEditPlanEndDate) {
		this.blockDayEditPlanEndDate = blockDayEditPlanEndDate;
	}
	public Timestamp getBlockCMonthEditPlanStartDate(){
		return blockCMonthEditPlanStartDate;
	}
	public void setBlockCMonthEditPlanStartDate(Timestamp blockCMonthEditPlanStartDate) {
		this.blockCMonthEditPlanStartDate = blockCMonthEditPlanStartDate;
	}
	public Timestamp getBlockCMonthEditPlanEndDate(){
		return blockCMonthEditPlanEndDate;
	}
	public void setBlockCMonthEditPlanEndDate(Timestamp blockCMonthEditPlanEndDate) {
		this.blockCMonthEditPlanEndDate = blockCMonthEditPlanEndDate;
	}
	public Timestamp getTaskCMonthEditPlanStartDate(){
		return taskCMonthEditPlanStartDate;
	}
	public void setTaskCMonthEditPlanStartDate(Timestamp taskCMonthEditPlanStartDate) {
		this.taskCMonthEditPlanStartDate = taskCMonthEditPlanStartDate;
	}
	public Timestamp getTaskCMonthEditPlanEndDate(){
		return taskCMonthEditPlanEndDate;
	}
	public void setTaskCMonthEditPlanEndDate(Timestamp taskCMonthEditPlanEndDate) {
		this.taskCMonthEditPlanEndDate = taskCMonthEditPlanEndDate;
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
	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
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
