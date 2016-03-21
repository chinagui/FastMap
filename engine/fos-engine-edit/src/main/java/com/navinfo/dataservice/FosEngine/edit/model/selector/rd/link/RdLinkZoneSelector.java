package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkZone;

public class RdLinkZoneSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdLinkZoneSelector.class);

	private Connection conn;

	public RdLinkZoneSelector(Connection conn) {
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
				"select * from rd_link_zone where link_pid =:1 ");

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
				RdLinkZone zone = new RdLinkZone();
				
				zone.setLinkPid(resultSet.getInt("link_pid"));
				
				zone.setRegionId(resultSet.getInt("region_id"));
				
				zone.setType(resultSet.getInt("type"));
				
				zone.setSide(resultSet.getInt("side"));
				
				zone.setRowId(resultSet.getString("row_id"));

				list.add(zone);
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
