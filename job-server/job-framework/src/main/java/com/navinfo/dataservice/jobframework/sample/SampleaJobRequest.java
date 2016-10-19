package com.navinfo.dataservice.jobframework.sample;


import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.JobRuntimeException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: SampleJobRequest 
* @author Xiao Xiaowen 
* @date 2016年5月25日 下午8:01:03 
* @Description: TODO
*  
*/
public class SampleaJobRequest extends AbstractJobRequest{
	private int sleepSeconds;

	@Override
	public void validate() throws JobRuntimeException {
		if(sleepSeconds<0){
			throw new JobRuntimeException("参数检查错误：睡眠时长不能小于0");
		}
		
	}
    
	public int getSleepSeconds() {
		return sleepSeconds;
	}
	
	public void setSleepSeconds(int sleepSeconds) {
		this.sleepSeconds = sleepSeconds;
	}
	@Override
	public String getJobType() {
		return "samplea";
	}

	@Override
	public String getJobTypeName(){
		return "示例A";
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	protected int myStepCount() throws JobException {
		return 3;
	}

}
