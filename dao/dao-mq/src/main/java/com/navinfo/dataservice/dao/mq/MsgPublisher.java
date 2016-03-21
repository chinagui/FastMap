package com.navinfo.dataservice.dao.mq;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.connection.Connection;

import com.rabbitmq.client.Channel;




/** 
 * @ClassName: MsgPublisher 
 * @author Xiao Xiaowen 
 * @date 2016-3-1 下午1:43:14 
 * @Description: TODO
 */
public class MsgPublisher {
	protected static  Logger  log = Logger.getLogger(MsgPublisher.class);
	private static class SingletonHolder{
		private static final MsgPublisher INSTANCE = new MsgPublisher();
	}
	public static final MsgPublisher getInstance(){
		return SingletonHolder.INSTANCE;
	}
	public void helloWord(String content)throws Exception{
		//Connection connection = factory.newConnection();
		Connection connection = MQConnector.getInstance().getConnectionFactory().createConnection();
		//Channel channel = connection.createChannel(true);
		Channel channel = connection.createChannel(false);
		channel.queueDeclare("hello_world", false, false, false, null);
		channel.basicPublish("", "hello_world", null, content.getBytes());
		channel.close();
		connection.close();
	}
	public void createJob(String type,JSONObject jobRequest)throws Exception{

	}
	public void runJob(long jobId,String jobType,JSONObject jobRequest)throws Exception{
		
	}
	public void responseJob(long jobId,JSONObject jobResponse)throws Exception{
		
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
									MsgPublisher.getInstance().helloWord(count+": Hello, Data Services.AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
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
