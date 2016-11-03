package com.navinfo.dataservice.dao.mq.email;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.dao.mq.MsgSubscriber;
import com.navinfo.dataservice.dao.mq.SubscriberSignal;
import com.navinfo.dataservice.dao.mq.sys.SysMsgSubscriber;

/**
 * 订阅消息
 * @ClassName EmailSubscriber
 * @author Han Shaoming
 * @date 2016年11月3日 下午4:47:45
 * @Description TODO
 */
public class EmailSubscriber {
	
	protected static Logger log = LoggerRepos.getLogger(SysMsgSubscriber.class);
	/**
	 * 一类消息只需订阅一遍
	 * @param jobMsgType
	 * @param handler
	 * @throws Exception
	 */
	public static void subscribeMsg( final String emailType,final MsgHandler handler,final SubscriberSignal signal){
		try{
			MsgSubscriber.getInstance().subscribeFromWorkQueue(emailType, handler,signal);
		}catch(Exception e){
			e.printStackTrace();
			log.error(e.getMessage(),e);
		}
	}
	public static void cancelSubScribe(String emailType)throws Exception{
		MsgSubscriber.getInstance().cancelSubScribe(emailType);
	}

}
