package com.navinfo.dataservice.jobframework.runjob;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.jobframework.exception.JobException;

/** 
* @ClassName: SampleJob 
* @author Xiao Xiaowen 
* @date 2016年4月6日 下午4:24:45 
* @Description: TODO
*/
public class SampleJob extends AbstractJob {

	/**
	 * @param jobInfo
	 */
	protected SampleJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#volidateRequest()
	 */
	@Override
	public void volidateRequest() throws JobException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#execute()
	 */
	@Override
	public void execute() throws JobException {
		// TODO Auto-generated method stub

	}

	@Override
	public int computeStepCount() throws JobException {
		return 10;
	}

}
