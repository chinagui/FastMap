package com.navinfo.dataservice.dao.log;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

/**
 * 日志查询类
 */
public class LogReader {

	private Connection conn;

	public LogReader(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 根据条件查询
	 * 
	 * @param jsonCondition
	 * @return
	 */
	public List<LogOperation> queryLog(JSONObject jsonCondition) {
		return null;
	}

	/**
	 * 查询对象状态：1新增，2删除，3修改
	 * 
	 * @param objPid
	 * @param objTable
	 * @param dbId
	 * @return
	 * @throws Exception
	 */
	public int getObjectState(int objPid, String objTable) throws Exception {

		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1 AND de.OB_NM= :2 ORDER BY op.OP_DT DESC";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
			pstmt.setString(2, objTable);
			resultSet = pstmt.executeQuery();
			Date lastObjOpDate = null;
			int opStatus = 0;
			while (resultSet.next()) {
				if (objTable.equals(resultSet.getString("TB_NM")) && opStatus == 0) {
					lastObjOpDate = resultSet.getTimestamp("op_dt");
					opStatus = resultSet.getInt("op_tp");
					if (1 == opStatus || 2 == opStatus) {
						return opStatus;
					} else {
						if (isExistsAddHis(objPid, objTable, lastObjOpDate)) {
							return 1;
						} else {
							return opStatus;
						}
					}
				}

				if (!objTable.equals(resultSet.getString("TB_NM")) && opStatus == 0) {
					lastObjOpDate = resultSet.getTimestamp("op_dt");
					opStatus = 3;
					if (isExistsAddHis(objPid, objTable, lastObjOpDate)) {
						return 1;
					} else {
						return opStatus;
					}
				}
			}

			return opStatus;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
//			DBUtils.closeConnection(conn);

		}
	}

	/**
	 * 查询是否存在某次履历时间之前的新增履历
	 * 
	 * @param objPid
	 * @param objTable
	 * @param dbId
	 * @return
	 * @throws Exception
	 */
	public boolean isExistsAddHis(int objPid, String objTable, Date lastObjOpDate) throws Exception {

		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1 AND de.OB_NM= :2 AND de.OP_TP=:3 AND op.op_dt<:4 ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
			pstmt.setString(2, objTable);
			pstmt.setInt(3, 1);
			pstmt.setDate(4, (java.sql.Date) lastObjOpDate);
			resultSet = pstmt.executeQuery();
			if (resultSet.getRow() == 0) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}

	/**
	 * 查询对象某字段是否修改
	 * 
	 * @param objPid
	 * @param objTable
	 * @param tbNm
	 * @param Feild
	 * @return
	 * @throws Exception
	 */
	public boolean isUpdateforObjFeild(int objPid, String objTable, String tbNm, String Feild) throws Exception {

		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1 AND de.OB_NM= :2 AND de.TB_NM=:3 AND de.FD_LST=:4 ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
			pstmt.setString(2, objTable);
			pstmt.setString(3, tbNm);
			pstmt.setString(4, Feild);
			resultSet = pstmt.executeQuery();
			if (resultSet.getRow() == 0) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}

	/**
	 * 查询poi是否仅有照片或者备注的履历
	 * 
	 * @param objPid
	 * @param objTable
	 * @param tbNm
	 * @param Feild
	 * @return
	 * @throws Exception
	 */
	public boolean isOnlyPhotoAndMetoHis(int objPid) throws Exception {

		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1 AND (de.TB_NM not in ('ix_poi','ix_poi_photo') or (de.TB_NM='ix_poi' AND instr(de.FD_LST,'poi_meto')=0)) ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
			resultSet = pstmt.executeQuery();
			if (resultSet.getRow() == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}

	/**
	 * 查询对象是否存在履历
	 * 
	 * @param objPid
	 * @return
	 * @throws Exception
	 */
	public boolean isExistObjHis(int objPid) throws Exception {

		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1  ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
			resultSet = pstmt.executeQuery();
			if (resultSet.getRow() == 0) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}
}
