package com.navinfo.dataservice.jobframework.runjob;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.job.model.JobStep;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.job.JobMsgPublisher;
import com.navinfo.dataservice.jobframework.exception.JobException;

/** 
* @ClassName: AbstractJob 
* @author Xiao Xiaowen 
* @date 2016年3月25日 下午4:12:36 
* @Description: 
* 1. 必须要有一个(JobInfo jobInfo,CountDownLatch doneSignal)参数的构造函数
*/
public abstract class AbstractJob implements Runnable {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected JobInfo jobInfo;
	protected JobStep currentJobStep;
	protected CountDownLatch doneSignal;
	
	protected int stepCount = 0;
	//protected boolean rerunnable=false;
	protected AbstractJob(JobInfo jobInfo,CountDownLatch doneSignal){
		this.jobInfo=jobInfo;
	}
	public JobInfo getJobInfo() {
		return jobInfo;
	}
	public void setJobInfo(JobInfo jobInfo) {
		this.jobInfo = jobInfo;
	}
	@Override
	public void run() {
		try{
			initLogger();
			volidateRequest();
			computeStepCount();
			execute();
		}catch(Exception e){
			
		}finally{
			
		}
	}

	/**
	 * 初始化每个任务一个日志文件的日志系统
	 * 
	 * @throws IOException
	 */
	private void initLogger() throws IOException {
		log.debug("初始化job日志,将日志对象Logger 放入ThreadLocal对象中：" + jobInfo.getId());
		log = LoggerRepos.createLogger(jobInfo.getIdentity());
	}
	public abstract void volidateRequest()throws JobException;
	public abstract void execute()throws JobException;
	public abstract void computeStepCount()throws JobException;
	//public abstract void computeRerunnable()throws JobException;
	public void startNewStep(String stepMsg)throws JobException{
		try{
			currentJobStep = jobInfo.addStep(stepMsg);
			//传什么信息过去还要修改
			JobMsgPublisher.responseJob(jobInfo.getId(), jobInfo.getResponse());
		}catch(Exception e){
			throw new JobException("");
		}
	}
	@Deprecated
	public void startNewStep(int progress,String stepMsg)throws JobException{
		try{
			currentJobStep = jobInfo.addStep(progress, stepMsg);
			//传什么信息过去还要修改
			JobMsgPublisher.responseJob(jobInfo.getId(), jobInfo.getResponse());
		}catch(Exception e){
			throw new JobException("");
		}
	}
	@Deprecated
	public void finishCurrentStep(int progress)throws JobException{
		try{
			currentJobStep.setProgress(progress);
			//传什么信息过去还要修改
			JobMsgPublisher.responseJob(jobInfo.getId(), jobInfo.getResponse());
		}catch(Exception e){
			throw new JobException("");
		}
	}

}
