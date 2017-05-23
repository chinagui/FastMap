package com.navinfo.dataservice.web.statics.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.navinfo.dataservice.dao.mq.MsgSubscriber;
import com.navinfo.dataservice.engine.statics.launcher.StatJobEndHandler;
import com.navinfo.dataservice.engine.statics.mqmsg.StaticsResulthandler;

/** 
* @ClassName: StartupListener 
* @author Xiao Xiaowen 
* @date 2016年3月30日 下午1:58:04 
* @Description: TODO
*/
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(event.getApplicationContext().getParent() == null){
			new Thread(){
				@Override
				public void run() {
					try{
						MsgSubscriber.getInstance().subscribeFromWorkQueue("Statics_Result", new StaticsResulthandler(), null);
					}catch(Exception e){
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}.start();
			new Thread(){
				@Override
				public void run() {
					try{
						MsgSubscriber.getInstance().subscribeFromWorkQueue("Statics_Job_End", new StatJobEndHandler(), null);
					}catch(Exception e){
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	
}
