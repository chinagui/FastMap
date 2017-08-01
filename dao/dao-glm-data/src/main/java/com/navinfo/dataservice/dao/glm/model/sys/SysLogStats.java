package com.navinfo.dataservice.dao.glm.model.sys;

public class SysLogStats {

	private int logType;
	private String createTime;
	private String beginTime;
	private String endTime;
	private int successTotal;
	private int failureTotal;
	private int total;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	private String errorMsg;
	private String logMsg;

	public int getLogType() {
		return logType;
	}

	public void setLogType(int logType) {
		this.logType = logType;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getSuccessTotal() {
		return successTotal;
	}

	public void setSuccessTotal(int successTotal) {
		this.successTotal = successTotal;
	}

	public int getFailureTotal() {
		return failureTotal;
	}

	public void setFailureTotal(int failureTotal) {
		this.failureTotal = failureTotal;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getLogMsg() {
		return logMsg;
	}

	public void setLogMsg(String logMsg) {
		this.logMsg = logMsg;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	private String userId;

}
