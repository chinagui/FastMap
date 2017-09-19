package com.navinfo.dataservice.engine.limit.datahub;

import java.util.Observable;

/** 
 * @ClassName: DynamicConfigMonitor 
 * @author Xiao Xiaowen 
 * @date 2015-11-30 下午8:13:57 
 * @Description: 定时扫描表或者配置文件是否有变更，有变更则通知观察者
 */
public class DbServerMonitor extends Observable {
	private static class SingletonHolder{
		private static final DbServerMonitor INSTANCE = new DbServerMonitor();
	}
	public static final DbServerMonitor getInstance(){
		return SingletonHolder.INSTANCE;
	}
}
