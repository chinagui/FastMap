package com.navinfo.dataservice.dao.mq.sys;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.dao.mq.MsgSubscriber;
import com.navinfo.dataservice.dao.mq.SubscriberSignal;

/**
 * 
 * @ClassName SysMsgSubscriber
 * @author Han Shaoming
 * @date 2016年9月21日 下午3:24:19
 * @Description 接收消息
 */
public class SysMsgSubscriber {
	protected static Logger log = LoggerRepos.getLogger(SysMsgSubscriber.class);
	/**
	 * 一类消息只需订阅一遍
	 * @param jobMsgType
	 * @param handler
	 * @throws Exception
	 */
	public static void subscribeMsg( final String sysMsgType,final MsgHandler handler,final SubscriberSignal signal){
		try{
			MsgSubscriber.getInstance().subscribeFromWorkQueue(sysMsgType, handler,signal);
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
	public static void cancelSubScribe(String sysMsgType)throws Exception{
		MsgSubscriber.getInstance().cancelSubScribe(sysMsgType);
	}

}
