package com.navinfo.dataservice.job.statics.manJob;

import com.navinfo.dataservice.job.statics.AbstractStatJobRequest;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;

/** 
 * @ClassName: PersonTipsJobRequest
 * @date 2017年8月9日
 * 
 */
public class PersonTipsJobRequest extends AbstractStatJobRequest {
	private String workDay;

	@Override
	public void defineSubJobRequests() throws JobCreateException {

	}

	@Override
	public String getJobType() {
		return "personTipsJob";
	}

	@Override
	public String getJobTypeName() {
		return "子任务Tips每人每天统计数据";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 0;
	}

	@Override
	public void validate() throws JobException {

	}
	public String getWorkDay() {
		return workDay;
	}

	public void setWorkDay(String workDay) {
		this.workDay = workDay;
	}

}
