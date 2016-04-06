package com.navinfo.dataservice.dao.mq.job;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobMsgType;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.dao.mq.MsgSubscriber;
import com.navinfo.dataservice.dao.mq.SubscriberSignal;

/** 
* @ClassName: JobMsgSubscriber 
* @author Xiao Xiaowen 
* @date 2016年3月24日 上午10:17:22 
* @Description: TODO
*/
public class JobMsgSubscriber {
	protected static Logger log = LoggerRepos.getLogger(JobMsgSubscriber.class);
	/**
	 * 一类消息只需订阅一遍
	 * @param jobMsgType
	 * @param handler
	 * @throws Exception
	 */
	public static void SubscribeJob(final JobMsgType jobMsgType,final MsgHandler handler,final SubscriberSignal signal){
		new Thread(){
			@Override
			public void run(){
				try{
					this.setName(jobMsgType.toString()+" main thread");
					MsgSubscriber.getInstance().subscribeFromWorkQueue(jobMsgType.toString(), handler,signal);
				}catch(Exception e){
					log.error(e.getMessage(),e);
				}
			}
		}.start();
	}
	public static void cancelSubScribe(JobMsgType jobMsgType)throws Exception{
		MsgSubscriber.getInstance().cancelSubScribe(jobMsgType.toString());
	}
	
}
