package com.navinfo.dataservice.jobframework.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.job.iface.JobApiService;
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
@Service("jobApiService")
public class JobApiServiceImpl implements JobApiService{
	protected Logger log = LoggerRepos.getLogger(this.getClass());

	public long createJob(String jobType,JSONObject request,long projectId,long userId,String descp)throws ServiceException{
		return JobService.getInstance().create(jobType, request, userId, descp);
	}
	public List<JobInfo> getAllJob()throws ServiceException{
		return null;
	}
	public JobInfo getJobById(long jobId)throws ServiceException{
		return null;
	}
	public JobInfo getJobByType(String jobType)throws ServiceException{
		return null;
	}
	public String help() {
		// TODO Auto-generated method stub
		return "Hello,Job External Service.";
	}
	public static void main(String[] args){
		
	}
}
