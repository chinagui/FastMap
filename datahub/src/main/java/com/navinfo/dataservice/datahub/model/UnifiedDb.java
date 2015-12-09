package com.navinfo.dataservice.datahub.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.datahub.exception.DataHubException;

/** 
 * @ClassName: AbstractDb 
 * @author Xiao Xiaowen 
 * @date 2015-11-26 下午3:12:50 
 * @Description: TODO
 *  
 */
public abstract class UnifiedDb {
	protected int dbId;
	protected String dbName;
	protected String dbPasswd;
	protected int dbRole;
	protected String tablespaceName;
	protected String dbType;
	protected Date createTime;
	protected String descp;
	protected DbServer dbServer;

	public UnifiedDb(){}
	public UnifiedDb(String dbName,String dbPasswd,int dbRole,String tablespaceName,String dbType){
		this.dbName=dbName;
		this.dbPasswd=dbPasswd;
		this.dbRole=dbRole;
		this.tablespaceName=tablespaceName;
		this.dbType=dbType;
	}
	public UnifiedDb(int dbId,String dbName,String dbPasswd,int dbRole,String tablespaceName,String dbType,DbServer dbServer){
		this.dbId=dbId;
		this.dbName=dbName;
		this.dbPasswd=dbPasswd;
		this.dbRole=dbRole;
		this.tablespaceName=tablespaceName;
		this.dbType=dbType;
		this.dbServer=dbServer;
	}
	
	public boolean isAdminDb()throws DataHubException{
		return dbRole==1?true:false;
	};
	/**
	 * 不同类型，可以覆盖此方法
	 * @return
	 */
	public Map<String,Object> getConnectParam(){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("dbName", dbName);
		map.put("dbPasswd", dbPasswd);
		map.put("serverIp", dbServer.getIp());
		map.put("serverPort", dbServer.getPort());
		map.put("serverType", dbServer.getType());
		return map;
	}
	public abstract UnifiedDb getAdminDb()throws DataHubException;
	/**
	 * 在DbServer上创建数据库
	 * @return
	 * @throws Exception
	 */
	public abstract boolean create(UnifiedDb adminDb)throws DataHubException;
	public abstract String getConnectString()throws DataHubException;
	
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
	public String getDbPasswd() {
		return dbPasswd;
	}
	public void setDbPasswd(String dbPasswd) {
		this.dbPasswd = dbPasswd;
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
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
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
