package com.navinfo.dataservice.dao.mq;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;

public class TestMQSubscribe {
	protected static  Logger  log = LoggerRepos.getLogger(TestMQSubscribe.class);
	public static void readRunJob()throws Exception{
		
	}
	public static void readWorkQueueMsg()throws Exception{

		MsgSubscriber.getInstance().subscribeFromWorkQueue("run_job", new MsgHandler(){

			@Override
			public void handle(String message) {
				log.info(message);
				try{
					Thread.sleep(8000);
				}catch(Exception e){
					e.printStackTrace();
				}
			}},new SubscriberSignal(){

				@Override
				public boolean needWait() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean isWaiting() {
					// TODO Auto-generated method stub
					return false;
				}
				
			});
		log.info("Over...");
	}
	public static void getWorkQueueMsg() throws Exception{
		while(true){
			final MsgSubscriber sub = MsgSubscriber.getInstance();
			new Thread(){
				@Override
				public void run(){
					try{
						sub.subscribeFromWorkQueue("work_queue", new MsgHandler(){

							@Override
							public void handle(String message) {
								log.info(message);
								try{
									Thread.sleep(1000);
								}catch(Exception e){
									e.printStackTrace();
								}
							}
							
						},null);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}.start();
			new Thread(){
				@Override
				public void run(){
					try{
						Thread.sleep(5000);
						sub.cancelSubScribe("work_queue");
						System.out.println("Over....");
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}.start();
			Thread.sleep(8000);
		}
	}
	public static void main(String[] args){
		try{
			readWorkQueueMsg();
//			getWorkQueueMsg();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
