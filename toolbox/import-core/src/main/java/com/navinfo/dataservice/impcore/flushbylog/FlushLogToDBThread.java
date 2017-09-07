package com.navinfo.dataservice.impcore.flushbylog;

import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.TransactionalDataSource;

/**
 * 刷履历线程
 * 
 * @author zhaokk
 * 
 */
public class FlushLogToDBThread extends Thread {
	protected Logger log = Logger.getLogger(FlushLogToDBThread.class);
	protected ThreadSharedObjectExtResult threadSharedObject;
	protected ThreadPoolExecutor threadPoolExecutor = null;
	protected LogReaderDay2Month logReader;
	protected LogWriterDay2Month logWriter;
	protected boolean ignoreSQLExeEffectException;
	protected TransactionalDataSource targetDataSource;

	public int totalCount = 0;
	public long totalStartTime = 0;
	public String totalCountLock = "totalCountLock";

	public List<EditLog> editLogs;

	long startTime;
	long endTime;

	private String type;

	public FlushLogToDBThread(ThreadSharedObjectExtResult threadSharedObject,
			LogReaderDay2Month logReader, LogWriterDay2Month logWriter,
			boolean ignoreSQLExeEffectException) throws Exception {
		this.threadSharedObject = threadSharedObject;
		this.logReader = logReader;
		this.logWriter = logWriter;
		this.ignoreSQLExeEffectException = ignoreSQLExeEffectException;
	}

	public void init() throws Exception {

		try {
			log.debug("logWriter.open");
			logWriter.open();
		} catch (Exception e) {
			throw new Exception("打开履历写入器出错:" + e.getMessage(), e);
		}
		try {
			log.debug("logWriter.open");
			logReader.open();
		} catch (Exception e) {
			throw new Exception("打开履历写入器出错:" + e.getMessage(), e);
		}
	}

	public void run() {
		try {
			startTime = System.currentTimeMillis();
			log.debug("FlushLogToDBThread.run");
			int index = 0;
			init();
			ResultSet rs = logReader.read();
			rs.setFetchSize(1000);
			FlushResult flushResult = new FlushResult();

			while (rs.next()) {

				try {
					flushResult.addTotal();
					int opType = rs.getInt("op_tp");
					String rowId = rs.getString("row_id");
					String opId = rs.getString("op_id");
					String newValue = rs.getString("new");
					String tableName = rs.getString("tb_nm");
					String tableRowId = rs.getString("tb_row_id");

					EditLog editLog = new EditLog(opType, rowId, opId, rowId,
							newValue, tableName, tableRowId);
					ILogWriteListener listener = new LogWriteListener(
							flushResult);
					logWriter.write(editLog, listener);
					index++;
					if (index % 10000 == 0) {
						totalCountPlus(10000);
					}
				} catch (Exception e) {
					if (ignoreSQLExeEffectException == false) {
						log.debug("ignoreSQLExeEffectException=false");
						throw e;
					} else {

						threadSharedObject.addWarn(e);
					}
				}

			}

			totalCountPlus(index % 10000);
			threadSharedObject.addFlushResult(flushResult);
			endTime = System.currentTimeMillis();
			log.debug("线程任务：" + Thread.currentThread().getName() + "," + index
					+ ","
					+ (int) (index / ((endTime - startTime + 0.1) / 1000))
					+ "/s.");

			doFinish();
		} catch (Exception e) {
			doException(e);
		} finally {
			doFinally();
		}
	}

	void totalCountPlus(int i) {
		synchronized (totalCountLock) {
			if (totalStartTime == 0) {
				totalStartTime = System.currentTimeMillis();
			}
			totalCount += i;
			long cost = System.currentTimeMillis() - totalStartTime;
			log.debug("总进度：已完成 " + totalCount + ",速度 "
					+ (int) (totalCount / ((cost + 0.1) / 1000)) + "/sec.");
		}
	}

	public void doFinish() {
		threadSharedObject.executeSuccess();

	}

	public void doFinally() {
		try {
			if (logReader != null) {
				logReader.close();
			}
		} catch (Exception e) {
			log.warn("关闭履历写入器时出错:", e);
		}

		try {
			if (logWriter != null) {
				logWriter.close();
			}
		} catch (Exception e) {
			log.warn("关闭履历写入器时出错:", e);
		}
	}

	protected void doException(Exception e) {
		log.error(e.getMessage(), e);
		log.debug("有错误发生，终止线程：" + Thread.currentThread().getName());
		log.debug("已终止线程：" + Thread.currentThread().getName());
		threadSharedObject.addException(e);
		threadSharedObject.executeFailAndNotifyMainThread();
	}

	public Logger getLog() {
		return log;
	}

	public void setLog(Logger log) {
		this.log = log;
	}

}
