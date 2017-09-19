package com.navinfo.dataservice.engine.limit.commons.database.navi;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 包装数据库连接，重写close、commit、rollback方法，实现统一事务管理
 * 
 * @author zhangjianjun
 * 
 */
public class TransactionalConnection implements Connection {
	/**
	 * 原始数据库连接
	 */
	Connection srcConnection;
	TransactionalDataSource transactionalDataSource;

	public Connection getDelegate() {
		return srcConnection;
	}

	public TransactionalConnection(
			TransactionalDataSource transactionalDataSource,
			Connection srcConnection) {
		this.srcConnection = srcConnection;
		this.transactionalDataSource = transactionalDataSource;
	}

	public void commit() throws SQLException {
		// srcConnection.commit();屏蔽提交
	}

	public void rollback() throws SQLException {
		// srcConnection.rollback();屏蔽回滚
	}

	public void close() throws SQLException {
		// srcConnection.close();屏蔽关闭
		transactionalDataSource.giveBackConnection(this);
	}

	/**
	 * 获取包装前的原始连接
	 * 
	 * @return
	 */
	public Connection getSrcConnection() {
		return srcConnection;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return srcConnection.unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return srcConnection.isWrapperFor(iface);
	}

	public Statement createStatement() throws SQLException {
		return srcConnection.createStatement();
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return srcConnection.prepareStatement(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return srcConnection.prepareCall(sql);
	}

	public String nativeSQL(String sql) throws SQLException {
		return srcConnection.nativeSQL(sql);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		srcConnection.setAutoCommit(autoCommit);
	}

	public boolean getAutoCommit() throws SQLException {
		return srcConnection.getAutoCommit();
	}

	public boolean isClosed() throws SQLException {
		return srcConnection.isClosed();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return srcConnection.getMetaData();
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		srcConnection.setReadOnly(readOnly);
	}

	public boolean isReadOnly() throws SQLException {
		return srcConnection.isReadOnly();
	}

	public void setCatalog(String catalog) throws SQLException {
		srcConnection.setCatalog(catalog);
	}

	public String getCatalog() throws SQLException {
		return srcConnection.getCatalog();
	}

	public void setTransactionIsolation(int level) throws SQLException {
		srcConnection.setTransactionIsolation(level);
	}

	public int getTransactionIsolation() throws SQLException {
		return srcConnection.getTransactionIsolation();
	}

	public SQLWarning getWarnings() throws SQLException {
		return srcConnection.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		srcConnection.clearWarnings();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return srcConnection.createStatement(resultSetType,
				resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return srcConnection.prepareStatement(sql, resultSetType,
				resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return srcConnection.prepareCall(sql, resultSetType,
				resultSetConcurrency);
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return srcConnection.getTypeMap();
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		srcConnection.setTypeMap(map);
	}

	public void setHoldability(int holdability) throws SQLException {
		srcConnection.setHoldability(holdability);
	}

	public int getHoldability() throws SQLException {
		return srcConnection.getHoldability();
	}

	public Savepoint setSavepoint() throws SQLException {
		return srcConnection.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return srcConnection.setSavepoint(name);
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		srcConnection.rollback(savepoint);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		srcConnection.releaseSavepoint(savepoint);
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return srcConnection.createStatement(resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return srcConnection.prepareStatement(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return srcConnection.prepareCall(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return srcConnection.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return srcConnection.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return srcConnection.prepareStatement(sql, columnNames);
	}

	public Clob createClob() throws SQLException {
		return srcConnection.createClob();
	}

	public Blob createBlob() throws SQLException {
		return srcConnection.createBlob();
	}

	public NClob createNClob() throws SQLException {
		return srcConnection.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return srcConnection.createSQLXML();
	}

	public boolean isValid(int timeout) throws SQLException {
		return srcConnection.isValid(timeout);
	}

	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		srcConnection.setClientInfo(name, value);
	}

	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		srcConnection.setClientInfo(properties);
	}

	public String getClientInfo(String name) throws SQLException {
		return srcConnection.getClientInfo(name);
	}

	public Properties getClientInfo() throws SQLException {
		return srcConnection.getClientInfo();
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return srcConnection.createArrayOf(typeName, elements);
	}

	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return srcConnection.createStruct(typeName, attributes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#abort(java.util.concurrent.Executor)
	 */
	@Override
	public void abort(Executor executor) throws SQLException {
		throw new UnsupportedOperationException(
				"Not supported by BasicDataSource");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getNetworkTimeout()
	 */
	@Override
	public int getNetworkTimeout() throws SQLException {
		return srcConnection.getNetworkTimeout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getSchema()
	 */
	@Override
	public String getSchema() throws SQLException {
		return srcConnection.getSchema();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setNetworkTimeout(java.util.concurrent.Executor,
	 * int)
	 */
	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds)
			throws SQLException {
		srcConnection.setNetworkTimeout(executor, milliseconds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setSchema(java.lang.String)
	 */
	@Override
	public void setSchema(String schema) throws SQLException {
		srcConnection.setSchema(schema);
	}
}
