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
public class QuickTaskJobRequest extends AbstractStatJobRequest {
	private int programType;
	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "quickTaskStat";
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

	public int getProgramType() {
		return programType;
	}

	public void setProgramType(int programType) {
		this.programType = programType;
	}

}
