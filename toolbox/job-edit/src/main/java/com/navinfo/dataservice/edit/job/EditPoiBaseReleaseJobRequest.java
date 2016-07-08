package com.navinfo.dataservice.edit.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

public class EditPoiBaseReleaseJobRequest extends AbstractJobRequest {
	private List<Integer> gridIds;
	private int targetDbId;
	private List<String> checkRules;
	private List<String> batchRules;


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

	public List<String> getCheckRules() {
		return checkRules;
	}

	public void setCheckRules(List<String> checkRules) {
		this.checkRules = checkRules;
	}

	public List<String> getBatchRules() {
		return batchRules;
	}

	public void setBatchRules(List<String> batchRules) {
		this.batchRules = batchRules;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		subJobRequests = new HashMap<String,AbstractJobRequest>();
		//validation
		List<String> checkRule=new ArrayList<String>();
		checkRule.add("GLM01004");
		this.setCheckRules(checkRule);
		AbstractJobRequest validation = JobCreateStrategy.createJobRequest("gdbValidation", null);
		validation.setAttrValue("grids", gridIds);
		validation.setAttrValue("rules", JSONArray.fromObject(this.checkRules));
		validation.setAttrValue("targetDbId", targetDbId);
		subJobRequests.put("validation", validation);
		//batch
		List<String> batchRule=new ArrayList<String>();
		batchRule.add("BATCH_LANE_PARKING_FLAG");
		this.setBatchRules(batchRule);
		AbstractJobRequest batch = JobCreateStrategy.createJobRequest("gdbBatch", null);
		batch.setAttrValue("grids", gridIds);
		batch.setAttrValue("rules", JSONArray.fromObject(this.batchRules));
		batch.setAttrValue("targetDbId", targetDbId);
		subJobRequests.put("batch", batch);
	}

	@Override
	protected int myStepCount() throws JobException {
		return 1;
	}

}
