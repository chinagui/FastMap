package com.navinfo.dataservice.commons.database;

import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


/** 
* @ClassName: OracleSchema 
* @author Xiao Xiaowen 
* @date 2016年6月7日 下午3:25:38 
* @Description: TODO
*/
public class OracleSchema {
	private Logger log = Logger.getLogger(OracleSchema.class);
	public OracleSchema(DbConnectConfig connConfig){
		this.connConfig = connConfig;
	}
	protected DbConnectConfig connConfig;
	protected DriverManagerDataSource dds;
	protected BasicDataSource bds;

	public DbConnectConfig getConnConfig() {
		return connConfig;
	}

	public void setConnConfig(DbConnectConfig connConfig) {
		this.connConfig = connConfig;
	}
	public synchronized DriverManagerDataSource getDriverManagerDataSource() throws SQLException{
		if(dds!=null){
			return dds;
		}
		String serverType = connConfig.getServerType();
		if(DbConnectConfig.TYPE_ORACLE.equals(serverType)){
			
			String url = MultiDataSourceFactory.createOracleJdbcUrl(
					connConfig.getServerIp(),connConfig.getServerPort(),connConfig.getDbName());
			dds = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(DbConnectConfig.TYPE_ORACLE
					, MultiDataSourceFactory.getDriverClassName(DbConnectConfig.TYPE_ORACLE)
					,url,connConfig.getUserName(), connConfig.getUserPasswd());
			return dds;
		}else{
			throw new SQLException("非Oracle类型数据库，无法建立OracleSchema连接。");
		}
	}
	
	public synchronized BasicDataSource getPoolDataSource() throws SQLException{
		if(bds!=null){
			return bds;
		}
		bds = MultiDataSourceFactory.getInstance().getDataSource(connConfig);
		return bds;
	}

	public void closePoolDataSource() {
		//
		String key = connConfig.getKey();
		if(MultiDataSourceFactory.getInstance().getDataSourceByKey(key)!=null){
			MultiDataSourceFactory.getInstance().closeDataSource(key);
			bds = null;
		}else{
			//
			if (bds != null && !bds.isClosed()) {
				try {
					log.debug("关闭连接池. url:"+bds.getUrl()+",username:"+bds.getUsername());
					bds.close();
					bds = null;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
}
