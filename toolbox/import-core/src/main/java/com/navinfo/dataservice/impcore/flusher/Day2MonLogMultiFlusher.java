package com.navinfo.dataservice.impcore.flusher;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.log.LoggerRepos;

import com.navinfo.dataservice.impcore.flushbylog.FlushLogBySQL;

/***
 * 
 * @author zhaokk 日落月多线程刷履历
 */
public class Day2MonLogMultiFlusher extends FlushLogBySQL {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	protected int tableDatacount;// 并发履历数量
	protected int concurrentSize;// 实际并发线程个数

	public Day2MonLogMultiFlusher(DataSource sourceDataSource,
			DataSource targetDataSource, String tempTable, boolean ignoreError,
			String type) {

		super(sourceDataSource, targetDataSource, tempTable, type, ignoreError);
		this.setThreads(true);

	}

	/**
	 * 根据表的个数、最大并发大小，取最小值作为并发大小
	 */
	protected void calcConcurrentSize(int count) {
		if (count <= 1000) {
			concurrentSize = 1;
			tableDatacount = 1;
		} else {
			concurrentSize = 10;
			tableDatacount = count / concurrentSize;
		}
	}

	/**
	 * 功能：获取差分履历库中单表多线程数据信息
	 * 
	 * @author zhaokk
	 * @param tableName
	 *            表名,innerCount 查询数量
	 * @return LogReader
	 * @throws Exception
	 * */
	public String getLogQuerySql(int innerCount, String tempTable) {
		log.info("createLogReaderInner begin");
		String queryLogSqlInner = "select a.*		  \n"
				+ "  from (select L.*, rownum rn                \n"
				+ "        from LOG_DETAIL L, tempTable T       \n"
				+ "      where L.OP_ID=T.OP_ID                  \n"
				+ "          ORDER BY T.OP_SEQ) a    \n"
				+ "         where rn >" + innerCount + "                    \n"
				+ "        AND rn <= " + (innerCount + tableDatacount) + " \n";

		log.info(queryLogSqlInner);
		log.info("createLogReaderInner end");
		return null;

	}

	/***
	 * 获取履历数量
	 * 
	 * @param tempTable
	 * @return
	 */
	public int getLogCount(String tempTable) {
		log.info("createLogReaderInner begin");
		String queryLogCountSql = "select count(1) \n"
				+ " from LOG_DETAIL L, tempTable T          \n"
				+ " where L.OP_ID=T.OP_ID                     \n";
		log.info(queryLogCountSql);
		return 0;

	}

}
