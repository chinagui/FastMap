package com.navinfo.dataservice.commons.job;

/** 
 * @ClassName: DSJob 
 * @author Xiao Xiaowen 
 * @date 2015-12-2 下午5:07:29 
 * @Description: TODO
 */
public abstract class DSJob {
	private String flowId;
	private String jobId;
	private String jobType;
	protected String jobRequest;
	protected String jobResponse;
	public String getFlowId() {
		return flowId;
	}
	public void setFlowId(String flowId) {
		this.flowId = flowId;
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
	public void preExecute(){
		parseJobRequest();
	}
	public abstract void parseJobRequest();
}
