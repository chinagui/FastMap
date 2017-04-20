package com.navinfo.dataservice.api.job.iface;

import java.util.List;
import com.navinfo.dataservice.api.job.model.JobInfo;
import net.sf.json.JSONObject;

/** 
* @ClassName: JobService 
* @author Xiao Xiaowen 
* @date 2016年3月30日 上午10:10:56 
* @Description: TODO
*/
public interface JobApi {
	long createJob(String jobType,JSONObject request,long userId,long taskId,String descp)throws Exception;
	JobInfo getJobById(long jobId)throws Exception;
	JSONObject getLatestJob(int subtaskId)throws Exception;
	JobInfo getJobByGuid(String jobGuid)throws Exception;
	String help();
	List<JobInfo> getJobInfoList(JSONObject parameterJson)throws Exception;
	JobInfo getLatestJobByDescp(String descp)throws Exception;
}
