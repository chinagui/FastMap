package com.navinfo.dataservice.job;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

import net.sf.json.JSONArray;

public class TaskMedium2QuickJobRequest extends AbstractJobRequest{
	
	private int jobId;
	private int phaseId;
	private JSONArray pids;
	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	private JSONArray tips;
	private int dbId;
	private int subtaskId;
	public int getPhaseId() {
		return phaseId;
	}

	public void setPhaseId(int phaseId) {
		this.phaseId = phaseId;
	}

	private int taskId;

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	public String getJobType() {
		return "taskMedium2QuickJob";
	}

	public JSONArray getPids() {
		return pids;
	}

	public void setPids(JSONArray pids) {
		this.pids = pids;
	}

	public JSONArray getTips() {
		return tips;
	}

	public void setTips(JSONArray tips) {
		this.tips = tips;
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	public int getSubtaskId() {
		return subtaskId;
	}

	public void setSubtaskId(int subtaskId) {
		this.subtaskId = subtaskId;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	@Override
	public String getJobTypeName() {
		return "中转快";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 0;
	}

	@Override
	public void validate() throws JobException {
		
	}

}
