package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBusinessTime;
/**
 * 索引:POI 深度信息(开放或营业时间)
 * @author zhaokk
 *
 */
public class IxPoiBusinessTimeSelector implements ISelector {
	private Connection conn;

	public IxPoiBusinessTimeSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		

		StringBuilder sb = new StringBuilder(
				"select * from ix_poi_businesstime  WHERE poi_pid  = :1 and  u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while(resultSet.next()) {
				IxPoiBusinessTime ixBusinessTime= new IxPoiBusinessTime();
				this.setAttr(ixBusinessTime, resultSet);
				rows.add(ixBusinessTime);
			} return rows;
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
	}

	private void setAttr(IxPoiBusinessTime ixPoiBusinessTime,ResultSet resultSet) throws SQLException{
		
		ixPoiBusinessTime.setPoiPid(resultSet.getInt("poi_pid"));
		ixPoiBusinessTime.setMonSrt(resultSet.getString("mon_srt"));
		ixPoiBusinessTime.setMonEnd(resultSet.getString("mon_end"));
		ixPoiBusinessTime.setWeekInMonthEnd(resultSet.getString("week_in_month_end"));
		ixPoiBusinessTime.setWeekInMonthSrt(resultSet.getString("week_in_month_srt"));
		ixPoiBusinessTime.setWeekInYearEnd(resultSet.getString("week_in_year_end"));
		ixPoiBusinessTime.setWeekInYearSrt(resultSet.getString("week_in_year_srt"));
		ixPoiBusinessTime.setVaildWeek(resultSet.getString("valid_week"));
		ixPoiBusinessTime.setDaySrt(resultSet.getString("day_srt"));
		ixPoiBusinessTime.setDayEnd(resultSet.getString("day_end"));
		ixPoiBusinessTime.setTimeSrt(resultSet.getString("time_srt"));
		ixPoiBusinessTime.setTimeDue(resultSet.getString("time_dur"));
		ixPoiBusinessTime.setReserved(resultSet.getString("reserved"));
		ixPoiBusinessTime.setMemo(resultSet.getString("memo"));
		ixPoiBusinessTime.setRowId(resultSet.getString("rowId"));

	}
	
}
