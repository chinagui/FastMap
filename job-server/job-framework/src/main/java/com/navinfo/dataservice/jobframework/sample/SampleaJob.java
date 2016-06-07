package com.navinfo.dataservice.jobframework.sample;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: SampleJob 
* @author Xiao Xiaowen 
* @date 2016年4月6日 下午4:24:45 
* @Description: TODO
*/
public class SampleaJob extends AbstractJob {
	
	public SampleaJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		log.info("开始A步骤...");
		sleepp(((SampleaJobRequest)request).getSleepSeconds());
		log.info("A步骤完成。");
		response("A步骤完成",null);
		log.info("开始B步骤...");
		sleepp(((SampleaJobRequest)request).getSleepSeconds());
		log.info("B步骤完成。");
		response("B步骤完成",null);
		/**
		 * C步骤有输出
		 */
		log.info("开始C步骤...");
		Map<String,Object> stepbData = new HashMap<String,Object>();
		stepbData.put("result", "Hello,Job Server!");
		response("C步骤完成",stepbData);
		log.info("C步骤完成。");
	}
	
	private void sleepp(long seconds)throws JobException{
		try{
			Thread.sleep(seconds*1000);
		}catch(Exception e){
			throw new JobException(e.getMessage(),e);
		}
	}

}
