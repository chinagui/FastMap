package com.navinfo.dataservice.job.statics.manJob;

import com.navinfo.dataservice.job.statics.AbstractStatJobRequest;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;

/** 
 * @ClassName: PoiDayStatJobRequest
 * @author songhe
 * @date 2017年7月31日
 * 
 */
public class PersonDayJobRequest extends AbstractStatJobRequest {

	@Override
	public void defineSubJobRequests() throws JobCreateException {

	}

	@Override
	public String getJobType() {
		return "personDayJob";
	}

	@Override
	public String getJobTypeName() {
		return "子任务日库每人每天统计数据";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 0;
	}

	@Override
	public void validate() throws JobException {

	}

}
