package com.navinfo.dataservice.dealership.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
 * @ClassName: DealershipTableAndDbDiffJobRequest
 * @author jicaihua
 * @date 2017年5月31日
 * @Description: DealershipTableAndDbDiffJobRequest.java
 */
public class DealershipTableAndDbDiffJobRequest extends AbstractJobRequest {
	

	protected List<String> chainCodeList;//库差分，重新库差分、补充数据、实时更新、品牌更新传入
	
	protected int sourceType;//1库差分，2重新库差分、3补充数据、4实时更新、5品牌更新
	
	public int getSourceType() {
		return sourceType;
	}

	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	protected List<Integer> resultIdList;//补充数据 ,实时更新传入

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "DealershipTableAndDbDiffJob";
	}

	@Override
	public String getJobTypeName() {
		return "代理店表库差分";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 3;
	}

	@Override
	public void validate() throws JobException {
		try{
			if(sourceType==0){throw new JobException("传入sourceType不能为空");}
			if(sourceType!=3){
				if(chainCodeList==null){throw new JobException("传入chainCodeList不能为空");}
			}else{
				if(resultIdList==null){throw new JobException("传入resultIdList不能为空");}
			}
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


	public List<Integer> getResultIdList() {
		return resultIdList;
	}

	public void setResultIdList(List<Integer> resultIdList) {
		this.resultIdList = resultIdList;
	}
	
}
