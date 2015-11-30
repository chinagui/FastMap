package com.navinfo.dataservice.expcore.sql.handler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.navinfo.dms.commons.exception.ThreadExecuteException;
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.dms.tools.vm.thread.ThreadLocalContext;

/**
 * User: liuqing
 * Date: 2010-9-3
 * Time: 15:37:23
 */
public abstract class ThreadHandler implements Runnable, Serializable {
	// protected Logger logger = Logger.getLogger(this.getClass());
	protected Logger logger;
	protected List<ExpSQL> sqlList = new ArrayList<ExpSQL>();
	protected DataSource dataSource;
	protected CountDownLatch doneSignal;
	protected ThreadLocalContext ctx;

	// protected Connection conn = null;

	public ThreadHandler(CountDownLatch doneSignal,
			ExpSQL sql,
			DataSource dataSource,
			ThreadLocalContext ctx) {
		this.doneSignal = doneSignal;
		this.dataSource = dataSource;
		this.sqlList.add(sql);
		this.logger = ctx.getLog();
		this.ctx = ctx;
	}

	/**
	 * 执行DDL/DML/QUERY SQL语句
	 * 通过回调方法，减少子类的重复，如异常处理、资源释放等
	 * 
	 * @param processor
	 */

	protected void execute(CallBackProcessor processor) throws ThreadExecuteException {
		try {
			for (ExpSQL expSQL : sqlList) {
				processor.process(expSQL);
			}
			doneSignal.countDown();
		} catch (Exception e) {
			String execSql = processor.getExecSql();
			// logger.error(execSql, e);
			throw new ThreadExecuteException(execSql, e);

		}

	}

	/**
	 * 执行DDL/DML/QUERY SQL语句
	 * 通过回调方法，减少子类的重复，如异常处理、资源释放等
	 * 
	 * @param processor
	 */

	protected void executeIgnoreException(CallBackProcessor processor) throws ThreadExecuteException {
		try {
			for (ExpSQL expSQL : sqlList) {
				processor.process(expSQL);
			}
			
		} catch (Exception e) {
			String execSql = processor.getExecSql();
			logger.error(execSql, e);

		}finally{
			doneSignal.countDown();
		}

	}

}
