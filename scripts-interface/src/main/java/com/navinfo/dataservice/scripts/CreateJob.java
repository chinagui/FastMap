package com.navinfo.dataservice.scripts;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.service.JobService;

import net.sf.json.JSONObject;

/** 
 * @ClassName: CreateJob
 * @author xiaoxiaowen4127
 * @date 2017年2月24日
 * @Description: CreateJob.java
 */
public class CreateJob {
	protected static Logger log = LoggerRepos.getLogger(InitDesgdb.class);
	public static JSONObject execute(JSONObject request) throws Exception{
		JSONObject response = new JSONObject();
		if (request == null) {
			throw new IllegalArgumentException("request参数不能为空。");
		}
		if(request.get("jobType")==null){
			throw new IllegalArgumentException("jobType参数不能为空。");
		}
		if(request.get("request")==null){
			throw new IllegalArgumentException("request参数不能为空。");
		}
		String jobType = request.getString("jobType");
		JSONObject jobRequest = request.getJSONObject("request");
		long userId = 0;
		String descp = null;
		if(request.containsKey("descp")){
			descp = request.getString("descp");
		}
		long taskId=0L;
		if(request.containsKey("taskId")){
			taskId=request.getLong("taskId");
		}
		long jobId = JobService.getInstance().create(jobType, jobRequest, userId,taskId, descp);
		log.info("Job created. jobId:"+jobId);
		response.put("jobId", jobId);
		return response;
	}
}
