package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

/**
 * 任务数据统计启动类
 * @ClassName DayProduceStatStarter
 * @author zl
 * @date 2017年9月23日 
 * @Description TODO
 */
public class DayProduceStatStarter extends StatJobStarter {

	@Override
	public String jobType() {
		// TODO Auto-generated method stub
		return "dayProduceStat";
	}

}
