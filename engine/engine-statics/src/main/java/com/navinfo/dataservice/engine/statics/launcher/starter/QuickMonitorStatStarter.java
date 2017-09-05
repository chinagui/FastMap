package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

/**
 * 快线监控统计启动类
 * @ClassName QuickMonitorStatStarter
 * @author zhangli5174
 * @date 2017年9月4日 
 * @Description TODO
 */
public class QuickMonitorStatStarter extends StatJobStarter {

	@Override
	public String jobType() {
		return "quickMonitorStat";
	}

}
