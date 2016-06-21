package com.navinfo.dataservice.cop.waistcoat.job;

import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: GdbValidationJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月21日 下午3:52:48 
* @Description: TODO
*  
*/
public class GdbValidationJobRequest extends AbstractJobRequest {

	@Override
	public String getJobType() {
		return "gdbValidation";
	}

	@Override
	public int getStepCount() throws JobException {
		return 0;
	}

	@Override
	public void validate() throws JobException {
		
	}

}
