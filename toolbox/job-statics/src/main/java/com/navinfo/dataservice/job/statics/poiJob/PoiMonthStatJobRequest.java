package com.navinfo.dataservice.job.statics.poiJob;

import com.navinfo.dataservice.job.statics.AbstractStatJobRequest;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
 * @ClassName: PoiMonthStatJobRequest
 * @author songdongyan
 * @date 2017年5月25日
 * @Description: PoiMonthStatJobRequest.java
 */
public class PoiMonthStatJobRequest extends AbstractStatJobRequest {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#defineSubJobRequests()
	 */
	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#getJobType()
	 */
	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "poiMonthStat";
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#getJobTypeName()
	 */
	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "POI月库作业数据统计";
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#myStepCount()
	 */
	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#validate()
	 */
	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub

	}

}
