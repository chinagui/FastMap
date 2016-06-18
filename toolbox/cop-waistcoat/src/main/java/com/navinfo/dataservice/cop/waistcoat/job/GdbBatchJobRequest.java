package com.navinfo.dataservice.cop.waistcoat.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: GdbBatchJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月17日 下午6:02:27 
* @Description: TODO
*  
*/
public class GdbBatchJobRequest extends AbstractJobRequest {
	protected List<Integer> grids;
	protected List<String> rules;
	
	@Override
	public int getStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#validate()
	 */
	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub

	}

}
