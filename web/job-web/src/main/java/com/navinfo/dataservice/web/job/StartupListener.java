package com.navinfo.dataservice.web.job;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.navinfo.dataservice.api.job.model.JobMsgType;
import com.navinfo.dataservice.jobframework.JobFinder;
import com.navinfo.dataservice.jobframework.JobFinderFromMQ;

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
			try{
				JobFinder finder = new JobFinderFromMQ();
				finder.startFinding(JobMsgType.MSG_RESPONSE_JOB);
			}catch(Exception e){
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
}
