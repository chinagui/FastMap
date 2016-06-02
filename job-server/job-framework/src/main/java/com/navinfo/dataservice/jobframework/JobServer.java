package com.navinfo.dataservice.jobframework;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobMsgType;
import com.navinfo.dataservice.commons.log.LoggerRepos;

/** 
* @ClassName: JobServer 
* @author Xiao Xiaowen 
* @date 2016年5月6日 上午10:45:33 
* @Description: TODO
*/
public class JobServer {
	protected static Logger log = LoggerRepos.getLogger(JobServer.class);
	public static void main(String[] args){
		try{
			log.info("Starting Server...");
			JobFinder finder = new JobFinderFromMQ();
			finder.startFinding(JobMsgType.MSG_RUN_JOB);
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
