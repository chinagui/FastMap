package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.cross;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossLink;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class RdCrossLinkSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdCrossLinkSelector.class);

	private Connection conn;

	public RdCrossLinkSelector(Connection conn) {
		this.conn = conn;

	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		RdCrossLink link = new RdCrossLink();

		String sql = "select * from " + link.tableName() + " where row_id=:1";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, rowId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				link.setPid(resultSet.getInt("pid"));

				link.setLinkPid(resultSet.getInt("link_pid"));

				link.setRowId(resultSet.getString("row_id"));
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

		return link;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_cross_link where pid=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdCrossLink link = new RdCrossLink();

				link.setPid(resultSet.getInt("pid"));

				link.setLinkPid(resultSet.getInt("link_pid"));

				link.setRowId(resultSet.getString("row_id"));

				rows.add(link);
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

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

}
