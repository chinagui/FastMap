package com.navinfo.dataservice.commons.job;

/** 
 * @ClassName: DSJob 
 * @author Xiao Xiaowen 
 * @date 2015-12-2 下午5:07:29 
 * @Description: TODO
 */
public class DSJob {
	private String taskId;
	private String jobId;
	private String jobType;
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
}
