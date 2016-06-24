package com.navinfo.dataservice.edit.job;

import java.util.HashMap;
import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

public class EditPoiBaseReleaseJobRequest extends AbstractJobRequest {
	private List<Integer> gridIds;
	private int targetDbId;
	private List<Integer> checkRules;
	private List<Integer> batchRules;


	@Override
	public String getJobType() {
		return "editPoiBaseRelease";
	}

	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub
		
	}

	public List<Integer> getGridIds() {
		return gridIds;
	}

	public void setGridIds(List<Integer> gridIds) {
		this.gridIds = gridIds;
	}

	public int getTargetDbId() {
		return targetDbId;
	}

	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}

	public List<Integer> getCheckRules() {
		return checkRules;
	}

	public void setCheckRules(List<Integer> checkRules) {
		this.checkRules = checkRules;
	}

	public List<Integer> getBatchRules() {
		return batchRules;
	}

	public void setBatchRules(List<Integer> batchRules) {
		this.batchRules = batchRules;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		subJobRequests = new HashMap<String,AbstractJobRequest>();
		//validation
		AbstractJobRequest validation = JobCreateStrategy.createJobRequest("gdbValidation", null);
		validation.setAttrValue("grids", gridIds);
		validation.setAttrValue("rules", checkRules);
		validation.setAttrValue("targetDbId", targetDbId);
		subJobRequests.put("validation", validation);
		//batch
		AbstractJobRequest batch = JobCreateStrategy.createJobRequest("gdbBatch", null);
		batch.setAttrValue("grids", gridIds);
		batch.setAttrValue("rules", batchRules);
		batch.setAttrValue("targetDbId", targetDbId);
		subJobRequests.put("batch", batch);
	}

	@Override
	protected int myStepCount() throws JobException {
		return 1;
	}

}
