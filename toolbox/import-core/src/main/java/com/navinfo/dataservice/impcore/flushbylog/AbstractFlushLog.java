package com.navinfo.dataservice.impcore.flushbylog;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.thread.ThreadSharedObject;
import com.navinfo.dataservice.commons.thread.ThreadSharedObjectExt;
import com.navinfo.navicommons.database.TransactionalDataSource;

public abstract class AbstractFlushLog implements Runnable {
	protected Logger log = Logger.getLogger(getClass());
	protected int maxConcurrentSize = 5;// 默认最大并发线程个数，实际并发小于等于此数
	protected int concurrentSize;// 实际并发线程个数
	protected TransactionalDataSource sourceDataSource;
	protected TransactionalDataSource targetDataSource;

	protected ThreadPoolExecutor threadPoolExecutor = null;

	protected List<String> tableNameList;
	protected ThreadSharedObjectExt threadSharedObj;
	protected boolean controlTransaction = true;
	protected String type;

	public AbstractFlushLog(DataSource sourceDataSource,
			DataSource targetDataSource) {

		if (!(sourceDataSource instanceof TransactionalDataSource)) {
			this.sourceDataSource = new TransactionalDataSource(
					sourceDataSource);
		} else {
			this.sourceDataSource = (TransactionalDataSource) sourceDataSource;
		}
		if (!(targetDataSource instanceof TransactionalDataSource)) {
			this.targetDataSource = new TransactionalDataSource(
					targetDataSource);
		} else {
			this.targetDataSource = (TransactionalDataSource) targetDataSource;
		}

	}

	public AbstractFlushLog() {

	}

	public void run() {
		try {

			calcConcurrentSize();
			if (concurrentSize == 0) {
				log.debug("无履历，直接返回");
				return;
			}
			initThreadSharedObject();
			createAndStartSubThread();
			doFinish();
		} catch (Exception e) {
			doException(e);
		} finally {
			doFinally();
		}
	}

	/**
	 * 根据表的个数、最大并发大小，取最小值作为并发大小
	 * 
	 * @throws Exception
	 */
	protected void calcConcurrentSize() throws Exception {
		int tableNameSize = tableNameList.size();
		if (tableNameSize == 0) {

			log.info("无履历数据");
		}
		concurrentSize = tableNameSize < maxConcurrentSize ? tableNameSize
				: maxConcurrentSize;

	}

	/**
	 * 创建子线程、启动子线程
	 * 
	 * @throws Exception
	 */
	protected void createAndStartSubThread() throws Exception {
		log.debug("createAndStartSubThread");

		boolean ignoreSQLExeEffectException = isIgnoreSQLException();
		log.debug("flushlog.ignoreSQLExeEffectException:"
				+ ignoreSQLExeEffectException);
		if (maxConcurrentSize == 1) {
			log.debug("并发数为1，不采用多线程刷履历");
		}

		LogReader logReader = new LogReader(sourceDataSource.getConnection(),
				type);
		LogWriterDay2Month logWriter = new LogWriterDay2Month(targetDataSource,
				true, type);

		FlushLogToDBThread flushLogToDBThread = new FlushLogToDBThread(
				threadSharedObj, logReader, logWriter,
				ignoreSQLExeEffectException);
		flushLogToDBThread.setLog(log);
		if (maxConcurrentSize == 1) {
			flushLogToDBThread.run();
			if (threadSharedObj.getExceptionList().size() > 0) {
				log.debug("刷履历发生错误，停止刷履历");

			}

		} else {
			threadPoolExecutor.execute(flushLogToDBThread);
		}

	}

	boolean isIgnoreSQLException() {
		boolean ignoreSQLExeEffectException;
		String configValue = "false";
		if ("false".equals(configValue)) {
			ignoreSQLExeEffectException = false;
		} else {
			ignoreSQLExeEffectException = true;
		}
		return ignoreSQLExeEffectException;
	}

	protected void doFinish() throws Exception {
		if (maxConcurrentSize == 1) {
			doSingleThreadFinish();
		} else {
			doMutilThreadFinish();
		}
	}

	protected void doSingleThreadFinish() throws Exception {

		if (threadSharedObj.getWarnTotal() > 0) {
			recordWarn();
		}
		if (threadSharedObj.getExceptionList().size() > 0) {
			// fail
			if (controlTransaction) {
				log.debug("有子线程执行失败，所有数据库事务回滚");
				if (sourceDataSource != null) {
					sourceDataSource.rollbackAll();
				}
				if (targetDataSource != null) {
					targetDataSource.rollbackAll();
				}
			}
			String exceptionInfo = formatSubThreadExceptionInfo();

			throw new RuntimeException("刷履历失败:" + exceptionInfo);
		} else {

			if (controlTransaction) {
				// 执行成功提交事物
				finishCommitTrans();
			}
		}

	}

	public void finishCommitTrans() throws Exception {
		log.debug("所有子线程执行成功，所有数据库事务提交");
		if (sourceDataSource != null) {
			sourceDataSource.commitAll();
		}
		if (targetDataSource != null) {
			targetDataSource.commitAll();
		}
	}

	protected void doMutilThreadFinish() throws Exception {
		// 等待子线程执行
		boolean failure = wait4ThreadsExecute(threadSharedObj);
		try {
			log.debug("终止线程池");
			List<Runnable> shutdownNow = threadPoolExecutor.shutdownNow();
			log.debug("终止线程池成功，终止任务数:" + shutdownNow.size());
			while (!threadPoolExecutor.isTerminated()) {
				log.debug("等待未执行完的任务：" + threadPoolExecutor.getActiveCount());
				Thread.sleep(2 * 1000);
			}
			log.debug("未执行完的任务数：" + threadPoolExecutor.getActiveCount());
		} catch (Exception e) {
			log.error("shutdownNow:", e);
			throw e;
		}

		if (failure) {
			if (controlTransaction) {
				log.debug("有子线程执行失败，所有数据库事务回滚");
				if (sourceDataSource != null) {
					sourceDataSource.rollbackAll();
				}
				if (targetDataSource != null) {
					targetDataSource.rollbackAll();
				}
			}
			String exceptionInfo = formatSubThreadExceptionInfo();

			throw new RuntimeException("刷履历失败:" + exceptionInfo);
		} else {
			if (threadSharedObj.getWarnTotal() > 0) {
				recordWarn();
			}
			if (controlTransaction) {
				finishCommitTrans();
			}
		}
	}

	/**
	 * 记录警告信息到任务表
	 * 
	 * @param taskId
	 * @param exception
	 */
	public void recordWarn() {
		log.warn(threadSharedObj.getWarns().toString());
	}

	/**
	 * 格式化子线程异常信息
	 * 
	 * @return
	 */
	public String formatSubThreadExceptionInfo() {
		if (threadSharedObj == null) {
			return null;
		}
		List<Exception> exceptionList = threadSharedObj.getExceptionList();
		StringBuilder sb = new StringBuilder();
		try {
			// 获取子线程的异常列表
			if (exceptionList != null && exceptionList.size() > 0) {
				for (int i = 0; i < exceptionList.size(); i++) {
					Exception exception = exceptionList.get(i);
					for (Throwable exp = exception; exp != null; exp = exp
							.getCause()) {
						String message = exp.getMessage();
						sb.append(exp.getClass().getName() + ":" + message
								+ "\n");
						StackTraceElement[] stackTrace = exp.getStackTrace();
						for (StackTraceElement st : stackTrace) {
							String className = st.getClassName();
							String fileName = st.getFileName();
							int lineNumber = st.getLineNumber();
							String methodName = st.getMethodName();
							sb.append("    " + fileName + "(" + lineNumber
									+ ") " + className + "." + methodName
									+ "()" + "\n");
						}
					}
				}
			}
		} catch (Exception e) {
			sb.append("子线程执行失败,但无法格式化异常信息,详情请看日志文件");
			log.error("格式化异常信息:", e);
			for (int i = 0; i < exceptionList.size(); i++) {
				log.error("子线程异常信息:", exceptionList.get(i));
			}
		}
		return sb.toString();
	}

	public void doFinally() {
		try {
			if (controlTransaction) {
				if (sourceDataSource != null) {
					sourceDataSource.closeAll();
				}
				if (targetDataSource != null) {
					targetDataSource.closeAll();
				}
			}

			if (threadPoolExecutor != null) {
				threadPoolExecutor.shutdown();
			}

		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	public void doException(Exception e) {
		try {
			if (controlTransaction) {
				log.debug("所有数据库事务回滚");
				if (sourceDataSource != null) {
					sourceDataSource.rollbackAll();
				}
				if (targetDataSource != null) {
					targetDataSource.rollbackAll();
				}
			}
		} catch (Exception e1) {
			log.warn(e.getMessage(), e);
		}
		throw new RuntimeException("刷履历失败：" + e.getMessage(), e);
	}

	public void initThreadSharedObject() throws Exception {
		log.debug("任务个数：" + tableNameList.size());
		threadSharedObj = new ThreadSharedObjectExt(tableNameList.size());
		threadPoolExecutor = new ThreadPoolExecutor(concurrentSize,
				concurrentSize, 3, TimeUnit.SECONDS, new LinkedBlockingQueue(),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}

	/**
	 * 主线程等待线程执行结果
	 * 
	 * @param sharedObj
	 */
	protected boolean wait4ThreadsExecute(ThreadSharedObject sharedObj) {
		synchronized (sharedObj.waitLock) {
			try {
				log.info("等待子线程执行");
				sharedObj.waitLock.wait();
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
			log.info("子线程结束，重新开始主线程的执行");
		}
		// 判断子线程执行是否有失败的
		return sharedObj.haveFailThread();
	}

	public void setMaxConcurrentSize(int max) {
		maxConcurrentSize = max;
	}

	/**
	 * 使用单线程执行,默认为false
	 */
	public void setUseSingleThread(boolean bool) {
		if (bool) {
			maxConcurrentSize = 1;
			sourceDataSource.setMaxConnectionSize(1);
			targetDataSource.setMaxConnectionSize(1);
		}
	}

	public Logger getLog() {
		return log;
	}

	public void setLog(Logger log) {
		this.log = log;
	}

	/**
	 * 设置是否控制事务提交和关闭连接,默认为true
	 * 
	 * @param bool
	 */
	public void setControlTransaction(boolean bool) {
		controlTransaction = bool;
	}

	/**
	 * 获取所有需要处理的表名
	 * 
	 * @return
	 * @throws Exception
	 */
	protected abstract List<String> gainAllTableName() throws Exception;

	public abstract LogReader createLogReader(String tableName);

	public abstract LogWriter createLogWriter(String tableName);
}