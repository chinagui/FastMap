package com.navinfo.dataservice.api.job.iface;

import com.navinfo.dataservice.api.ExternalService;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobService 
* @author Xiao Xiaowen 
* @date 2016年3月30日 上午10:10:56 
* @Description: TODO
*/
public interface JobExternalService extends ExternalService {
	long createJob(String jobType,JSONObject request,long projectId,long userId,String descp)throws Exception;
}
