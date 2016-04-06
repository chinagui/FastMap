package com.navinfo.dataservice.jobframework.runjob;


import com.navinfo.dataservice.api.job.model.JobMsgType;
import com.navinfo.dataservice.dao.mq.job.JobMsgSubscriber;
import com.navinfo.dataservice.jobframework.JobFinder;

/** 
* @ClassName: JobFinderFromMQ 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午5:01:03 
* @Description: TODO
*/
public class JobFinder4RunFromMQ implements JobFinder {
	@Override
	public void startFinding() throws Exception{
		JobMsgSubscriber.SubscribeJob(JobMsgType.MSG_RUN_JOB, new RunJobHandler());
	}
	@Override
	public void stopFinding()  throws Exception{
		JobMsgSubscriber.cancelSubScribe(JobMsgType.MSG_RUN_JOB);
	}

}
