package com.navinfo.dataservice.dao.glm.selector.rd.link;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;

public class RdLinkSpeedlimitSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdLinkSpeedlimitSelector.class);

	private Connection conn;

	public RdLinkSpeedlimitSelector(Connection conn) {
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
				"select * from rd_link_speedlimit where link_pid =:1 and u_record !=2");

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
				RdLinkSpeedlimit row = new RdLinkSpeedlimit();
				
				row.setLinkPid(resultSet.getInt("link_pid"));
				
				row.setSpeedType(resultSet.getInt("speed_type"));

				row.setFromSpeedLimit(resultSet.getInt("from_speed_limit"));
				
				row.setToSpeedLimit(resultSet.getInt("to_speed_limit"));
				
				row.setSpeedClass(resultSet.getInt("speed_class"));
				
				row.setFromLimitSrc(resultSet.getInt("from_limit_src"));
				
				row.setToLimitSrc(resultSet.getInt("to_limit_src"));
				
				row.setSpeedDependent(resultSet.getInt("speed_dependent"));
				
				row.setTimeDomain(resultSet.getString("time_domain"));
				
				row.setSpeedClassWork(resultSet.getInt("speed_class_work"));
				
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
