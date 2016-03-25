package com.navinfo.dataservice.dao.mq.job;

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
	public static void SubscribeCreateJob(JobMsgType jobMsgType,MsgHandler handler)throws Exception{
		MsgSubscriber.subscribeFromWorkQueue(jobMsgType.toString(), handler);
	}
}
