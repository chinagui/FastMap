package com.navinfo.dataservice.dao.mq;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.connection.Connection;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;


/** 
 * @ClassName: MsgSubscriber 
 * @author Xiao Xiaowen 
 * @date 2016-3-1 下午1:43:53 
 * @Description: TODO
 */
public class MsgSubscriber {

	protected static Logger log = Logger.getLogger(MsgSubscriber.class);
	private static class SingletonHolder{
		private static final MsgSubscriber INSTANCE = new MsgSubscriber();
	}
	public static final MsgSubscriber getInstance(){
		return SingletonHolder.INSTANCE;
	}
	public void helloWorld()throws Exception{
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
		          String message = new String(body, "UTF-8");
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
	public static void main(String[] args){
		try{
			MsgSubscriber.getInstance().helloWorld();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
