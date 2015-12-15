package com.navinfo.dataservice.expcore.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.navinfo.navicommons.config.MavenConfigMap;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.utils.StringUtils;
import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.database.oracle.PoolDataSource;
import com.navinfo.dataservice.commons.database.oracle.PoolDataSourceFactory;
import com.navinfo.dataservice.commons.log.DSJobLogger;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/** 
 * @ClassName: OracleSchema 
 * @author Xiao Xiaowen 
 * @date 2015-10-27 上午11:15:33 
 * @Description: TODO
 *  
 */
@XStreamAlias("OracleSchema")
public class OracleSchema{

	private Logger log = Logger.getLogger(this.getClass());

    private String userName;
    private String password;
    private String ip;
    private int port;
    private String serviceName;
    private String tablespaceName;

	@XStreamOmitField
	private PoolDataSource bds;

    public OracleSchema(){
    	log = DSJobLogger.getLogger(log);
    }
    public OracleSchema(String userName,String password,String ip,int port,String serviceName,String tablespaceName){
    	this.userName=userName;
    	this.password=password;
    	this.ip=ip;
    	this.port=port;
    	this.serviceName=serviceName;
    	this.tablespaceName=tablespaceName;
    }

	public String getIdentity() {
		return ip+":"+port+":"+userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	/**
	 * @return the tablespaceName
	 */
	public String getTablespaceName() {
		return tablespaceName;
	}

	/**
	 * @param tablespaceName the tablespaceName to set
	 */
	public void setTablespaceName(String tablespaceName) {
		this.tablespaceName = tablespaceName;
	}

	public boolean create(String sysName,String sysPassword) throws SQLException{
		//
		if(StringUtils.isNotEmpty(sysName)&&StringUtils.isNotEmpty(sysPassword)){
			if(StringUtils.isEmpty(tablespaceName)){
				tablespaceName=SystemConfig.getSystemConfig().getValue("export.target.defaultTablespaces", "GDB_DATA");
			}
			Connection conn = null;
			try{
				Properties conProps = new Properties();
				conProps.put("user", sysName);
				conProps.put("password", sysPassword);
				conProps.put("defaultRowPrefetch", "15");
//				conProps.put("internal_logon", "sysdba");
				DriverManagerDataSource dataSource = new DriverManagerDataSource();
				dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
				String url = PoolDataSourceFactory.createUrl(ip, port, serviceName);
				dataSource.setUrl(url);
				/*
				 * dataSource.setUsername(sysName);
				 * dataSource.setPassword(sysPassword);
				 */
				dataSource.setConnectionProperties(conProps);
				conn = dataSource.getConnection();
				String createUseSql = "create user "
						+ userName
						+ " identified by "
						+ password
						+ " default tablespace " + tablespaceName;
				QueryRunner runner = new QueryRunner();
				runner.execute(conn, createUseSql);
				runner.execute(conn, "grant connect,resource to  " + userName);
				runner.execute(conn, "grant create session to  " + userName);
				runner.execute(conn, "grant alter session to  " + userName);
				runner.execute(conn, "grant create sequence to " + userName);
				runner.execute(conn, "grant create table to  " + userName);
				runner.execute(conn, "grant create view to " + userName);
				runner.execute(conn, "grant create trigger to " + userName);
				runner.execute(conn, "grant create synonym  to " + userName);
				runner.execute(conn, "grant create type to " + userName);
				runner.execute(conn, "grant create  snapshot to " + userName);
				runner.execute(conn, "grant create procedure  to   " + userName);
				runner.execute(conn, "grant query rewrite to " + userName);
				runner.execute(conn, "grant analyze any to  " + userName);
				runner.execute(conn, "grant create database link to " + userName);
				runner.execute(conn, "grant execute on dbms_lock to " + userName);
				runner.execute(conn, "grant create job to " + userName);
				runner.execute(conn, "grant execute any procedure to " + userName);
				runner.execute(conn, "grant manage scheduler to " + userName);
				runner.execute(conn, "GRANT SELECT_CATALOG_ROLE TO PUBLIC ");
				runner.execute(conn, "GRANT ALTER SYSTEM TO PUBLIC ");
				runner.execute(conn, "grant execute any type to " + userName);

				runner.execute(conn, "grant create public database link to "
						+ userName);
				runner
						.execute(conn, "grant debug connect session to   "
								+ userName);
			}catch (SQLException e) {
				log.error(e.getMessage(), e);
				throw e;
			} finally {
				DbUtils.closeQuietly(conn);
			}
		}
		//...
		return false;
	}

	public synchronized PoolDataSource getPoolDataSource() {
		if (bds == null || bds.isClosed()) {
			String dsKey = this.ip+":" + this.port + ":" + this.userName;
			MavenConfigMap dsConfig = new MavenConfigMap();
			MavenConfigMap systemConfig = SystemConfig.getSystemConfig();
			String driveClassName = systemConfig.getValue("jdbc.driverClassName.oracle");
			String url = PoolDataSourceFactory.createUrl(this.ip,this.port,this.serviceName);
			dsConfig.put(dsKey + ".jdbc.driverClassName", driveClassName);
			dsConfig.put(dsKey + ".jdbc.url", url);
			dsConfig.put(dsKey + ".jdbc.username", this.userName);
			dsConfig.put(dsKey + ".jdbc.password", this.password);
			dsConfig.put(dsKey + ".dataSource.initialSize", "2");
			dsConfig.put(dsKey + ".dataSource.maxActive", "30");

			log.debug("不缓存创建连接池. url:"+url+",username:"+this.userName);
			//不缓存,大多数版本都是用一次就不再用,缓存反而造成内存浪费
			bds = PoolDataSourceFactory.getInstance().getPoolDataSource(dsKey,dsConfig,false);
		}
		return bds;
	}
	public synchronized void closePoolDataSource() {
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
