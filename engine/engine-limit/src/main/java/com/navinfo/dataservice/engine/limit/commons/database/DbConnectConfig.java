package com.navinfo.dataservice.engine.limit.commons.database;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/** 
 * 特别说明：
 * connectString格式："serverType(大写),serverIp,serverPort,serviceName/dbName,userName,userPasswd"
* @ClassName: DbConnectConfig 
* @author Xiao Xiaowen 
* @date 2016年6月7日 下午4:38:23 
* @Description: TODO
*/
public class DbConnectConfig {

	protected String bizType;
	protected String dbName;//ORACLE为空，其余不为空
	protected String userName;
	protected String userPasswd;
	protected String serverIp;
	protected int serverPort;
	protected String serverType;
	protected String serviceName;//ORACLE不为空，其余为空
	protected String key;
	public DbConnectConfig(String bizType, String dbName, String userName, String userPasswd
			, String serverIp, int serverPort, String serverType, String serviceName){
		this.bizType=bizType;
		this.dbName=dbName;
		this.userName=userName;
		this.userPasswd=userPasswd;
		this.serverIp=serverIp;
		this.serverPort=serverPort;
		this.serverType=serverType;
		this.serviceName=serviceName;
		if(DbServerType.TYPE_ORACLE.equals(serverType)){
			this.key=serverIp+":"+serverPort+"/"+userName;
		}else{
			this.key=serverIp+":"+serverPort+"/"+dbName;
		}
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
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * 
	 * @return
	 */
	public String toConnectString(){
		return this.serverType+","+this.serverIp+","+String.valueOf(this.serverPort)+","
		+(DbServerType.TYPE_ORACLE.equals(serverType)?serviceName:dbName)+","+this.userName+","+userPasswd;
	};
	
	/**
	 * 
	 * @param connectString:"ORALCE/MYSQL,ip,port,sid,username/dbname,user passwd"
	 * @param bizType
	 * @return
	 * @throws Exception
	 */
	public static DbConnectConfig createConnectConfig(String connectString,String bizType)throws Exception{
		if(StringUtils.isEmpty(connectString))throw new Exception("连接参数为空");
		String[] conArr = connectString.split(",");
		if(conArr.length!=6)throw new Exception("连接参数个数不为6，请检查连接参数");
		String serverType = conArr[0];
		String serverIp = conArr[1];
		int serverPort = Integer.valueOf(conArr[2]);
		String serviceName = null;
		String dbName = null;
		if(DbServerType.TYPE_ORACLE.equals(serverType)){
			serviceName = conArr[3];
		}else{
			dbName = conArr[3];
		}
		String userName = conArr[4];
		String userPasswd = conArr[5];
		//validation
		//...
		return new DbConnectConfig(bizType,dbName,userName,userPasswd
				,serverIp,serverPort,serverType,serviceName);
	}

	public static DbConnectConfig createConnectConfig(Map<String,Object> connParam){
		String bizType=(String)connParam.get("bizType");
		String dbName=(String)connParam.get("dbName");
		String userName = (String)connParam.get("dbUserName");
		String userPasswd=(String)connParam.get("dbUserPasswd");
		String serverIp=(String)connParam.get("serverIp");
		int serverPort=(Integer)connParam.get("serverPort");
		String serverType=(String)connParam.get("serverType");
		String serviceName=(String)connParam.get("serviceName");
		//validation
		//...
		return new DbConnectConfig(bizType,dbName,userName,userPasswd
				,serverIp,serverPort,serverType,serviceName);
	}
}
