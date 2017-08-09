package com.navinfo.dataservice.job.statics.manJob;

import com.navinfo.dataservice.job.statics.AbstractStatJobRequest;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;

/**
 * 
 * @ClassName TaskJobRequest
 * @author Han Shaoming
 * @date 2017年8月4日 下午8:43:33
 * @Description TODO
 */
public class TaskJobRequest extends AbstractStatJobRequest {

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "taskStat";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "任务数据统计";
	}

	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub

	}

}
