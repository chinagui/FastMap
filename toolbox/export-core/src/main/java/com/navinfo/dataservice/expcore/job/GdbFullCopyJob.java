package com.navinfo.dataservice.expcore.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/** 
* @ClassName: GdbFullCopyJob 
* @author Xiao Xiaowen 
* @date 2016年6月14日 下午2:54:27 
* @Description: TODO
*  
*/
public class GdbFullCopyJob extends AbstractJob {

	public GdbFullCopyJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#execute()
	 */
	@Override
	public void execute() throws JobException {
		// TODO Auto-generated method stub

	}

}
