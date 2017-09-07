package com.navinfo.dataservice.engine.statics.writer;

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
		return JSONObject.fromObject(StaticsService.getInstance().quickMonitor()).toString();
	}
}
