package com.navinfo.dataservice.job.statics.job;

import com.navinfo.dataservice.job.statics.AbstractStatJobRequest;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;

public class ExportQualityPoiReportJobRequest extends AbstractStatJobRequest {

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "exportQualityPoiReport";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "导出POI外业质检样本统计Job";
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
