package com.navinfo.dataservice.monitor.agent.job;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.monitor.agent.model.Constrant;
import com.navinfo.dataservice.monitor.agent.runjob.AbstractJob;

/**
 * job任务适配器
 * @ClassName JobAdapter
 * @author Han Shaoming
 * @date 2017年6月26日 下午1:52:59
 * @Description TODO
 */
public class JobAdapter implements Job {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getKey().getName();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();  
        Object jobObj = dataMap.get(Constrant.JOB_CLASS_NAME);
        log.info("job类型:"+jobObj +",任务名字:"+jobName+"任务时间:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        if (jobObj instanceof AbstractJob) {
        	AbstractJob job = (AbstractJob) jobObj;
        	job.execute();
		} else {
			log.info("未知的job类型:" + jobObj.getClass());
		}
	}

}
