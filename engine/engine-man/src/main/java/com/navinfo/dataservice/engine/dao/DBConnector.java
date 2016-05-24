package com.navinfo.dataservice.engine.dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;

public class DBConnector {

	private static class SingletonHolder {
		private static final DBConnector INSTANCE = new DBConnector();
	}

	public static final DBConnector getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		if (dataSource == null) {
			synchronized (this) {
				if (dataSource == null) {
					dataSource = MultiDataSourceFactory.getInstance()
							.getManDataSource();
				}
			}
		}
		return dataSource.getConnection();
	}
	
	private DataSource metaDataSource;

	public Connection getMetaConnection() throws SQLException {
		if (metaDataSource == null) {
			synchronized (this) {
				if (metaDataSource == null) {
					metaDataSource = MultiDataSourceFactory.getInstance()
							.getMetaDataSource();
				}
			}
		}
		return metaDataSource.getConnection();
	}

}
