package com.navinfo.dataservice.dao.mq;

import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import com.rabbitmq.client.Channel;



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
	protected Logger log = Logger.getLogger(MQConnector.class);
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
	/**
	 * 采用CachingConnectionFactory，不需要关闭connection
	 * @return
	 */
	public Connection getConnection(){
		return getConnectionFactory().createConnection();
	}
	public Channel createChannel(Connection conn){
		return conn.createChannel(false);
	}
	/**
	 * 只有采用连接池的情形可用
	 * @return
	 */
	public Channel createChannel(){
		return getConnection().createChannel(false);
	}
	public void closeChannelQuietly(Channel channel){
		try{
			if(channel!=null)channel.close();
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
}
