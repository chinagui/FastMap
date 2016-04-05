package com.navinfo.dataservice.jobframework.service;

import java.util.List;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.jobframework.exception.JobServiceException;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobInfoService 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午5:14:03 
* @Description: TODO
*/
public class JobInfoService {

	public long createJob(String jobType,JSONObject request,long projectId,long userId)throws JobServiceException{
		return 0L;
	}
	public List<JobInfo> getAllJob()throws JobServiceException{
		return null;
	}
	public JobInfo getJobById(long jobId)throws JobServiceException{
		return null;
	}
	public JobInfo getJobByType(String jobType)throws JobServiceException{
		return null;
	}
}
