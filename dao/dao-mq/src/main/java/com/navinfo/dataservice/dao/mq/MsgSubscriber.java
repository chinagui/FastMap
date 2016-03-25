package com.navinfo.dataservice.dao.mq;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.util.StringUtils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;


/** 
 * @ClassName: MsgSubscriber 
 * @author Xiao Xiaowen 
 * @date 2016-3-1 下午1:43:53 
 * @Description: TODO
 */
public class MsgSubscriber {

	protected static Logger log = Logger.getLogger(MsgSubscriber.class);
	/**
	 * 注意：同一个queueName的消息只需要订阅一次
	 * 订阅简单消息，将自动确认消息接收
	 * @param name
	 * @param handler
	 * @throws Exception
	 */
	public static void subscribeFromSimpleQueue(String name,MsgHandler handler)throws Exception{
		Connection conn=null;
		Channel channel = null;
		try{
			conn = MQConnector.getInstance().getConnectionFactory().createConnection();
			channel = conn.createChannel(false);
			channel.queueDeclare(name, false, false, false, null);
			Consumer consumer = new SimpleMsgConsumer(channel,handler);
		    channel.basicConsume(name, true, consumer);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(channel!=null)channel.close();
			if(conn!=null)conn.close();
		}
	}
	/**
	 * 注意：同一个queueName的消息只需要订阅一次
	 * 任务队列需要处理完消息后手动确认
	 * @param name
	 * @param handler
	 * @throws Exception
	 */
	public static void subscribeFromWorkQueue(String name,final MsgHandler handler)throws Exception{

		Connection conn=null;
		Channel channel = null;
		try{
			conn = MQConnector.getInstance().getConnectionFactory().createConnection();
			channel = conn.createChannel(false);
			channel.queueDeclare(name, true, false, false, null);
			channel.basicQos(1);
			QueueingConsumer consumer = new QueueingConsumer(channel);
			//设置auto acknowledgment = false
		    channel.basicConsume(name, false, consumer);
		    while(true){
		    	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		    	String message = new String(delivery.getBody(), "UTF-8");
		    	handler.handle(message);
		    	channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		    }
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(channel!=null)channel.close();
			if(conn!=null)conn.close();
		}
	}
	/**
	 * 接收到消息后，自动确认
	 * @param name
	 * @param handler
	 * @throws Exception
	 */
	public static void subscribeFromBCQueue(String name,MsgHandler handler)throws Exception{
		Connection conn=null;
		Channel channel = null;
		try{
			conn = MQConnector.getInstance().getConnectionFactory().createConnection();
			channel = conn.createChannel(false);
			channel.exchangeDeclare(name, "fanout");
			String queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, name, "");
			
			QueueingConsumer consumer = new QueueingConsumer(channel);
			//设置auto acknowledgment = true
		    channel.basicConsume(queueName, true, consumer);
		    while(true){
		    	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		    	String message = new String(delivery.getBody(), "UTF-8");
		    	handler.handle(message);
		    }
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(channel!=null)channel.close();
			if(conn!=null)conn.close();
		}
	}
	/**
	 * 只接收队列中routingkey in msgIdentitySet的消息
	 * 接收到消息后，自动确认
	 * @param name
	 * @param handler
	 * @throws Exception
	 */
	public static void subscribeFromRoutingQueue(String name,Set<String> msgIdentitySet,RoutingMsgHandler handler)throws Exception{
		if(StringUtils.isEmpty(name)||msgIdentitySet==null||msgIdentitySet.size()==0){
			throw new Exception("name、msgIdentitySet不能为空");
		}
		Connection conn=null;
		Channel channel = null;
		try{
			conn = MQConnector.getInstance().getConnectionFactory().createConnection();
			channel = conn.createChannel(false);
			channel.exchangeDeclare(name, "direct");
			String queueName = channel.queueDeclare().getQueue();
			for(String identity:msgIdentitySet){
				channel.queueBind(queueName, name, identity);
			}
			
			QueueingConsumer consumer = new QueueingConsumer(channel);
			//设置auto acknowledgment = true
		    channel.basicConsume(queueName, true, consumer);
		    while(true){
		    	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		    	String message = new String(delivery.getBody(), "UTF-8");
		    	String routingKey = delivery.getEnvelope().getRoutingKey();
		    	handler.handle(routingKey,message);
		    }
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(channel!=null)channel.close();
			if(conn!=null)conn.close();
		}
	}
	public static void helloWorld()throws Exception{
		Connection conn=null;
		Channel channel = null;
		try{
			conn = MQConnector.getInstance().getConnectionFactory().createConnection();
			channel = conn.createChannel(false);
			channel.queueDeclare("hello_world", false, false, false, null);
		    Consumer consumer = new DefaultConsumer(channel) {
		    	
		        @Override
		        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
		            throws IOException {
		          //String message = new String(body, "UTF-8");
		          //log.debug(" [x] Received '" + message + "'");
		        }
		      };
		      channel.basicConsume("hello_world", true, consumer);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(channel!=null)channel.close();
			if(conn!=null)conn.close();
		}
	}
	
	static class SimpleMsgConsumer extends DefaultConsumer{
		MsgHandler handler = null;
		SimpleMsgConsumer(Channel channel,MsgHandler handler){
			super(channel);
			this.handler=handler;
		}
		@Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
          String message = new String(body, "UTF-8");
          handler.handle(message);
        }
	}
	
	public static void main(String[] args){
		try{
			//MsgSubscriber.helloWorld();
			SimpleMsgConsumer msg1 = new SimpleMsgConsumer(null,null);
			SimpleMsgConsumer msg2 = new SimpleMsgConsumer(null,null);
			System.out.println(msg1);
			System.out.println(msg2);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
