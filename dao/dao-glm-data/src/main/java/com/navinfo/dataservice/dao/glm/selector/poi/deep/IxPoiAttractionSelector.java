package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAttraction;
/**
 * 索引:POI 深度信息(景点类)查询接口
 * @author zhaokk
 *
 */
public class IxPoiAttractionSelector implements ISelector {
	private Connection conn;

	public IxPoiAttractionSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiAttraction poiAttraction = new IxPoiAttraction();

		StringBuilder sb = new StringBuilder(
				"select * from " + poiAttraction.tableName() + " WHERE attraction_id  = :1 and  u_record !=2");

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
				this.setAttr(poiAttraction, resultSet);
				return poiAttraction;
			} else {
				throw new Exception("对应"+poiAttraction.tableName()+"数据不存在不存在!");
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
		List<IRow> rows = new ArrayList<IRow>();
		

		StringBuilder sb = new StringBuilder(
				"select * from IX_POI_ATTRACTION  WHERE poi_pid  = :1 and  u_record !=2");

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
				IxPoiAttraction poiAttraction = new IxPoiAttraction();
				this.setAttr(poiAttraction, resultSet);
				rows.add(poiAttraction);
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

	private void setAttr(IxPoiAttraction ixPoiAttraction,ResultSet resultSet) throws SQLException{
		ixPoiAttraction.setPid(resultSet.getInt("attraction_id"));
		ixPoiAttraction.setPoiPid(resultSet.getInt("poi_pid"));
		ixPoiAttraction.setSightLevel(resultSet.getInt("sight_level"));
		ixPoiAttraction.setLongDescription(resultSet.getString("long_description"));
		ixPoiAttraction.setLongDescripEng(resultSet.getString("long_description_eng"));
		ixPoiAttraction.setTicketPrice(resultSet.getString("ticket_price"));
		ixPoiAttraction.setTicketPriceEng(resultSet.getString("ticket_price_eng"));
		ixPoiAttraction.setOpenHour(resultSet.getString("open_hour"));
		ixPoiAttraction.setOpenHourEng(resultSet.getString("open_hour_eng"));
		ixPoiAttraction.setTelephone(resultSet.getString("telephone"));
		ixPoiAttraction.setAddress(resultSet.getString("address"));
		ixPoiAttraction.setCity(resultSet.getString("city"));
		ixPoiAttraction.setPhotoName(resultSet.getString("photo_name"));
		ixPoiAttraction.setParking(resultSet.getInt("parking"));
		ixPoiAttraction.setTravelguideFlag(resultSet.getInt("travelguide_flag"));
		ixPoiAttraction.setRowId(resultSet.getString("row_id"));
	}
	
}
