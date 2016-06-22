package com.navinfo.dataservice.jobframework.runjob;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;

import net.sf.json.JSONObject;

/** 
* @ClassName: RunJobHandler 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午6:16:57 
* @Description: TODO
*/
public class RunJobHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.mq.MsgHandler#handle(java.lang.String)
	 */
	@Override
	public void handle(String message) {
		try{
			//解析message生成jobInfo
			JSONObject jo = JSONObject.fromObject(message);
			int jobId = jo.getInt("jobId");
			String jobGuid = jo.getString("jobGuid");
			String type = jo.getString("type");
			JSONObject request = JSONObject.fromObject(jo.get("request"));
			JobInfo jobInfo = new JobInfo(jobId,jobGuid);
			jobInfo.setType(type);
			jobInfo.setRequest(request);
			//添加到任务线程池
			JobThreadPoolExecutor.getInstance().execute(jobInfo);
		}catch(Exception e){
			log.warn("接收到job执行消息,但加入job线程池失败，该消息已消费。message："+message);
			log.error(e.getMessage(),e);
			
		}
	}

}
