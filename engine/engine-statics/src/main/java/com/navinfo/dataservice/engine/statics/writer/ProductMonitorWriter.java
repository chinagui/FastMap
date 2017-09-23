package com.navinfo.dataservice.engine.statics.writer;

import java.util.HashMap;
import java.util.Map;

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
//		return JSONObject.fromObject(StaticsService.getInstance().quickMonitor()).toString();
		Map<String,Object> stat = new HashMap<String,Object>();
		stat.put("test", "websocket测试");
		return JSONObject.fromObject(stat).toString();
	}
}
