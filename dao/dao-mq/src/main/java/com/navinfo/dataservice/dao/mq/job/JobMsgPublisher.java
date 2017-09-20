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
	public static void runJob(long jobId,String jobGuid,String type,JSONObject jobRequest,long userId,long taskId)throws Exception{
		if(jobRequest==null){
			throw new Exception("jobRequest不能为空");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("jobId", jobId);
		jobMsg.put("jobGuid", jobGuid);
		jobMsg.put("type", type);
		jobMsg.put("request", jobRequest);
		jobMsg.put("userId", userId);
		jobMsg.put("taskId", taskId);
		MsgPublisher.publish2WorkQueue("run_job", jobMsg.toString());
	}
	
	public static void runStaticsJob(long jobId,String jobGuid,String type,JSONObject jobRequest,long userId,long taskId)throws Exception{
		if(jobRequest==null){
			throw new Exception("jobRequest不能为空");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("jobId", jobId);
		jobMsg.put("jobGuid", jobGuid);
		jobMsg.put("type", type);
		jobMsg.put("request", jobRequest);
		jobMsg.put("userId", userId);
		jobMsg.put("taskId", taskId);
		MsgPublisher.publish2WorkQueue("run_statics_job", jobMsg.toString());
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

	/**
	 * 用于job Server 在执行完一个job时发送end_job消息
	 * @param jobId
	 * @param jobResponse
	 * @param durationSeconds 
	 * @param jobTypeName 
	 * @throws Exception
	 */
	public static void endJob(long userId,long jobId,int status,String resultMsg,JSONObject jobResponse, String jobTypeName, long durationSeconds)throws Exception{
		if(jobResponse==null){
			throw new Exception("step不能为空");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("userId", userId);
		jobMsg.put("jobId", jobId);
		jobMsg.put("status", status);
		jobMsg.put("resultMsg", StringUtils.isEmpty(resultMsg)?"":resultMsg);
		jobMsg.put("response", jobResponse);
		jobMsg.put("jobTypeName", jobTypeName);
		jobMsg.put("durationSeconds", durationSeconds);
		MsgPublisher.publish2WorkQueue("end_job", jobMsg.toString());
	}

	/**
	 * 用于统计类job发送统计结果
	 * @param jobType
	 * @param timestamp
	 * @param statResult
	 * @param jobId
	 * @throws Exception
	 */
	public static void sendStatJobResult(String jobType,String timestamp,String identify,JSONObject identifyJson,String statResult,long jobId)throws Exception{
		if(statResult==null){
			throw new Exception("空statResult不能写入消息队列");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("jobId", jobId);
		jobMsg.put("statResult", statResult);
		jobMsg.put("jobType", jobType);
		jobMsg.put("timestamp", timestamp);
		jobMsg.put("identify", identify);
		jobMsg.put("identifyJson", identifyJson);
		MsgPublisher.publish2WorkQueue("stat_job_result", jobMsg.toString());
	}
}
