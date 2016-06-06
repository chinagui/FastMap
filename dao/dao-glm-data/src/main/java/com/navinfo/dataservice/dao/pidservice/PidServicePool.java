package com.navinfo.dataservice.dao.pidservice;

import java.sql.Connection;

import javax.sql.DataSource;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;

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

	private DataSource dataSource;

	/**
	 * 通過項目ID獲取數據庫連接
	 * 
	 * @param projectId
	 *            项目ID
	 * @return Oracle连接
	 * @throws Exception
	 */
	public Connection getConnection() throws Exception {

		if (dataSource == null) {
			synchronized (this) {
				if (dataSource == null) {

					dataSource = MultiDataSourceFactory.getInstance()
							.getPidDataSource();

				}
			}
		}

		return dataSource.getConnection();
	}
}