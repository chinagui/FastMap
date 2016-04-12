package com.navinfo.dataservice.dao.mq;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;

/** 
* @ClassName: TestMQ 
* @author Xiao Xiaowen 
* @date 2016年4月5日 下午6:00:33 
* @Description: TODO
*/
public class TestMQPublish {
	protected static  Logger  log = LoggerRepos.getLogger(TestMQPublish.class);
	public static void sendRunJobMsg(){
		try{
		    int count=0;
			while(count<100000){
				MsgPublisher.publish2WorkQueue("run_job", count+": Hello, Data Services.");
				count++;
				log.info("sended msg index:"+count);
				Thread.sleep(500);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void sendWorkQueue(){
		try{
			try{
			    int count=0;
				while(count<100000){
					MsgPublisher.publish2WorkQueue("work_queue", count+": Hello, Data Services.");
					count++;
					log.info("sended msg index:"+count);
					Thread.sleep(100);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			//System.exit(-1);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		sendWorkQueue();
	}
}
