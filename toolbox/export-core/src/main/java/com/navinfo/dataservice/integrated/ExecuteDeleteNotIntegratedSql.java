package com.navinfo.dataservice.integrated;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.sql.DMLExecThreadHandler2;
import com.navinfo.dataservice.commons.thread.MultiThreadExecute;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;

/**
 * 
 * @author liuqing
 * 
 */
public class ExecuteDeleteNotIntegratedSql extends MultiThreadExecute {
	protected Logger log = Logger.getLogger(this.getClass());

	public ExecuteDeleteNotIntegratedSql() {

	}

	/**
	 * 执行参数中的sql
	 * 
	 * @param batchId
	 * @param expSQLs
	 * @throws Exception
	 */
	public void execute(DataSource ds, List<TableConfig> tableConfigs)
			throws Exception {
		try {
			if (tableConfigs == null || tableConfigs.isEmpty()) {
				return;

			}
			CountDownLatch executeDoneSignal = new CountDownLatch(
					tableConfigs.size());
			VMThreadPoolExecutor executePoolExecutor = createThreadPool();
			executePoolExecutor.addDoneSignal(executeDoneSignal);
			ThreadLocalContext ctx = new ThreadLocalContext(log);

			for (TableConfig tableConfig : tableConfigs) {
				List<String> refSqls = tableConfig.getRefSql();
				refSqls.addAll(tableConfig.getRemoveSql());
				DMLExecThreadHandler2 handler = new DMLExecThreadHandler2(
						executeDoneSignal, refSqls, ds, ctx);
				executePoolExecutor.execute(handler);
			}
			executeDoneSignal.await();
			log.debug("销毁线程池");
			if (!executePoolExecutor.isShutdown())
				executePoolExecutor.shutdown();
			if (executePoolExecutor.getExceptions().size() > 0) {
				throw new Exception(executePoolExecutor.getExceptions().get(0));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}

	}

}
