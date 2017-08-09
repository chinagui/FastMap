package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

/**
 * 任务数据统计启动类
 * @ClassName TaskStatStarter
 * @author Han Shaoming
 * @date 2017年8月4日 下午8:48:48
 * @Description TODO
 */
public class TaskStatStarter extends StatJobStarter {

	@Override
	public String jobType() {
		// TODO Auto-generated method stub
		return "taskStat";
	}

}
