package com.navinfo.dataservice.engine.limit.commons.database.oracle;

import com.navinfo.dataservice.engine.limit.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.engine.limit.commons.database.oracle.MyPoolGuardConnectionWrapper;
import com.navinfo.dataservice.engine.limit.commons.database.oracle.MyPoolableConnectionFactory;
import com.navinfo.dataservice.engine.limit.commons.database.oracle.MyPoolingDataSource;
import com.navinfo.dataservice.engine.limit.commons.database.navi.TraceConnection;
import org.apache.commons.dbcp.*;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class PoolDataSource extends BasicDataSource {

	public static final String SYS_KEY = "SYS";
	private static Logger log = Logger.getLogger(PoolDataSource.class);
    private static boolean traceConnection;

	/**
     * Creates the PoolableConnectionFactory and attaches it to the connection pool.  This method only exists
     * so subclasses can replace the default implementation.
     * 
     * @param driverConnectionFactory JDBC connection factory
     * @param statementPoolFactory statement pool factory (null if statement pooling is turned off)
     * @param configuration abandoned connection tracking configuration (null if no tracking)
     * @throws SQLException if an error occurs creating the PoolableConnectionFactory
     */
    protected void createPoolableConnectionFactory(ConnectionFactory driverConnectionFactory,
            KeyedObjectPoolFactory statementPoolFactory, AbandonedConfig configuration) throws SQLException {
        PoolableConnectionFactory connectionFactory = null;
        try {
            connectionFactory =
                new MyPoolableConnectionFactory(driverConnectionFactory,
                                              connectionPool,
                                              statementPoolFactory,
                                              validationQuery,
                                              validationQueryTimeout,
                                              connectionInitSqls,
                                              defaultReadOnly,
                                              defaultAutoCommit,
                                              defaultTransactionIsolation,
                                              defaultCatalog,
                                              configuration);
            validateConnectionFactory(connectionFactory);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Cannot create PoolableConnectionFactory (" + e.getMessage() + ")", e);
        }
    }
    
    
    /**
     * Creates the actual data source instance.  This method only exists so
     * subclasses can replace the implementation class.
     * 
     * @throws SQLException if unable to create a datasource instance
     */
    protected void createDataSourceInstance() throws SQLException {
        PoolingDataSource pds = new MyPoolingDataSource(connectionPool);
        pds.setAccessToUnderlyingConnectionAllowed(isAccessToUnderlyingConnectionAllowed());
        pds.setLogWriter(logWriter);
        dataSource = pds;
    }
    
    public MyPoolGuardConnectionWrapper wrapConnection() throws SQLException {
		Connection conn = super.getConnection();
		MyPoolGuardConnectionWrapper  connWrapper=(MyPoolGuardConnectionWrapper)conn;
		
		
		connWrapper.setPassword(getPassword());
		connWrapper.setUrl(getUrl());
		connWrapper.setUsername(getUsername());
		return connWrapper;
	}

	public Connection getConnection() throws SQLException {
		MyPoolGuardConnectionWrapper connWrapper = wrapConnection();
        if(traceConnection)
        {
            traceConnection(connWrapper);
        }
		return ConnectionRegister.registerConnection(connWrapper);
	}

	public Connection getConnection(String vmTaskId) throws SQLException {
		MyPoolGuardConnectionWrapper connWrapper = wrapConnection();
		return ConnectionRegister.registerConnection(connWrapper, vmTaskId);
	}

    public void traceConnection(Connection con)
    {
        try {
            String moduleName = "";
            String actionName = "open connection";
            Thread currentThread = Thread.currentThread();
            String threadName = currentThread.getName();
            moduleName += threadName + ":";
            StackTraceElement[] stackTrace = currentThread.getStackTrace();
            if (stackTrace != null && stackTrace.length > 3) {
                String classname = stackTrace[3].getClassName();
                classname = classname.substring(classname.lastIndexOf(".") + 1);
                moduleName += classname + ".";
                moduleName += stackTrace[3].getMethodName() + "() ";
                moduleName += stackTrace[3].getLineNumber();
            }
            else if (stackTrace != null && stackTrace.length > 2) {
                String classname = stackTrace[2].getClassName();
                classname = classname.substring(classname.lastIndexOf(".") + 1);
                moduleName += classname + ".";
                moduleName += stackTrace[2].getMethodName() + "() ";
                moduleName += stackTrace[2].getLineNumber();
            }


            TraceConnection.trace(con, moduleName, actionName);
        } catch (Exception e)
        {
            log.warn("跟踪数据库连接失败:" + e.getMessage(), e);
        }
    }


    static
    {
        traceConnection =
                Boolean.valueOf(SystemConfigFactory.getSystemConfig().getValue("PoolDataSource.trace.enable","false"));
    }

}
