package com.navinfo.dataservice.bizcommons.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import com.navinfo.dataservice.commons.database.oracle.ConnectionRegister;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;

/**
 * 执行DDL语句
 * 
 * @author liuqing 。
 */
public class DMLExecThreadHandler2 implements Runnable {
	protected Logger log;
	protected List<String> sqlList = null;
	protected DataSource dataSource;
	protected CountDownLatch doneSignal;
	protected ThreadLocalContext ctx;

	public DMLExecThreadHandler2(CountDownLatch doneSignal,
			List<String> sqlList,
			DataSource ds, ThreadLocalContext ctx) {
		this.doneSignal = doneSignal;
		this.dataSource = ds;
		this.log = ctx.getLog();
		this.ctx = ctx;
		this.sqlList = sqlList;

	}

	public void run() {
		String execSql = null;
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = ConnectionRegister.subThreadGetConnection(ctx, dataSource);
			for (String sql : sqlList) {
				long t1 = System.currentTimeMillis();
				execSql = sql;
				run.update(conn, execSql);
				long t2 = System.currentTimeMillis();
				// if ((t2 - t1) > 3000)
				log.debug("[" + (t2 - t1) + "ms] " + execSql);
			}
			conn.commit();
			doneSignal.countDown();
		} catch (Exception e) {
			log.error(e);
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new ThreadExecuteException(execSql, e);

		} finally {
			afterRun(conn);
			DbUtils.closeQuietly(conn);
		}

	}

	private void afterRun(Connection conn) {		
		//默认什么都不做，子类可以实现针对conn的一些处理，比如关闭conn使用到的dblink,但只能关闭一个DbLink
		String dbLinkName = getConnDbLink();
		if(StringUtils.isBlank(dbLinkName)) return ;
		new DbLinkCreator().closeQuietly(dbLinkName, conn);
	}

	 protected String getConnDbLink() {
		return null;
	}

}
