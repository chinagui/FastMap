package com.navinfo.dataservice.dao.glm.selector.rd.link;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkIntRtic;

public class RdLinkIntRticSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdLinkIntRticSelector.class);

	private Connection conn;

	public RdLinkIntRticSelector(Connection conn) {
		this.conn = conn;

	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		StringBuilder sb = new StringBuilder(
				"select * from rd_link_int_rtic where link_pid =:1 and u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		List<IRow> list = new ArrayList<IRow>();

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLinkIntRtic row = new RdLinkIntRtic();
				
				row.setLinkPid(resultSet.getInt("link_pid"));
				
				row.setCode(resultSet.getInt("code"));

				row.setRank(resultSet.getInt("rank"));
				
				row.setRticDir(resultSet.getInt("rtic_dir"));
				
				row.setUpdownFlag(resultSet.getInt("updown_flag"));
				
				row.setRangeType(resultSet.getInt("range_type"));
				
				row.setRowId(resultSet.getString("row_id"));

				list.add(row);
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

		return list;
	}
}
