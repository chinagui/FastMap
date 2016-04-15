package com.navinfo.dataservice.dao.mq;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;

import com.alibaba.druid.util.StringUtils;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
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
	private final String MAIN_KEY="main";
	private Map<String,ConnectionFactory> connFactoryMap=new HashMap<String,ConnectionFactory>();
	public ConnectionFactory getConnectionFactory()throws MQConnectionException{
		return getConnectionFactory(MAIN_KEY);
	}
	public ConnectionFactory getConnectionFactory(String key)throws MQConnectionException{
		if(StringUtils.isEmpty(key)){
			throw new MQConnectionException("传入键值为空，无法初始化ConnectionFacgtory。");
		}
		ConnectionFactory connFactory = connFactoryMap.get(key);
		if(connFactory==null){
			synchronized(this){
				connFactory = connFactoryMap.get(key);
				if(connFactory==null){
					String uriString = SystemConfigFactory.getSystemConfig().getValue(key+".mq.uri");
					if(StringUtils.isEmpty(uriString)){
						throw new MQConnectionException("该键值未配置系统参数，无法获取uri，无法初始化ConnectionFacgtory。");
					}
					try{
						//单connection，多cache channel模式
						CachingConnectionFactory facInstance = new CachingConnectionFactory(new URI(uriString));
						facInstance.setChannelCacheSize(SystemConfigFactory.getSystemConfig().getIntValue(key+".mq.cacheSize.channel", 20));
						connFactory = facInstance;
					}catch(Exception e){
						throw new MQConnectionException("在系统参数中该键值配置的uri格式错误，无法初始化ConnectionFacgtory。");
					}
					connFactoryMap.put(key, connFactory);
				}
			}
		}
		return connFactory;
	}

	/**
	 * 获取默认消息队列服务器的连接
	 */
	public Connection getConnection()throws MQConnectionException{
		return getConnectionFactory().createConnection();
	}
	/**
	 * 
	 * @param key:消息队列服务器的类型键值
	 * @return
	 * @throws MQConnectionException
	 */
	public Connection getConnection(String key)throws MQConnectionException{
		return getConnectionFactory(key).createConnection();
	}
}
