package com.navinfo.dataservice.bizcommons.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.mongodb.MongoClient;
import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

public class DBConnector {

	private static class SingletonHolder {
		private static final DBConnector INSTANCE = new DBConnector();
	}

	public static final DBConnector getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private DataSource manDataSource;
	private DataSource metaDataSource;
	private DataSource mkDataSource;
	private DataSource pidDataSource;
	private MongoClient statClient;

	// 大区库连接池
	private Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();

	public Connection getManConnection() throws SQLException {
		if (manDataSource == null) {
			synchronized (this) {
				if (manDataSource == null) {
					DatahubApi datahub = (DatahubApi) ApplicationContextUtil
							.getBean("datahubApi");
					DbInfo manDb = null;
					DbConnectConfig connConfig = null;
					try {
						manDb = datahub.getOnlyDbByType("fmMan");
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

	public Connection getMetaConnection() throws SQLException {
		if (metaDataSource == null) {
			synchronized (this) {
				if (metaDataSource == null) {
					DatahubApi datahub = (DatahubApi) ApplicationContextUtil
							.getBean("datahubApi");
					DbInfo metaDb = null;
					DbConnectConfig connConfig = null;
					try {
						metaDb = datahub.getOnlyDbByType("metaRoad");
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

	public Connection getMkConnection() throws SQLException {
		if (mkDataSource == null) {
			synchronized (this) {
				if (mkDataSource == null) {
					DatahubApi datahub = (DatahubApi) ApplicationContextUtil
							.getBean("datahubApi");
					DbInfo metaDb = null;
					DbConnectConfig connConfig = null;
					try {
						metaDb = datahub.getOnlyDbByType("nationRoad");
						connConfig = DbConnectConfig
								.createConnectConfig(metaDb.getConnectParam());
					} catch (Exception e) {
						throw new SQLException("从datahub获取FM母库信息失败："
								+ e.getMessage(), e);
					}
					mkDataSource = MultiDataSourceFactory.getInstance()
							.getDataSource(connConfig);
				}
			}
		}
		return mkDataSource.getConnection();
	}
	
	public Connection getPidConnection() throws SQLException {
		if (pidDataSource == null) {
			synchronized (this) {
				if (pidDataSource == null) {
					DatahubApi datahub = (DatahubApi) ApplicationContextUtil
							.getBean("datahubApi");
					DbInfo metaDb = null;
					DbConnectConfig connConfig = null;
					try {
						metaDb = datahub.getOnlyDbByType("pidCenter");
						connConfig = DbConnectConfig
								.createConnectConfig(metaDb.getConnectParam());
					} catch (Exception e) {
						throw new SQLException("从datahub获取PID数据库信息失败："
								+ e.getMessage(), e);
					}
					pidDataSource = MultiDataSourceFactory.getInstance()
							.getDataSource(connConfig);
				}
			}
		}
		return pidDataSource.getConnection();
	}

	public Connection getConnectionById(int dbId) throws Exception {

		String str = String.valueOf(dbId);

		if (!dataSourceMap.containsKey(str)) {
			synchronized (this) {
				if (!dataSourceMap.containsKey(str)) {

					try {
						DatahubApi datahub = (DatahubApi) ApplicationContextUtil
								.getBean("datahubApi");

						DbInfo db = datahub.getDbById(dbId);

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
	
	public MongoClient getStatConnection() throws Exception {
		if (statClient == null) {
			synchronized (this) {
				if (statClient == null) {
					DatahubApi datahub = (DatahubApi) ApplicationContextUtil
							.getBean("datahubApi");
					DbInfo db = null;
					DbConnectConfig connConfig = null;
					try {
						db = datahub.getOnlyDbByType("fmStat");
						connConfig = DbConnectConfig
								.createConnectConfig(db.getConnectParam());
					} catch (Exception e) {
						throw new SQLException("从datahub获取统计库信息失败："
								+ e.getMessage(), e);
					}
					statClient = MultiDataSourceFactory.getInstance()
							.getMongoClient(connConfig);
				}
			}
		}
		return statClient;
	}
}