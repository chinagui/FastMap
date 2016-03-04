package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class RdBranchRealimageSelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdBranchRealimageSelector.class);

	private Connection conn;

	public RdBranchRealimageSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		RdBranchRealimage realimage = new RdBranchRealimage();

		String sql = "select * from " + realimage.tableName()
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

				realimage.setBranchPid(resultSet.getInt("branch_pid"));

				realimage.setImageType(resultSet.getInt("image_type"));

				realimage.setRealCode(resultSet.getString("real_code"));

				realimage.setArrowCode(resultSet.getString("arrow_code"));

				realimage.setRowId(resultSet.getString("row_id"));
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

		return realimage;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_branch_realimage where branch_pid=:1 and u_record!=:2";

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

				RdBranchRealimage realimage = new RdBranchRealimage();

				realimage.setBranchPid(resultSet.getInt("branch_pid"));

				realimage.setImageType(resultSet.getInt("image_type"));

				realimage.setRealCode(resultSet.getString("real_code"));

				realimage.setArrowCode(resultSet.getString("arrow_code"));

				realimage.setRowId(resultSet.getString("row_id"));

				rows.add(realimage);
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

}
