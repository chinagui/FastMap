package com.navinfo.dataservice.dealership.job;

import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.commons.util.DateUtils;
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
	

	protected String chainCode;
	
	private int specRegionId;//需要日落月的大区id,为空表示全部大区都要落
	private List<Integer> specMeshes;//需要日落月的大区id,为空表示全部大区都要落
	int phaseId;
    

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
			if(chainCode==null||chainCode.isEmpty())throw new JobException("传入品牌代码不能为空");
		}catch(JobException e){
			throw e;
		}catch(Exception ex){
			log.error(ex.getMessage(),ex);
			throw new JobException("job参数验证不通过："+ex.getMessage(),ex);
		}
	}
	
	public String getChainCode() {
		return chainCode;
	}

	public void setChainCode(String chainCode) {
		this.chainCode = chainCode;
	}

	public int getSpecRegionId() {
		return specRegionId;
	}

	public void setSpecRegionId(int specRegionId) {
		this.specRegionId = specRegionId;
	}

	public List<Integer> getSpecMeshes() {
		return specMeshes;
	}

	public void setSpecMeshes(List<Integer> specMeshes) {
		this.specMeshes = specMeshes;
	}
	
	public int getPhaseId() {
		return phaseId;
	}

	public void setPhaseId(int phaseId) {
		this.phaseId = phaseId;
	}

}
