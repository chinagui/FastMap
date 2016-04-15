package com.navinfo.dataservice.api.job.iface;

import com.navinfo.dataservice.api.ServiceException;
import com.navinfo.dataservice.api.job.model.JobType;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobService 
* @author Xiao Xiaowen 
* @date 2016年3月30日 上午10:10:56 
* @Description: TODO
*/
public interface JobExternalService {
	long createJob(JobType jobType,JSONObject request,long projectId,long userId,String descp)throws ServiceException;
}
