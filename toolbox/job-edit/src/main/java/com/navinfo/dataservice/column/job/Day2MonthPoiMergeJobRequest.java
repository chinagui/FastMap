package com.navinfo.dataservice.column.job;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

public class Day2MonthPoiMergeJobRequest extends AbstractJobRequest {
	private String cityId;//需要日落月的城市id，为空表示全部DAY2MONTH_CONFIG中处于打开状态的城市
	

	@Override
	public void defineSubJobRequests() throws JobCreateException {

	}

	@Override
	public String getJobType() {
		return "day2MonSync";
	}

	@Override
	public String getJobTypeName() {
		return "日落月";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 0;
	}

	@Override
	public void validate() throws JobException {

	}
	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

}
