package com.navinfo.dataservice.dao.mq.job;

import org.springframework.util.StringUtils;

import com.navinfo.dataservice.dao.mq.MsgPublisher;

import net.sf.json.JSONObject;

/** 
* @ClassName: JobMsgPublisher 
* @author Xiao Xiaowen 
* @date 2016年3月23日 下午5:57:57 
* @Description: TODO
*/
public class JobMsgPublisher {

	public static void createJob(String type,JSONObject jobRequest)throws Exception{
		if(StringUtils.isEmpty(type)||jobRequest==null){
			throw new Exception("type和jobRequest不能为空");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("type", type);
		jobMsg.put("request", jobRequest);
		MsgPublisher.publish2WorkQueue("create_job", jobMsg.toString());
	}
	public static void runJob(long jobId,String type,JSONObject jobRequest)throws Exception{
		if(StringUtils.isEmpty(type)||jobRequest==null){
			throw new Exception("type和jobRequest不能为空");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("jobId", jobId);
		jobMsg.put("type", type);
		jobMsg.put("request", jobRequest);
		MsgPublisher.publish2WorkQueue("run_job", jobMsg.toString());
	}
	public static void responseJob(long jobId,JSONObject jobResponse)throws Exception{
		if(jobResponse==null){
			throw new Exception("jobResponse不能为空");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("jobId", jobId);
		jobMsg.put("response", jobResponse);
		MsgPublisher.publish2WorkQueue("resp_job", jobMsg.toString());
	}
	/**
	 * 用于job web Server 持久化好job结束信息后，向应用web服务器发送消息
	 * @param jobId
	 * @param jobResponse
	 * @throws Exception
	 */
	public static void endJob(long jobId,JSONObject jobResponse)throws Exception{
		if(jobResponse==null){
			throw new Exception("jobResponse不能为空");
		}
		JSONObject jobMsg = new JSONObject();
		jobMsg.put("jobId", jobId);
		jobMsg.put("response", jobResponse);
		MsgPublisher.publish2WorkQueue("resp_job", jobMsg.toString());
	}
}
