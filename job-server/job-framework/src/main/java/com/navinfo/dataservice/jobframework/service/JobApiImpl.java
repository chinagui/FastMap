package com.navinfo.dataservice.jobframework.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobInfoService 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午5:14:03 
* @Description: TODO
*/
@Service("jobApi")
public class JobApiImpl implements JobApi{
	protected Logger log = LoggerRepos.getLogger(this.getClass());

	public long createJob(String jobType,JSONObject request,long userId,long taskId,String descp)throws Exception{
		try{
			return JobService.getInstance().create(jobType, request, userId,taskId,descp);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new Exception(e.getMessage(),e);
		}
	}
	public JobInfo getJobById(long jobId)throws Exception{
		try{
			return JobService.getInstance().getJobById(jobId);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new Exception(e.getMessage(),e);
		}
	}
	public JobInfo getJobByGuid(String jobGuid)throws Exception{
		try{
			return JobService.getInstance().getJobByGuid(jobGuid);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new Exception(e.getMessage(),e);
		}
	}
	public String help() {
		return "Hello,Job Api.";
	}
	public static void main(String[] args){
		
	}
}
