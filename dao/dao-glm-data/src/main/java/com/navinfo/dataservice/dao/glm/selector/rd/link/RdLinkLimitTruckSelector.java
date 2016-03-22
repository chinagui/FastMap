package com.navinfo.dataservice.dao.glm.selector.rd.link;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimitTruck;

public class RdLinkLimitTruckSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdLinkLimitTruckSelector.class);

	private Connection conn;

	public RdLinkLimitTruckSelector(Connection conn) {
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
				"select * from rd_link_limit_truck where link_pid =:1 ");

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
				RdLinkLimitTruck row = new RdLinkLimitTruck();
				
				row.setLinkPid(resultSet.getInt("link_pid"));
				
				row.setLimitDir(resultSet.getInt("limit_dir"));
				
				row.setTimeDomain(resultSet.getString("time_domain"));
				
				row.setResTrailer(resultSet.getInt("res_trailer"));
				
				row.setResWeigh(resultSet.getInt("res_weigh"));
				
				row.setResAxleLoad(resultSet.getInt("res_axle_load"));
				
				row.setResAxleCount(resultSet.getInt("res_axle_count"));
				
				row.setResOut(resultSet.getInt("res_out"));
				
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
