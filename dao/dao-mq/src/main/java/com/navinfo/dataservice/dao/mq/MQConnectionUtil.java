package com.navinfo.dataservice.dao.mq;

import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.connection.Connection;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.rabbitmq.client.Channel;

/** 
* @ClassName: MQConnectionUtil 
* @author Xiao Xiaowen 
* @date 2016年4月5日 上午10:08:52 
* @Description: TODO
*/
public class MQConnectionUtil {

	protected static Logger log = LoggerRepos.getLogger(MQConnectionUtil.class);
	public static void closeQuietly(Connection conn){
		try{
			if(conn!=null)conn.close();
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
	public static void closeQuietly(Channel channel){
		try{
			if(channel!=null)channel.close();
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
}
