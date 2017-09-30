package com.navinfo.dataservice.row.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/**
 * 点门牌行编检查
 * checkType == 6 点门牌行编
 * 必传参数：subtaskId,ckRules
 * 测试用参数：pids
 * 
 * @Title: PointAddressRowValidationJobRequest
 * @Package: com.navinfo.dataservice.row.job
 * @Description:
 * @Author: LittleDog
 * @Date: 2017年9月27日
 * @Version: V1.0
 */
public class PointAddressRowValidationJobRequest extends AbstractJobRequest {

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
		return "pointAddressRowValidation";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "pointAddress点门牌行编自定义检查";
	}

	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 0;
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
