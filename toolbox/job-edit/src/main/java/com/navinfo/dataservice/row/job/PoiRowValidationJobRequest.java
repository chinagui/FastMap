package com.navinfo.dataservice.row.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
/**
 * poi行编检查
 *checkType ==0poi行编 
 * 必传参数：subtaskId,ckRules
 * 测试用参数：pids
 */
public class PoiRowValidationJobRequest extends AbstractJobRequest {

	private List<Long> pids;
	private List<String> rules;
	private int targetDbId;

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "poiRowValidation";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "poi行编自定义检查";
	}

	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub
		
	}
	
	public List<Long> getPids() {
		return pids;
	}

	public void setPids(List<Long> pids) {
		this.pids = pids;
	}

	public List<String> getRules() {
		return rules;
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	public int getTargetDbId() {
		return targetDbId;
	}

	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}
}
