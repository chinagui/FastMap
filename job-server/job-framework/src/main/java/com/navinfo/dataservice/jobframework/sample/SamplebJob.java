package com.navinfo.dataservice.jobframework.sample;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONObject;

/** 
* @ClassName: SampleJob 
* @author Xiao Xiaowen 
* @date 2016年4月6日 下午4:24:45 
* @Description: TODO
*/
public class SamplebJob extends AbstractJob {

	public SamplebJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		try{
			JobInfo subJobInfo1 = new JobInfo(jobInfo.getProjectId(),jobInfo.getId());
			subJobInfo1.setType("samplea");
			AbstractJob subJob1 = JobCreateStrategy.createAsSubJob(jobInfo, parent);
			
			subJob1.run();
			jobInfo.getResponse().put("subJob1", subJob1.getJobInfo().getResponse());
			log.info("Sub Job 1 完成。");
			subJob1.getJobInfo().getResponse();
			
			log.info("B步骤开始...");
			sleepp(3);
			response("B步骤",null);
			log.info("B步骤完成。");
		}catch(Exception e){
			log.error(e.getMessage(),e);
			if(!(e instanceof JobException)){
				throw new JobException(e.getMessage(),e);
			}
			throw (JobException)e;
		}
	}

	
	private void sleepp(long seconds)throws JobException{
		try{
			Thread.sleep(seconds*1000);
		}catch(Exception e){
			throw new JobException(e.getMessage(),e);
		}
	}
}
