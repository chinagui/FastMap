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
public class SQLMultiThreadExecutor {
	protected static Logger log = LoggerRepos.getLogger(SQLMultiThreadExecutor.class);

	public static void execute(DataSource dataSource,List<ExpSQL> sqlList, ThreadLocalContext ctx)throws Exception{
		int poolSize = 10;
		if(sqlList.size()<10){
			poolSize = sqlList.size();
		}
		executeMultiThread(poolSize,dataSource,sqlList,ctx);
	}
    public static void executeMultiThread(int poolSize,DataSource dataSource,List<ExpSQL> sqlList, ThreadLocalContext ctx) throws Exception {
    	VMThreadPoolExecutor threadPoolExecutor = null;
    	try {
    		threadPoolExecutor = new VMThreadPoolExecutor(poolSize,
            		poolSize,
    				3,
    				TimeUnit.SECONDS,
    				new LinkedBlockingQueue(),
    				new ThreadPoolExecutor.CallerRunsPolicy());
			if (threadPoolExecutor.isShutdown())
				return;
			int theadCount = 0;
			// 创建线程执行计数器
			int threadCount = sqlList.size();
			// 
			CountDownLatch executeDoneSignal = new CountDownLatch(threadCount);
			threadPoolExecutor.addDoneSignal(executeDoneSignal);

			for (ExpSQL expSQL : sqlList) {
				Runnable handler = new DMLExecThreadHandler(executeDoneSignal, expSQL, dataSource, ctx);
				threadPoolExecutor.execute(handler);

			}

			// 主线程等待子线程执行结果
			executeDoneSignal.await();

			if (threadPoolExecutor.getExceptions().size() > 0) {
				throw new Exception(threadPoolExecutor.getExceptions().get(0));
			}

		} catch (Exception e) {
			if(threadPoolExecutor!=null)
			threadPoolExecutor.shutdownNow();
			throw e;
		}
	}
}
