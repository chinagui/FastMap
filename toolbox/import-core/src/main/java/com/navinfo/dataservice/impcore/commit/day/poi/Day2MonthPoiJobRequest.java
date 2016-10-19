package com.navinfo.dataservice.impcore.commit.day.poi;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：CommitDay2MonthPoiJob 请求参数的解析处理类
 * 
 */
public class Day2MonthPoiJobRequest extends AbstractJobRequest {
	@Override
	public void validate() throws JobException {

	}
	@Override
	public String getJobType() {
		return "day2MonthPoiJob";
	}
	@Override
	public String getJobTypeName(){
		return "POI日落月";
	}
	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}
	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 1;
	}

}

