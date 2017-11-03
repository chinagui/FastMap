package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

/**
 * 导出POI外业质检样本统计Starter
 * @Title:ExportQualityPoiReportStarter
 * @Package:com.navinfo.dataservice.engine.statics.launcher.starter
 * @Description: 
 * @author:Jarvis 
 * @date: 2017年10月30日
 */
public class ExportQualityPoiReportStarter extends StatJobStarter {
	@Override
	public String jobType() {
		// TODO Auto-generated method stub
		return "exportQualityPoiReport";
	}

}
