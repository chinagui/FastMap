package com.navinfo.dataservice.dao.mq;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobMsgType;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.job.JobMsgSubscriber;

/** 
* @ClassName: TestMQJobSubscribe 
* @author Xiao Xiaowen 
* @date 2016年4月6日 下午2:56:33 
* @Description: TODO
*/
public class TestMQJobSubscribe {
	private static Logger log = LoggerRepos.getLogger(TestMQJobSubscribe.class);
	
	public static void getRunJobMsg(){
		JobMsgSubscriber.SubscribeJob(JobMsgType.MSG_RUN_JOB, new MsgHandler(){

			@Override
			public void handle(String message) {
				log.info(message);
				try{
					Thread.sleep(5000);
				}catch(Exception e){
					e.printStackTrace();
				}
			}},new SubscriberSignal(){

				@Override
				public boolean needWait() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean isWaiting() {
					// TODO Auto-generated method stub
					return false;
				}
				
			});
	}
	
	public static void main(String[] args){
		try{
			getRunJobMsg();
			log.info("main thread...");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
