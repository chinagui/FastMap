package com.navinfo.dataservice.engine.limit.commons.database.oracle;


import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-8-1
 * Time: 下午9:43
 */
public class MyDriverManagerDataSource extends DriverManagerDataSource {
	


	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	private boolean autoCommit = false;

	/**
	 * Getting a Connection using the nasty static from DriverManager is
	 * extracted
	 * into a protected method to allow for easy unit testing.
	 * 
	 * @see DriverManager#getConnection(String, Properties)
	 */
	protected Connection getConnectionFromDriverManager(String url, Properties props) throws SQLException {
		Connection conn = DriverManager.getConnection(url, props);
		if (conn.getAutoCommit() != autoCommit) {
			conn.setAutoCommit(autoCommit);
		}
		return conn;
	}

	public Connection getConnection() throws SQLException {
		MyDriverManagerConnectionWrapper connWrapper = wrapConnection();
		return ConnectionRegister.registerConnection(connWrapper);
	}

	public MyDriverManagerConnectionWrapper wrapConnection() throws SQLException {
		Connection conn = super.getConnection();
		long sessionPro[] = ConnectionRegister.getOracleSessionProperties(conn);
		MyDriverManagerConnectionWrapper connWrapper = new MyDriverManagerConnectionWrapper(conn);
		SessionProperties sessionProperties=new SessionProperties(sessionPro[0], sessionPro[1], getUrl(), getUsername(), getPassword());
		connWrapper.setSessionProperties(sessionProperties);
		return connWrapper;
	}

	public Connection getConnection(String vmTaskId) throws SQLException {
		MyDriverManagerConnectionWrapper connWrapper = wrapConnection();
		return ConnectionRegister.registerConnection(connWrapper, vmTaskId);
	}
}
