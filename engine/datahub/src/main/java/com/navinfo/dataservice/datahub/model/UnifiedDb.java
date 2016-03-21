package com.navinfo.dataservice.datahub.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/** 
 * @ClassName: AbstractDb 
 * @author Xiao Xiaowen 
 * @date 2015-11-26 下午3:12:50 
 * @Description: TODO
 *  
 */
@XStreamAlias("OracleSchema")
public abstract class UnifiedDb {
	protected int dbId;
	protected String dbName;
	protected String dbUserName;
	protected String dbUserPasswd;
	protected int dbRole=0;
	protected String tablespaceName;
	protected String bizType;
	protected String gdbVersion;
	protected int dbStatus;
	protected Date createTime;
	protected String descp;
	protected DbServer dbServer;

	@XStreamOmitField
	protected DriverManagerDataSource dds;

	@XStreamOmitField
	protected BasicDataSource bds;

	public UnifiedDb(int dbId,String dbName,String bizType,String gdbVersion
			,DbServer dbServer,int dbStatus){
		this.dbId=dbId;
		this.dbName=dbName;
		this.bizType=bizType;
		this.dbServer=dbServer;
		this.gdbVersion=gdbVersion;
		this.dbStatus=dbStatus;
	}
	public UnifiedDb(int dbId,String dbName,String dbUserName,String dbUserPasswd,int dbRole,String tablespaceName,String bizType
			,DbServer dbServer,String gdbVersion,int dbStatus,Date createTime,String descp){
		this.dbId=dbId;
		this.dbName=dbName;
		this.dbUserName=dbUserName;
		this.dbUserPasswd=dbUserPasswd;
		this.dbRole=dbRole;
		this.tablespaceName=tablespaceName;
		this.bizType=bizType;
		this.dbServer=dbServer;
		this.gdbVersion=gdbVersion;
		this.dbStatus=dbStatus;
		this.createTime=createTime;
		this.descp=descp;
	}
	
	public boolean isSuperDb()throws DataHubException{
		return dbRole==1?true:false;
	};
	/**
	 * 不同类型，可以覆盖此方法
	 * @return
	 */
	public Map<String,Object> getConnectParam(){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("dbName", dbName);
		map.put("dbUserName", dbUserName);
		map.put("dbUserPasswd", dbUserPasswd);
		map.put("serverIp", dbServer.getIp());
		map.put("serverPort", dbServer.getPort());
		map.put("serverType", dbServer.getType());
		return map;
	}
	public abstract UnifiedDb getSuperDb()throws DataHubException;
	public abstract String getConnectString()throws DataHubException;

	/**
	 * 没有使用连接池的数据库连接，一般子版本使用
	 */
	public abstract DriverManagerDataSource getDriverManagerDataSource();

	/**
	 * 使用连接池的数据库连接
	 * 一般子版本请不要使用
	 * 
	 */
	public abstract BasicDataSource getPoolDataSource();
	public abstract void closePoolDataSource();
	
/* getter&setter */
	
	public int getDbId() {
		return dbId;
	}
	public void setDbId(int dbId) {
		this.dbId = dbId;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getDbUserName() {
		return dbUserName;
	}
	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
	}
	public String getDbUserPasswd() {
		return dbUserPasswd;
	}
	public void setDbUserPasswd(String dbUserPasswd) {
		this.dbUserPasswd = dbUserPasswd;
	}
	public int getDbRole() {
		return dbRole;
	}
	public void setDbRole(int dbRole) {
		this.dbRole = dbRole;
	}
	public String getTablespaceName() {
		return tablespaceName;
	}
	public void setTablespaceName(String tablespaceName) {
		this.tablespaceName = tablespaceName;
	}
	public String getBizType() {
		return bizType;
	}
	public void setBizType(String bizType) {
		this.bizType = bizType;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getDescp() {
		return descp;
	}
	public void setDescp(String descp) {
		this.descp = descp;
	}
	public DbServer getDbServer() {
		return dbServer;
	}
	public void setDbServer(DbServer dbServer) {
		this.dbServer = dbServer;
	}
}
