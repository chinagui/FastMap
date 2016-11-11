package com.navinfo.dataservice.impcore.release.day.poi;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
 * @ClassName: ReleaseFmIdbDailyPoiJobRequest
 * @author songdongyan
 * @date 2016年11月10日
 * @Description: ReleaseFmIdbDailyPoiJobRequest.java
 */
public class ReleaseFmIdbDailyPoiJobRequest extends AbstractJobRequest {

	private String featureType;//参考LogFlusher.FEATURE_POI
	private int produceId;
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
		return "releaseFmIdbDailyPoiJob";
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#getJobTypeName()
	 */
	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "POI日出品";
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

	/**
	 * @return the featureType
	 */
	public String getFeatureType() {
		return featureType;
	}

	/**
	 * @param featureType the featureType to set
	 */
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}

	/**
	 * @return the produceId
	 */
	public int getProduceId() {
		return produceId;
	}

	/**
	 * @param produceId the produceId to set
	 */
	public void setProduceId(int produceId) {
		this.produceId = produceId;
	}

}
