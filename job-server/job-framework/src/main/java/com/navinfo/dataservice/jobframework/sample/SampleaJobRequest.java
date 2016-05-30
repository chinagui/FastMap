package com.navinfo.dataservice.jobframework.sample;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.JobRuntimeException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

import net.sf.json.JSONObject;

/** 
* @ClassName: SampleJobRequest 
* @author Xiao Xiaowen 
* @date 2016年5月25日 下午8:01:03 
* @Description: TODO
*  
*/
public class SampleaJobRequest extends AbstractJobRequest{
	private int sleepSeconds;
    public SampleaJobRequest() {
		super();
		log = LoggerRepos.getLogger(log);
	}
    public SampleaJobRequest(JSONObject jsonConfig){
    	super();
		log = LoggerRepos.getLogger(log);
    	this.parseByJsonConfig(jsonConfig);
    }

	@Override
	public int getStepCount() throws JobException {
		return 3;
	}
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

}
