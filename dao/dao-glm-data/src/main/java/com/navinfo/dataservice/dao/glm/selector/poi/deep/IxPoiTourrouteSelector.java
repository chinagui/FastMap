package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiTourroute;
/**
 * 索引:POI 深度信息(旅游线路类)查询接口
 * @author zhaokk
 *
 */
public class IxPoiTourrouteSelector implements ISelector {
	private Connection conn;

	public IxPoiTourrouteSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiTourroute ixPoiTourroute = new IxPoiTourroute();

		StringBuilder sb = new StringBuilder(
				"select * from " + ixPoiTourroute.tableName() + " WHERE  tour_id= :1 and  u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				this.setAttr(ixPoiTourroute, resultSet);
				return ixPoiTourroute;
			} else {
				throw new Exception("对应"+ixPoiTourroute.tableName()+"数据不存在不存在!");
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
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		return null;
	}
	private void setAttr(IxPoiTourroute ixPoiTourroute,ResultSet resultSet) throws SQLException{
		ixPoiTourroute.setPid(resultSet.getInt("tour_id"));
		ixPoiTourroute.setTourName(resultSet.getString("tour_name"));
		ixPoiTourroute.setTourNameEng(resultSet.getString("tour_name_eng"));
		ixPoiTourroute.setTourIntr(resultSet.getString("tour_intr"));
		ixPoiTourroute.setTourIntrEng(resultSet.getString("tour_intr_eng"));
		ixPoiTourroute.setTourType(resultSet.getString("tour_type"));
		ixPoiTourroute.setTourTypeEng(resultSet.getString("tour_type_eng"));
		ixPoiTourroute.setTourX(resultSet.getDouble("tour_x"));
		ixPoiTourroute.setTourY(resultSet.getDouble("tour_y"));
		ixPoiTourroute.setTourLen(resultSet.getDouble("tour_len"));
		ixPoiTourroute.setTrailTime(resultSet.getString("trail_time"));
		ixPoiTourroute.setVisitTime(resultSet.getString("visit_time"));
		ixPoiTourroute.setPoiPid(resultSet.getString("poi_pid"));
		ixPoiTourroute.setReserved(resultSet.getString("reserved"));
		ixPoiTourroute.setMemo(resultSet.getString("memo"));
		ixPoiTourroute.setRowId(resultSet.getString("row_id"));

	}
	
}
