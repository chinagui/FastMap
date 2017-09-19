package com.navinfo.dataservice.engine.limit.commons.database.navi;

import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 受事务控制的数据源，对所有新打开的连接进行统一事务控制，实现统一提交、回滚、关闭
 *
 * 使用场景:
 *  主要用于多线程,多连接的统一事务提交.
 *
 * 使用方法:
 * 1.传入数据源给构造器,得到包装后的数据源对象
 * 2.各子线程程序获取连接,使用完后,调用连接的close方法,调用连接的commit和rollback将被忽略掉
 * 3.当各子线程处理完毕后,依次调用此类的commitAll和closeAll方法
 * 4.调用rollback可回滚所有连接的事务
 *
 * 注意:
 *  1.获取新连接时,已关闭连接的自动事务提交
 *  2.当出现错误时,此类不会自动回滚事务
 *
 *
 * 实现原理：
 *  根据客户程序传入的数据源，生成新的数据源包装类.
 *  当客户程序获取新连接时，首先从连接列表中获取空闲的连接，
 *  如果没有找到，则从原始数据源获取新连接,并包装为 TransactionalConnection返回给客户程序.
 *  (连接会被标记为使用中,然后放入连接列表中)
 *  客户程序使用完连接后调用TransactionalConnection.close方法可归还连接,
 *  如果继续使用此连接将抛出异常.
 *  调用TransactionalConnection 的 commit 和 rollback 将不会实际触发提交和回滚操作,而是被忽略掉。
 *  最后通过依次调用TransactionalDataSource的commitAll()和closeAll()来提交所有连接的事务,关闭所有连接.
 *
 * </pre>
 *
 * @author zhangjianjun
 *
 */
public class TransactionalDataSource implements DataSource {
	protected static Logger log = Logger.getLogger(TransactionalDataSource.class);
	protected DataSource srcDataSource;
	protected List<ConnectionInfo> connectionInfoList = new ArrayList<ConnectionInfo>(10);
	/**
	 * 可打开的最大连接数,超过则抛出异常
	 */
	protected int maxConnectionSize = 20;// TODO 可由配置文件管理
	private boolean rollbacked;
	private boolean closed;
	private boolean commited;
//	private static Connection connection;

	/**
	 *
	 * @param srcDataSource
	 *            原始数据源，由外部传入
	 *
	 */
	public TransactionalDataSource(DataSource srcDataSource) {
		if (srcDataSource == null) {
			throw new NullPointerException("数据源为空");
		}
		this.srcDataSource = srcDataSource;
	}

	public TransactionalDataSource(DataSource srcDataSource, int maxConnectionSize) {
		if (srcDataSource == null) {
			throw new NullPointerException("数据源为空");
		}
		this.maxConnectionSize=maxConnectionSize;
		this.srcDataSource = srcDataSource;
	}


	/**
	 * 将已打开的连接放入列表中保存，当获取新连接时，首先从已保存的连接中查找空闲的连接
	 *
	 * @param conn
	 */
	public synchronized ConnectionInfo put(TransactionalConnection conn) {
		ConnectionInfo connInfo = new ConnectionInfo();
		connInfo.idle = true;
		connInfo.connection = conn;
		connectionInfoList.add(connInfo);
		log.debug("新连接:" + conn);
		return connInfo;
	}

	public synchronized ConnectionInfo put(TransactionalConnection conn,boolean use) {
		ConnectionInfo connInfo = new ConnectionInfo();
		connInfo.idle = false;
		connInfo.connection = conn;
		connectionInfoList.add(connInfo);
		log.debug("put:hashcode=" + conn.hashCode() + "," + conn);
		return connInfo;
	}
	/**
	 * 归还连接，此类不会提交事务，不会关闭连接
	 */
	public synchronized void giveBackConnection(Connection conn) {
		if (conn == null) {
			throw new RuntimeException("收回连接失败：连接不能为空");
		}
		if(conn instanceof TransactionalConnection){
			log.debug("准备收回连接：" + ((TransactionalConnection)conn).getSrcConnection());
		}else{
			log.debug("准备收回连接：" + conn);
		}
		for (int i = 0; i < connectionInfoList.size(); i++) {
			ConnectionInfo connInfo = connectionInfoList.get(i);
			if (connInfo.connection == (conn)) {
				connInfo.idle = true;
				log.debug("已回收连接:" + conn);
				return;
			}
		}
		log.debug("收回连接失败,连接列表:");
		for (int i = 0; i < connectionInfoList.size(); i++) {
			ConnectionInfo connInfo = connectionInfoList.get(i);
			log.debug(connInfo.connection);
		}
		throw new RuntimeException("收回连接失败：" + conn);
	}

	public synchronized Connection getConnection() throws SQLException {
		if(rollbacked){
			throw new RuntimeException("连接池已回滚，不能获取新连接");
		}
		if(commited){
			throw new RuntimeException("连接池已提交，不能获取新连接");
		}
		if(closed){
			throw new RuntimeException("连接池已关闭，不能获取新连接");
		}
		for (int i = 0; i < connectionInfoList.size(); i++) {
			ConnectionInfo connInfo = connectionInfoList.get(i);
			if (connInfo.idle) {
				connInfo.idle = false;
				log.debug("获取到空闲的连接：" + connInfo.connection);
				return connInfo.connection;
			}
		}
		log.debug("未找到空闲的连接，尝试从原始数据源获取新的连接");
		if (connectionInfoList.size() >= maxConnectionSize) {
			throw new SQLException("已超过允许打开的最大连接数:" + maxConnectionSize);
		}
		try {
			Connection srcConnection = srcDataSource.getConnection();
			srcConnection.setAutoCommit(false);
			TransactionalConnection connection = new TransactionalConnection(this, srcConnection);// 包装连接
			//记录连接打开的函数调用者
			String moduleName = "";
			String actionName = "open new connection";
			Thread currentThread = Thread.currentThread();
			String threadName = currentThread.getName();
			moduleName += threadName + ":";
			StackTraceElement[] stackTrace = currentThread.getStackTrace();
			if (stackTrace != null && stackTrace.length > 2) {
				String classname = stackTrace[2].getClassName();
				classname = classname.substring(classname.lastIndexOf(".") + 1);
				moduleName += classname + ".";
				moduleName += stackTrace[2].getMethodName() + "() ";
				moduleName += stackTrace[2].getLineNumber();
			}
			TraceConnection.trace(connection, moduleName, actionName);
			put(connection,true);
			log.debug("从原始数据源获取新的连接：" + srcConnection);
			return connection;
		} catch (SQLException e) {
			throw new SQLException("从原始数据源获取新的连接失败：" + e.getMessage(),e);
		}
	}

	public synchronized Connection getConnection(String username, String password) throws SQLException {
		if(rollbacked){
			throw new RuntimeException("连接池已回滚，不能获取新连接");
		}
		if(commited){
			throw new RuntimeException("连接池已提交，不能获取新连接");
		}
		if(closed){
			throw new RuntimeException("连接池已关闭，不能获取新连接");
		}
		for (int i = 0; i < connectionInfoList.size(); i++) {
			ConnectionInfo connInfo = connectionInfoList.get(i);
			if (connInfo.idle) {
				connInfo.idle = false;
				log.debug("getIdleConnection:" + connInfo.connection);
				return connInfo.connection;
			}
		}
		log.debug("未找到空闲的连接，尝试从原始数据源获取新的连接");
		if (connectionInfoList.size() >= maxConnectionSize) {
			throw new SQLException("已超过允许打开的最大连接数:" + maxConnectionSize);
		}
		try {
			Connection srcConnection = srcDataSource.getConnection(username, password);
			srcConnection.setAutoCommit(false);
			TransactionalConnection connection = new TransactionalConnection(this, srcConnection);// 包装连接
			//记录连接打开的函数调用者
			String moduleName = "";
			String actionName = "open new connection";
			Thread currentThread = Thread.currentThread();
			String threadName = currentThread.getName();
			moduleName += threadName + ":";
			StackTraceElement[] stackTrace = currentThread.getStackTrace();
			if (stackTrace != null && stackTrace.length > 2) {
				String classname = stackTrace[2].getClassName();
				classname = classname.substring(classname.lastIndexOf(".") + 1);
				moduleName += classname + ".";
				moduleName += stackTrace[2].getMethodName() + "() ";
				moduleName += stackTrace[2].getLineNumber();
			}
			TraceConnection.trace(connection, moduleName, actionName);
			put(connection);
			return connection;
		} catch (SQLException e) {
			throw new SQLException("从原始数据源获取新的连接失败：" + e.getMessage());
		}
	}



	/**
	 * 提交所有连接的事务
	 */
	public synchronized void commitAll() throws Exception {
		try {
			for (int i = 0; i < connectionInfoList.size(); i++) {
				ConnectionInfo connInfo = connectionInfoList.get(i);
				connInfo.connection.getSrcConnection().commit();
				log.debug("成功提交事务:" + connInfo.connection);
			}
			commited=true;
		} catch (SQLException e) {
			log.error("提交事务失败：", e);
			throw new Exception("提交事务失败：", e);
		}
	}

	/**
	 * 回滚所有连接的事务
	 */
	public synchronized void rollbackAll() throws Exception {
		try {
			for (int i = 0; i < connectionInfoList.size(); i++) {
				ConnectionInfo connInfo = connectionInfoList.get(i);
				connInfo.connection.getSrcConnection().rollback();
				log.error("已回滚数据库事务:" + connInfo.connection);
			}
			rollbacked=true;
		} catch (SQLException e) {
			log.error("回滚数据库事务失败：", e);
			throw new Exception("回滚数据库事务失败：", e);
		}
	}

	/**
	 * 关闭所有的连接
	 */
	public synchronized void closeAll() throws Exception {
		try {
			for (int i = 0; i < connectionInfoList.size(); i++) {
				ConnectionInfo connInfo = connectionInfoList.get(i);
				log.debug("关闭连接:" + connInfo.connection);
				//记录连接打开的函数调用者
				String moduleName = "";
				String actionName = "prepare close connection";
				Thread currentThread = Thread.currentThread();
				String threadName = currentThread.getName();
				moduleName += threadName + ":";
				StackTraceElement[] stackTrace = currentThread.getStackTrace();
				if (stackTrace != null && stackTrace.length > 2) {
					String classname = stackTrace[2].getClassName();
					classname = classname.substring(classname.lastIndexOf(".") + 1);
					moduleName += classname + ".";
					moduleName += stackTrace[2].getMethodName() + "() ";
					moduleName += stackTrace[2].getLineNumber();
				}
				TraceConnection.trace(connInfo.connection, moduleName, actionName);
				connInfo.connection.getSrcConnection().close();
			}
			closed=true;
		} catch (SQLException e) {
			log.error("关闭连接失败：", e);
			throw new Exception("关闭连接失败：", e);
		}
	}

	public PrintWriter getLogWriter() throws SQLException {
		return srcDataSource.getLogWriter();
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return srcDataSource.unwrap(iface);
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		srcDataSource.setLogWriter(out);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return srcDataSource.isWrapperFor(iface);
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		srcDataSource.setLoginTimeout(seconds);
	}

	public int getLoginTimeout() throws SQLException {
		return srcDataSource.getLoginTimeout();
	}

	public class ConnectionInfo {
		String name; // 连接名称
		TransactionalConnection connection;// 数据库连接
		boolean idle;// 是否空闲
		public String getName() {
			return name;
		}
		public TransactionalConnection getConnection() {
			return connection;
		}
		public boolean isIdle() {
			return idle;
		}

	}

	public int getMaxConnectionSize() {
		return maxConnectionSize;
	}
	/**
	 * 设置最大连接个数
	 * @param maxConnectionSize
	 */
	public void setMaxConnectionSize(int maxConnectionSize) {
		this.maxConnectionSize = maxConnectionSize;
	}
	/**
	 * 获取原始数据源
	 * @return
	 */
	public DataSource getSrcDataSource() {
		return srcDataSource;
	}

	public List<ConnectionInfo> getConnectionInfoList() {
		return connectionInfoList;
	}

	/* (non-Javadoc)
	 * @see javax.sql.CommonDataSource#getParentLogger()
	 */
	@Override
	public java.util.logging.Logger getParentLogger()
			throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException("No support for an optional feature");
	}


}
