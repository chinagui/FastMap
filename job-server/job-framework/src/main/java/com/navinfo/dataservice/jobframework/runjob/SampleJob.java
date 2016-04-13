package com.navinfo.dataservice.jobframework.runjob;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobException;

/** 
* @ClassName: SampleJob 
* @author Xiao Xiaowen 
* @date 2016年4月6日 下午4:24:45 
* @Description: TODO
*/
public class SampleJob extends AbstractJob {
	/**
	 * @param jobInfo
	 */
	public SampleJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#volidateRequest()
	 */
	@Override
	public void volidateRequest() throws JobException {
		// TODO Auto-generated method stub
		log.info("开始验证request参数...");
		try{
			Thread.sleep(3000);
		}catch(Exception e){
			throw new JobException(e.getMessage(),e);
		}
		log.info("验证request参数完成。");
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#execute()
	 */
	@Override
	public void execute() throws JobException {

		// TODO Auto-generated method stub
		log.info("开始A步骤...");
		sleepp(3000);
		log.info("A完成。");
		finishStep("A步骤");
		log.info("开始B步骤...");
		sleepp(3000);
		log.info("B完成。");
		finishStep("B步骤");
	}

	@Override
	public int computeStepCount() throws JobException {
		
		log.info("开始计算本次任务总步骤数...");
		sleepp(3000);
		log.info("计算完成。总步骤数为3。");
		return 3;
	}
	
	private void sleepp(long millis)throws JobException{
		try{
			Thread.sleep(millis);
		}catch(Exception e){
			throw new JobException(e.getMessage(),e);
		}
	}

}
