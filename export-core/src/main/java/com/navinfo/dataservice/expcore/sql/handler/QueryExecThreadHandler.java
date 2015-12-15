package com.navinfo.dataservice.expcore.sql.handler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.dataservice.expcore.output.DataOutput;
import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.database.oracle.ConnectionRegister;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;

/**
 * @author liuqing 此方法主要是读取数据写入sqlite
 */
public class QueryExecThreadHandler extends ThreadHandler {
	/**
     *
     */
	private static final long serialVersionUID = 1L;
	protected DataOutput dataOutput = null;
	

	/**
	 * @param doneSignal
	 * @param sql
	 * @param log
	 */
	public QueryExecThreadHandler(CountDownLatch doneSignal,
			ExpSQL sql,
			DataSource ds,
			DataOutput dataOutput,
			ThreadLocalContext ctx) {
		super(doneSignal, sql, ds, ctx);
		this.dataOutput = dataOutput;
	}

	/**
	 * 多线程执行查询语句
	 */
	public void run() {

		execute(new ExpSqlProcessor() {

			public void process(ExpSQL expSQL) throws Exception {
				long t1 = System.currentTimeMillis();
				execSql = expSQL.getSql();

				int fetchSize = SystemConfig.getSystemConfig().getIntValue("selectFetchSize", 500);
				final String tableName = expSQL.getRetureTableName();
//				logger.debug(" start execute:"+execSql);
				// System.out.println(execSql + " " + tableName);
				Connection conn = null;
				QueryRunner runner = new QueryRunner();
				try {
					conn = ConnectionRegister.subThreadGetConnection(ctx, dataSource);
					runner.query(
							conn,
							execSql,
							fetchSize,
							new ResultSetHandler<ResultSet>() {
								public ResultSet handle(ResultSet rs) throws SQLException {
									try {
										dataOutput.output(rs, tableName);
									} catch (Exception e) {
										logger.error(e.getMessage(), e);
										throw new SQLException(e);
									}
									return null;
								}
							},
							expSQL.getArgs());
					// 输出 resultSet中的数据到sqlite
				} catch (Exception e) {
					throw e;
				} finally {
					DbUtils.closeQuietly(conn);
				}
				long t2 = System.currentTimeMillis();
				long time = t2 - t1;
//				if (time > 3000)
					logger.debug("[" + (time) + "ms] " + execSql);
			}
		});

	}

}
