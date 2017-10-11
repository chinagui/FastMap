package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.sqlite.SQLiteConfig;

import com.navinfo.dataservice.scripts.model.EightTypesPoi;

/**
 * @Title: ExportEightTypes2Sqlite
 * @Package: com.navinfo.dataservice.scripts
 * @Description:
 * @Author: LittleDog
 * @Date: 2017年9月18日
 * @Version: V1.0
 */
public class ExportEightTypes2Sqlite {

	public static Connection createSqlite(String dir) throws Exception {
		Class.forName("org.sqlite.JDBC");

		Connection sqliteConn = null;
		Statement stmt = null;
		
		try {
			SQLiteConfig config = new SQLiteConfig();
			config.enableLoadExtension(true);

			sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + dir);
			stmt = sqliteConn.createStatement();
			stmt.setQueryTimeout(30); 

			sqliteConn.setAutoCommit(false);

			stmt.execute(
					"CREATE TABLE NI_VAL_EXCEPTION(VAL_EXCEPTION_ID INTEGER , GROUPID INTEGER, LEVEL INTEGER , RULEID INTEGER, SITUATION TEXT, INFORMATION TEXT, SUGGESTION TEXT , LOCATION TEXT , TARGETS TEXT , ADDITIONINFO INTEGER , SCOPEFLAG INTEGER , CREATED TEXT , UPDATED TEXT , MESHID TEXT , PROVINCENAME TEXT , TASK_ID INTEGER , QA_STATUS INTEGER , WORKER TEXT , QA_WORKER TEXT , RESERVED TEXT , TASK_NAME TEXT , LOG_TYPE INTEGER)");

			return sqliteConn;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(sqliteConn);
			throw new Exception(e);
			
		} finally {
			DbUtils.closeQuietly(stmt);
		}
	}

	public static void exportEightTypesPoi(Connection sqliteConn, List<EightTypesPoi> list) throws Exception {

		String insertSql = "INSERT INTO NI_VAL_EXCEPTION VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = sqliteConn.prepareStatement(insertSql);

			for (EightTypesPoi eightTypesPoi : list) {

				pstmt.setInt(1, eightTypesPoi.getValExceptionId());
				pstmt.setInt(2, eightTypesPoi.getGroupId());
				pstmt.setInt(3, eightTypesPoi.getLevel());
				pstmt.setInt(4, eightTypesPoi.getRuleId());

				pstmt.setString(5, eightTypesPoi.getSituation());
				pstmt.setString(6, eightTypesPoi.getInformation());
				pstmt.setString(7, eightTypesPoi.getSuggestion());
				pstmt.setString(8, eightTypesPoi.getLocation());
				pstmt.setString(9, eightTypesPoi.getTargets());

				pstmt.setInt(10, eightTypesPoi.getAdditionInfo());
				pstmt.setInt(11, eightTypesPoi.getScopeFlag());

				pstmt.setString(12, eightTypesPoi.getCreated());
				pstmt.setString(13, eightTypesPoi.getUpdated());
				pstmt.setString(14, eightTypesPoi.getMeshId());
				pstmt.setString(15, eightTypesPoi.getProvinceName());

				pstmt.setLong(16, eightTypesPoi.getTaskId());
				pstmt.setInt(17, eightTypesPoi.getQaStatus());

				pstmt.setString(18, eightTypesPoi.getWorker());
				pstmt.setString(19, eightTypesPoi.getQaWorker());
				pstmt.setString(20, eightTypesPoi.getReserved());
				pstmt.setString(21, eightTypesPoi.getTaskName());

				pstmt.setInt(22, eightTypesPoi.getLogType());

				pstmt.executeUpdate();
			}

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(sqliteConn);
			throw new Exception(e);
			
		} finally {
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(sqliteConn);
		}
		
	}
}
