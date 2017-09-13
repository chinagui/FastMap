package com.navinfo.dataservice.job.statics.manJob;

import com.navinfo.dataservice.job.statics.AbstractStatJobRequest;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;

/**
 * QuickMonitorJobRequest
 * @author zl
 *
 */
public class QuickMonitorJobRequest extends AbstractStatJobRequest {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#defineSubJobRequests()
	 */
	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#getJobType()
	 */
	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "quickMonitorStat";
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#getJobTypeName()
	 */
	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "快线监控统计";
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#myStepCount()
	 */
	@Override
	protected int myStepCount() throws JobException {
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
