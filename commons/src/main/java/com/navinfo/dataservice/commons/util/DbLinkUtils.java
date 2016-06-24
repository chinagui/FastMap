package com.navinfo.dataservice.commons.util;

import com.navinfo.dataservice.commons.database.DbConnectConfig;

public class DbLinkUtils {

	private static String getConnectionString(DbConnectConfig config){
		
		StringBuilder sb = new StringBuilder("(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)");
		
		sb.append("(HOST ="+config.getServerIp()+ ")");
		sb.append("(PORT =" +config.getServerPort() + ")))");
		sb.append("(CONNECT_DATA = (SERVICE_NAME = " + config.getServiceName() +")))");
		
		return sb.toString();

	}
	
	public static String getCreateSql(String dblinkName, DbConnectConfig config){
		
		String str = getConnectionString(config);
		
		String sql = "CREATE DATABASE LINK "+  dblinkName;
		
		sql += " CONNECT TO "+config.getUserName();
		
		sql += " IDENTIFIED BY "+ config.getUserPasswd();
		
		sql += " USING '"+str+"'";
	
		return sql;
	}
}
