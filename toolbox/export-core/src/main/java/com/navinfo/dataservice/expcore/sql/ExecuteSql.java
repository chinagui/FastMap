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

import com.navinfo.navicommons.exception.DMSException;
import com.navinfo.dataservice.expcore.config.ExportConfig;
import com.navinfo.dataservice.expcore.input.OracleInput;
import com.navinfo.dataservice.expcore.output.DataOutput;
import com.navinfo.dataservice.expcore.sql.handler.DDLExecThreadHandler;
import com.navinfo.dataservice.expcore.sql.handler.DMLExecThreadHandler;
import com.navinfo.dataservice.expcore.sql.handler.ProgramBlockExecThreadHandler;
import com.navinfo.dataservice.expcore.sql.handler.QueryExecThreadHandler;
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.oracle.ConnectionRegister;
import com.navinfo.dataservice.commons.log.JobLogger;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;

/**
 * 多线程按步骤执行sql 每个数据库生成一个文件（如果导入sqlite），然后进行文件合并
 * 
 * @author liuqing
 */
public class ExecuteSql {

	protected Logger log = Logger.getLogger(this.getClass());
	protected VMThreadPoolExecutor executePoolExecutor;
	protected VMThreadPoolExecutor queryPoolExecutor; // 查询sql执行线程，最后一步select *语句的查询线程
	protected ExportConfig expConfig;
	protected OracleInput input; 
	protected DataOutput output;

	/**
	 * @param exportSource
	 *            导出数据源
	 * @param exportTarget
	 *            输出方式
	 * @param config
	 *            系统配置
	 */
	public ExecuteSql(ExportConfig expConfig,OracleInput input,DataOutput output) {
		log = JobLogger.getLogger(log);
		this.expConfig = expConfig;
		this.input=input;
		this.output=output;
		createThreadPool();
	}

	/**
	 * 创建线程池
	 * 
	 * @return
	 */
	protected void createThreadPool() {
		int inputPoolSize = 1;
		if(expConfig.isMultiThread4Input()){
			inputPoolSize = SystemConfigFactory.getSystemConfig().getIntValue("export.multiThread.inputPoolSize", 10);
		}
		int outPoolSize = 1;
		if(expConfig.isMultiThread4Output()){
			outPoolSize = SystemConfigFactory.getSystemConfig().getIntValue("export.multiThread.outputPoolSize", 10);
		}
		
		try {
			executePoolExecutor = new VMThreadPoolExecutor(inputPoolSize,
					inputPoolSize,
					3,
					TimeUnit.SECONDS,
					new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());

			queryPoolExecutor = new VMThreadPoolExecutor(outPoolSize,
					outPoolSize,
					3,
					TimeUnit.SECONDS,
					new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
		} catch (Exception e) {
			throw new DMSException("初始化线程池错误", e);
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
			// 获取当前执行任务对应的sql集合
			Set<Entry<Integer, List<ExpSQL>>> sqlEntrySet = expSqlMap.entrySet();

			for (Iterator iterator = sqlEntrySet.iterator(); iterator.hasNext();) {

				long t1 = System.currentTimeMillis();
				Entry sqlEntry = (Entry) iterator.next();
				Integer step = (Integer) sqlEntry.getKey();
				List<ExpSQL> sqlList = (List<ExpSQL>) sqlEntry.getValue();
				
				// 100 输出数据
				// 101 删除数据
				if(expConfig.getExportMode().equals(ExportConfig.MODE_COPY)){
					// step 大于100不执行
					if (step > 100) continue;
				}else if (expConfig.getExportMode().equals(ExportConfig.MODE_DELETE)) {
					// 101 删除数据
					if (step == 100) continue;
				} 
				//cut 模式 100和101都执行
				//full_copy不会到这来
				log.debug("start execute step "+step);
				execute(step, filterSql(sqlList), ctx);

				long t2 = System.currentTimeMillis();
				log.debug("step:" + step + " " + (t2 - t1) + "ms");
			}

			// shutdownNormalThreadPool();
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
			if (expConfig.isDataIntegrity()) {

				// 毛边导出
				if (expSQL.getSqlType() == null
						|| ExportConfig.DATA_INTEGRITY.equals(expSQL.getSqlType())) {
					// log.debug("毛边导出："+expSQL.getSql());
					filterSqlList.add(expSQL);
				}
			} else {
				// 非毛边导出

				if (expSQL.getSqlType() == null
						|| ExportConfig.DATA_NOT_INTEGRITY.equals(expSQL.getSqlType())) {
					// log.debug("非毛边导出,"+expSQL.getSqlType()+"："+expSQL.getSql());
					filterSqlList.add(expSQL);
				}
			}

		}
		return filterSqlList;
	}


	private void execute(Integer step, List<ExpSQL> sqlList, ThreadLocalContext ctx) throws Exception {
		try {
			DataSource dataSource = input.getSource().getSchema().getPoolDataSource();
			if (executePoolExecutor.isShutdown())
				return;
			if (queryPoolExecutor.isShutdown())
				return;
			int executeTheadCount = 0;
			int queryTheadCount = 0;
			// 创建线程执行计数器
			for (ExpSQL expSQL : sqlList) {
				if (expSQL.isDML() || expSQL.isDDL()|| expSQL.isProgramBlock())
					executeTheadCount++;
				else {
					queryTheadCount++;
				}

			}
			// 创建线程执行计数器
			int threadCount = sqlList.size();
			// log.debug("start to execute step" + step + ",线程数：" +
			// threadCount);
			CountDownLatch executeDoneSignal = new CountDownLatch(executeTheadCount);
			CountDownLatch queryDoneSignal = new CountDownLatch(queryTheadCount);
			executePoolExecutor.addDoneSignal(executeDoneSignal);
			queryPoolExecutor.addDoneSignal(queryDoneSignal);

			for (ExpSQL expSQL : sqlList) {
				// log.debug(sqlprocess.getSql());
				// 执行sql

				Runnable handler = null;
				if (expSQL.isDML()) {
					handler = new DMLExecThreadHandler(executeDoneSignal, expSQL, dataSource, ctx);
					executePoolExecutor.execute(handler);
				} else if (expSQL.isDDL()) {
					handler = new DDLExecThreadHandler(executeDoneSignal, expSQL, dataSource, ctx);
					executePoolExecutor.execute(handler);
				} else if (expSQL.isProgramBlock()) {
					handler = new ProgramBlockExecThreadHandler(executeDoneSignal, expSQL, dataSource, ctx);
					executePoolExecutor.execute(handler);
				} else {
					handler = new QueryExecThreadHandler(queryDoneSignal,
							expSQL,
							dataSource,
							output,
							ctx);

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

		} catch (Exception e) {
			executePoolExecutor.shutdownNow();
			queryPoolExecutor.shutdownNow();
			throw e;
		}
	}

}
