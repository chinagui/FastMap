package com.navinfo.dataservice.dao.fcc.check.model;

/** 
 * @ClassName: CheckTask.java
 * @author y
 * @date 2017-5-31 上午10:31:20
 * @Description: 质检任务信息model
 *  
 */
public class CheckTask {
	
	private int taskId;
	private String taskName;
	private String subTaskName;
	private String wokerInfo;
	private String checkInfo;
	private String workGroup;
	private int workTotalCount;
	private int checkTotalCount;
	private int checkStatus;
	/**
	 * @return the taskId
	 */
	public int getTaskId() {
		return taskId;
	}
	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	/**
	 * @return the taskName
	 */
	public String getTaskName() {
		return taskName;
	}
	/**
	 * @param taskName the taskName to set
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	/**
	 * @return the subTaskName
	 */
	public String getSubTaskName() {
		return subTaskName;
	}
	/**
	 * @param subTaskName the subTaskName to set
	 */
	public void setSubTaskName(String subTaskName) {
		this.subTaskName = subTaskName;
	}
	/**
	 * @return the wokerInfo
	 */
	public String getWokerInfo() {
		return wokerInfo;
	}
	/**
	 * @param wokerInfo the wokerInfo to set
	 */
	public void setWokerInfo(String wokerInfo) {
		this.wokerInfo = wokerInfo;
	}
	/**
	 * @return the checkInfo
	 */
	public String getCheckInfo() {
		return checkInfo;
	}
	/**
	 * @param checkInfo the checkInfo to set
	 */
	public void setCheckInfo(String checkInfo) {
		this.checkInfo = checkInfo;
	}
	/**
	 * @return the workGroup
	 */
	public String getWorkGroup() {
		return workGroup;
	}
	/**
	 * @param workGroup the workGroup to set
	 */
	public void setWorkGroup(String workGroup) {
		this.workGroup = workGroup;
	}
	/**
	 * @return the workTotalCount
	 */
	public int getWorkTotalCount() {
		return workTotalCount;
	}
	/**
	 * @param workTotalCount the workTotalCount to set
	 */
	public void setWorkTotalCount(int workTotalCount) {
		this.workTotalCount = workTotalCount;
	}
	/**
	 * @return the checkTotalCount
	 */
	public int getCheckTotalCount() {
		return checkTotalCount;
	}
	/**
	 * @param checkTotalCount the checkTotalCount to set
	 */
	public void setCheckTotalCount(int checkTotalCount) {
		this.checkTotalCount = checkTotalCount;
	}
	/**
	 * @return the checkStatus
	 */
	public int getCheckStatus() {
		return checkStatus;
	}
	/**
	 * @param checkStatus the checkStatus to set
	 */
	public void setCheckStatus(int checkStatus) {
		this.checkStatus = checkStatus;
	}
	
	
	
	

}
