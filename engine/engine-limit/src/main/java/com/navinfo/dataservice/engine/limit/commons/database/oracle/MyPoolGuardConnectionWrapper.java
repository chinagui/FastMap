package com.navinfo.dataservice.engine.limit.commons.database.oracle;


import org.apache.commons.dbcp.DelegatingCallableStatement;
import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DelegatingPreparedStatement;
import org.apache.commons.dbcp.DelegatingStatement;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Map;

/**
 * PoolGuardConnectionWrapper is a Connection wrapper that makes sure a
 * closed connection cannot be used anymore.
 */
public class MyPoolGuardConnectionWrapper extends DelegatingConnection {
	private static Logger log = Logger.getLogger(MyPoolGuardConnectionWrapper.class);
	private MyPoolingDataSource myPoolingDataSource;

	private Connection delegate;

	/**
	 * 服务端采用 ALTER SYSTEM DISCONNECT SESSION 'sid,serial#' IMMEDIATE;关闭当前连接
	 * 
	 * @throws SQLException
	 */
	/*
	 * public void serverClose() throws SQLException {
	 * //if (!isClosed()) {
	 * Connection conn = null;
	 * try {
	 * org.springframework.jdbc.datasource.DriverManagerDataSource dataSource =
	 * new org.springframework.jdbc.datasource.DriverManagerDataSource();
	 * dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
	 * dataSource.setUrl(url);
	 * dataSource.setUsername(username);
	 * dataSource.setPassword(password);
	 * String sql = "ALTER SYSTEM DISCONNECT SESSION '" + sid + "," + serial +
	 * "' IMMEDIATE";
	 * log.debug(sql);
	 * conn = dataSource.getConnection();
	 * QueryRunner runner = new QueryRunner();
	 * runner.execute(conn, sql);
	 * } catch (Exception e) {
	 * log.error(e.getMessage());
	 * } finally {
	 * DbUtils.closeQuietly(conn);
	 * }
	 * 
	 * //}
	 * 
	 * }
	 */

	private SessionProperties sessionProperties = new SessionProperties();

	public SessionProperties getSessionProperties() {
		return sessionProperties;
	}

	public void setSessionProperties(SessionProperties sessionProperties) {
		this.sessionProperties = sessionProperties;
	}

	public void setUrl(String url) {
		sessionProperties.setUrl(url);
	}

	public void setUsername(String username) {
		sessionProperties.setUsername(username);
	}

	public void setPassword(String password) {
		sessionProperties.setPassword(password);
	}

	public void setSid(long sid) {
		sessionProperties.setSid(sid);
	}

	public void setSerial(long serial) {
		sessionProperties.setSerial(serial);
	}

	public MyPoolGuardConnectionWrapper(Connection delegate, MyPoolingDataSource myPoolingDataSource) {
		super(delegate);
		this.delegate = delegate;
		this.myPoolingDataSource = myPoolingDataSource;
		if (delegate instanceof MyPoolableConnection) {
			MyPoolableConnection myPoolableConnection = (MyPoolableConnection) delegate;
			setSid(myPoolableConnection.getSid());
			setSerial(myPoolableConnection.getSerial());
		}

	}

	protected void checkOpen() throws SQLException {
		if (delegate == null) {
			throw new SQLException("Connection is closed.");
		}
	}

	public void close() throws SQLException {
		if (delegate != null) {
			this.delegate.close();
			this.delegate = null;
			super.setDelegate(null);
		}
	}

	public boolean isClosed() throws SQLException {
		if (delegate == null) {
			return true;
		}
		return delegate.isClosed();
	}

	public void clearWarnings() throws SQLException {
		checkOpen();
		delegate.clearWarnings();
	}

	public void commit() throws SQLException {
		checkOpen();
		delegate.commit();
	}

	public Statement createStatement() throws SQLException {
		checkOpen();
		return new DelegatingStatement(this, delegate.createStatement());
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		checkOpen();
		return new DelegatingStatement(this, delegate.createStatement(resultSetType, resultSetConcurrency));
	}

	public boolean innermostDelegateEquals(Connection c) {
		Connection innerCon = super.getInnermostDelegate();
		if (innerCon == null) {
			return c == null;
		} else {
			return innerCon.equals(c);
		}
	}

	public boolean getAutoCommit() throws SQLException {
		checkOpen();
		return delegate.getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		checkOpen();
		return delegate.getCatalog();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		checkOpen();
		return delegate.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		checkOpen();
		return delegate.getTransactionIsolation();
	}

	public Map getTypeMap() throws SQLException {
		checkOpen();
		return delegate.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		checkOpen();
		return delegate.getWarnings();
	}

	public int hashCode() {
		if (delegate == null) {
			return 0;
		}
		return delegate.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		// Use superclass accessor to skip access test
		Connection conn = super.getInnermostDelegate();
		if (conn == null) {
			return false;
		}
		if (obj instanceof DelegatingConnection) {
			DelegatingConnection c = (DelegatingConnection) obj;
			return c.innermostDelegateEquals(conn);
		}
		else {
			return conn.equals(obj);
		}
	}

	public boolean isReadOnly() throws SQLException {
		checkOpen();
		return delegate.isReadOnly();
	}

	public String nativeSQL(String sql) throws SQLException {
		checkOpen();
		return delegate.nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		checkOpen();
		return new DelegatingCallableStatement(this, delegate.prepareCall(sql));
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		checkOpen();
		return new DelegatingCallableStatement(this, delegate.prepareCall(sql, resultSetType, resultSetConcurrency));
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		checkOpen();
		return new DelegatingPreparedStatement(this, delegate.prepareStatement(sql));
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		checkOpen();
		return new DelegatingPreparedStatement(this, delegate.prepareStatement(sql, resultSetType, resultSetConcurrency));
	}

	public void rollback() throws SQLException {
		checkOpen();
		delegate.rollback();
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		checkOpen();
		delegate.setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException {
		checkOpen();
		delegate.setCatalog(catalog);
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		checkOpen();
		delegate.setReadOnly(readOnly);
	}

	public void setTransactionIsolation(int level) throws SQLException {
		checkOpen();
		delegate.setTransactionIsolation(level);
	}

	public void setTypeMap(Map map) throws SQLException {
		checkOpen();
		delegate.setTypeMap(map);
	}

	public String toString() {
		if (delegate == null) {
			return "NULL";
		}
		return delegate.toString();
	}

	public int getHoldability() throws SQLException {
		checkOpen();
		return delegate.getHoldability();
	}

	public void setHoldability(int holdability) throws SQLException {
		checkOpen();
		delegate.setHoldability(holdability);
	}

	public java.sql.Savepoint setSavepoint() throws SQLException {
		checkOpen();
		return delegate.setSavepoint();
	}

	public java.sql.Savepoint setSavepoint(String name) throws SQLException {
		checkOpen();
		return delegate.setSavepoint(name);
	}

	public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException {
		checkOpen();
		delegate.releaseSavepoint(savepoint);
	}

	public void rollback(java.sql.Savepoint savepoint) throws SQLException {
		checkOpen();
		delegate.rollback(savepoint);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		checkOpen();
		return new DelegatingStatement(this, delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		checkOpen();
		return new DelegatingCallableStatement(this, delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		checkOpen();
		return new DelegatingPreparedStatement(this, delegate.prepareStatement(sql, autoGeneratedKeys));
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		checkOpen();
		return new DelegatingPreparedStatement(this, delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		checkOpen();
		return new DelegatingPreparedStatement(this, delegate.prepareStatement(sql, columnIndexes));
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		checkOpen();
		return new DelegatingPreparedStatement(this, delegate.prepareStatement(sql, columnNames));
	}

	/**
	 * @see DelegatingConnection#getDelegate()
	 */
	public Connection getDelegate() {
		if (myPoolingDataSource.isAccessToUnderlyingConnectionAllowed()) {
			return super.getDelegate();
		} else {
			return null;
		}
	}

	/**
	 * @see DelegatingConnection#getInnermostDelegate()
	 */
	public Connection getInnermostDelegate() {
		if (myPoolingDataSource.isAccessToUnderlyingConnectionAllowed()) {
			return super.getInnermostDelegate();
		} else {
			return null;
		}
	}
}