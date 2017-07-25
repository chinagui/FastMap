package com.navinfo.dataservice.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

public class TaskMedium2QuickJobRequest extends AbstractJobRequest{
	
	private int jobId;
	private int phaseId;
	private List<?> pois;
	private List<?> tips;
	
	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public List<?> getPois() {
		return pois;
	}

	public void setPois(List<?> pois) {
		this.pois = pois;
	}

	public List<?> getTips() {
		return tips;
	}

	public void setTips(List<?> tips) {
		this.tips = tips;
	}

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
