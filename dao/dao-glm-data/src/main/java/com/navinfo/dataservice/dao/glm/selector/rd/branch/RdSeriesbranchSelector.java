package com.navinfo.dataservice.dao.glm.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;

public class RdSeriesbranchSelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdSeriesbranchSelector.class);

	private Connection conn;

	public RdSeriesbranchSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		RdSeriesbranch seriesbranch = new RdSeriesbranch();

		String sql = "select * from " + seriesbranch.tableName()
				+ " where row_id=hextoraw(:1) and u_record!=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, rowId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				setAttr(seriesbranch, resultSet);
			} else {

				throw new DataNotFoundException("数据不存在");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

		return seriesbranch;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_seriesbranch where branch_pid=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdSeriesbranch seriesbranch = new RdSeriesbranch();

				setAttr(seriesbranch, resultSet);

				rows.add(seriesbranch);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

		return rows;
	}

	private void setAttr(RdSeriesbranch seriesbranch, ResultSet resultSet)
			throws SQLException {

		seriesbranch.setBranchPid(resultSet.getInt("branch_pid"));

		seriesbranch.setType(resultSet.getInt("type"));

		seriesbranch.setVoiceDir(resultSet.getInt("voice_dir"));

		seriesbranch.setPatternCode(resultSet.getString("pattern_code"));

		seriesbranch.setArrowCode(resultSet.getString("arrow_code"));

		seriesbranch.setArrowFlag(resultSet.getInt("arrow_flag"));

		seriesbranch.setRowId(resultSet.getString("row_id"));
	}

}
