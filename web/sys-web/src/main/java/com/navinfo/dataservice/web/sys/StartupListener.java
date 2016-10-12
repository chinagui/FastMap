package com.navinfo.dataservice.web.sys;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.navinfo.dataservice.dao.mq.sys.SysMsgSubscriber;
import com.navinfo.dataservice.dao.mq.sys.SysMsgType;
import com.navinfo.dataservice.engine.sys.msg.handle.SysMsgHandler;

/**
 * 
 * @ClassName StartupListener
 * @author Han Shaoming
 * @date 2016年9月21日 下午7:24:38
 * @Description TODO
 */
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(event.getApplicationContext().getParent() == null){
			new Thread(){
				@Override
				public void run(){
					try{
						SysMsgSubscriber.subscribeMsg(SysMsgType.MSG_ALL_JOB, new SysMsgHandler(), null);
					}catch(Exception e){
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}.start();
			new Thread(){
				@Override
				public void run(){
					try{
						SysMsgSubscriber.subscribeMsg(SysMsgType.MSG_PERSONAL_JOB, new SysMsgHandler(), null);
					}catch(Exception e){
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}.start();
			new Thread(){
				@Override
				public void run(){
					try{
						SysMsgSubscriber.subscribeMsg(SysMsgType.MSG_GROUP_JOB, new SysMsgHandler(), null);
					}catch(Exception e){
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	
}
