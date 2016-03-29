package com.navinfo.dataservice.diff;

import com.navinfo.dataservice.jobframework.AbstractJobResponse;
import com.navinfo.dataservice.jobframework.exception.JobRuntimeException;

import net.sf.json.JSONObject;

/** 
 * @ClassName: DiffResponse 
 * @author Xiao Xiaowen 
 * @date 2016-1-20 下午6:54:01 
 * @Description: TODO
 */
public class DiffResponse extends AbstractJobResponse {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.commons.job.AbstractJobResponse#generateDataJson()
	 */
	@Override
	protected JSONObject generateDataJson() throws JobRuntimeException {
		// TODO Auto-generated method stub
		JSONObject json = new JSONObject();
		return json;
	}

}
