package com.navinfo.dataservice.monitor.agent.quartz;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.monitor.agent.starter.AgentStarter;

public class TomcatJob implements Job {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		AgentStarter.tomcatRun();
		String jobName = context.getJobDetail().getKey().getName();
        log.info("任务名称是="+jobName+",任务时间:"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ").format(new Date()));
	}

}
