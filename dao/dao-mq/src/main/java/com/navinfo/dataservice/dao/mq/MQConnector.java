package com.navinfo.dataservice.dao.mq;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;



/** 
 * @ClassName: MQConnector 
 * @author Xiao Xiaowen 
 * @date 2016-3-3 上午11:04:12 
 * @Description: 单例
 */
public class MQConnector {

	private static class SingletonHolder{
		private static final MQConnector INSTANCE = new MQConnector();
	}
	public static final MQConnector getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private ConnectionFactory connFactory;
	public ConnectionFactory getConnectionFactory(){
		if(connFactory==null){
			synchronized(this){
				if(connFactory==null){
					CachingConnectionFactory facInstance = new CachingConnectionFactory("192.168.4.188",5672);
					facInstance.setUsername("fos");
					facInstance.setPassword("fos");
					connFactory = facInstance;
				}
			}
		}
		return connFactory;
	}
}
