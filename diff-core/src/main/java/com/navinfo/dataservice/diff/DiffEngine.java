package com.navinfo.dataservice.diff;

import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.diff.config.DiffConfig;
import com.navinfo.dataservice.diff.dataaccess.AccessType;
import com.navinfo.dataservice.diff.dataaccess.DataAccess;
import com.navinfo.dataservice.diff.dataaccess.Table;
import com.navinfo.dataservice.diff.exception.DiffException;
import com.navinfo.dataservice.diff.scanner.PLSQLDiffScanner;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.exception.ThreadExecuteException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-8 下午4:33
 */
public class DiffEngine 
{
	private  Logger log = Logger
			.getLogger(DiffEngine.class);

	private DiffConfig config;
	private DataAccess leftAccess;
	private DataAccess rightAccess;
	private List<Table> diffTables;
	protected VMThreadPoolExecutor poolExecutor;
	protected PLSQLDiffScanner diffScanner;


	public DiffEngine(DiffConfig config) {
		this.config = config;
		initEngine();
	}

	public void initEngine(){
		//差分配置发现表没有主键报错
		//schema
		
		//data access
		
		//diffScanner
		
		//diffTables
		log.debug("需要差分的表的个数为：" + diffTables.size());
	}
	
	private void preExecute(){
		//truncate log
		//安装包和类型
		
	}

	public void execute() {
		try {
			initPoolExecutor();
			preExecute();
			diffScan();
			fillLogDetail();
		} finally {
			shutDownPoolExecutor();
		}
	}


	/**
	 * 执行差分扫描
	 */
	protected void diffScan() {
		final CountDownLatch latch = new CountDownLatch(diffTables.size());
		poolExecutor.addDoneSignal(latch);
		// 执行差分
		log.debug("开始执行差分");
		long t = System.currentTimeMillis();
		for (final Table table : diffTables) {
			log.debug("添加差分执行任务，表名为：" + table.getTableName());
			poolExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try{
						diffScanner.scan(table,leftAccess.accessTable(table), rightAccess.accessTable(table));
						latch.countDown();
					}catch(Exception e){
						throw new ThreadExecuteException("表名："+table.getTableName()+"差分失败。",e);
					}
				}
			});
		}
		try {
			log.debug("等待各差分任务执行完成");
			latch.await();
		} catch (InterruptedException e) {
			log.warn("线程被打断");
		}
		if (poolExecutor.getExceptions().size() > 0)
			throw new ServiceException("执行差分时发生异常"+poolExecutor
					.getExceptions().get(0).getMessage(), poolExecutor
					.getExceptions().get(0));
		log.debug("所有表差分完成,用时：" + (System.currentTimeMillis() - t) + "ms");
	}

	protected void fillLogDetail() {
		final CountDownLatch latch = new CountDownLatch(diffTables.size());
		poolExecutor.addDoneSignal(latch);
		// 
		log.debug("开始填充履历详细改前改后值");
		long t = System.currentTimeMillis();
		for (final Table table : diffTables) {
			log.debug("添加生成履历执行任务，任务名为：" + table.getTableName());
			poolExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try{
						diffScanner.fillLogDetail(table,leftAccess.accessTable(table), rightAccess.accessTable(table));
						latch.countDown();
					}catch(Exception e){
						throw new ThreadExecuteException("表名："+table.getTableName()+"差分失败。",e);
					}
				}
			});
		}
		try {
			log.debug("等待各生成履历任务执行完成");
			latch.await();
		} catch (InterruptedException e) {
			log.warn("线程被打断");
		}
		if (poolExecutor.getExceptions().size() > 0)
			throw new ServiceException("执行生成履历时发生异常", poolExecutor
					.getExceptions().get(0));
		log.debug("各生成履历任务执行完成,用时：" + (System.currentTimeMillis() - t) + "ms");
	}


	protected void initPoolExecutor() {
		// int poolSize = config.getThreadCount();
		int poolSize = 10;
		try {
			poolExecutor = new VMThreadPoolExecutor(poolSize, poolSize, 3,
					TimeUnit.SECONDS, new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
		} catch (Exception e) {
			throw new ServiceException("初始化线程池错误:" + e.getMessage(), e);
		}
	}

	protected void shutDownPoolExecutor() {
		log.debug("关闭线程池");
		if (poolExecutor != null && !poolExecutor.isShutdown()) {
			poolExecutor.shutdownNow();
			try {
				while (!poolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + poolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭线程池失败");
				throw new ServiceException("关闭线程池失败", e);
			}
		}
	}

}
