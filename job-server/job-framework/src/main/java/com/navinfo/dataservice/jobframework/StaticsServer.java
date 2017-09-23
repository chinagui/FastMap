package com.navinfo.dataservice.jobframework;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.job.model.JobMsgType;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

/** 
* @ClassName: JobServer 
* @author Xiao Xiaowen 
* @date 2016年5月6日 上午10:45:33 
* @Description: TODO
*/
public class StaticsServer {
	protected static Logger log = LoggerRepos.getLogger(StaticsServer.class);
	public static void main(String[] args){
		try{
			log.info("Starting Statics Server...");
			initContext();
			JobFinder finder = new JobFinderFromMQ();
			finder.startFinding(JobMsgType.MSG_RUN_STATICS_JOB);
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void initContext(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-app-jobserver.xml","dubbo-jobserver.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		log.info("initialized app context.");
	}
}
