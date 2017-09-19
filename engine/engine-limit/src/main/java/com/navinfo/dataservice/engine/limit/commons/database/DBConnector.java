package com.navinfo.dataservice.engine.limit.commons.database;

import com.navinfo.dataservice.engine.limit.datahub.model.DbInfo;
import com.navinfo.dataservice.engine.limit.datahub.service.DbService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DBConnector {

	private static class SingletonHolder {
		private static final DBConnector INSTANCE = new DBConnector();
	}

	public static final DBConnector getInstance() {
		return SingletonHolder.INSTANCE;
	}


	private DataSource metaDataSource;
	private DataSource manDataSource;

	// 大区库连接池
	private Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();

	public Connection getMetaConnection() throws SQLException {
		if (metaDataSource == null) {
			synchronized (this) {
				if (metaDataSource == null) {

					DbInfo metaDb = null;
					DbConnectConfig connConfig = null;
					try {
						metaDb = DbService.getInstance().getOnlyDbByBizType("metaRoad");
						connConfig = DbConnectConfig
								.createConnectConfig(metaDb.getConnectParam());
					} catch (Exception e) {
						throw new SQLException("从datahub获取元数据信息失败："
								+ e.getMessage(), e);
					}
					metaDataSource = MultiDataSourceFactory.getInstance()
							.getDataSource(connConfig);
				}
			}
		}
		return metaDataSource.getConnection();
	}
	public Connection getManConnection() throws SQLException {
		if (manDataSource == null) {
			synchronized (this) {
				if (manDataSource == null) {

					DbInfo manDb = null;
					DbConnectConfig connConfig = null;
					try {
						manDb = DbService.getInstance().getOnlyDbByBizType("fmMan");
						connConfig = DbConnectConfig
								.createConnectConfig(manDb.getConnectParam());
					} catch (Exception e) {
						throw new SQLException("从datahub获取管理库信息失败："
								+ e.getMessage(), e);
					}
					manDataSource = MultiDataSourceFactory.getInstance()
							.getDataSource(connConfig);
				}
			}
		}
		return manDataSource.getConnection();
	}



	public Connection getConnectionById(int dbId) throws Exception {

		String str = String.valueOf(dbId);

		if (!dataSourceMap.containsKey(str)) {
			synchronized (this) {
				if (!dataSourceMap.containsKey(str)) {

					try {

						DbInfo db =DbService.getInstance().getDbById(dbId);

						DbConnectConfig connConfig = DbConnectConfig
								.createConnectConfig(db.getConnectParam());

						dataSourceMap.put(str, MultiDataSourceFactory
								.getInstance().getDataSource(connConfig));
					} catch (Exception e) {
						throw new SQLException("从datahub获取大区库连接失败："
								+ e.getMessage(), e);
					}
				}
			}
		}

		return dataSourceMap.get(str).getConnection();
	}


	public static void main(String[] args) {


		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

//			conn = DBConnector.getInstance().getConnectionById(13);
			//conn = DBConnector.getInstance().getMetaConnection();
			conn = DBConnector.getInstance().getManConnection();
		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}