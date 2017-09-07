package com.navinfo.dataservice.impcore.flushbylog;

import java.sql.Connection;

import java.sql.SQLException;

import com.navinfo.navicommons.database.TransactionalDataSource;

/*
 * @author zhaokk
 * 2017年9月6日
 * 描述：import-coreLogWriter.java
 */
public class LogWriterDay2Month extends LogWriter {

	private TransactionalDataSource targetDataSource;

	public LogWriterDay2Month(TransactionalDataSource targetDataSource,
			boolean ignoreError, String type) throws SQLException {

		this.targetDataSource = targetDataSource;
		this.type = type;
		this.ignoreError = ignoreError;

	}

	public void open() throws SQLException {
		targetDbConn = targetDataSource.getConnection();
		targetDbConn.setAutoCommit(false);

	}

	public void close() {
		targetDataSource.giveBackConnection(targetDbConn);

	}

}
