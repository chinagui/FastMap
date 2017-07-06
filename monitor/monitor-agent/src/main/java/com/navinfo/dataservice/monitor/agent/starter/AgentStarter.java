package com.navinfo.dataservice.monitor.agent.starter;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.monitor.agent.job.JobServer;

/** 
 * @ClassName: AgentStarter
 * @author xiaoxiaowen4127
 * @date 2017年6月13日
 * @Description: AgentStarter.java
 */
public class AgentStarter {
	
	protected static Logger log = LoggerRepos.getLogger(AgentStarter.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			//开始定时服务
			JobServer.run();

        } catch (Exception e) {  
            log.error(e.getMessage(),e);
        }

	}
	
	
}
