package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkLimit;

public class RdLinkLimitSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdLinkLimitSelector.class);

	private Connection conn;

	public RdLinkLimitSelector(Connection conn) {
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
				"select * from rd_link_limit where link_pid =:1 and u_record!=2");

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
				RdLinkLimit limit = new RdLinkLimit();

				limit.setLimitDir(resultSet.getInt("limit_dir"));

				limit.setTimeDomain(resultSet.getString("time_domain"));

				limit.setType(resultSet.getInt("type"));

				limit.setLinkPid(id);
				
				limit.setVehicle(resultSet.getInt("vehicle"));
				
				limit.setTollType(resultSet.getInt("toll_type"));
				
				limit.setWeather(resultSet.getInt("weather"));
				
				limit.setInputTime(resultSet.getString("input_time"));
				
				limit.setProcessFlag(resultSet.getInt("process_flag"));

				limit.setRowId(resultSet.getString("row_id"));

				list.add(limit);
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
