package com.navinfo.dataservice.dealership.job;

import java.util.HashMap;
import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

public class DealershipAddChainDataJobRequest extends AbstractJobRequest {

	protected List<Integer> resultIdList;
	protected List<String> chainCodeList;
	protected long userId;

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		subJobRequests = new HashMap<String,AbstractJobRequest>();
		if(resultIdList != null && resultIdList.size() > 0){
			AbstractJobRequest Dealershipdiff = JobCreateStrategy.createJobRequest("DealershipTableAndDbDiffJob", null);
			Dealershipdiff.setAttrValue("resultIdList", resultIdList);
			Dealershipdiff.setAttrValue("chainCodeList", chainCodeList);
			//增加数据sourceType=3
			Dealershipdiff.setAttrValue("sourceType", 3);
			subJobRequests.put("DealershipTableAndDbDiffJob", Dealershipdiff);
		}
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
	public String getJobType() {
		return "dealershipAddChainDataJob";
	}

	@Override
	public String getJobTypeName() {
		return "补充数据job";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 0;
	}

	@Override
	public void validate() throws JobException {
		try{
			if(resultIdList == null || resultIdList.size() < 1){throw new JobException("传入resultIdList不能为空");}
			if(chainCodeList == null || chainCodeList.size() < 1){throw new JobException("传入chainCode不能为空");}
		}catch(JobException e){
			throw e;
		}catch(Exception ex){
			log.error(ex.getMessage(),ex);
			throw new JobException("job参数验证不通过："+ex.getMessage(),ex);
		}
	}

	public List<Integer> getResultIdList() {
		return resultIdList;
	}

	public List<String> getChainCodeList() {
		return chainCodeList;
	}

	public void setChainCodeList(List<String> chainCodeList) {
		this.chainCodeList = chainCodeList;
	}

	public void setResultIdList(List<Integer> resultIdList) {
		this.resultIdList = resultIdList;
	}
	
}

