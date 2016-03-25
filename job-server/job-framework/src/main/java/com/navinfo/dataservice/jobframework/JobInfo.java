package com.navinfo.dataservice.jobframework;

import java.util.Date;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobInfo 
* @author Xiao Xiaowen 
* @date 2016年3月25日 下午4:04:34 
* @Description: TODO
*/
public class JobInfo {
	private long id;
	private String type;
	private Date createTime;
	private Date beginTime;
	private Date endTime;
	private int status;
	private JSONObject request;
	private JSONObject response;
	private long projectId;
	private long userId;
	private String descp;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public JSONObject getRequest() {
		return request;
	}
	public void setRequest(JSONObject request) {
		this.request = request;
	}
	public JSONObject getResponse() {
		return response;
	}
	public void setResponse(JSONObject response) {
		this.response = response;
	}
	public long getProjectId() {
		return projectId;
	}
	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getDescp() {
		return descp;
	}
	public void setDescp(String descp) {
		this.descp = descp;
	}
	
}
