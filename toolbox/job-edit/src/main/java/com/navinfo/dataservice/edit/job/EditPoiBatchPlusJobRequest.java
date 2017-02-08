package com.navinfo.dataservice.edit.job;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

public class EditPoiBatchPlusJobRequest extends AbstractJobRequest {
	private int targetDbId;
	private List<String> batchRules;
	private List<Long> pids;
	
	public List<Long> getPids() {
		return pids;
	}

	public void setPids(List<Long> pids) {
		this.pids=new ArrayList<Long>();
		for(Object tmp:pids){
			this.pids.add(Long.valueOf(tmp.toString()));
		}
		//this.pids = pids;
	}


	@Override
	public String getJobType() {
		return "editPoiBatchPlus";
	}
	@Override
	public String getJobTypeName(){
		return "POI行编批处理";
	}

	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub
		
	}

	public int getTargetDbId() {
		return targetDbId;
	}

	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}

	public List<String> getBatchRules() {
		return batchRules;
	}

	public void setBatchRules(List<String> batchRules) {
		this.batchRules = batchRules;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
	}

	@Override
	protected int myStepCount() throws JobException {
		return 1;
	}

}
