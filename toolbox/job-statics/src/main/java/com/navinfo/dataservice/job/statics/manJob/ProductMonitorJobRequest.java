package com.navinfo.dataservice.job.statics.manJob;

import com.navinfo.dataservice.job.statics.AbstractStatJobRequest;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;


/**
 * 
 * @ClassName ProductMonitorJobRequest
 * @author Han Shaoming
 * @date 2017年9月22日 下午4:09:35
 * @Description TODO
 */
public class ProductMonitorJobRequest extends AbstractStatJobRequest {

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "productMonitorStat";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "大屏统计任务";
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
