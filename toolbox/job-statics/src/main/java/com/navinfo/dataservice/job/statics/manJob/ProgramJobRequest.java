package com.navinfo.dataservice.job.statics.manJob;

import com.navinfo.dataservice.job.statics.AbstractStatJobRequest;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;

/**
 * 
 * @ClassName ProgramJobRequest
 * @author songhe
 * @date 2017年9月4日
 * 
 */
public class ProgramJobRequest extends AbstractStatJobRequest {

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "programStat";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "项目数据统计";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 0;
	}

	@Override
	public void validate() throws JobException {

	}

}
