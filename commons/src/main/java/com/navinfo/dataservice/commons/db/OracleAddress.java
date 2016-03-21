package com.navinfo.dataservice.commons.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * oracle连接类
 */
public class OracleAddress {


	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			
		}
	}

	private String username;

	private Connection conn;

	public Connection getConn() {
		return conn;
	}

	public OracleAddress(String username, String password, int port, String ip,
			String serviceName) throws SQLException {

		this.username = username;
		this.password = password;
		this.port = port;
		this.ip = ip;
		this.serviceName = serviceName;

		this.conn = DriverManager.getConnection("jdbc:oracle:thin:@" + ip + ":"
				+ port + ":" + serviceName, username, password);
	}

	private String password;

	private int port;

	private String ip;

	private String serviceName;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
