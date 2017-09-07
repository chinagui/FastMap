package com.navinfo.dataservice.impcore.flusher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.log.LoggerRepos;

import com.navinfo.dataservice.impcore.flushbylog.FlushLogBySQL;
import com.navinfo.navicommons.database.sql.DBUtils;

/***
 * 
 * @author zhaokk 日落月多线程刷履历
 */
public class Day2MonLogMultiFlusher extends FlushLogBySQL {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	public Day2MonLogMultiFlusher(DataSource sourceDataSource,
			DataSource targetDataSource, String tempTable, boolean ignoreError,
			String type) {

		super(sourceDataSource, targetDataSource, tempTable, type, ignoreError);
		this.setThreads(true);

	}

	/**
	 * 根据表的个数、最大并发大小，取最小值作为并发大小
	 * 
	 * @throws SQLException
	 */
	@Override
	protected void calcConcurrentSize() throws SQLException {
		this.dataCount = this.getLogCount(this.tempTable);
		if (dataCount <= 1000) {
			concurrentSize = 1;
			tableDatacount = 1;
		} else {
			concurrentSize = 10;
			tableDatacount = dataCount / concurrentSize;
		}
		innerCount += (int) Math.ceil((double) dataCount / tableDatacount);
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
				+ "        from LOG_DETAIL L, " + tempTable + " T       \n"
				+ "      where L.OP_ID=T.OP_ID                  \n"
				+ "          ORDER BY T.OP_SEQ) a    \n"
				+ "         where rn >" + innerCount + "                    \n"
				+ "        AND rn <= " + (innerCount + tableDatacount) + " \n";

		log.info(queryLogSqlInner);
		log.info("createLogReaderInner end");
		return queryLogSqlInner;

	}

	/***
	 * 获取履历数量
	 * 
	 * @param tempTable
	 * @return
	 * @throws SQLException
	 */
	@Override
	public int getLogCount(String tempTable) throws SQLException {
		log.info("createLogReaderInner begin");
		Connection conn = this.sourceDataSource.getConnection();

		int count = 0;
		String queryLogCountSql = "select count(1) count \n"
				+ " from LOG_DETAIL L, " + tempTable + " T          \n"
				+ " where L.OP_ID=T.OP_ID                     \n";
		log.info(queryLogCountSql);
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(queryLogCountSql);

			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				count = resultSet.getInt("count");
			}

			return count;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeConnection(conn);

		}

	}

}
