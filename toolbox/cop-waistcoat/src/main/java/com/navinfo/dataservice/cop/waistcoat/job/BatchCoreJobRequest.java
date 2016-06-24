package com.navinfo.dataservice.cop.waistcoat.job;

import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: BatchCoreJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月21日 上午11:57:17 
* @Description: TODO
*  
*/
public class BatchCoreJobRequest extends AbstractJobRequest {
	

	@Override
	public String getJobType() {
		return "batchCore";
	}


	@Override
	public void validate() throws JobException {

	}

	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void defineSubJobRequests() {
		
	}

}
