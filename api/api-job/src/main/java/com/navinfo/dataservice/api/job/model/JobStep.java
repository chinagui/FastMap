package com.navinfo.dataservice.api.job.model;

import java.util.Date;

/** 
* @ClassName: JobStep 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午1:29:36 
* @Description: TODO
*/
public class JobStep {
	private long jobId;
	private int stepSeq;
	private String stepMsg;
	private Date beginTime;
	private Date endTime;
	private int status;
	private int progress;
	public JobStep(long jobId){
		this.jobId=jobId;
	}
	public JobStep(long jobId,int stepSeq,String stepMsg){
		this.jobId=jobId;
		this.stepSeq=stepSeq;
		this.stepMsg=stepMsg;
	}
	public long getJobId() {
		return jobId;
	}
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	public int getStepSeq() {
		return stepSeq;
	}
	public void setStepSeq(int stepSeq) {
		this.stepSeq = stepSeq;
	}
	public String getStepMsg() {
		return stepMsg;
	}
	public void setStepMsg(String stepMsg) {
		this.stepMsg = stepMsg;
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
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}

}
