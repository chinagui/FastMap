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
public class QuickProgramJobRequest extends AbstractStatJobRequest {
	private int type;

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "quickProgramStat";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "快线项目数据统计";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 0;
	}

	@Override
	public void validate() throws JobException {

	}

	public int getType() {
		// TODO Auto-generated method stub
		return this.type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
