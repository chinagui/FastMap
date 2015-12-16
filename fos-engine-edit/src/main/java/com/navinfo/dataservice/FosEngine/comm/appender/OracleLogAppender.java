package com.navinfo.dataservice.FosEngine.comm.appender;

import org.apache.log4j.Priority;
import org.apache.log4j.jdbc.JDBCAppender;

/**
 * 只输出错误等级相等的日志到oracle
 */
public class OracleLogAppender extends JDBCAppender {

	@Override
	public boolean isAsSevereAsThreshold(Priority priority) {
		// 只判断是否相等，而不判断优先级
		return this.getThreshold().equals(priority);
	}
}
