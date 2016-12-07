package com.navinfo.dataservice.row.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/** 
 * @ClassName: MultiSrc2FmMonthSyncJob
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: MultiSrc2FmMonthSyncJob.java
 */
public class MultiSrc2FmMonthSyncJob extends AbstractJob {

	public MultiSrc2FmMonthSyncJob(JobInfo jobInfo) {
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
