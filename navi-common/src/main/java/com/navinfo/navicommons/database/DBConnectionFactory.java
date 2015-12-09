package com.navinfo.navicommons.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.config.Config;
import com.navinfo.navicommons.config.DefaultProperties;
import com.navinfo.navicommons.config.SystemGlobals;
import com.navinfo.navicommons.exception.PropertiesParseException;
import com.navinfo.navicommons.resource.SchemaPool;
/**
*@deprecated
*/
public class DBConnectionFactory {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(DBConnectionFactory.class);

    private static DBConnectionFactory factory;
    private Map<String, BasicDataSource> dataSourceMap = new HashMap<String, BasicDataSource>();

    public static synchronized DBConnectionFactory getInstance() {
        if (factory == null) {
            factory = new DBConnectionFactory();
        }
        return factory;
    }


    public DataSource getDataSource(String key) {
        BasicDataSource source = dataSourceMap.get(key);
        if (source == null) {
            throw new PropertiesParseException("当前数据源[" + key + "]没有定义,请在属性文件*Config.properties中定义数据源");
        }
        return source;
    }

    public DataSource getDataSoure(SchemaPool resourcePool) {
        BasicDataSource ds = dataSourceMap.get(resourcePool.getResourceName());
        if (ds == null) {
            logger.debug("初次装载数据源" + resourcePool);
            ds = setupDataSource(resourcePool);
            dataSourceMap.put(resourcePool.getResourceName(), ds);
        }
        return ds;
    }


    private DBConnectionFactory() {


        String dataSourceKey = SystemGlobals.getValue("dataSourceKey");
        if (StringUtils.isNotBlank(dataSourceKey)) {

            String keyArray[] = dataSourceKey.split(",");
            for (int i = 0; i < keyArray.length; i++) {
                String key = keyArray[i];
                logger.info("start to setup datasource :" + key);
                BasicDataSource ds = setupDataSource(key);
                dataSourceMap.put(key, ds);
            }

        }


    }

    public BasicDataSource setupDataSource(SchemaPool resourcePool) {
        BasicDataSource bds = new BasicDataSourceExt();
        bds.setDriverClassName(resourcePool.getDriveClassName());
        bds.setUrl(resourcePool.getUrl());
        bds.setUsername(resourcePool.getUserName());
        bds.setPassword(resourcePool.getPassword());
        if (resourcePool.getInitialSize() > -1)
            bds.setInitialSize(resourcePool.getInitialSize());
        if (resourcePool.getMinIdle() > -1)
            bds.setMinIdle(resourcePool.getMinIdle());
        if (resourcePool.getMaxIdle() > -1)
            bds.setMaxIdle(resourcePool.getMaxIdle());
        if (resourcePool.getMaxWait() > -1)
            bds.setMaxWait(resourcePool.getMaxWait());
        if (resourcePool.getMaxActive() > -1)
            bds.setMaxActive(resourcePool.getMaxActive());
        bds.setAccessToUnderlyingConnectionAllowed(resourcePool.getAccessPhysicCon() == 1);
        /*bds.setTestOnBorrow(resourcePool.getTestOnBorrow() == 1);
        bds.setTestOnReturn(resourcePool.getTestOnReturn() == 1);
        if (resourcePool.getValidationQuery() != null && !"".equals(resourcePool.getValidationQuery()))
            bds.setValidationQuery(resourcePool.getValidationQuery());*/
        bds.setTestWhileIdle(true);
        bds.setTestOnBorrow(false);
        bds.setTestOnReturn(false);
        bds.setValidationQuery("select sysdate from dual");
        bds.setValidationQueryTimeout(1);
        bds.setTimeBetweenEvictionRunsMillis(30000);
        bds.setDefaultAutoCommit(false);
        bds.setNumTestsPerEvictionRun(resourcePool.getMaxActive());
        return bds;

    }

    public BasicDataSource setupDataSource(String dataSourceKey,
                                           Config config) {
    	
    	BasicDataSource bds = dataSourceMap.get(dataSourceKey);
        if (bds == null||bds.isClosed()) {
        	 String dSeKey = dataSourceKey + ".";
             String driveClassName = config.getValue(dSeKey + "jdbc.driverClassName");
             String url = config.getValue(dSeKey + "jdbc.url");
             String username = config.getValue(dSeKey + "jdbc.username");
             String password = config.getValue(dSeKey + "jdbc.password");
             String initialSize = config.getValue(dSeKey + "dataSource.initialSize");
             String minIdle = config.getValue(dSeKey + "dataSource.minIdle");
             String maxIdle = config.getValue(dSeKey + "dataSource.maxIdle");
             String maxWait = config.getValue(dSeKey + "dataSource.maxWait");
             String maxActive = config.getValue(dSeKey + "dataSource.maxActive");
             bds = new BasicDataSourceExt();
             bds.setDriverClassName(driveClassName);
             bds.setUrl(url);
             bds.setUsername(username);
             bds.setPassword(password);
             bds.setInitialSize(Integer.parseInt(initialSize));
             bds.setMinIdle(Integer.parseInt(minIdle));
             bds.setMaxIdle(Integer.parseInt(maxIdle));
             bds.setMaxWait(Long.parseLong(maxWait));
             bds.setMaxActive(Integer.parseInt(maxActive));
             bds.setAccessToUnderlyingConnectionAllowed(true);
             /*dbcp 是采用了 commons-pool 做为其连接池管理， testOnBorrow,testOnReturn, testWhileIdle 是 pool 是提供的几种校验机制，通过外部钩子的方式回调 dbcp 的相关数据库链接 (validationQuery) 校验 , dbcp 相关外部钩子类： PoolableConnectionFactory, 继承于 common-pool PoolableObjectFactory , dbcp 通过 GenericObjectPool 这一入口，进行连接池的 borrow,return 处理。
             具体参数描述：
             1. testOnBorrow : 顾明思义，就是在进行borrowObject进行处理时，对拿到的connection进行validateObject校验
             2. testOnReturn : 顾明思义，就是在进行returnObject对返回的connection进行validateObject校验，个人觉得对数据库连接池的管理意义不大
             3. testWhileIdle : 关注的重点，GenericObjectPool中针对pool管理，起了一个 异步Evict的TimerTask定时线程进行控制 ( 可通过设置参数 timeBetweenEvictionRunsMillis>0), 定时对线程池中的链接进行validateObject校验，对无效的链接进行关闭后，会调用ensureMinIdle，适当建立链接保证最小的minIdle连接数。
             4. timeBetweenEvictionRunsMillis, 设置的Evict线程的时间，单位ms，大于0才会开启evict检查线程
             5. validateQuery ， 代表检查的sql
             6. validateQueryTimeout ， 代表在执行检查时，通过statement设置，statement.setQueryTimeout(validationQueryTimeout)
             7. numTestsPerEvictionRun ，代表每次检查链接的数量，建议设置和maxActive一样大，这样每次可以有效检查所有的链接.
              <property name= "testWhileIdle" ><value> true </value></property>
              <property name= "testOnBorrow" ><value> false </value></property>
              <property name= "testOnReturn" ><value> false </value></property>
              <property name= "validationQuery" ><value>select sysdate from dual</value></property>
              <property name= "validationQueryTimeout" ><value>1</value></property>
              <property name= "timeBetweenEvictionRunsMillis" ><value>30000</value></property>
              <property name= "numTestsPerEvictionRun" ><value>16</value></property>*/
             bds.setTestWhileIdle(true);
             bds.setTestOnBorrow(false);
             bds.setTestOnReturn(false);
             bds.setDefaultAutoCommit(false);
             bds.setValidationQuery("select sysdate from dual");
             bds.setValidationQueryTimeout(1);
             bds.setTimeBetweenEvictionRunsMillis(30000);
             bds.setNumTestsPerEvictionRun(Integer.parseInt(maxActive));
             dataSourceMap.put(dataSourceKey, bds);
        }     
       
        return bds;

    }


    private BasicDataSource setupDataSource(String dataSourceKey) {
        return setupDataSource(dataSourceKey, new DefaultProperties());
    }


    public void shutdown() throws SQLException {
        Iterator<BasicDataSource> dataSourceIte = dataSourceMap.values().iterator();
        while (dataSourceIte.hasNext()) {
            BasicDataSource basicDataSource = dataSourceIte.next();
            basicDataSource.close();
        }
        factory = null;
    }

    public static Connection adapteDbcpPoolConnection(Connection con) {
        Connection oracleConnection = con;
       /* if(con instanceof com.navinfo.dms.tools.vm.database.MyDriverManagerConnectionWrapper){
        	con = ((com.navinfo.dms.tools.vm.database.MyDriverManagerConnectionWrapper)con).getDelegate();
        }*/
        if (con instanceof org.apache.commons.dbcp.DelegatingConnection)
            oracleConnection = ((org.apache.commons.dbcp.DelegatingConnection) con).getInnermostDelegate();
        return oracleConnection;
    }


    public static void main(String[] args) {
        DBConnectionFactory.getInstance();
    }

}
