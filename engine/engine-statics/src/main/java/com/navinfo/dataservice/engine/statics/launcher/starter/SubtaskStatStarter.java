package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

/**
 * 子任务数据统计启动类
 * @ClassName SubtaskStatStarter
 * @author Han Shaoming
 * @date 2017年8月1日 上午11:36:40
 * @Description TODO
 */
public class SubtaskStatStarter extends StatJobStarter {

	@Override
	public String jobType() {
		// TODO Auto-generated method stub
		return "subtaskStat";
	}

}
