package com.navinfo.dataservice.jobframework;


import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobMsgType;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.job.JobMsgSubscriber;
import com.navinfo.dataservice.jobframework.runjob.RunJobHandler;
import com.navinfo.dataservice.jobframework.runjob.RunJobSubscriberSignal;
import com.navinfo.dataservice.jobframework.service.ResponseJobHandler;

/** 
* @ClassName: JobFinderFromMQ 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午5:01:03 
* @Description: TODO
*/
public class JobFinder4RunFromMQ implements JobFinder {
	protected static Logger log = LoggerRepos.getLogger(JobFinder4RunFromMQ.class);
	@Override
	public void startFinding(JobMsgType jobMsgType) throws Exception{
		log.info("Starting find JobMsgType:"+jobMsgType.toString());
		switch(jobMsgType){
		case MSG_RUN_JOB:
			JobMsgSubscriber.SubscribeJob(JobMsgType.MSG_RUN_JOB, new RunJobHandler(),new RunJobSubscriberSignal());
			break;
		case MSG_RESPONSE_JOB:
			JobMsgSubscriber.SubscribeJob(JobMsgType.MSG_RESPONSE_JOB, new ResponseJobHandler(),null);
			break;
		case MSG_CREATE_JOB:
		case MSG_END_JOB:
			// ...
			break;
		}
	}
	@Override
	public void stopFinding(JobMsgType jobMshType)  throws Exception{
		JobMsgSubscriber.cancelSubScribe(jobMshType);
	}
	


}
