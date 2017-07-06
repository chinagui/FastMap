package com.navinfo.dataservice.monitor.agent.job;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.monitor.agent.quartz.QuartzManager;
import com.navinfo.dataservice.monitor.agent.runjob.AbstractJob;

/**
 * 定时任务服务
 * 
 * @ClassName JobServer
 * @author Han Shaoming
 * @date 2017年6月26日 下午2:19:00
 * @Description TODO
 */
public class JobServer {

	protected static Logger log = LoggerRepos.getLogger(JobServer.class);
	
	public static void run() throws Exception	{
		try {
			// 获取待执行job
			List<JobConfig> jobs = JobCreate.getJobs();

			if (jobs == null || jobs.size() < 1)
				log.info("没有配置任务实例!");

			for (int i = 0; i < jobs.size(); i++) {
				JobConfig jobConfig = jobs.get(i);
				try {
					if (jobConfig.isActivity()) {
						scheduleJob(jobConfig);
						log.info("任务实例[" + jobConfig.getName() + "]已加入定时调度.");
					} else {
						log.info(jobConfig.getName() + "任务实例Activity=false 不进行处理");
					}
				} catch (Exception cve) {
					// 配置错误忽略这个任务
					log.error(cve.getMessage(),cve);
				}
			}
			/* 启动调度服务器 */
			startJobServer();
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			log.info("启动出现异常,3秒钟后自动关闭");
			Thread.sleep(1000 * 3);
			QuartzManager.shutdownJobs();
		}

	}


	/**
	 * 调度一个任务
	 */
	private static void scheduleJob(JobConfig jobConfig) throws Exception {
		String className = jobConfig.getClassName();
		AbstractJob job = (AbstractJob) Class.forName(className).newInstance();

		// 将具体的任务实例存储到jobdetail中，这样每次触发jobadapter时，都会调用我们声明的deliveryJob这个实例了。
		String jobName = jobConfig.getName();
		String time = jobConfig.getScanPeriod();
		QuartzManager.addJob(jobName,JobAdapter.class,time,job);
	}

	/**
	 * 启动调度服务
	 * 
	 * @throws SchedulerException
	 */
	private static void startJobServer() throws SchedulerException {
		QuartzManager.startJobs();
	}

}
