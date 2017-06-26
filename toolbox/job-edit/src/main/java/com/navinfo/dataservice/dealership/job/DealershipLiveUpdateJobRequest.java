package com.navinfo.dataservice.dealership.job;

import java.util.HashMap;
import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

public class DealershipLiveUpdateJobRequest extends AbstractJobRequest {

	protected List<String> chainCodeList;
	protected List<Integer> resultIdList;
	protected int sourceType; 
	protected int userId;
	

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		subJobRequests = new HashMap<String,AbstractJobRequest>();
		if(chainCodeList != null && chainCodeList.size() > 0 &&
				resultIdList != null && resultIdList.size() > 0 && sourceType != 0 ){
			AbstractJobRequest dealershipdiff = JobCreateStrategy.createJobRequest("DealershipTableAndDbDiffJob", null);
			dealershipdiff.setAttrValue("chainCodeList", chainCodeList);
			dealershipdiff.setAttrValue("resultIdList", resultIdList);
			//增加数据sourceType=3
			dealershipdiff.setAttrValue("sourceType", sourceType);
			subJobRequests.put("DealershipTableAndDbDiffJob", dealershipdiff);
		}
	}

	@Override
	public String getJobType() {
		return "dealershipLiveUpdateJob";
	}

	@Override
	public String getJobTypeName() {
		return "实时更新job";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 0;
	}

	@Override
	public void validate() throws JobException {
		try{
			if(chainCodeList==null){throw new JobException("传入chainCodeList不能为空");}
			if(resultIdList==null){throw new JobException("传入resultIdList不能为空");}
		}catch(JobException e){
			throw e;
		}catch(Exception ex){
			log.error(ex.getMessage(),ex);
			throw new JobException("job参数验证不通过："+ex.getMessage(),ex);
		}
	}

	public List<String> getChainCodeList() {
		return chainCodeList;
	}

	public void setChainCodeList(List<String> chainCodeList) {
		this.chainCodeList = chainCodeList;
	}

	public int getSourceType() {
		return sourceType;
	}

	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	public List<Integer> getResultIdList() {
		return resultIdList;
	}

	public void setResultIdList(List<Integer> resultIdList) {
		this.resultIdList = resultIdList;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	
	
	
}

