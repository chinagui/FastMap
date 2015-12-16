package com.navinfo.dataservice.FosEngine.comm.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.service.PidService.PidRangeCombine;

public class PidServiceUtils {
	
	private static Logger logger = Logger.getLogger(PidServiceUtils.class);

	public static final String sqlPidRange = "select pid_range from pid_storage where sequence_name=:1";

	public static final String sqlUpdatePidRange = "update pid_storage set pid_range=:1 where sequence_name=:2";
	
	public static final String sqlTransportPids = "call proc_apply_pid(?,?,?)";

	/**
	 * 通過序列名稱查詢PID可用範圍
	 */
	public static String getPidRange(Connection conn, String sequenceName) throws Exception{
		
		String pidRange = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sqlPidRange);

			pstmt.setString(1, sequenceName);

			resultSet = pstmt.executeQuery();

			resultSet.next();
			
			pidRange = resultSet.getString(1);
			
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {
			}

			try {
				pstmt.close();
			} catch (Exception e) {
			}
		}

		return pidRange;
	}

	/**
	 * 通過當前PID範圍，獲取下一個PID
	 */
	public static PidRangeCombine applyPid(String pidRange)  {
		PidRangeCombine prc = new PidRangeCombine();

		if (pidRange.split(",").length > 1) {
			String firstPart = pidRange.split(",")[0];

			String[] minMax = firstPart.split("\\-");

			int minValue = Integer.parseInt(minMax[0]);

			int maxValue = Integer.parseInt(minMax[1]);

			prc.setPid(minValue);

			if (++minValue < maxValue) {

				prc.setPidRange(minValue + "-" + maxValue + ","
						+ pidRange.split(",")[1]);

			} else {
				prc.setPidRange(pidRange.split(",")[1]);
			}
		} else {
			String[] minMax = pidRange.split("\\-");

			int minValue = Integer.parseInt(minMax[0]);

			int maxValue = Integer.parseInt(minMax[1]);

			prc.setPid(minValue);

			if (++minValue < (maxValue - 500)) {

				prc.setPidRange(minValue + "-" + maxValue);

			} else {
				prc.setPid(-1);
			}
		}

		return prc;
	}
	
	
	/**
	 * 從ID分配器搬運PID
	 * @throws Exception 
	 */
	public static int transportPid(Connection conn,int batchSize,String sequenceName) throws Exception{
		int pid = 0;
		
		CallableStatement proc = null;
		
		try{
			proc = conn.prepareCall(sqlTransportPids);
			
			proc.setString(1, sequenceName);
			
			proc.setInt(2, batchSize);
			
			proc.registerOutParameter(3, Types.INTEGER);
			
			proc.execute();
			
			pid = proc.getInt(3);
		}catch(Exception e){
			throw e;
		}finally{
			try {
				proc.close();
			} catch (SQLException e) {
			}
		}
		
		return pid;
	}
	
	/**
	 * 更新PID範圍
	 * @throws Exception 
	 */
	public static void updatePidRange(Connection conn,String sequeceName,String pidRange) throws Exception{
		
		PreparedStatement pstmt = null;
		
		try{
			pstmt = conn.prepareStatement(sqlUpdatePidRange);
			
			pstmt.setString(1, pidRange);
			
			pstmt.setString(2, sequeceName);
			
			pstmt.executeUpdate();
			
			conn.commit();
		}catch(Exception e){
			
			
			try {
				conn.rollback();
			} catch (SQLException e1) {
			}
			
			throw e;
		}finally{
			try{
				pstmt.close();
			}catch(SQLException e){
			}
		}
	}
}
