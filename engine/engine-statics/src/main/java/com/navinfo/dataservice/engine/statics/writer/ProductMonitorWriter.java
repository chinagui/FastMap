package com.navinfo.dataservice.engine.statics.writer;

import com.navinfo.dataservice.engine.statics.service.StaticsService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName ProductMonitorWriter
 * @author Han Shaoming
 * @date 2017年9月23日 下午1:08:55
 * @Description TODO
 */

public class ProductMonitorWriter extends DefaultWriter {
	public String getLatestStatic() throws Exception {
		JSONObject data = new JSONObject();
		String platForm = "productMonitor";		
		data.putAll(StaticsService.getInstance().getOracleMonitorData(platForm));
		data.putAll(StaticsService.getInstance().getMongoMonitorData());
		return data.toString();
	}
}
