package com.navinfo.dataservice.commons.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.mongodb.MongoClient;
import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.oracle.MyDriverManagerDataSource;
import com.navinfo.dataservice.commons.database.oracle.MyDriverManagerConnectionWrapper;
import com.navinfo.dataservice.commons.database.oracle.PoolDataSource;
import com.navinfo.dataservice.commons.exception.DataSourceException;
import com.navinfo.dataservice.commons.log.LoggerRepos;

/**
 * 
 * 连接池 支持多类型数据库
 * 
 * @author liuqing Fast
 * 
 */
public class MultiDataSourceFactory {

	private static Logger log = LoggerRepos.getLogger(MultiDataSourceFactory.class);
	private Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
	private SystemConfig systemConfig = SystemConfigFactory.getSystemConfig();
	private Set<String> noCacheBizType = null;
	private static class SingletonHolder{
		private static final MultiDataSourceFactory INSTANCE = new MultiDataSourceFactory();
	}
	public static final MultiDataSourceFactory getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private MultiDataSourceFactory(){
		noCacheBizType = new HashSet<String>();
		String types = systemConfig.getValue("datasource.pool.nocache");
		if(StringUtils.isNotEmpty(types)){
			CollectionUtils.addAll(noCacheBizType, types.split(","));
		}
	}

	/**
	 * DriverManagerDataSource不做缓存,不托管，没有连接池
	 * @param dbServerType
	 * @param config
	 * @return
	 */
	public DriverManagerDataSource getDriverManagerDataSource(String serverType,String driverClassName,String url,String username,String pwd){
		DriverManagerDataSource dataSource = null;
		if(DbServerType.TYPE_ORACLE.equals(serverType)){
			dataSource = new MyDriverManagerDataSource();
		}else if(DbServerType.TYPE_MYSQL.equals(serverType)){
			dataSource = new DriverManagerDataSource();
		}else{
			throw new DataSourceException("不支持的数据库类型，找不到相应的DriverManagerDataSource");
		}
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(pwd);
		return dataSource;
	}
    /**
	 * 获取连接到管理库的数据库连接池
	 * 
	 * @return
	 * @throws SQLException
	 */
	public synchronized DataSource getSysDataSource() {
		return getDataSource(PoolDataSource.SYS_KEY,systemConfig,true);
	}
	/**
	 * 从配置中读取的数据库连接，均做cache
	 * @param dataSourceKey
	 * @return
	 * @throws SQLException
	 */
	public DataSource getDataSourceByKey(String dataSourceKey) {

		return dataSourceMap.get(dataSourceKey);
	}


/**
     * 初始化mongo连接
     * 
     * @param connConfig
     * @return
     * @throws Exception
     */
    public MongoClient getMongoClient(DbConnectConfig connConfig)
            throws Exception {

        try {
            String ip = connConfig.getServerIp();
            Integer port = connConfig.getServerPort();

            MongoClient mongoClient = new MongoClient(ip, port);
            
            return mongoClient;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

	/**
	 * 初始化连接池
	 * 
	 * @param dataSourceKey
	 * @param config
	 * @return
	 * @throws SQLException
	 */

	public DataSource getDataSource(DbConnectConfig connConfig) throws SQLException{
		if(!(DbServerType.TYPE_ORACLE.equals(connConfig.getServerType())
				||DbServerType.TYPE_MYSQL.equals(connConfig.getServerType()))){
			throw new SQLException("非ORACLE,MYSQL类型库，无法通过此方法获取连接类");
		}
		String key = connConfig.getKey();
		DataSource dataSource = dataSourceMap.get(key);
		if(dataSource!=null&&DataSourceUtil.isEnable(dataSource)){
			log.debug("find datasource from cache,key=" + key);
			return dataSource;
		}
		//缓存中没找到，新建并加入缓存
		try {
			String serverType = connConfig.getServerType();
			String driveClassName = DataSourceUtil.getDriverClassName(serverType);
			String ip = connConfig.getServerIp();
			Integer port = connConfig.getServerPort();
			String name = DbServerType.TYPE_ORACLE.equals(connConfig.getServerType())?connConfig.getServiceName():connConfig.getDbName();
			String url = DataSourceUtil.createJdbcUrl(serverType,ip,port,name);
			String username = connConfig.getUserName();
			String password = connConfig.getUserPasswd();
			int initialSize = SystemConfigFactory.getSystemConfig().getIntValue(PoolDataSource.SYS_KEY + ".dataSource.initialSize", 2);
			int maxActive = SystemConfigFactory.getSystemConfig().getIntValue(PoolDataSource.SYS_KEY + ".dataSource.maxActive", 25);
			log.debug("=============================DBINFO==========================");
			log.debug("url:"+url);
			log.debug("username:"+username);
			log.debug("password:"+password);
			String validationQuery = DataSourceUtil.getValidationQuery(serverType);
			String dsType = SystemConfigFactory.getSystemConfig().getValue(PropConstant.dataSourceType,"dbcp");
	    	DataSource newdataSource=null;
			if(dsType.equals("druid")){
	    		newdataSource = createDruidDataSource(serverType,driveClassName, url, username, password, initialSize, maxActive,validationQuery);
	    	}else{
	    		newdataSource = createDBCPPoolDataSource(serverType,driveClassName, url, username, password, initialSize, maxActive,validationQuery);
	    	}
			//根据配置的业务类型 库来缓存
			String bizType = connConfig.getBizType();
			if (StringUtils.isNotEmpty(bizType)&&(!noCacheBizType.contains(bizType))){
				synchronized(this){
					dataSource = dataSourceMap.get(key);
					if(dataSource==null||(!DataSourceUtil.isEnable(dataSource))){
						dataSource = newdataSource;
						dataSourceMap.put(key, newdataSource);
					}else{
						DataSourceUtil.close(newdataSource);
						newdataSource = null;
					}
				}
				dataSourceMap.put(key, dataSource);
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new SQLException(e);
		}
		return dataSource;
	}

	/**
	 * 初始化连接池
	 * 
	 * @param dataSourceKey
	 * @param config
	 * @return
	 * @throws SQLException
	 */

	private DataSource getDataSource(String dataSourceKey, SystemConfig config, boolean cache) {
		
		DataSource dataSource = dataSourceMap.get(dataSourceKey);
		if(dataSource!=null&&DataSourceUtil.isEnable(dataSource)){
			log.debug("find datasource from cache,key=" + dataSourceKey);
			return dataSource;
		}
		//缓存中没找到，新建并加入缓存
		try {
			String dSeKey = dataSourceKey + ".";
			String serverType = config.getValue(dSeKey + "server.type");
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
			String validationQuery = config.getValue(dSeKey+"dataSource.validationQuery");
			String dsType = SystemConfigFactory.getSystemConfig().getValue(PropConstant.dataSourceType,"dbcp");
			DataSource newdataSource=null;
			if(dsType.equals("druid")){
	    		newdataSource = createDruidDataSource(serverType,driveClassName, url, username, password, initialSize, maxActive,validationQuery);
	    	}else{
	    		newdataSource = createDBCPPoolDataSource(serverType,driveClassName, url, username, password, initialSize, maxActive,validationQuery);
	    	}
			if (cache){
				synchronized(this){
					dataSource = dataSourceMap.get(dataSourceKey);
					if(dataSource==null||(!DataSourceUtil.isEnable(dataSource))){
						dataSource = newdataSource;
						dataSourceMap.put(dataSourceKey, newdataSource);
					}else{
						DataSourceUtil.close(newdataSource);
						newdataSource = null;
					}
				}
			}
		} catch (Exception e) {
			throw new DataSourceException(e);
		}
		return dataSource;

	}
	private DruidDataSource createDruidDataSource(String serverType,
			String driveClassName,
			String url,
			String username,
			String password,
			int initialSize,
			int maxActive,String validationQuery)
			throws SQLException {
		DruidDataSource ds = new DruidDataSource();
		
		ds.setUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setInitialSize(initialSize);
		ds.setMaxActive(maxActive);
		ds.setAccessToUnderlyingConnectionAllowed(true);
		ds.setUseGlobalDataSourceStat(true);
		ds.setAccessToUnderlyingConnectionAllowed(true);
		ds.setTestWhileIdle(true);
		ds.setTestOnBorrow(false);
		ds.setTestOnReturn(false);
		ds.setDefaultAutoCommit(false);
		ds.setValidationQuery(validationQuery);
		ds.setValidationQueryTimeout(1);
		ds.setTimeBetweenEvictionRunsMillis(30000);
		//统计开关
		String statEnable= SystemConfigFactory.getSystemConfig().getValue(PropConstant.dataSourceStatEnable);
		if(StringUtils.isNotEmpty(statEnable)&&statEnable.equalsIgnoreCase("true")){
			ds.setFilters("stat");
		}
		return ds;
	}

	
	/**
	 * 创建普通的basicDataSource
	 */

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

	private BasicDataSource createDBCPPoolDataSource(
			String serverType,
			String driveClassName,
			String url,
			String username,
			String password,
			int initialSize,
			int maxActive,String validationQuery)
			throws SQLException {
		BasicDataSource bds = null;
		if(DbServerType.TYPE_ORACLE.equals(serverType)){
			bds = new PoolDataSource();
		}else if(DbServerType.TYPE_MYSQL.equals(serverType)){
			bds = new BasicDataSource();
		}else if(DbServerType.TYPE_POSTGRES.equals(serverType)){
			bds = new BasicDataSource();
		}else{
			throw new DataSourceException("不支持的数据库类型，找不到相应的DriverManagerDataSource");
		}
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
		bds.setValidationQuery(validationQuery);
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

	public boolean isRACEnv() {
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
		Iterator<DataSource> dataSourceIte = dataSourceMap.values().iterator();
		while (dataSourceIte.hasNext()) {
			DataSource dataSource = dataSourceIte.next();
			DataSourceUtil.close(dataSource);
		}
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

	
	public void closeDataSource(String key) {
		DataSource ds = dataSourceMap.get(key);
		if(ds!=null){
			synchronized(this){
				dataSourceMap.remove(key);
			}
			DataSourceUtil.close(ds);
		}
	}
	
	
	public static void main(String[] args) throws SQLException {
	}

}
