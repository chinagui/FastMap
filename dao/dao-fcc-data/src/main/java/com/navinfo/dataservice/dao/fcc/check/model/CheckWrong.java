package com.navinfo.dataservice.dao.fcc.check.model;

import java.util.Date;

/** 
 * @ClassName: CheckWrong.java
 * @author y
 * @date 2017-5-25 下午2:39:59
 * @Description:质检问题记录java模型
 *  
 */
public class CheckWrong {
	
	
	private String logId; //问题记录ID 后台生成
	private int checkTaskId; //质检任务号
	private String objectType; //tips类型
	private String objectId; //tips rowkey
	private int erType; //错误信息类型	
	private String quDesc; //问题描述
	private String reason; //问题原因
	private String erContent;//错误内容
	private String quRank; //错误等级
	private String worker; //作业员(姓名+id) 后台自动获取
	private String checker; //质检员  (姓名+id)
	private String workTime; //作业时间 后台自动获取
	private String checkTime; //质检时间  后台自动获取
	private int isPrefer; //是否倾向性
	
	
	
	/**
	 * @return the logId
	 */
	public String getLogId() {
		return logId;
	}
	/**
	 * @param logId the logId to set
	 */
	public void setLogId(String logId) {
		this.logId = logId;
	}
	/**
	 * @return the checkTaskId
	 */
	public int getCheckTaskId() {
		return checkTaskId;
	}
	/**
	 * @param checkTaskId the checkTaskId to set
	 */
	public void setCheckTaskId(int checkTaskId) {
		this.checkTaskId = checkTaskId;
	}
	
	/**
	 * @return the tipsCode
	 */
	public String getObjectType() {
		return objectType;
	}
	/**
	 * @param tipsCode the tipsCode to set
	 */
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	/**
	 * @return the tipsRowkey
	 */
	public String getObjectId() {
		return objectId;
	}
	/**
	 * @param tipsRowkey the tipsRowkey to set
	 */
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public int getErType() {
		return erType;
	}
	public void setErType(int erType) {
		this.erType = erType;
	}
	/**
	 * @return the quDesc
	 */
	public String getQuDesc() {
		return quDesc;
	}
	/**
	 * @param quDesc the quDesc to set
	 */
	public void setQuDesc(String quDesc) {
		this.quDesc = quDesc;
	}
	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}
	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}
	/**
	 * @return the erContent
	 */
	public String getErContent() {
		return erContent;
	}
	/**
	 * @param erContent the erContent to set
	 */
	public void setErContent(String erContent) {
		this.erContent = erContent;
	}
	/**
	 * @return the quRank
	 */
	public String getQuRank() {
		return quRank;
	}
	/**
	 * @param quRank the quRank to set
	 */
	public void setQuRank(String quRank) {
		this.quRank = quRank;
	}
	
	
	
	/**
	 * @return the workTime
	 */
	public String getWorkTime() {
		return workTime;
	}
	/**
	 * @param workTime the workTime to set
	 */
	public void setWorkTime(String workTime) {
		this.workTime = workTime;
	}
	/**
	 * @return the checkTime
	 */
	public String getCheckTime() {
		return checkTime;
	}
	/**
	 * @param checkTime the checkTime to set
	 */
	public void setCheckTime(String checkTime) {
		this.checkTime = checkTime;
	}
	/**
	 * @return the isPrefer
	 */
	public int getIsPrefer() {
		return isPrefer;
	}
	/**
	 * @param isPrefer the isPrefer to set
	 */
	public void setIsPrefer(int isPrefer) {
		this.isPrefer = isPrefer;
	}
	/**
	 * @return the worker
	 */
	public String getWorker() {
		return worker;
	}
	/**
	 * @param worker the worker to set
	 */
	public void setWorker(String worker) {
		this.worker = worker;
	}
	/**
	 * @return the checker
	 */
	public String getChecker() {
		return checker;
	}
	/**
	 * @param checker the checker to set
	 */
	public void setChecker(String checker) {
		this.checker = checker;
	}
	
	
	
	
	



}
