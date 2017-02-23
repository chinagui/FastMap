package com.navinfo.dataservice.engine.edit.operation.parameterCheck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.navicommons.database.sql.DBUtils;

public class DepartCheck {

	private Connection conn;

	public DepartCheck(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 该线是经过线，移动该线造成线线关系（车信、线线交限、线线语音引导、线线分歧、线线顺行）从inLink到outlink的不连续
	 * 
	 * @return
	 */
	public void checkIsVia(int linkPid) throws Exception {
		String sql = "SELECT LINK_PID FROM RD_LANE_VIA WHERE LINK_PID = :1 AND ROWNUM = 1 AND U_RECORD != 2 UNION ALL SELECT LINK_PID FROM RD_RESTRICTION_VIA WHERE LINK_PID = :2 AND ROWNUM = 1 AND U_RECORD != 2 UNION ALL SELECT LINK_PID FROM RD_VOICEGUIDE_VIA WHERE LINK_PID = :3 AND ROWNUM = 1 AND U_RECORD != 2 UNION ALL SELECT LINK_PID FROM RD_BRANCH_VIA WHERE LINK_PID = :4 AND ROWNUM = 1 AND U_RECORD != 2 UNION ALL SELECT LINK_PID FROM RD_DIRECTROUTE_VIA WHERE LINK_PID = :5 AND ROWNUM = 1 AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, linkPid);

			pstmt.setInt(3, linkPid);

			pstmt.setInt(4, linkPid);

			pstmt.setInt(5, linkPid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				throw new Exception(
						"该线是经过线，移动该线造成线线关系（车信、线线交限、线线语音引导、线线分歧、线线顺行）从inLink到outlink的不连续");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	

	public void checkIsSameNode(int nodePid, String tableName) throws Exception {

		String sql = "SELECT GROUP_ID FROM RD_SAMENODE_PART WHERE NODE_PID = :1 AND TABLE_NAME = :2 AND ROWNUM = 1 AND U_RECORD != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);

			pstmt.setString(2, tableName);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				throw new Exception("不允许分离做了同一点的节点");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
}
