package com.navinfo.dataservice.expcore.sql.handler;

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
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.dataservice.commons.database.oracle.ConnectionRegister;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;

/**
 * 执行DDL语句
 * 
 * @author liuqing 。
 */
public class DMLExecThreadHandlerWithoutCommit implements Runnable {
	protected Logger log;
	protected List<String> sqlList = null;
	protected Connection conn;
	protected CountDownLatch doneSignal;
	protected ThreadLocalContext ctx;

	public DMLExecThreadHandlerWithoutCommit(CountDownLatch doneSignal,
			List<String> sqlList,
			Connection conn, ThreadLocalContext ctx) {
		this.doneSignal = doneSignal;
		this.conn = conn;
		this.log = ctx.getLog();
		this.ctx = ctx;
		this.sqlList = sqlList;

	}

	public void run() {
		String execSql = null;
		try {
			QueryRunner run = new QueryRunner();
			for (String sql : sqlList) {
				long t1 = System.currentTimeMillis();
				execSql = sql;
				run.update(conn, execSql);
				long t2 = System.currentTimeMillis();
				log.debug("[" + (t2 - t1) + "ms] " + execSql);
			}
			doneSignal.countDown();
		} catch (Exception e) {
//			if((e.getMessage().indexOf("ORA-00001")!=-1)){
//				log.warn("新增已存在："+e.getMessage(), e);
//			}
			log.error(e);
			throw new ThreadExecuteException(execSql, e);

		}

	}

}
