package com.navinfo.dataservice.job.statics;

import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
 * @ClassName: AbstractStatJobRequest
 * @author xiaoxiaowen4127
 * @date 2017年5月23日
 * @Description: AbstractStatJobRequest.java
 */
public abstract class AbstractStatJobRequest extends AbstractJobRequest {
	
	protected String timestamp;

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
