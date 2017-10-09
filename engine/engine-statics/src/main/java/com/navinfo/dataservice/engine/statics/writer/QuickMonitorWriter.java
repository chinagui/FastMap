package com.navinfo.dataservice.engine.statics.writer;

import java.util.Map;

import com.navinfo.dataservice.engine.statics.service.StaticsService;

import net.sf.json.JSONObject;

/**
 * 任务数据写入oracle
 * @ClassName TaskWriter
 * @author Han Shaoming
 * @date 2017年8月4日 下午8:46:58
 * @Description TODO
 */
public class QuickMonitorWriter extends DefaultWriter {
	public String getLatestStatic() throws Exception {
		Map<String, Object> msg = StaticsService.getInstance().quickMonitor();
		log.info(msg);
		try{
			JSONObject msgJson = JSONObject.fromObject(msg);
			log.info("msgJson="+msgJson);
		}catch (Exception e) {
			log.error("JSONObject.fromObject error", e);
			throw e;
		}
		return JSONObject.fromObject(msg).toString();
	}
}
