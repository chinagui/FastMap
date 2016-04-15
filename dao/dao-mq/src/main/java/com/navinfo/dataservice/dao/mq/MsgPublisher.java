package com.navinfo.dataservice.dao.mq;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.util.StringUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;




/** 
 * @ClassName: MsgPublisher 
 * @author Xiao Xiaowen 
 * @date 2016-3-1 下午1:43:14 
 * @Description: TODO
 */
public class MsgPublisher {
	protected static  Logger  log = Logger.getLogger(MsgPublisher.class);
	/**
	 * 简单信息不持久化
	 * @param name
	 * @param msgContent
	 * @throws Exception
	 */
	public static void publish2SimpleQueue(String name,String msgContent)throws Exception{

		if(StringUtils.isEmpty(name)||StringUtils.isEmpty(msgContent)){
			throw new Exception("queueName和msgContent不能为空");
		}
		Connection conn = null;
		Channel channel = null;
		try{
			conn = MQConnector.getInstance().getConnectionFactory().createConnection();
			channel = conn.createChannel(false);
			channel.queueDeclare(name, false, false, false, null);
			channel.basicPublish("", name, null, msgContent.getBytes());
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(channel!=null)channel.close();
			if(conn!=null)conn.close();
		}
	}
	/**
	 * 任务队列会持久化
	 * @param name
	 * @param msgContent
	 * @throws Exception
	 */
	public static void publish2WorkQueue(String name,String msgContent)throws Exception{
		if(StringUtils.isEmpty(name)||StringUtils.isEmpty(msgContent)){
			throw new Exception("name和msgContent不能为空");
		}
		Connection conn = null;
		Channel channel = null;
		try{
			conn = MQConnector.getInstance().getConnectionFactory().createConnection();
			channel = conn.createChannel(false);
			channel.queueDeclare(name, true, false, false, null);
			channel.basicPublish("", name, MessageProperties.PERSISTENT_TEXT_PLAIN, msgContent.getBytes());
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(channel!=null)channel.close();
			if(conn!=null)conn.close();
		}
	}
	public static void publish2BCQueue(String name,String msgContent)throws Exception{
		if(StringUtils.isEmpty(name)||StringUtils.isEmpty(msgContent)){
			throw new Exception("name和msgContent不能为空");
		}
		Connection conn = null;
		Channel channel = null;
		try{
			conn = MQConnector.getInstance().getConnectionFactory().createConnection();
			channel = conn.createChannel(false);
			channel.exchangeDeclare(name, "fanout");
			channel.basicPublish(name, "",null, msgContent.getBytes());
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(channel!=null)channel.close();
			if(conn!=null)conn.close();
		}
	}
	public static void publish2RoutingQueue(String name,String msgIdentity,String msgContent)throws Exception{
		if(StringUtils.isEmpty(name)||StringUtils.isEmpty(msgIdentity)||StringUtils.isEmpty(msgContent)){
			throw new Exception("name、msgIdentity和msgContent不能为空");
		}
		Connection conn = null;
		Channel channel = null;
		try{
			conn = MQConnector.getInstance().getConnectionFactory().createConnection();
			channel = conn.createChannel(false);
			channel.exchangeDeclare(name, "direct");
			channel.basicPublish(name, msgIdentity,null, msgContent.getBytes());
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(channel!=null)channel.close();
			if(conn!=null)conn.close();
		}
	}
	public static void helloWord(String content)throws Exception{
		Connection connection = MQConnector.getInstance().getConnectionFactory().createConnection();
		Channel channel = connection.createChannel(false);
		channel.queueDeclare("hello_world", false, false, false, null);
		channel.basicPublish("", "hello_world", null, content.getBytes());
		channel.close();
		connection.close();
	}
	public static void main(String[] args){
		try{
			for(int i=0;i<10;i++){
				new Thread(){
					@Override
					public void run() {
						try{
						    int count=0;
						    while(true){
								log.debug("sending msg...");
								while(count<100000){
									MsgPublisher.helloWord(count+": Hello, Data Services.AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
									count++;
								}
								log.debug("sent msg...");
								count=0;
								Thread.sleep(2000);
						    }
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					
				}.start();
			}
			//System.exit(-1);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
