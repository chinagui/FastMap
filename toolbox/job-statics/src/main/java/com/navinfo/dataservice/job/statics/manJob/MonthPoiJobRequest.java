package com.navinfo.dataservice.job.statics.manJob;

import com.navinfo.dataservice.job.statics.AbstractStatJobRequest;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;

/**
 * 
 * @ClassName MonthPoiJobRequest
 * @author Han Shaoming
 * @date 2017年7月31日 下午1:26:12
 * @Description TODO
 */
public class MonthPoiJobRequest extends AbstractStatJobRequest {

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "monthPoiStat";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "POI月库作业数据统计";
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
