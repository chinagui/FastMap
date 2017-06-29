package com.navinfo.dataservice.monitor.agent.quartz;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.monitor.agent.starter.AgentStarter;

/**
 * 定时任务执行类
 * @ClassName QuartzJob
 * @author Han Shaoming
 * @date 2017年6月16日 上午9:20:40
 * @Description TODO
 */
public class ServiceJob implements Job {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//执行方法
		AgentStarter.serviceRun();
        String jobName = context.getJobDetail().getKey().getName();
        log.info("任务名称是="+jobName+",任务时间:"+	new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
	}

}
