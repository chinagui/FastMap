package com.navinfo.dataservice.edit.job;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

public class EditPoiBaseReleaseJobRequest extends AbstractJobRequest {
	private List<Integer> gridIds;
	private int targetDbId;
	private List<String> checkRuleList;
	private List<String> batchRuleList;
	private AbstractJobRequest gdbBatchRequest;
	private AbstractJobRequest gdbValidationRequest;

	public EditPoiBaseReleaseJobRequest() {
		// TODO Auto-generated constructor stub
	}
	
	public JSONObject createDbJSON(String desc){
		String createValDbString="{\"type\":\"createDb\","
				+ "\"request\":{\"serverType\":\"ORACLE\","
				+ "\"bizType\":\"copVersion\","
				+ "\"descp\":\""+desc+"\"}}";
		JSONObject createValDbRequestJSON=JSONObject.fromObject(createValDbString);
		return createValDbRequestJSON;
	}
	
	public JSONObject expDbJSON(){
		String expValDbString="{\"type\":\"gdbExport\","
				+ "\"request\":{\"condition\":\"mesh\","
				+ "\"featureType\":\"all\","
				+ "\"dataIntegrity\":true}}";
		JSONObject expValDbRequestJSON=JSONObject.fromObject(expValDbString);
		return expValDbRequestJSON;
	}

	@Override
	public String getJobType() {
		return "editPoiBaseRelease";
	}

	@Override
	public int getStepCount() throws JobException {
		int count=1;
		count+=gdbBatchRequest.getStepCount();
		count+=gdbValidationRequest.getStepCount();
		return count;
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
	
	public AbstractJobRequest getGdbBatchRequest() {
		return gdbBatchRequest;
	}

	public void setGdbBatchRequest(AbstractJobRequest gdbBatchRequest) {
		this.gdbBatchRequest = gdbBatchRequest;
	}
	
	public AbstractJobRequest getGdbValidationRequest() {
		return gdbValidationRequest;
	}

	public void setGdbValidationRequest(AbstractJobRequest gdbValidationRequest) {
		this.gdbValidationRequest = gdbValidationRequest;
	}

	public List<String> getCheckRuleList() {
		List<String> checkRuleList=new ArrayList<String>();
		checkRuleList.add("check1");
		return checkRuleList;
		//return checkRuleList;
	}

	public void setCheckRuleList(List<String> checkRuleList) {
		this.checkRuleList = checkRuleList;
	}

	public List<String> getBatchRuleList() {
		List<String> batchRuleList=new ArrayList<String>();
		batchRuleList.add("batch1");
		return batchRuleList;
		//return batchRuleList;
	}

	public void setBatchRuleList(List<String> batchRuleList) {
		this.batchRuleList = batchRuleList;
	}

}
