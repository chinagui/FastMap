package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.laneconnexity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.exception.DataNotFoundException;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneVia;

public class RdLaneViaSelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdLaneViaSelector.class);

	private Connection conn;

	public RdLaneViaSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		RdLaneVia via = new RdLaneVia();

		String sql = "select * from " + via.tableName()
				+ " where row_id=:1 and u_record!=2";

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

				via.setTopologyId(resultSet.getInt("topology_id"));

				via.setLinkPid(resultSet.getInt("link_pid"));

				via.setGroupId(resultSet.getInt("group_id"));

				via.setSeqNum(resultSet.getInt("seq_num"));

				via.setRowId(resultSet.getString("row_id"));
			} else {
				
				throw new DataNotFoundException(null);
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

		return via;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_lane_via where topology_id=:1 and u_record!=:2 order by seq_num";

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

				RdLaneVia via = new RdLaneVia();

				via.setTopologyId(resultSet.getInt("topology_id"));

				via.setLinkPid(resultSet.getInt("link_pid"));

				via.setGroupId(resultSet.getInt("group_id"));

				via.setSeqNum(resultSet.getInt("seq_num"));

				via.setRowId(resultSet.getString("row_id"));

				rows.add(via);
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
