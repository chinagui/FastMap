package com.navinfo.dataservice.row.job;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
 * @ClassName: MultiSrc2FmMonthSyncJobRequest
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: MultiSrc2FmMonthSyncJobRequest.java
 */
public class MultiSrc2FmMonthSyncJobRequest extends AbstractJobRequest {

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
		return null;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#getJobTypeName()
	 */
	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return null;
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
