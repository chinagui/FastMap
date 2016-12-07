package com.navinfo.dataservice.api.job.model;

import java.io.Serializable;
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
public class JobInfo implements Serializable{
	private long id;
	private String type;
	private Date createTime;
	private Date beginTime;
	private Date endTime;
	private int status;
	private JSONObject request;
	private JSONObject response;
	private String resultMsg;
	private long userId;
	private long taskId;
	private String descp;
	private List<JobStep> steps;
	private int stepCount=0;
	private String guid;
	private String identity;
	public JobInfo(long id,String guid){
		this.id=id;
		this.guid=guid;
		this.identity=id+"-"+guid;
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
	public String getResultMsg() {
		return resultMsg;
	}
	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
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
	public List<JobStep> getSteps(){
		return steps;
	}
	public void setSteps(List<JobStep> steps){
		this.steps = steps;
	}
	public int getStepCount() {
		return stepCount;
	}
	public void setStepCount(int stepCount) {
		this.stepCount = stepCount;
	}
	public String getIdentity() {
		return identity;
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
	public JobStep addStep(String stepMsg){
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
	/**
	 * 线程安全
	 * @param progress
	 * @param stepMsg
	 */
	@Deprecated
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

	public void addResponse(String key,Object value) {
		if(response==null){
			response = new JSONObject();
		}
		response.put(key, value);
	}
	public void startJob(int stepCount){
		this.status=JobStatus.STATUS_START;
		this.stepCount=stepCount;
	}
	public void endJob(int status,String resultMsg) {
		this.status=status;
		this.resultMsg=resultMsg;
		this.setEndTime(new Date());
	}
	public static void main(String[] args){
		List<String> list = new ArrayList<String>();
		list.add(99, "99");
		System.out.println(list);
	}
}
