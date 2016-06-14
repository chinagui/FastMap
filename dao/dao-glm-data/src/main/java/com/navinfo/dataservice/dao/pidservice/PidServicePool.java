package com.navinfo.dataservice.dao.pidservice;

import java.sql.Connection;

import javax.sql.DataSource;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

/**
 * oracle连接池管理器
 */
public class PidServicePool {

	private static class SingletonHolder {
		private static final PidServicePool INSTANCE = new PidServicePool();
	}

	public static final PidServicePool getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * 通過項目ID獲取數據庫連接
	 * 
	 * @param projectId
	 *            项目ID
	 * @return Oracle连接
	 * @throws Exception
	 */
	public Connection getConnection() throws Exception {
		return DBConnector.getInstance().getPidConnection();
	}
}