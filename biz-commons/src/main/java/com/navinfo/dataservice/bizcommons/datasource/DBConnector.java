package com.navinfo.dataservice.bizcommons.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

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

	public Connection getManConnection() throws SQLException {
		if (manDataSource == null) {
			synchronized (this) {
				if (manDataSource == null) {
					DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
					DbInfo manDb = null;
					DbConnectConfig connConfig = null;
					try{
						manDb = datahub.getOnlyDbByType("fmMan");
						connConfig = MultiDataSourceFactory.createConnectConfig(manDb.getConnectParam());
					}catch(Exception e){
						throw new SQLException("从datahub获取元数据信息失败："+e.getMessage(),e);
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
					DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApiService");
					DbInfo metaDb = null;
					DbConnectConfig connConfig = null;
					try{
						metaDb = datahub.getOnlyDbByType("metaRoad");
						connConfig = MultiDataSourceFactory.createConnectConfig(metaDb.getConnectParam());
					}catch(Exception e){
						throw new SQLException("从datahub获取元数据信息失败："+e.getMessage(),e);
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
					DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
					DbInfo metaDb = null;
					DbConnectConfig connConfig = null;
					try{
						metaDb = datahub.getOnlyDbByType("nationRoad");
						connConfig = MultiDataSourceFactory.createConnectConfig(metaDb.getConnectParam());
					}catch(Exception e){
						throw new SQLException("从datahub获取元数据信息失败："+e.getMessage(),e);
					}
					mkDataSource = MultiDataSourceFactory.getInstance()
							.getDataSource(connConfig);
				}
			}
		}
		return mkDataSource.getConnection();
	}

}
