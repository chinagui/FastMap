package com.navinfo.dataservice.bizcommons.datarow;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.sql.DMLExecThreadHandler;
import com.navinfo.dataservice.bizcommons.sql.ExpSQL;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;

/** 
 * @ClassName: ExecuteFullCopySql 
 * @author Xiao Xiaowen 
 * @date 2016-1-9 下午11:28:02 
 * @Description: TODO
 */
public class ExecuteExternlToolSql {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected VMThreadPoolExecutor threadPoolExecutor;
	protected OracleSchema targetDb;
	public ExecuteExternlToolSql(OracleSchema targetDb)throws Exception{
		this.targetDb=targetDb;
		createThreadPool();
	}

    protected void createThreadPool() throws Exception{
		int outPoolSize = SystemConfigFactory.getSystemConfig().getIntValue("export.multiThread.outputPoolSize", 10);
        threadPoolExecutor = new VMThreadPoolExecutor(outPoolSize,
				outPoolSize,
				3,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue(),
				new ThreadPoolExecutor.CallerRunsPolicy());
    }	
    public void execute(List<ExpSQL> sqlList, ThreadLocalContext ctx) throws Exception {
		try {
			DataSource dataSource = targetDb.getPoolDataSource();
			if (threadPoolExecutor.isShutdown())
				return;
			int theadCount = 0;
			// 创建线程执行计数器
			int threadCount = sqlList.size();
			// 
			CountDownLatch executeDoneSignal = new CountDownLatch(threadCount);
			threadPoolExecutor.addDoneSignal(executeDoneSignal);

			for (ExpSQL expSQL : sqlList) {
				// log.debug(sqlprocess.getSql());
				// 执行sql

				Runnable handler = new DMLExecThreadHandler(executeDoneSignal, expSQL, dataSource, ctx);
				threadPoolExecutor.execute(handler);

			}

			// 主线程等待子线程执行结果
			executeDoneSignal.await();

			if (threadPoolExecutor.getExceptions().size() > 0) {
				throw new Exception(threadPoolExecutor.getExceptions().get(0));
			}

		} catch (Exception e) {
			threadPoolExecutor.shutdownNow();
			throw e;
		}
	}
}
