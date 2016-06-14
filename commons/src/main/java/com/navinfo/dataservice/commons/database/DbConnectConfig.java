package com.navinfo.dataservice.commons.database;

import java.util.Map;

/** 
* @ClassName: DbConnectConfig 
* @author Xiao Xiaowen 
* @date 2016年6月7日 下午4:38:23 
* @Description: TODO
*/
public class DbConnectConfig {
	public static final String TYPE_ORACLE = "ORACLE";
	public static final String TYPE_MYSQL = "MYSQL";

	protected String bizType;
	protected String dbName;
	protected String userName;
	protected String userPasswd;
	protected String serverIp;
	protected int serverPort;
	protected String serverType;
	protected String key;
	public DbConnectConfig(String bizType,String dbName,String userName,String userPasswd
			,String serverIp,int serverPort,String serverType,String key){
		this.bizType=bizType;
		this.dbName=dbName;
		this.userName=userName;
		this.userPasswd=userPasswd;
		this.serverIp=serverIp;
		this.serverPort=serverPort;
		this.serverType=serverType;
		this.key=serverIp+":"+serverPort+"/"+dbName;
	}
	
	public String getBizType() {
		return bizType;
	}
	public void setBizType(String bizType) {
		this.bizType = bizType;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserPasswd() {
		return userPasswd;
	}
	public void setUserPasswd(String userPasswd) {
		this.userPasswd = userPasswd;
	}
	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	public String getServerType() {
		return serverType;
	}
	public void setServerType(String serverType) {
		this.serverType = serverType;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
