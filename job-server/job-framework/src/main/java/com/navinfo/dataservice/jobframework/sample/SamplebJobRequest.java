package com.navinfo.dataservice.jobframework.sample;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
public class SamplebJobRequest extends AbstractJobRequest{
	private AbstractJobRequest sampleaJobRequest1;
	private String type;
    public SamplebJobRequest() {
		super();
		log = LoggerRepos.getLogger(log);
	}
    public SamplebJobRequest(JSONObject jsonConfig){
    	super();
		log = LoggerRepos.getLogger(log);
    	this.parseByJsonConfig(jsonConfig);
    }

	@Override
	public int getStepCount() throws JobException {
		return 1+sampleaJobRequest1.getStepCount();
	}
	
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
	public AbstractJobRequest getSampleaJobRequest1() {
		return sampleaJobRequest1;
	}
	public void setSampleaJobRequest1(AbstractJobRequest sampleaJobRequest1) {
		this.sampleaJobRequest1 = sampleaJobRequest1;
	}
	@Override
	public String getJobType() {
		return "sampleb";
	}

}
