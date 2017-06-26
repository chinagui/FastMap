package com.navinfo.dataservice.engine.statics.launcher;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;

import net.sf.json.JSONObject;

/** 
 * @ClassName: StatJobEndHandler
 * @author xiaoxiaowen4127
 * @date 2017年5月23日
 * @Description: StatJobEndHandler.java
 */
public class StatJobEndHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	@Override
	public void handle(String message) {
		try{
			log.info("stat_job_end:"+message);
			JSONObject msgObj = JSONObject.fromObject(message);
			
			GroupStatJobLauncher.getInstance().exchange(msgObj.getString("timestamp"),msgObj.getString("jobType"));
		}catch(Exception e){
			log.warn("接收到Statics_Job_End消息,但处理过程中出错，消息已消费。message："+message);
			log.error(e.getMessage(),e);
		}
	}

}
