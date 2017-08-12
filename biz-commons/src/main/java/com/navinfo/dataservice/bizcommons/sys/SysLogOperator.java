package com.navinfo.dataservice.bizcommons.sys;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;

/**
 * @ClassName: SysLogOperator
 * @author 赵凯凯
 * @date 2017年8月2日
 * @Description: SysLogOperator.java
 */
public class SysLogOperator {
	private static Logger log = LoggerRepos.getLogger(SysLogOperator.class);

	private SysLogOperator() {
	}

	private static class SingletonHolder {
		private static final SysLogOperator INSTANCE = new SysLogOperator();
	}

	public static final SysLogOperator getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void insertSysLog(SysLogStats stats) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			String sql = "INSERT INTO FM_LOG_STATS (LOG_ID,LOG_TYPE,CREATE_TIME,BEGIN_TIME,END_TIME,TOTAL,SUCCESS_TOTAL,FAILURE_TOTAL,ERROR_MSG,LOG_DESC,USER_ID) VALUES (?,?,sysdate,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, UuidUtils.genUuid());
			pstmt.setInt(2, stats.getLogType());

			java.text.SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy/MM/dd HH:mm:ss");// TODO 日期格式需确认

			if (StringUtils.isNotEmpty(stats.getBeginTime())) {
				pstmt.setTimestamp(3,
						new Timestamp(sdf.parse(stats.getBeginTime())

						.getTime()));
			} else {
				pstmt.setTimestamp(3, null);
			}
			if (StringUtils.isNotEmpty(stats.getEndTime())) {
				pstmt.setTimestamp(4,
						new Timestamp(sdf.parse(stats.getEndTime())

						.getTime()));
			} else {
				pstmt.setTimestamp(4, null);
			}

			pstmt.setInt(5, stats.getTotal());
			pstmt.setInt(6, stats.getSuccessTotal());
			pstmt.setInt(7, stats.getFailureTotal());

			Clob clob = conn.createClob();
			clob.setString(1, stats.getErrorMsg());
			if (clob instanceof com.alibaba.druid.proxy.jdbc.ClobProxyImpl) {
				com.alibaba.druid.proxy.jdbc.ClobProxyImpl impl = (com.alibaba.druid.proxy.jdbc.ClobProxyImpl) clob;
				clob = impl.getRawClob();
			}
			pstmt.setClob(8, clob);
			pstmt.setString(9, stats.getLogDesc());
			pstmt.setString(10, stats.getUserId());
			pstmt.execute();

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("插入Sys系统日志出错，原因：" + e.getMessage(), e);

		} finally {
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
		}

	}
}
