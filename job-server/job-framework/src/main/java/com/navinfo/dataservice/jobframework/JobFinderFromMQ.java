package com.navinfo.dataservice.jobframework;

import java.util.Set;

import com.navinfo.dataservice.dao.mq.job.JobMsgSubscriber;
import com.navinfo.dataservice.dao.mq.job.JobMsgType;

/** 
* @ClassName: JobFinderFromMQ 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午5:01:03 
* @Description: TODO
*/
public class JobFinderFromMQ implements JobFinder {
	@Override
	public void startFinding() throws Exception{
		JobMsgSubscriber.SubscribeJob(JobMsgType.MSG_RUN_JOB, new RunJobHandler());
	}
	@Override
	public void stopFinding()  throws Exception{
		JobMsgSubscriber.cancelSubScribe(JobMsgType.MSG_RUN_JOB);
	}

}
