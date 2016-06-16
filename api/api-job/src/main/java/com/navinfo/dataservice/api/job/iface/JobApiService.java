package com.navinfo.dataservice.api.job.iface;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobService 
* @author Xiao Xiaowen 
* @date 2016年3月30日 上午10:10:56 
* @Description: TODO
*/
public interface JobApiService {
	long createJob(String jobType,JSONObject request,long userId,String descp)throws Exception;
	String help();
}
