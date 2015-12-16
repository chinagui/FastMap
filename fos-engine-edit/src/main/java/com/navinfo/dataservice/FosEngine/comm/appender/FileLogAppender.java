package com.navinfo.dataservice.FosEngine.comm.appender;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Priority;

/**
 * 只输出错误等级相等的日志到文件
 */
public class FileLogAppender extends DailyRollingFileAppender {

	@Override
	public boolean isAsSevereAsThreshold(Priority priority) {
		// 只判断是否相等，而不判断优先级
		return this.getThreshold().equals(priority);
	}
}
