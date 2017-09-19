package com.navinfo.dataservice.engine.limit.commons.database.navi;

import com.navinfo.dataservice.engine.limit.commons.database.navi.TraceConnection;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 扩展 BasicDataSource ,将所有从此数据源打开的连接设置上跟踪标记,<br>
 * 在plsql developer的session管理上即可查看跟踪标记
 * 
 * @author zhangjianjun
 * 
 */
public class BasicDataSourceExt extends BasicDataSource {
	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = super.getConnection();
		try {
			String moduleName = "";
			String actionName = "open new connection";
			Thread currentThread = Thread.currentThread();
			String threadName = currentThread.getName();
			moduleName += threadName + ":";
			StackTraceElement[] stackTrace = currentThread.getStackTrace();
			if (stackTrace != null && stackTrace.length > 2) {
				String classname = stackTrace[2].getClassName();
				classname = classname.substring(classname.lastIndexOf(".") + 1);
				moduleName += classname + ".";
				moduleName += stackTrace[2].getMethodName() + "() ";
				moduleName += stackTrace[2].getLineNumber();
			}
			TraceConnection.trace(connection, moduleName, actionName);
		} catch (Exception e) {
		}
		return connection;
	}

	@Override
	public Connection getConnection(String user, String pass) throws SQLException {
		Connection connection = super.getConnection(user, pass);
		TraceConnection.trace(connection);
		return connection;
	}

}
