package com.navinfo.dataservice.engine.edit.datasource;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

/** 
* @ClassName: EditDataSource 
* @author Xiao Xiaowen 
* @date 2016年6月7日 下午7:21:36 
* @Description: TODO
*  
*/
public class DbConnector {
	private static class SingletonHolder {
		private static final DbConnector INSTANCE = new DbConnector();
	}

	public static final DbConnector getInstance() {
		return SingletonHolder.INSTANCE;
	}
	private DataSource manDataSource;
	private DataSource metaDataSource;
	public DataSource getManDataSource() throws SQLException {
		if (manDataSource == null) {
			synchronized (this) {
				if (manDataSource == null) {
					DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApiService");
					DbInfo manDb = null;
					DbConnectConfig connConfig = null;
					try{
						manDb = datahub.getOnlyDbByType("fmManRoad");
						connConfig = MultiDataSourceFactory.createConnectConfig(manDb.getConnectParam());
					}catch(Exception e){
						throw new SQLException("从datahub获取元数据信息失败："+e.getMessage(),e);
					}
					manDataSource = MultiDataSourceFactory.getInstance()
							.getDataSource(connConfig);
				}
			}
		}
		return manDataSource;
	}

	public DataSource getMetaDataSource() throws SQLException {
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
		return metaDataSource;
	}
}
