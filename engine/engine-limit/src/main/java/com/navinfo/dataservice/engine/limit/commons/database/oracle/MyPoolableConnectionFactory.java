package com.navinfo.dataservice.engine.limit.commons.database.oracle;

import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingConnection;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.Collection;


public class MyPoolableConnectionFactory extends PoolableConnectionFactory {
	private static Logger log = Logger.getLogger(MyPoolableConnectionFactory.class);

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, boolean defaultReadOnly,
                                       boolean defaultAutoCommit, AbandonedConfig config) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, config);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, boolean defaultReadOnly,
                                       boolean defaultAutoCommit, int defaultTransactionIsolation, AbandonedConfig config) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, config);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, boolean defaultReadOnly,
                                       boolean defaultAutoCommit, int defaultTransactionIsolation, String defaultCatalog, AbandonedConfig config) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog, config);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, Boolean defaultReadOnly,
                                       boolean defaultAutoCommit, int defaultTransactionIsolation, String defaultCatalog, AbandonedConfig config) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog, config);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, boolean defaultReadOnly,
                                       boolean defaultAutoCommit, int defaultTransactionIsolation) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, boolean defaultReadOnly,
                                       boolean defaultAutoCommit) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery,
                                       Collection connectionInitSqls, Boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation, String defaultCatalog, AbandonedConfig config) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, connectionInitSqls, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog, config);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery,
                                       Collection connectionInitSqls, boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, connectionInitSqls, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery,
                                       Collection connectionInitSqls, boolean defaultReadOnly, boolean defaultAutoCommit) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, connectionInitSqls, defaultReadOnly, defaultAutoCommit);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout,
                                       Boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation, String defaultCatalog, AbandonedConfig config) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog, config);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout,
                                       boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout,
                                       boolean defaultReadOnly, boolean defaultAutoCommit) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, defaultReadOnly, defaultAutoCommit);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout,
                                       Collection connectionInitSqls, Boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation, String defaultCatalog, AbandonedConfig config) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, connectionInitSqls, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation,
				defaultCatalog, config);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout,
                                       Collection connectionInitSqls, boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, connectionInitSqls, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation);
	}

	public MyPoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout,
                                       Collection connectionInitSqls, boolean defaultReadOnly, boolean defaultAutoCommit) {
		super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, connectionInitSqls, defaultReadOnly, defaultAutoCommit);
	}

	public Object makeObject() throws Exception {
//		log.debug("连接池创建新连接");
		Connection conn = _connFactory.createConnection();
		if (conn == null) {
			throw new IllegalStateException("Connection factory returned null from createConnection");
		}
		initializeConnection(conn);
		if (null != _stmtPoolFactory) {
			KeyedObjectPool stmtpool = _stmtPoolFactory.createPool();
			conn = new PoolingConnection(conn, stmtpool);
			stmtpool.setFactory((PoolingConnection) conn);
		}
		
		
		MyPoolableConnection myPoolableConnection = new MyPoolableConnection(conn, _pool, _config);
		long sessionPro[]=ConnectionRegister.getOracleSessionProperties(conn);
		myPoolableConnection.setSid(sessionPro[0]);
		myPoolableConnection.setSerial(sessionPro[1]);
		return myPoolableConnection;
	}

	

}
