package com.navinfo.dataservice.commons.database.oracle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.exception.DataSourceException;
import com.navinfo.dataservice.commons.log.DSJobLogger;
import com.navinfo.navicommons.config.Config;
import com.navinfo.navicommons.config.MavenConfigMap;

/**
 * 
 * 连接池 支持RAC环境
 * 
 * @author liuqing Fast
 * 
 */
public class PoolDataSourceFactory {

	private static Logger log = Logger.getLogger(PoolDataSourceFactory.class);
	private Map<String, PoolDataSource> dataSourceMap = new HashMap<String, PoolDataSource>();
	private MavenConfigMap systemConfig = SystemConfig.getSystemConfig();
	private static PoolDataSourceFactory factory;

	public static synchronized PoolDataSourceFactory getInstance() {
		if (factory == null) {
			factory = new PoolDataSourceFactory();
		}
		return factory;
	}

	
	
	public static void setFactory(PoolDataSourceFactory factory) {
        PoolDataSourceFactory.factory = factory;
    }



    /**
	 * 获取连接到子版本库的数据库连接池
	 * 
	 * @return
	 * @throws SQLException
	 */

	public synchronized PoolDataSource getManPoolDataSource() {
		return getPoolDataSource(PoolDataSource.MAN_KEY);
	}

		
	public MyDriverManagerDataSource getDriverManagerDataSource(String ip,Integer port,String sid,String username,String pwd,String driveClassName){
		MyDriverManagerDataSource dataSource = new MyDriverManagerDataSource();
		dataSource.setDriverClassName(driveClassName);
		String url = PoolDataSourceFactory.createUrl(ip, port, sid);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(pwd);
		return dataSource;
	}
	

	/**
	 * 
	 * @param dataSourceKey
	 * @return
	 * @throws SQLException
	 */
	public PoolDataSource getPoolDataSource(String dataSourceKey) {

		return getPoolDataSource(dataSourceKey, systemConfig);
	}

	/**
	 * 初始化连接池
	 * 
	 * @param dataSourceKey
	 * @param config
	 * @return
	 * @throws SQLException
	 */

	public PoolDataSource getPoolDataSource(String dataSourceKey, Config config) {
		return getPoolDataSource(dataSourceKey, config, true);
	}

	/**
	 * 初始化连接池
	 * 
	 * @param dataSourceKey
	 * @param config
	 * @return
	 * @throws SQLException
	 */

	public PoolDataSource getPoolDataSource(String dataSourceKey, Config config, boolean cache) {
		log = DSJobLogger.getLogger(log);
		PoolDataSource dataSource = dataSourceMap.get(dataSourceKey);
		if (dataSource == null || dataSource.isClosed()) {
			try {
				String dSeKey = dataSourceKey + ".";
				String driveClassName = config.getValue(dSeKey + "jdbc.driverClassName");
				String url = config.getValue(dSeKey + "jdbc.url");
				String username = config.getValue(dSeKey + "jdbc.username");
				String password = config.getValue(dSeKey + "jdbc.password");
				int initialSize = config.getIntValue(dSeKey + "dataSource.initialSize", 1);
				int maxActive = config.getIntValue(dSeKey + "dataSource.maxActive", 1);
				log.debug("=============================DBINFO==========================");
				log.debug("url:"+url);
				log.debug("username:"+username);
				log.debug("password:"+password);
				// DataSource pds = createDBCPPoolDataSource(dSeKey,
				// driveClassName, url, username, password, initialSize,
				// maxActive);
				dataSource = createDBCPPoolDataSource(dSeKey, driveClassName, url, username, password, initialSize, maxActive);
				// dataSource = new PoolDataSource(pds, dataSourceKey);
				if (cache)
					dataSourceMap.put(dataSourceKey, dataSource);
			} catch (Exception e) {
				throw new DataSourceException(e);
			}

		} else
			log.debug("find datasource from cache,key=" + dataSourceKey);
		return dataSource;

	}

	/**
	 * 创建支持rac的连接池
	 * Fast Connection Failover 连接池 支持RAC环境
	 * 
	 * @param dSeKey
	 * @param driveClassName
	 * @param url
	 * @param username
	 * @param password
	 * @param initialSize
	 * @param maxActive
	 * @return
	 * @throws SQLException
	 */

	public PoolDataSource createDBCPPoolDataSource(
			String dSeKey,
			String driveClassName,
			String url,
			String username,
			String password,
			int initialSize,
			int maxActive)
			throws SQLException {
		PoolDataSource bds = new PoolDataSource();
		bds.setDriverClassName(driveClassName);
		bds.setUrl(url);
		bds.setUsername(username);
		bds.setPassword(password);
		bds.setInitialSize(initialSize);
		bds.setMaxActive(maxActive);
		bds.setAccessToUnderlyingConnectionAllowed(true);
		/*
		 * dbcp 是采用了 commons-pool 做为其连接池管理， testOnBorrow,testOnReturn,
		 * testWhileIdle 是 pool 是提供的几种校验机制，通过外部钩子的方式回调 dbcp 的相关数据库链接
		 * (validationQuery) 校验 , dbcp 相关外部钩子类： PoolableConnectionFactory, 继承于
		 * common-pool PoolableObjectFactory , dbcp 通过 GenericObjectPool
		 * 这一入口，进行连接池的 borrow,return 处理。
		 * 具体参数描述：
		 * 1. testOnBorrow :
		 * 顾明思义，就是在进行borrowObject进行处理时，对拿到的connection进行validateObject校验
		 * 2. testOnReturn :
		 * 顾明思义，就是在进行returnObject对返回的connection进行validateObject校验
		 * ，个人觉得对数据库连接池的管理意义不大
		 * 3. testWhileIdle : 关注的重点，GenericObjectPool中针对pool管理，起了一个
		 * 异步Evict的TimerTask定时线程进行控制 ( 可通过设置参数 timeBetweenEvictionRunsMillis>0),
		 * 定时对线程池中的链接进行validateObject校验
		 * ，对无效的链接进行关闭后，会调用ensureMinIdle，适当建立链接保证最小的minIdle连接数。
		 * 4. timeBetweenEvictionRunsMillis, 设置的Evict线程的时间，单位ms，大于0才会开启evict检查线程
		 * 5. validateQuery ， 代表检查的sql
		 * 6. validateQueryTimeout ，
		 * 代表在执行检查时，通过statement设置，statement.setQueryTimeout
		 * (validationQueryTimeout)
		 * 7. numTestsPerEvictionRun
		 * ，代表每次检查链接的数量，建议设置和maxActive一样大，这样每次可以有效检查所有的链接.
		 * <property name= "testWhileIdle" ><value> true </value></property>
		 * <property name= "testOnBorrow" ><value> false </value></property>
		 * <property name= "testOnReturn" ><value> false </value></property>
		 * <property name= "validationQuery" ><value>select sysdate from
		 * dual</value></property>
		 * <property name= "validationQueryTimeout" ><value>1</value></property>
		 * <property name= "timeBetweenEvictionRunsMillis"
		 * ><value>30000</value></property>
		 * <property name= "numTestsPerEvictionRun"
		 * ><value>16</value></property>
		 */
		bds.setTestWhileIdle(true);
		bds.setTestOnBorrow(false);
		bds.setTestOnReturn(false);
		bds.setDefaultAutoCommit(false);
		bds.setValidationQuery("select sysdate from dual");
		bds.setValidationQueryTimeout(1);
		bds.setTimeBetweenEvictionRunsMillis(30000);
		bds.setNumTestsPerEvictionRun(maxActive);
		return bds;
	}

	/**
	 * 是否为RAC环境
	 * 
	 * @param config
	 * @return
	 */

	private boolean isRACEnv() {
		boolean isRAC = BooleanUtils.toBooleanObject(systemConfig.getValue("IS_RAC_ENV", "false"));
		return isRAC;
	}

	public void removeDataSource(String dsKey) {
		dataSourceMap.remove(dsKey);
	}

	/**
	 * 关闭所有连接池
	 * 
	 * @throws SQLException
	 */
	public void shutdown() throws SQLException {
		Iterator<PoolDataSource> dataSourceIte = dataSourceMap.values().iterator();
		while (dataSourceIte.hasNext()) {
			PoolDataSource basicDataSource = dataSourceIte.next();
			basicDataSource.close();
		}
		factory = null;
	}

	/**
	 * 
	 * @param con
	 * @return
	 */
	public static Connection adapteDbcpPoolConnection(Connection con) {
		Connection oracleConnection = con;
		if (con instanceof org.apache.commons.dbcp.DelegatingConnection)
			oracleConnection = ((org.apache.commons.dbcp.DelegatingConnection) con).getInnermostDelegate();
		if (con instanceof MyDriverManagerConnectionWrapper)
			oracleConnection = ((MyDriverManagerConnectionWrapper) con).getDelegate();
			
		return oracleConnection;
	}

	public static String createUrl(String host, Integer port, String serverName) {
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
	
	
	public static void main(String[] args) throws SQLException {
	}

}
