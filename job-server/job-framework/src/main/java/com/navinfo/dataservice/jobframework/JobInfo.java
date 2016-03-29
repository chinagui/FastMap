package com.navinfo.dataservice.jobframework;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobInfo 
* @author Xiao Xiaowen 
* @date 2016年3月25日 下午4:04:34 
* @Description: 不属于任何
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
	private List<JobStep> steps;
	private String identity;
	public JobInfo(long id){
		this.projectId=0L;
		this.id=id;
		this.identity=projectId+"_"+id;
	}
	public JobInfo(long projectId,long id){
		this.projectId=projectId;
		this.id=id;
		this.identity=projectId+"_"+id;
	}
/* getter & setter */
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
/* override hashCode() & equals() */
	public int hashCode(){
		return getIdentity().hashCode();
	}
	public boolean equals(Object anObject){
		if(anObject==null)return false;
		if(anObject instanceof JobInfo
				&&getIdentity().equals(((JobInfo) anObject).getIdentity())){
			return true;
		}else{
			return false;
		}
	}
/* methods */
	public String getIdentity(){
		return identity;
	}
	public int getStepListSize(){
		if(steps==null){
			return -1;
		}
		return steps.size();
	}
	/**
	 * 线程安全
	 * @param progress
	 * @param stepMsg
	 */
	public JobStep addStep(int progress,String stepMsg){
		if(steps==null){
			synchronized(this){
				if(steps==null){
					steps = new ArrayList<JobStep>();
				}
			}
		}
		JobStep step = new JobStep(id);
		step.setStepMsg(stepMsg);
		synchronized(this){
			int seq = steps.size();
			step.setStepSeq(seq);
			steps.add(step);
		}
		return step;
	}
	public static void main(String[] args){
		List<String> list = new ArrayList<String>();
		list.add(99, "99");
		System.out.println(list);
	}
}
