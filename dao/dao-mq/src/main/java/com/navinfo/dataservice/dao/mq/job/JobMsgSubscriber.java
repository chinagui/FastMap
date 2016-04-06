package com.navinfo.dataservice.dao.mq.job;

import com.navinfo.dataservice.api.job.model.JobMsgType;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.dao.mq.MsgSubscriber;

/** 
* @ClassName: JobMsgSubscriber 
* @author Xiao Xiaowen 
* @date 2016年3月24日 上午10:17:22 
* @Description: TODO
*/
public class JobMsgSubscriber {
	/**
	 * 一类消息只需订阅一遍
	 * @param jobMsgType
	 * @param handler
	 * @throws Exception
	 */
	public static void SubscribeJob(JobMsgType jobMsgType,MsgHandler handler)throws Exception{
		MsgSubscriber.getInstance().subscribeFromWorkQueue(jobMsgType.toString(), handler);
	}
	public static void cancelSubScribe(JobMsgType jobMsgType)throws Exception{
		MsgSubscriber.getInstance().cancelSubScribe(jobMsgType.toString());
	}
}
