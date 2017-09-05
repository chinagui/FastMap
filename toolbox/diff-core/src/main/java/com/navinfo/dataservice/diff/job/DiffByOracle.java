package com.navinfo.dataservice.diff.job;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.diff.dataaccess.CrossSchemaDataAccess;
import com.navinfo.dataservice.diff.dataaccess.DataAccess;
import com.navinfo.dataservice.diff.dataaccess.LocalDataAccess;
import com.navinfo.dataservice.diff.exception.DiffException;
import com.navinfo.dataservice.diff.exception.InitException;
import com.navinfo.dataservice.diff.scanner.ChangeLogFiller;
import com.navinfo.dataservice.diff.scanner.JavaChangeLogFiller;
import com.navinfo.dataservice.diff.scanner.JavaDiffScanner;
import com.navinfo.dataservice.diff.scanner.LogActionGenerator;
import com.navinfo.dataservice.diff.scanner.LogGridCalculator;
import com.navinfo.dataservice.diff.scanner.LogGridCalculatorByCrossUser;
import com.navinfo.dataservice.diff.scanner.LogOperationGenerator;
import com.navinfo.navicommons.database.sql.PackageExec;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.exception.ThreadExecuteException;

/** 
 * @ClassName: DiffByOracle
 * @author xiaoxiaowen4127
 * @date 2017年8月31日
 * @Description: DiffByOracle.java
 */
public class DiffByOracle extends DiffTool{

	public DiffByOracle(DiffJobRequest req) {
		super(req);
	}
	private  Logger log = Logger
			.getLogger(DiffJob.class);

	private DataAccess leftAccess;
	private DataAccess rightAccess;
	protected VMThreadPoolExecutor diffPoolExecutor;
	protected VMThreadPoolExecutor logPoolExecutor;
	protected VMThreadPoolExecutor logGridPoolExecutor;
	protected JavaDiffScanner diffScanner;
	protected LogActionGenerator logActGen;
	protected LogOperationGenerator logOpGen;
	protected ChangeLogFiller changeLogFiller;
	protected LogGridCalculator gridCalc;

	@Override
	public String diff() throws DiffException {
		final CountDownLatch latch = new CountDownLatch(diffTables.size());
		diffPoolExecutor.addDoneSignal(latch);
		// 执行差分
		log.debug("开始执行差分");
		long t = System.currentTimeMillis();
		for (final GlmTable table : diffTables) {
			log.debug("添加差分线程，表名为：" + table.getName());
			diffPoolExecutor.execute(new DiffScannerThread(table,latch,logTables));
		}
		try {
			log.debug("等待各差分任务执行完成");
			latch.await();
		} catch (InterruptedException e) {
			log.warn("线程被打断");
		}
		if (diffPoolExecutor.getExceptions().size() > 0)
			throw new ServiceRtException("执行差分时发生异常"+diffPoolExecutor
					.getExceptions().get(0).getMessage(), diffPoolExecutor
					.getExceptions().get(0));
		log.debug("所有表差分完成,用时：" + (System.currentTimeMillis() - t) + "ms");
		return "字段差分完成。";
	}

	@Override
	public String writeLog(long userId,String actName, long subtaskId) throws Exception {
		String actId = logActGen.generate(userId, actName, subtaskId);
		logOpGen.generate(actId);
		if(logTables.size()>0){
			fillLogDetailOldNew();
			calcLogDetailGrid();
		}
		return actId;
	}
	
	@Override
	public String init()throws Exception{
		super.init();
		
		//data access
		leftAccess = new LocalDataAccess(leftSchema);
		rightAccess = new CrossSchemaDataAccess(rightSchema);
		
		//diffScanner
		diffScanner = new JavaDiffScanner(leftSchema);
		logActGen = new LogActionGenerator(leftSchema);
		logOpGen = new LogOperationGenerator(leftSchema);
		changeLogFiller = new JavaChangeLogFiller(leftSchema);
		gridCalc = new LogGridCalculatorByCrossUser(leftSchema,rightSchema.getConnConfig().getUserName());
		
		initPoolExecutor();
		return "差分初始化完成";
	}

	protected void initPoolExecutor() {
		// int poolSize = config.getThreadCount();
		int poolSize = 10;
		try {
			diffPoolExecutor = new VMThreadPoolExecutor(poolSize, poolSize, 3,
					TimeUnit.SECONDS, new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
			logPoolExecutor = new VMThreadPoolExecutor(poolSize, poolSize, 3,
					TimeUnit.SECONDS, new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
			logGridPoolExecutor = new VMThreadPoolExecutor(poolSize, poolSize, 3,
					TimeUnit.SECONDS, new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
		} catch (Exception e) {
			throw new ServiceRtException("初始化线程池错误:" + e.getMessage(), e);
		}
	}
	
	public void releaseResources(){
		shutDownPoolExecutor();
	}

	protected void fillLogDetailOldNew() {
		final CountDownLatch latch4Log = new CountDownLatch(logTables.size());
		logPoolExecutor.addDoneSignal(latch4Log);
		// 
		log.debug("开始填充履历详细改前改后值");
		long t = System.currentTimeMillis();
		for (final GlmTable table : logTables) {
			log.debug("添加填充履历执行线程，表名为：" + table.getName());
			logPoolExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try{
						log.debug("开始填充履历线程，表名为：" + table.getName());
						changeLogFiller.fill(table,leftAccess.accessTable(table), rightAccess.accessTable(table));
						latch4Log.countDown();
						log.debug("填充履历完成，表名为：" + table.getName());
					}catch(Exception e){
						throw new ThreadExecuteException("表名："+table.getName()+"差分失败。",e);
					}
				}
			});
		}
		try {
			log.debug("等待各生成履历任务执行完成");
			latch4Log.await();
		} catch (InterruptedException e) {
			log.warn("线程被打断");
		}
		if (logPoolExecutor.getExceptions().size() > 0)
			throw new ServiceRtException("执行生成履历时发生异常", logPoolExecutor
					.getExceptions().get(0));
		log.debug("各生成履历任务执行完成,用时：" + (System.currentTimeMillis() - t) + "ms");
	}

	protected void calcLogDetailGrid(){
		final String gdbVersion = req.getGdbVersion();
		
		//计算
		final CountDownLatch latch4LogGrid = new CountDownLatch(logTables.size());
		logGridPoolExecutor.addDoneSignal(latch4LogGrid);
		// 
		log.debug("开始填充履历grid号");
		long t = System.currentTimeMillis();
		for (final GlmTable table : logTables) {
			log.debug("添加填充履历grid号执行线程，表名为：" + table.getName());
			logGridPoolExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try{
						log.debug("开始填充履历grid号，表名为：" + table.getName());
						gridCalc.calc(table,gdbVersion);
						latch4LogGrid.countDown();
						log.debug("填充履历grid号完成，表名为：" + table.getName());
					}catch(Exception e){
						log.debug("填充履历Grid失败，表名为：" + table.getName());
						throw new ThreadExecuteException("表名："+table.getName()+"差分失败。",e);
					}
				}
			});
		}
		try {
			log.debug("等待各计算履历grid号任务执行完成");
			latch4LogGrid.await();
		} catch (InterruptedException e) {
			log.warn("线程被打断");
		}
		if (logGridPoolExecutor.getExceptions().size() > 0)
			throw new ServiceRtException("计算履历grid号时发生异常", logGridPoolExecutor
					.getExceptions().get(0));
		log.debug("各计算履历grid号任务执行完成,用时：" + (System.currentTimeMillis() - t) + "ms");
	}

	protected void shutDownPoolExecutor() {
		log.debug("关闭线程池");
		if (diffPoolExecutor != null && !diffPoolExecutor.isShutdown()) {
			diffPoolExecutor.shutdownNow();
			try {
				while (!diffPoolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + diffPoolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭线程池失败");
				throw new ServiceRtException("关闭线程池失败", e);
			}
		}
		if (logPoolExecutor != null && !logPoolExecutor.isShutdown()) {
			logPoolExecutor.shutdownNow();
			try {
				while (!logPoolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + logPoolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭填充履历线程池失败");
				throw new ServiceRtException("关闭填充履历线程池失败", e);
			}
		}
		if (logGridPoolExecutor != null && !logGridPoolExecutor.isShutdown()) {
			logGridPoolExecutor.shutdownNow();
			try {
				while (!logGridPoolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + logGridPoolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭计算履历grid号的线程池");
				throw new ServiceRtException("关闭计算履历grid号的线程池", e);
			}
		}
	}
	
	class DiffScannerThread implements Runnable{
		GlmTable table = null;
		CountDownLatch latch = null;
		Set<GlmTable> logTables = null;
		DiffScannerThread(GlmTable table,CountDownLatch latch,Set<GlmTable> logTables){
			this.table=table;
			this.latch=latch;
			this.logTables=logTables;
		}
		@Override
		public void run() {
			try{
				int logCount = diffScanner.scan(table,leftAccess, rightAccess);
				if(logCount>0){
					logTables.add(table);
				}
				latch.countDown();
				log.debug("差分完成，表名为：" + table.getName());
			}catch(Exception e){
				throw new ThreadExecuteException("表名："+table.getName()+"差分失败。",e);
			}
		}
	}

}
