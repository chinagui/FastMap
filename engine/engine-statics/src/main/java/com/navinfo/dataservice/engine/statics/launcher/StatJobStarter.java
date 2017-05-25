package com.navinfo.dataservice.engine.statics.launcher;

import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.job.model.RunJobInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.jobframework.service.JobService;

/** 
 * @ClassName: StatJobStarter
 * @author xiaoxiaowen4127
 * @date 2017年5月18日
 * @Description: StatJobStarter.java
 */
public abstract class StatJobStarter {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	public abstract String jobType();
	
	/**
	 * 判断是否已经有相同类型的job正在执行
	 * @return
	 * @throws Exception 
	 */
	protected boolean isRunning() throws Exception{
		Map<String, Object> jobDetail = JobService.getInstance().getJobByTask(0,Long.valueOf(0),jobType());
		if(jobDetail==null||jobDetail.size()==0){return false;}
		int status=(int) jobDetail.get("status");
		if(status==3||status==4){
			return false;
		}
		log.info("有正在执行的"+jobType()+"任务，本次统计不执行");
		return true;
	};
	
	/**
	 * 如果不需要启动，RunJobInfo==null，配置job启动参数
	 * @return
	 */
	protected RunJobInfo startRun(){		
		//默认启动参数timestamp，取当前时间的小时的整点
		String timestamp=DateUtils.dateToString(DateUtils.getSysdate(), "yyyyMMddHHss00");
		JSONObject request=new JSONObject();
		request.put("timestamp", timestamp);
		RunJobInfo info = new RunJobInfo(jobType(),request);
		return info;
	}
	
	public void start(){
		start(null);
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
			//构造统计job参数
			info = startRun();
			if(info==null){
				return false;
			}
			if(StringUtils.isNotEmpty(timestamp)){
				info.getRequest().put("timestamp", timestamp);
			}
			log.info("create job:jobType="+jobType()+",timestamp="+info.getRequest().getString("timestamp"));
			JobApi jobApi = (JobApi)ApplicationContextUtil.getBean("jobApi");
			jobApi.createJob(info.getJobType(), info.getRequest(), info.getUserId(), info.getTaskId(), info.getDescp());
			return true;
		}catch(Exception e){
			log.warn("jobType:"+jobType()+"，request:"+(info==null||info.getRequest()==null?"null":info.getRequest().toString())+"启动错误:"+e.getMessage(),e);
		}
		return false;
	}
}
