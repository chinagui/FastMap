package com.navinfo.dataservice.web.man.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.navinfo.dataservice.dao.mq.MsgSubscriber;
import com.navinfo.dataservice.dao.mq.email.EmailSubscriber;
import com.navinfo.dataservice.dao.mq.sys.SysMsgSubscriber;
import com.navinfo.dataservice.dao.mq.sys.SysMsgType;
import com.navinfo.dataservice.engine.man.mqmsg.InfoChangeMsgHandler;
import com.navinfo.dataservice.engine.man.mqmsg.InfoFeedbackMsgHandler;
import com.navinfo.dataservice.engine.man.mqmsg.SendEmailMsgHandler;

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
						MsgSubscriber.getInstance().subscribeFromWorkQueue("Info_Change", new InfoChangeMsgHandler(), null);
					}catch(Exception e){
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}.start();
			//发送邮件
			new Thread(){
				@Override
				public void run(){
					try{
						EmailSubscriber.subscribeMsg("send_email", new SendEmailMsgHandler(), null);
					}catch(Exception e){
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}.start();
			//一级情报反馈
			new Thread(){
				@Override
				public void run() {
					try{
						MsgSubscriber.getInstance().subscribeFromWorkQueue("Info_Feedback", new InfoFeedbackMsgHandler(), null);
					}catch(Exception e){
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	
}
