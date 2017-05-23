package com.navinfo.dataservice.engine.statics.launcher;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.job.model.RunJobInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

/** 
 * @ClassName: StatJobStarter
 * @author xiaoxiaowen4127
 * @date 2017年5月18日
 * @Description: StatJobStarter.java
 */
public abstract class StatJobStarter {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	public abstract String jobType();
	
	protected abstract boolean isRunning();
	
	/**
	 * 如果不需要启动，RunJobInfo==null
	 * @return
	 */
	protected abstract RunJobInfo startRun();
	
	public boolean start(){
		return start(null);
	}
	public boolean start(String timestamp){
		RunJobInfo info = null;
		try{
			//根据配置，是否可以重复启动相同的统计job
			if(!SystemConfigFactory.getSystemConfig().getBooleanValue("stat.job.parallel")){
				if(isRunning()){
					return false;
				}
			}
			//
			info = startRun();
			if(info==null){
				return false;
			}
			if(StringUtils.isNotEmpty(timestamp)){
				info.getRequest().put("timestamp", timestamp);
			}
			JobApi jobApi = (JobApi)ApplicationContextUtil.getBean("jobApi");
			jobApi.createJob(info.getJobType(), info.getRequest(), info.getUserId(), info.getTaskId(), info.getDescp());
			return true;
		}catch(Exception e){
			log.warn("jobType:"+jobType()+"，request:"+(info==null||info.getRequest()==null?"null":info.getRequest().toString())+"启动错误:"+e.getMessage());
		}
		return false;
	}
}
