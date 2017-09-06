package com.navinfo.dataservice.impcore.flushbylog;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.navinfo.navicommons.database.TransactionalDataSource;

public class FlushLogBySQL extends AbstractFlushLog {
	protected String tempTable;
	protected int tableDatacount;
	protected int innerCount = 0;
	protected boolean isThreads = false;
	protected int dataCount = 0;

	public FlushLogBySQL(DataSource sourceDataSource,
			DataSource targetDataSource, String tempTable, String type,
			boolean ignoreError) {
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
		this.tempTable = tempTable;
		this.type = type;
		threadSharedObj = new ThreadSharedObjectExtResult(0);
	}

	protected void setThreads(boolean isThreads) {
		this.isThreads = isThreads;
	}

	public void run() {
		try {

			if (!isThreads) {
				log.info("开始单线程刷履历。。。");
				setUseSingleThread(true);
				flush();
			} else {
				log.info("开始多线程刷履历。。。");
				setUseSingleThread(false);
				calcConcurrentSize();
				if (concurrentSize == 0) {
					log.debug("无履历，直接返回");
					return;
				}
				initThreadSharedObject();
				flushThreads();

			}
			doFinish();
		} catch (Exception e) {

			doException(e);
		} finally {
			doFinally();
		}
	}

	public void flush() throws Exception {
		LogReader logReader = new LogReader(sourceDataSource.getConnection(),
				this.getLogQuerySql(this.dataCount, tempTable));
		LogWriterDay2Month logWriter = new LogWriterDay2Month(targetDataSource,
				true, type);
		boolean ignoreSQLExeEffectException = false;
		FlushLogToDBThread flushLogToDBThread = new FlushLogToDBThread(
				threadSharedObj, logReader, logWriter,
				ignoreSQLExeEffectException);
		flushLogToDBThread.setLog(log);
		flushLogToDBThread.run();

	}

	private void flushThreads() throws Exception {
		FlushLogToDBThread flushLogToDBThread = null;
		FlushLogToDBThread flushLogToDBThreadInner = null;

		if (dataCount >= tableDatacount) {
			for (int j = 0; j < dataCount; j += tableDatacount) {
				LogReader logReader = new LogReader(
						sourceDataSource.getConnection(), this.getLogQuerySql(
								j, tempTable));
				LogWriterDay2Month logWriter = new LogWriterDay2Month(
						targetDataSource, true, type);
				flushLogToDBThreadInner = new FlushLogToDBThread(
						threadSharedObj, logReader, logWriter, true);

				if (flushLogToDBThreadInner != null) {
					flushLogToDBThreadInner.setLog(log);
				}
				if (flushLogToDBThreadInner != null) {
					threadPoolExecutor.execute(flushLogToDBThreadInner);
				}
			}
		} else {

			LogReader logReader = new LogReader(
					sourceDataSource.getConnection(), this.getLogQuerySql(
							dataCount, tempTable));
			LogWriterDay2Month logWriter = new LogWriterDay2Month(
					targetDataSource, true, type);
			flushLogToDBThread = new FlushLogToDBThread(threadSharedObj,
					logReader, logWriter, false);

		}
		if (flushLogToDBThread != null) {
			flushLogToDBThread.setLog(log);
		}
		if (maxConcurrentSize == 1) {
			flushLogToDBThread.run();
			if (threadSharedObj.getExceptionList().size() > 0) {
				log.debug("刷履历发生错误，停止刷履历");

			}

		} else {
			if (flushLogToDBThread != null) {
				threadPoolExecutor.execute(flushLogToDBThread);

			}

		}
	}

	/**
	 * 根据表的个数、最大并发大小，取最小值作为并发大小
	 * 
	 * @throws Exception
	 */
	@Override
	protected void calcConcurrentSize() throws Exception {

		this.dataCount = this.getLogCount();

		if (dataCount == 1) {
			concurrentSize = 1;
			tableDatacount = 1;
		} else if (dataCount < 1000 && dataCount > 1) {
			concurrentSize = 2;
			tableDatacount = dataCount / concurrentSize;
		} else if (dataCount >= 1000 && dataCount < 10000) {
			concurrentSize = 3;
			tableDatacount = dataCount / concurrentSize;
		} else if (dataCount >= 10000 && dataCount < 50000) {
			concurrentSize = 5;
			tableDatacount = dataCount / concurrentSize;
		} else {
			concurrentSize = 20;
			tableDatacount = dataCount / concurrentSize;
		}

		innerCount += (int) Math.ceil((double) dataCount / tableDatacount);

	}

	public void initThreadSharedObject() throws Exception {
		threadSharedObj = new ThreadSharedObjectExtResult(innerCount);
		log.debug("innerCount=====" + innerCount);
		threadPoolExecutor = new ThreadPoolExecutor(concurrentSize,
				concurrentSize, 3, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}

	protected int getLogCount() throws Exception {
		return 0;
	}

	public String getLogQuerySql(int innerCount, String tempTable) {

		return null;
	}

	@Override
	protected List<String> gainAllTableName() throws Exception {
		return null;
	}

	@Override
	public LogReader createLogReader(String tableName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogWriter createLogWriter(String tableName) {
		// TODO Auto-generated method stub
		return null;
	}

}
