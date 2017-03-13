package com.navinfo.dataservice.bizcommons.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.dataservice.commons.database.oracle.ConnectionRegister;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;

/**
 * 执行DDL语句
 * 
 * @author liuqing 。
 */
public class DMLExecThreadHandler extends ThreadHandler {
	Logger log = Logger.getLogger(this.getClass());
	/**
     *
     */
	private static final long serialVersionUID = 1L;

	public DMLExecThreadHandler(CountDownLatch doneSignal,
			ExpSQL sql,
			DataSource ds, ThreadLocalContext ctx) {
		super(doneSignal, sql, ds, ctx);

	}

	private String dbLinkName;

	public DMLExecThreadHandler(CountDownLatch doneSignal,
			ExpSQL sql,
			DataSource ds,
			String dbLinkName,
			ThreadLocalContext ctx) {
		super(doneSignal, sql, ds, ctx);
		this.dbLinkName = dbLinkName;
	}

	public void run() {
		execute(new ExpSqlProcessor() {
			public void process(ExpSQL expSQL) throws Exception {

				long t1 = System.currentTimeMillis();
				QueryRunner run = new QueryRunner();
				execSql = expSQL.getSql();
				Object[] args = expSQL.getArgs();
				// logger.debug(execSql);
				/*
				 * for (int i = 0; i < args.length; i++) {
				 * Object arg = args[i];
				 * logger.debug(arg);
				 * 
				 * }
				 */

				Connection conn = null;
				try {
					conn=ConnectionRegister.subThreadGetConnection(ctx, dataSource);
					run.update(conn, execSql, args);
					conn.commit();
				} catch (SQLException e) {
					log.error(e.getMessage(), e);
					conn.rollback();
					throw e;
				} finally {
					if (StringUtils.isNotBlank(dbLinkName)) {
						DbLinkCreator creator = new DbLinkCreator();
						creator.closeQuietly(dbLinkName, conn);
					}
					DbUtils.closeQuietly(conn);
				}

				long t2 = System.currentTimeMillis();
				//if ((t2 - t1) > 3000)
					logger.debug("[" + (t2 - t1) + "ms] " + execSql);
			}
		});

	}
}
