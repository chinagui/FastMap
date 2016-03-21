package com.navinfo.dataservice.datahub.model;

import java.util.Date;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.navinfo.dataservice.api.model.DbServerType;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.navicommons.config.MavenConfigMap;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/** 
 * @ClassName: OracleSchema 
 * @author Xiao Xiaowen 
 * @date 2015-11-26 下午8:08:01 
 * @Description: TODO
 */
public class OracleSchema extends UnifiedDb {
	protected Logger log = Logger.getLogger(this.getClass());
	public OracleSchema(int dbId,String dbName,String dbType,String gdbVersion
			,DbServer dbServer,int createStatus){
		super(dbId,dbName,dbType,gdbVersion
				,dbServer,createStatus);
	}
	public OracleSchema(int dbId,String dbName,String dbUserName,String dbUserPasswd,int dbRole,String tablespaceName,String dbType
			,DbServer dbServer,String gdbVersion,int createStatus,Date createTime,String descp){
		super(dbId,dbName,dbUserName,dbUserPasswd,dbRole,tablespaceName,dbType
				,dbServer,gdbVersion,createStatus,createTime,descp);
	}
	
	@Override
	public UnifiedDb getSuperDb()throws DataHubException{
		return new DbManager().getSuperDb(this);
	}
	
	public String getConnectString()throws DataHubException{
		String conString = null;
		try{
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, Boolean.TRUE);
			conString = mapper.writeValueAsString(this);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new DataHubException("将oracleschema实体对象解析成json时发生错误，原因："+e.getMessage(),e);
		}
		return conString;
	}
	@Override
	public synchronized DriverManagerDataSource getDriverManagerDataSource() {
		if(dds!=null){
			return dds;
		}
		dds = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(DbServerType.TYPE_ORACLE
				, MultiDataSourceFactory.getDriverClassName(DbServerType.TYPE_ORACLE)
				, MultiDataSourceFactory.createOracleJdbcUrl(dbServer.getIp(), dbServer.getPort(), dbServer.getServiceName())
				, dbUserName, dbUserPasswd);
		return dds;
	}


	@Override
	public synchronized BasicDataSource getPoolDataSource() {
		if(bds!=null){
			return bds;
		}
		String dsKey = dbServer.getIp()+":" + dbServer.getPort() + ":" + dbUserName;
		MavenConfigMap dsConfig = new MavenConfigMap();
		String url = MultiDataSourceFactory.createOracleJdbcUrl(dbServer.getIp(), dbServer.getPort(), dbServer.getServiceName());
		dsConfig.put(dsKey + ".server.type",dbServer.getType());
		dsConfig.put(dsKey + ".jdbc.driverClassName", MultiDataSourceFactory.getDriverClassName(DbServerType.TYPE_ORACLE));
		dsConfig.put(dsKey + ".jdbc.url", url);
		dsConfig.put(dsKey + ".jdbc.username", dbUserName);
		dsConfig.put(dsKey + ".jdbc.password", dbUserPasswd);
		dsConfig.put(dsKey + ".dataSource.initialSize", "2");
		dsConfig.put(dsKey + ".dataSource.maxActive", "30");
		bds = MultiDataSourceFactory.getInstance().getDataSource(dsKey, dsConfig);
		return bds;
	}
	@Override
	public void closePoolDataSource() {
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
