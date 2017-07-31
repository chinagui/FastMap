package com.navinfo.dataservice.dao.glm.model.sys;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;


/**
 * @ClassName: TipsIndexOracleOperator
 * @author xiaoxiaowen4127
 * @date 2017年7月20日
 * @Description: TipsIndexOracleOperator.java
 */
public class SysLogOperator {
	private static Logger log = LoggerRepos.getLogger(SysLogOperator.class);

	public SysLogOperator() {
	}

	private void insertSysLog(SysLogStats stats) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			String sql = "INSERT INTO FM_LOG_STATS (LOG_ID,LOG_TYPE,CREATE_TIME,BEGIN_TIME,END_TIME,SUCCESS_TOTAL,FAILURE_TOTAL,ERROR_MSG,LOG_DESC,USER_ID) VALUES (?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, UuidUtils.genUuid());
			pstmt.setInt(2, stats.getLogType());
			pstmt.setString(3, stats.getBeginTime());
			pstmt.setString(3, stats.getEndTime());
			pstmt.setString(3, stats.getCreateTime());
			pstmt.setInt(2, stats.getLogType());
			pstmt.setInt(2, stats.getLogType());
			Clob oldclob = conn.createClob();
			//oldclob.setString(1, );

			if (oldclob instanceof com.alibaba.druid.proxy.jdbc.ClobProxyImpl) {
				com.alibaba.druid.proxy.jdbc.ClobProxyImpl impl = (com.alibaba.druid.proxy.jdbc.ClobProxyImpl) oldclob;
				oldclob = impl.getRawClob(); // 获取原生的这个 Clob
			}
			pstmt.execute();

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("从管理库中查询出现sql或格式错误，原因：" + e.getMessage(), e);

		} finally {
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
		}

	}
}