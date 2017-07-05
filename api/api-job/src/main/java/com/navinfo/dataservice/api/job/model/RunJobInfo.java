package com.navinfo.dataservice.api.job.model;

import java.io.Serializable;

import net.sf.json.JSONObject;

/** 
 * @ClassName: RunJobInfo
 * @author xiaoxiaowen4127
 * @date 2017年5月18日
 * @Description: RunJobInfo.java
 */
public class RunJobInfo implements Serializable{
	private String jobType;
	private JSONObject request;
	private long userId = 0;
	private long taskId = 0;
	private String descp;
	
	public RunJobInfo(String jobType,JSONObject request){
		this.jobType=jobType;
		this.request=request;
	}
	
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public JSONObject getRequest() {
		return request;
	}
	public void setRequest(JSONObject request) {
		this.request = request;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	public String getDescp() {
		return descp;
	}
	public void setDescp(String descp) {
		this.descp = descp;
	}
}
