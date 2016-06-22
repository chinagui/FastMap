package com.navinfo.dataservice.dao.mq.job;

import org.springframework.util.StringUtils;

import com.navinfo.dataservice.api.job.model.JobStep;
import com.navinfo.dataservice.dao.mq.MsgPublisher;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobMsgPublisher 
* @author Xiao Xiaowen 
* @date 2016年3月23日 下午5:57:57 
* @Description: TODO
*/
public class JobMsgPublisher {

	/**
	 * 返回创建job的唯一标识
	 * @param type
	 * @param jobRequest
	 * @return
	 * @throws Exception
	 */
	public static String createJob(String guid,String type,JSONObject jobRequest)throws Exception{
		if(StringUtils.isEmpty(type)||jobRequest==null){
			throw new Exception("typeName和jobRequest不能为空");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("guid", guid);
		jobMsg.put("type", type);
		jobMsg.put("request", jobRequest);
		MsgPublisher.publish2WorkQueue("create_job", jobMsg.toString());
		return "";
	}
	public static void runJob(int jobId,String jobGuid,String type,JSONObject jobRequest)throws Exception{
		if(jobRequest==null){
			throw new Exception("jobRequest不能为空");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("jobId", jobId);
		jobMsg.put("jobGuid", jobGuid);
		jobMsg.put("type", type);
		jobMsg.put("request", jobRequest);
		MsgPublisher.publish2WorkQueue("run_job", jobMsg.toString());
	}

	/**
	 * 用于job web Server 持久化job执行过程中的反馈
	 * @param jobId
	 * @param jobResponse
	 * @throws Exception
	 */
	public static void responseJob(long jobId,int status,int stepCount,JSONObject jobResponse,JobStep step)throws Exception{
		if(jobResponse==null){
			throw new Exception("jobResponse不能为空");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("jobId", jobId);
		jobMsg.put("response", jobResponse);
		jobMsg.put("status", status);
		jobMsg.put("stepCount", stepCount);
		jobMsg.put("step", JSONObject.fromObject(step));
		MsgPublisher.publish2WorkQueue("resp_job", jobMsg.toString());
	}

	/**
	 * 用于job web Server 持久化job执行过程中的反馈
	 * @param jobId
	 * @param jobResponse
	 * @throws Exception
	 */
	public static void responseJob(long jobId,JobStep step)throws Exception{
		if(step==null){
			throw new Exception("step不能为空");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("jobId", jobId);
		jobMsg.put("step", JSONObject.fromObject(step));
		MsgPublisher.publish2WorkQueue("resp_job", jobMsg.toString());
	}
}
