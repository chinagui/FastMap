package com.navinfo.dataservice.expcore.sql;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.sql.handler.DMLExecThreadHandler;

/** 
 * @ClassName: ExecuteFullCopySql 
 * @author Xiao Xiaowen 
 * @date 2016-1-9 下午11:28:02 
 * @Description: TODO
 */
public class ExecuteFullCopySql {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected VMThreadPoolExecutor threadPoolExecutor;
	protected OracleSchema targetSchema;
	protected boolean multiThread4Output;
	public ExecuteFullCopySql(OracleSchema targetSchema,boolean multiThread4Output)throws ExportException{
		this.targetSchema=targetSchema;
		this.multiThread4Output=multiThread4Output;
		createThreadPool();
	}

    protected void createThreadPool() throws ExportException{
		int outPoolSize = 1;
		if(multiThread4Output){
			outPoolSize = SystemConfigFactory.getSystemConfig().getIntValue("export.multiThread.outputPoolSize", 10);
		}
        try {
            threadPoolExecutor = new VMThreadPoolExecutor(outPoolSize,
					outPoolSize,
					3,
					TimeUnit.SECONDS,
					new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
        } catch (Exception e) {
            throw new ExportException("初始化线程池错误", e);
        }
    }	
    public void execute(List<ExpSQL> sqlList, ThreadLocalContext ctx) throws Exception {
		try {
			DataSource dataSource = targetSchema.getPoolDataSource();
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
