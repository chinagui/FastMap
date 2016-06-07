package org.navinfo.dataservice.engine.meta.dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.navinfo.dataservice.api.datahub.iface.DatahubApiService;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

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
					DatahubApiService datahub = (DatahubApiService)ApplicationContextUtil.getBean("datahubApiService");
					DbInfo metaDb = null;
					try{
						metaDb = datahub.getOnlyDbByType("metaRoad");
					}catch(Exception e){
						throw new SQLException("从datahub获取元数据信息失败："+e.getMessage(),e);
					}
					dataSource = MultiDataSourceFactory.getInstance()
							.getDataSource(metaDb.getConnectParam());
				}
			}
		}
		return dataSource.getConnection();
	}

}
