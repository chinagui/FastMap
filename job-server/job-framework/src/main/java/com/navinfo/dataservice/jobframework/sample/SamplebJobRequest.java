package com.navinfo.dataservice.jobframework.sample;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.JobRuntimeException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;


/** 
* @ClassName: SampleJobRequest 
* @author Xiao Xiaowen 
* @date 2016年5月25日 下午8:01:03 
* @Description: TODO
*  
*/
public class SamplebJobRequest extends AbstractJobRequest{
	private String type;

	@Override
	public void validate() throws JobRuntimeException {
		//不需要验证子job的request
		if(StringUtils.isEmpty(type)){
			throw new JobRuntimeException("参数检查错误：类型参数不能为空");
		}
		
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getJobType() {
		return "sampleb";
	}

	@Override
	public String getJobTypeName(){
		return "示例B";
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		subJobRequests = new HashMap<String,AbstractJobRequest>();
		AbstractJobRequest samplea1 = JobCreateStrategy.createJobRequest("samplea", null);
		subJobRequests.put("samplea1", samplea1);
	}

	@Override
	protected int myStepCount() throws JobException {
		return 1;
	}

}
