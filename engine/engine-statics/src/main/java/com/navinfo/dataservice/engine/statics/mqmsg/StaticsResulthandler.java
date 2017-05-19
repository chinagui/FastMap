package com.navinfo.dataservice.engine.statics.mqmsg;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.engine.statics.writer.DefaultWriter;
import com.navinfo.dataservice.engine.statics.writer.WriterFactory;

public class StaticsResulthandler implements MsgHandler  {
	protected Logger log = LoggerRepos.getLogger(this.getClass());

	@Override
	public void handle(String message) {
		try {
			// 解析保存到man库infor表中
			save(message);
		} catch (Exception e) {
			log.warn("接收到info_change消息,但保存失败，该消息已消费。message：" + message);
			log.error(e.getMessage(), e);

		}
	}
	/**
	 * 处理保存统计结果
	 * @param message
	 * @throws Exception 
	 */
	private void save(String message) throws Exception {
		JSONObject messageJSON = JSONObject.fromObject(message);
		String jobName=messageJSON.getString("jobName");
		String statDate=messageJSON.getString("statDate");
		log.info("start write:jobName="+jobName+",statDate="+statDate);
		DefaultWriter writer=WriterFactory.createWriter(jobName);
		writer.write(messageJSON);
		log.info("end write:jobName="+jobName+",statDate="+statDate);
	}
}
