package com.navinfo.dataservice.impcore.flushbylog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.TransactionalDataSource;
import com.navinfo.navicommons.database.sql.DBUtils;

public class MultiFlushLogByTable extends AbstractFlushLog {
	protected String tempTable;
	protected Collection<String> tables;

	public MultiFlushLogByTable(DataSource sourceDataSource,
			DataSource targetDataSource, String tempTable,String type,
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

	public void run() {
		try {
			log.info("开始多线程刷履历。。。");
			setUseSingleThread(false);
			initConcurrentEnv();
			if (concurrentSize == 0) {
				log.debug("无履历，直接返回");
				return;
			}
			initThreadSharedObject();
			flushThreads();
			doFinish();
		} catch (Exception e) {

			doException(e);
		} finally {
			doFinally();
		}
	}


	private void flushThreads() throws Exception {
		FlushLogToDBThread flushLogToDBThread = null;
		if(tables==null||tables.size()==0){
			log.warn("没传入任何表需要刷库。");
			return;
		}else{
			for(String table:tables){
				LogReaderDay2Month logReader = new LogReaderDay2Month(
						sourceDataSource, this.getLogQuerySql(table, tempTable));
				LogWriterDay2Month logWriter = new LogWriterDay2Month(
						targetDataSource, true, type);
				flushLogToDBThread = new FlushLogToDBThread(threadSharedObj,
						logReader, logWriter, false);
				flushLogToDBThread.setLog(log);
				threadPoolExecutor.execute(flushLogToDBThread);
			}
		}
	}

	/**
	 * 根据表的个数、最大并发大小，取最小值作为并发大小
	 * 
	 * @throws Exception
	 */
	protected void initConcurrentEnv() throws Exception {
		tables = this.getTableCount(tempTable);
		if (tables.size() == 0) {
			return;
		}
		if (tables.size() <= 20) {
			concurrentSize = tables.size();
		} else {
			concurrentSize = 20;
		}
	}

	public void initThreadSharedObject() throws Exception {
		threadSharedObj = new ThreadSharedObjectExtResult(tables.size());
		log.debug("Thread size=====" + tables.size());
		log.debug("Concurrent size=====" + concurrentSize);
		threadPoolExecutor = new ThreadPoolExecutor(concurrentSize,
				concurrentSize, 3, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}

	protected Set<String> getTableCount(String tempTable) throws Exception {
		log.info("get table size...");
		Connection conn = null;
		try{
			conn = this.sourceDataSource.getConnection();
			String sql = "select DISTINCT L.TB_NM FROM FROM LOG_DETAIL L, ? T  where L.OP_ID=T.OP_ID";
			log.info(sql);
			Set<String> tables = new QueryRunner().query(conn, sql, new ResultSetHandler<Set<String>>(){

				@Override
				public Set<String> handle(ResultSet rs) throws SQLException {
					Set<String> tabs = new HashSet<String>();
					while(rs.next()){
						tabs.add(rs.getString(1));
					}
					return tabs;
				}},tempTable);
			log.info("table size:"+tables.size());
			return tables;
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

	public String getLogQuerySql(String tableName, String tempTable) {

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
