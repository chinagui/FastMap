package com.navinfo.dataservice.impcore.commit.day.poi;

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
	public int getStepCount() throws JobException {
		return 1;
	}
	@Override
	public void validate() throws JobException {

	}

}

