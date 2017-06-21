package com.navinfo.dataservice.dealership.job;

import java.util.HashMap;
import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONObject;

public class DealershipAddChainDataJobRequest extends AbstractJobRequest {

	protected List<Integer> resultIdList;

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		subJobRequests = new HashMap<String,AbstractJobRequest>();
		if(resultIdList != null && resultIdList.size() > 0){
			AbstractJobRequest Dealershipdiff = JobCreateStrategy.createJobRequest("DealershipTableAndDbDiffJob", null);
			Dealershipdiff.setAttrValue("resultIdList", resultIdList);
			Dealershipdiff.setAttrValue("sourceType", 3);
			subJobRequests.put("DealershipTableAndDbDiffJob", Dealershipdiff);
		}
	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
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
			if(resultIdList==null){throw new JobException("传入resultIdList不能为空");}
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

	public void setResultIdList(List<Integer> resultIdList) {
		this.resultIdList = resultIdList;
	}
	
}

