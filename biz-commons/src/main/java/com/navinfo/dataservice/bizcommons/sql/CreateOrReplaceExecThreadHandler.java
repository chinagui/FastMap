package com.navinfo.dataservice.bizcommons.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import com.navinfo.dataservice.commons.database.oracle.ConnectionRegister;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;

/**
 * Create Table语句执行
 * 创建之前会检查是否table存在，如果存在，不执行，如果不存在，则执行
 * @author XXW
 */
public class CreateOrReplaceExecThreadHandler implements Runnable  {
	
    private static final long serialVersionUID = 1L;

	protected Logger log;
	protected List<String> sqlList = null;
	protected DataSource dataSource;
	protected CountDownLatch doneSignal;
	protected ThreadLocalContext ctx;

    public CreateOrReplaceExecThreadHandler(CountDownLatch doneSignal,
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
				String tableName;
				tableName=sql.substring(0, sql.indexOf("(")).trim().split(" ")[2];
				log.debug("从创建表语句中拆分出来的表名是："+tableName);
				String checkSql="SELECT count(1) FROM user_tables t WHERE t.table_name = ? ";
				long t1 = System.currentTimeMillis();
				boolean exists = run.queryForInt(dataSource, checkSql, tableName)<1?false:true;
				if(!exists){
					log.debug(tableName+"不存在，开始创建。");
					execSql = sql;
					run.update(conn, execSql);
				}else{
					log.debug(tableName+"已存在，无需创建。");
				}
				long t2 = System.currentTimeMillis();
				// if ((t2 - t1) > 3000)
				log.debug("[" + (t2 - t1) + "ms] " + execSql);
			}
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
			DbUtils.closeQuietly(conn);
		}

    }


}
