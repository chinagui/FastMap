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
	
	
	private String logId;
	private int checkTaskId;
	private String tipsCode;
	private String tipsRowkey;
	private String quDesc;
	private String reason;
	private String erContent;
	private String quRank;
	private Date workTime;
	private Date checkTime;
	private String isPrefer;
	
	
	
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
	public String getTipsCode() {
		return tipsCode;
	}
	/**
	 * @param tipsCode the tipsCode to set
	 */
	public void setTipsCode(String tipsCode) {
		this.tipsCode = tipsCode;
	}
	/**
	 * @return the tipsRowkey
	 */
	public String getTipsRowkey() {
		return tipsRowkey;
	}
	/**
	 * @param tipsRowkey the tipsRowkey to set
	 */
	public void setTipsRowkey(String tipsRowkey) {
		this.tipsRowkey = tipsRowkey;
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
	public Date getWorkTime() {
		return workTime;
	}
	/**
	 * @param workTime the workTime to set
	 */
	public void setWorkTime(Date workTime) {
		this.workTime = workTime;
	}
	
	
	
	/**
	 * @return the checkTime
	 */
	public Date getCheckTime() {
		return checkTime;
	}
	/**
	 * @param checkTime the checkTime to set
	 */
	public void setCheckTime(Date checkTime) {
		this.checkTime = checkTime;
	}
	/**
	 * @return the isPrefer
	 */
	public String getIsPrefer() {
		return isPrefer;
	}
	/**
	 * @param isPrefer the isPrefer to set
	 */
	public void setIsPrefer(String isPrefer) {
		this.isPrefer = isPrefer;
	}
	
	
	
	
	
	



}
