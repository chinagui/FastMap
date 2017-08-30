package com.navinfo.dataservice.expcore.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.expcore.input.OracleInput;
import com.navinfo.dataservice.expcore.output.DataOutput;
import com.navinfo.dataservice.expcore.sql.handler.QueryExecThreadHandler;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import com.navinfo.dataservice.bizcommons.sql.DDLExecThreadHandler;
import com.navinfo.dataservice.bizcommons.sql.DMLExecThreadHandler;
import com.navinfo.dataservice.bizcommons.sql.ExpSQL;
import com.navinfo.dataservice.bizcommons.sql.ProgramBlockExecThreadHandler;
import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.oracle.ConnectionRegister;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;

/**
 * 多线程按步骤执行sql 每个数据库生成一个文件（如果导入sqlite），然后进行文件合并
 * 
 * @author liuqing
 */
public class ExecuteSql {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected VMThreadPoolExecutor executePoolExecutor;
	protected VMThreadPoolExecutor queryPoolExecutor; // 查询sql执行线程，最后一步select
														// *语句的查询线程
	protected OracleInput input;
	protected DataOutput output;
	protected String exportMode;
	protected boolean dataIntegrity;
	protected boolean multiThread4Input;
	protected boolean multiThread4Output;

	/**
	 * @param exportSource
	 *            导出数据源
	 * @param exportTarget
	 *            输出方式
	 * @param config
	 *            系统配置
	 */
	public ExecuteSql(OracleInput input, DataOutput output, String exportMode,
			boolean dataIntegrity, boolean multiThread4Input,
			boolean multiThread4Output) {
		this.input = input;
		this.output = output;
		this.exportMode = exportMode;
		this.dataIntegrity = dataIntegrity;
		this.multiThread4Input = multiThread4Input;
		this.multiThread4Output = multiThread4Output;
		createThreadPool();
	}

	/**
	 * 创建线程池
	 * 
	 * @return
	 */
	protected void createThreadPool() {
		int inputPoolSize = 1;
		if (multiThread4Input) {
			inputPoolSize = SystemConfigFactory.getSystemConfig().getIntValue(
					"export.multiThread.inputPoolSize", 10);
		}
		int outPoolSize = 1;
		if (multiThread4Output) {
			outPoolSize = SystemConfigFactory.getSystemConfig().getIntValue(
					"export.multiThread.outputPoolSize", 10);
		}

		try {
			executePoolExecutor = new VMThreadPoolExecutor(inputPoolSize,
					inputPoolSize, 3, TimeUnit.SECONDS,
					new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());

			queryPoolExecutor = new VMThreadPoolExecutor(outPoolSize,
					outPoolSize, 3, TimeUnit.SECONDS,
					new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
		} catch (Exception e) {
			throw new ThreadExecuteException("初始化线程池错误", e);
		}
	}

	/**
	 * 
	 * 
	 * @param exportSource
	 * @return
	 * @throws Exception
	 */
	public void execute() throws Exception {
		ThreadLocalContext ctx = new ThreadLocalContext(log);
		try {
			Map<Integer, List<ExpSQL>> expSqlMap = input.getExpSqlMap();
			// 获取当前执行任务对应的sql集合，执行临时表装载
			Set<Entry<Integer, List<ExpSQL>>> sqlEntrySet = expSqlMap
					.entrySet();
			for (Iterator iterator = sqlEntrySet.iterator(); iterator.hasNext();) {
				Entry sqlEntry = (Entry) iterator.next();
				Integer step = (Integer) sqlEntry.getKey();
				if (step > 99)
					continue;
				List<ExpSQL> sqlList = (List<ExpSQL>) sqlEntry.getValue();
				execute(step, sqlList, ctx);
			}
			// 导出数据
			// 100 输出数据
			// 101 删除数据
			if (exportMode.equals(ExportConfig.MODE_COPY)) {
				Integer step = new Integer(100);
				execute(step, expSqlMap.get(step), ctx);
			} else if (exportMode.equals(ExportConfig.MODE_DELETE)) {
				Integer step = new Integer(101);
				execute(step, expSqlMap.get(step), ctx);
			} else if (exportMode.equals(ExportConfig.MODE_DELETE_COPY)) {
				Integer step = new Integer(101);
				execute(step, expSqlMap.get(step), ctx);
				step = new Integer(100);
				execute(step, expSqlMap.get(step), ctx);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			log.debug("所有的导出sql执行结束");
			ConnectionRegister.closeSubThreadUnCloseConnection(ctx);
			log.debug("销毁线程池");
			if (!executePoolExecutor.isShutdown())
				executePoolExecutor.shutdown();

			if (!queryPoolExecutor.isShutdown())
				queryPoolExecutor.shutdown();

		}
		// check exception

	}

	/**
	 * 根据配置参数exportconfig 决定数据导出是毛边导出还是非毛边导出
	 * 
	 * @param sqlList
	 * @return
	 */
	private List<ExpSQL> filterSql(List<ExpSQL> sqlList) {
		List<ExpSQL> filterSqlList = new ArrayList<ExpSQL>();
		for (ExpSQL expSQL : sqlList) {
			if (dataIntegrity) {

				// 毛边导出
				if (expSQL.getSqlType() == null
						|| ExportConfig.DATA_INTEGRITY.equals(expSQL
								.getSqlType())) {
					// log.debug("毛边导出："+expSQL.getSql());
					filterSqlList.add(expSQL);
				}
			} else {
				// 非毛边导出

				if (expSQL.getSqlType() == null
						|| ExportConfig.DATA_NOT_INTEGRITY.equals(expSQL
								.getSqlType())
				/*
				 * || ExportConfig.DATA_NOT_INTEGRITY.equals(expSQL
				 * .getSqlExtendType())
				 */) {
					log.debug("非毛边导出," + expSQL.getSqlType() + "："
							+ expSQL.getSql() + ":" + expSQL.getSqlExtendType());
					filterSqlList.add(expSQL);
				}
			}

		}
		return filterSqlList;
	}

	private void execute(Integer step, List<ExpSQL> oriSqlList,
			ThreadLocalContext ctx) throws Exception {
		try {
			List<ExpSQL> sqlList = filterSql(oriSqlList);
			long t1 = System.currentTimeMillis();
			log.debug("start execute step " + step);
			DataSource dataSource = input.getSource().getSchema()
					.getPoolDataSource();
			if (executePoolExecutor.isShutdown())
				return;
			if (queryPoolExecutor.isShutdown())
				return;
			int executeTheadCount = 0;
			int queryTheadCount = 0;
			// 创建线程执行计数器
			for (ExpSQL expSQL : sqlList) {
				if (expSQL.isDML() || expSQL.isDDL() || expSQL.isProgramBlock())
					executeTheadCount++;
				else {
					queryTheadCount++;
				}

			}
			// 创建线程执行计数器
			int threadCount = sqlList.size();
			// log.debug("start to execute step" + step + ",线程数：" +
			// threadCount);
			CountDownLatch executeDoneSignal = new CountDownLatch(
					executeTheadCount);
			CountDownLatch queryDoneSignal = new CountDownLatch(queryTheadCount);
			executePoolExecutor.addDoneSignal(executeDoneSignal);
			queryPoolExecutor.addDoneSignal(queryDoneSignal);

			for (ExpSQL expSQL : sqlList) {
				// log.debug(sqlprocess.getSql());
				// 执行sql

				Runnable handler = null;
				if (expSQL.isDML()) {
					handler = new DMLExecThreadHandler(executeDoneSignal,
							expSQL, dataSource, ctx);
					executePoolExecutor.execute(handler);
				} else if (expSQL.isDDL()) {
					handler = new DDLExecThreadHandler(executeDoneSignal,
							expSQL, dataSource, ctx);
					executePoolExecutor.execute(handler);
				} else if (expSQL.isProgramBlock()) {
					handler = new ProgramBlockExecThreadHandler(
							executeDoneSignal, expSQL, dataSource, ctx);
					executePoolExecutor.execute(handler);
				} else {
					handler = new QueryExecThreadHandler(queryDoneSignal,
							expSQL, dataSource, output, ctx);

					queryPoolExecutor.execute(handler);

				}

			}

			// 主线程等待子线程执行结果
			executeDoneSignal.await();
			queryDoneSignal.await();

			if (executePoolExecutor.getExceptions().size() > 0) {
				throw new Exception(executePoolExecutor.getExceptions().get(0));
			}
			if (queryPoolExecutor.getExceptions().size() > 0) {
				throw new Exception(queryPoolExecutor.getExceptions().get(0));
			}

			long t2 = System.currentTimeMillis();
			log.debug("step:" + step + " " + (t2 - t1) + "ms");

		} catch (Exception e) {
			executePoolExecutor.shutdownNow();
			queryPoolExecutor.shutdownNow();
			throw e;
		}
	}

}
