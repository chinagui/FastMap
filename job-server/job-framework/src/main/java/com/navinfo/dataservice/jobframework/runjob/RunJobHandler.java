package com.navinfo.dataservice.jobframework.runjob;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.job.model.JobType;
import com.navinfo.dataservice.dao.mq.MsgHandler;

import net.sf.json.JSONObject;

/** 
* @ClassName: RunJobHandler 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午6:16:57 
* @Description: TODO
*/
public class RunJobHandler implements MsgHandler {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.mq.MsgHandler#handle(java.lang.String)
	 */
	@Override
	public void handle(String message) {
		//解析message生成jobInfo
		JSONObject jo = JSONObject.fromObject(message);
		long jobId = jo.getLong("jobId");
		JobType type = JobType.getJobType(jo.getString("type"));
		JSONObject request = JSONObject.fromObject(jo.get("request"));
		JobInfo jobInfo = new JobInfo(jobId);
		jobInfo.setType(type);
		jobInfo.setRequest(request);
		//添加到任务线程池
		JobThreadPoolExecutor.getInstance().execute(jobInfo);
	}

}
