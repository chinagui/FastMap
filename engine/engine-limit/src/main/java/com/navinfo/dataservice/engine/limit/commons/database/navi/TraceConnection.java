package com.navinfo.dataservice.engine.limit.commons.database.navi;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 跟踪 Oracle 数据库连接,协助解决连接打开个数过多,或打开但未释放等问题<br>
 * 通过Plsql Developer等工具的session查看功能即可看到跟踪信息<br>
 * <br>
 * 注:此类仅调试用,不会对连接做任何打开,关闭,检测操作.<br>
 * @author zhangjianjun
 * 
 */
public class TraceConnection {
	protected static Logger log = Logger.getLogger(TraceConnection.class);

	/**
	 * 自动将调用者的类名,方法名,代码行号放入连接的session.module_name
	 * 中,并设置session.action_name操作为"open new connection"<br>
	 * 其它信息请参考方法traceConnection(Connection connection, String moduleName, String
	 * actionName)<br>
	 */
	public static void trace(Connection connection) {
		if (connection == null) {
			return;
		}
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
			trace(connection, moduleName, actionName);
		} catch (Exception e) {
			log.warn("跟踪数据库连接失败:" + e.getMessage(), e);
		}
	}

	/**
	 * moduleName 和 actionName 将会被写入到连接的session信息中,<br>
	 * 通过Plsql Developer等工具的session查看功能即可看到此信息<br>
	 * 
	 * 调用方式:<br>
	 * <code>trace(conn,"TransactionalDataSource.getConnection()","open new connection");<code>
	 * <br>
	 * 注:本方法在自治事务中执行,不会影响传入连接的事务
	 * 
	 * @param connection
	 *            连接
	 * @param moduleName
	 *            模块名
	 * @param actionName
	 *            操作名
	 */
	public static void trace(Connection connection, String moduleName, String actionName) {
		if (connection == null) {
			return;
		}
		CallableStatement ps = null;
		try {
			ps = connection.prepareCall("{call dbms_application_info.set_module(MODULE_NAME=>?,ACTION_NAME=>?)}");
			ps.setString(1, moduleName);
			ps.setString(2, actionName);
			ps.execute();
		} catch (Exception e) {
			log.warn("跟踪数据库连接失败:" + e.getMessage(), e);
		} finally {
			try {
				if (ps != null && !ps.isClosed()) {
					ps.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 测试跟踪数据库连接
	 * 
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		dataSource.setUrl("jdbc:oracle:thin:@" + "192.168.3.61" + ":" + "1521" + ":" + "orcl");
		dataSource.setUsername("vm_dev");
		dataSource.setPassword("vm_dev");
		Properties connectionProperties = new Properties();
		connectionProperties.put("v$session.program", "Java程序");
		connectionProperties.put("v$session.module", "新打开连接");
		dataSource.setConnectionProperties(connectionProperties);
		Connection connection = dataSource.getConnection();
		trace(connection);
		trace(connection, "main()", "new connection");
		connection.close();
	}
}
