package com.navinfo.dataservice.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

import net.sf.json.JSONArray;

public class MidTask2QuickJobRequest  extends AbstractJobRequest{
	
	private int dbId;
	private int subtaskId;
	private int taskId;
	private JSONArray pois;
	private JSONArray tips;


	public int getDbId() {
		return dbId;
	}

	public JSONArray getPois() {
		return pois;
	}

	public void setPois(JSONArray pois) {
		this.pois = pois;
	}

	public JSONArray getTips() {
		return tips;
	}

	public void setTips(JSONArray tips) {
		this.tips = tips;
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
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	public String getJobType() {
		return "midTask2QuickJob";
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
