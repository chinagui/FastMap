package com.navinfo.dataservice.engine.limit.datahub.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/** 
* @ClassName: DbInfo 
* @author Xiao Xiaowen 
* @date 2016年6月6日 下午8:49:27 
* @Description: TODO
*  
*/
public class DbInfo implements Serializable{
	//nationRoad,desDayAll,desMon,desDayPoi,fmMan,fmSys
	public static enum BIZ_TYPE {
        DES_MON("desMon"), 
        DES_DAY_POI("desDayPoi"), 
        DES_DAY_ALL("desDayAll"),
        GDB_PLUS("nationRoad");//gdb+母库
        private String value;
		private BIZ_TYPE(String value) {
            this.value = value;
        }
		public String getValue() {
			return value;
		}
		public String toString(){
			return value;
		}
		
    }
	
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
	public DbInfo(){
	}
	public DbInfo(int dbId, String dbName, String dbUserName, String dbUserPasswd, String bizType, String gdbVersion
			, DbServer dbServer, int dbStatus){
		this.dbId=dbId;
		this.dbName=dbName;
		this.dbUserName=dbUserName;
		this.dbUserPasswd=dbUserPasswd;
		this.bizType=bizType;
		this.dbServer=dbServer;
		this.gdbVersion=gdbVersion;
		this.dbStatus=dbStatus;
	}
	public DbInfo(int dbId, String dbName, String dbUserName, String dbUserPasswd, int dbRole, String tablespaceName, String bizType
			, DbServer dbServer, String gdbVersion, int dbStatus, Date createTime, String descp){
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
	
	public boolean isSuperDb(){
		return dbRole==1?true:false;
	}
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
		map.put("bizType", bizType);
		map.put("serviceName", dbServer.getServiceName());
		return map;
	}
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
	public String getGdbVersion() {
		return gdbVersion;
	}
	public void setGdbVersion(String gdbVersion) {
		this.gdbVersion = gdbVersion;
	}
	public int getDbStatus() {
		return dbStatus;
	}
	public void setDbStatus(int dbStatus) {
		this.dbStatus = dbStatus;
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
	@Override
	public String toString() {
		return "DbInfo [dbId=" + dbId + ", dbName=" + dbName + ", dbUserName="
				+ dbUserName + ", dbUserPasswd=" + dbUserPasswd + ", dbRole="
				+ dbRole + ", tablespaceName=" + tablespaceName + ", bizType="
				+ bizType + ", gdbVersion=" + gdbVersion + ", dbStatus="
				+ dbStatus + ", createTime=" + createTime + ", descp=" + descp
				+ ", dbServer=" + dbServer + "]";
	}
	
	
}
