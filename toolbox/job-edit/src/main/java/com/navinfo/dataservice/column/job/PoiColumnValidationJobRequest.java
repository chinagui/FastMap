package com.navinfo.dataservice.column.job;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
/**
 * poi精编检查
 * checkType ==1poi精编 
 * 必传参数：subtaskId，firstWorkItem，secondWorkItem
 * 测试用参数：pids,ckRules
 * @author zhangxiaoyi
 */
public class PoiColumnValidationJobRequest extends AbstractJobRequest {

	private List<Long> pids;
	private List<String> rules;
	private String firstWorkItem;
	private String secondWorkItem;
	private int status;
	private int targetDbId;

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "poiColumnValidation";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "poi精编自定义检查";
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
		this.pids=new ArrayList<Long>();
		for(Object tmp:pids){
			this.pids.add(Long.valueOf(tmp.toString()));
		}
		//this.pids = pids;
	}

	public List<String> getRules() {
		return rules;
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	public String getFirstWorkItem() {
		return firstWorkItem;
	}

	public void setFirstWorkItem(String firstWorkItem) {
		this.firstWorkItem = firstWorkItem;
	}

	public String getSecondWorkItem() {
		return secondWorkItem;
	}

	public void setSecondWorkItem(String secondWorkItem) {
		this.secondWorkItem = secondWorkItem;
	}

	public int getTargetDbId() {
		return targetDbId;
	}

	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
