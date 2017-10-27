package com.navinfo.dataservice.api.job.iface;

import java.util.List;
import java.util.Map;

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
	long createStaticsJob(String jobType,JSONObject request,long userId,long taskId,String descp)throws Exception;
	JobInfo getJobById(long jobId)throws Exception;
	JSONObject getLatestJob(int subtaskId, String jobType, String jobDescp)throws Exception;
	JobInfo getJobByGuid(String jobGuid)throws Exception;
	String help();
	List<JobInfo> getJobInfoList(JSONObject parameterJson)throws Exception;
	JobInfo getLatestJobByDescp(String descp)throws Exception;
	JobInfo getJobByDescp(String descp)throws Exception;
	Integer getJobByUserAndSubTask(long userId, long subtaskId,String jobType)throws Exception;
	
	/**
	 * 判断是否有正在执行的job
	 * @author Han Shaoming
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> getJobIsRunning(String jobType) throws Exception;
}
