package com.navinfo.dataservice.engine.limit.commons.database;

import com.alibaba.druid.pool.DruidDataSource;
import com.navinfo.dataservice.engine.limit.commons.exception.DataSourceException;
import com.navinfo.dataservice.engine.limit.commons.log.LoggerRepos;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import javax.sql.DataSource;

/** 
* @ClassName: A 
* @author Xiao Xiaowen 
* @date 2016年7月13日 下午2:38:51 
* @Description: TODO
*/
public class DataSourceUtil {
	protected static Logger log = LoggerRepos.getLogger(DataSourceUtil.class);
	public static boolean isEnable(DataSource dataSource){
		if(dataSource==null)return false;
		if(dataSource instanceof BasicDataSource){
			return (!((BasicDataSource)dataSource).isClosed());
		}else if(dataSource instanceof DruidDataSource){
			return ((DruidDataSource)dataSource).isEnable();
		}
		return false;
	}
	public static void close(DataSource dataSource){
		if(dataSource==null)return;
		if(dataSource instanceof BasicDataSource){
			BasicDataSource bds = (BasicDataSource)dataSource;
			if(!bds.isClosed()){
				try {
					log.debug("关闭连接池. url:"+bds.getUrl()+",username:"+bds.getUsername());
					bds.close();
					bds = null;
					dataSource=null;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}else if(dataSource instanceof DruidDataSource){
			DruidDataSource dds = (DruidDataSource)dataSource;
			if(dds.isEnable()){
				try {
					log.debug("关闭连接池. url:"+dds.getUrl()+",username:"+dds.getUsername());
					dds.close();
					dds = null;
					dataSource=null;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	public static String createMysqlJdbcUrl(String host, Integer port, String dbName) {
		return "jdbc:mysql://"+host+":"+port+"/"+"dbName";
	
	}

	public static String getDriverClassName(String serverType){
		if(DbServerType.TYPE_ORACLE.equals(serverType)){
			return "oracle.jdbc.driver.OracleDriver";
		}else if(DbServerType.TYPE_MYSQL.equals(serverType)){
			return "com.mysql.jdbc.Driver";
		}else{
			throw new DataSourceException("不支持的数据库类型，找不到相应的jdbc.Driver");
		}
	}
	public static String getValidationQuery(String serverType){
		if(DbServerType.TYPE_ORACLE.equals(serverType)){
			return "select sysdate from dual";
		}else if(DbServerType.TYPE_MYSQL.equals(serverType)){
			return "select 1";
		}else{
			throw new DataSourceException("不支持的数据库类型，找不到相应的jdbc.Driver");
		}
	}
	/**
	 * 
	 * @param host
	 * @param port
	 * @param name:oracle-Service name，mysql-DBNAME
	 * @return
	 */
	public static String createJdbcUrl(String serverType,String host,Integer port,String name){
		if(DbServerType.TYPE_ORACLE.equals(serverType)){
			return createOracleJdbcUrl(host,port,name);
		}else if(DbServerType.TYPE_MYSQL.equals(serverType)){
			return DataSourceUtil.createMysqlJdbcUrl(host,port,name);
		}else{
			throw new DataSourceException("不支持的数据库类型，无法生成相应的URL");
		}
	}
	public static String createOracleJdbcUrl(String host, Integer port, String serverName) {
		//
		// 判断ip个数，如果是多个，则表示rac
		String url = "jdbc:oracle:thin:@" + host + ":" + port + "/" + serverName;
		/*String url = "jdbc:oracle:thin:@ (DESCRIPTION =\r\n" + 
				"		(ADDRESS_LIST =\r\n" + 
				"		(ADDRESS = (PROTOCOL = TCP)(HOST = "+host+")(PORT = "+port+"))\r\n" + 
				"		)\r\n" + 
				"		(CONNECT_DATA =\r\n" + 
				"		(SERVICE_NAME = "+serverName+")\r\n" + 
				"		)\r\n" + 
				"		)";
		*/
//		log.debug(url);
		
		return url;

	}
}
