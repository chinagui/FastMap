package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.restrict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class RdRestrictionConditionSelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdRestrictionConditionSelector.class);

	private Connection conn;

	public RdRestrictionConditionSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		RdRestrictionCondition condition = new RdRestrictionCondition();

		String sql = "select * from " + condition.tableName()
				+ " where row_id=:1 and u_record!=2 ";

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

				condition.setDetailId(resultSet.getInt("detail_id"));

				condition.setTimeDomain(resultSet.getString("time_domain"));

				condition.setVehicle(resultSet.getInt("vehicle"));

				condition.setResTrailer(resultSet.getInt("res_trailer"));

				condition.setResWeigh(resultSet.getDouble("res_weigh"));

				condition.setResAxleLoad(resultSet.getDouble("res_axle_load"));

				condition.setResAxleCount(resultSet.getInt("res_axle_count"));

				condition.setResOut(resultSet.getInt("res_out"));

				condition.setRowId(resultSet.getString("row_id"));
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

		return condition;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_restriction_condition where detail_id=:1 and u_record!=:2";

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

				RdRestrictionCondition condition = new RdRestrictionCondition();

				condition.setDetailId(resultSet.getInt("detail_id"));

				condition.setTimeDomain(resultSet.getString("time_domain"));

				condition.setVehicle(resultSet.getInt("vehicle"));

				condition.setResTrailer(resultSet.getInt("res_trailer"));

				condition.setResWeigh(resultSet.getDouble("res_weigh"));

				condition.setResAxleLoad(resultSet.getDouble("res_axle_load"));

				condition.setResAxleCount(resultSet.getInt("res_axle_count"));

				condition.setResOut(resultSet.getInt("res_out"));

				condition.setRowId(resultSet.getString("row_id"));

				rows.add(condition);
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
