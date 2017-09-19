package com.navinfo.dataservice.engine.limit.commons.database.oracle;

import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.SQLNestedException;
import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;


public class MyPoolingDataSource extends PoolingDataSource {
	private static Logger log = Logger.getLogger(MyPoolingDataSource.class);

	public MyPoolingDataSource() {
		super();
	}

	public MyPoolingDataSource(ObjectPool pool) {
		super(pool);
	}

	public Connection getConnection() throws SQLException {
		try {
			Connection conn = (Connection) (_pool.borrowObject());
			if (conn != null) {
				conn = new MyPoolGuardConnectionWrapper(conn, this);
			}
			return conn;
		} catch (SQLException e) {
			throw e;
		} catch (NoSuchElementException e) {
			throw new SQLNestedException("Cannot get a connection, pool error " + e.getMessage(), e);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new SQLNestedException("Cannot get a connection, general error", e);
		}
	}

}