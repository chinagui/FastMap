package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

/**
 * 中线监控统计启动类
 * @ClassName MediumMonitorStatStarter
 * @author zhangli5174
 * @date 2017年9月4日 
 * @Description TODO
 */
public class MediumMonitorStatStarter extends StatJobStarter {

	@Override
	public String jobType() {
		return "mediumMonitorStat";
	}

}
