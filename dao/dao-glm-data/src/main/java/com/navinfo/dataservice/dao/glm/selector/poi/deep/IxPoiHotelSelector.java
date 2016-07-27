package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
/**
 * 索引:POI 深度信息(住宿酒店类)查询接口
 * @author zhaokk
 *
 */
public class IxPoiHotelSelector extends AbstractSelector {
	private Connection conn;

	public IxPoiHotelSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoiHotel.class);
	}
	
	public List<IRow> loadByIdForAndroid(int id)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		

		StringBuilder sb = new StringBuilder(
				"select credit_card,rating,checkin_time,checkout_time,room_count,room_type,room_price,breakfast,service,parking,long_description,open_hour,row_id");
		sb.append("  from ix_poi_hotel WHERE poi_pid  = :1 and  u_record !=2");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while(resultSet.next()) {
				IxPoiHotel ixPoiHotel = new IxPoiHotel();
				ixPoiHotel.setCreditCard(resultSet.getString("credit_card"));
				ixPoiHotel.setRating(resultSet.getInt("rating"));
				ixPoiHotel.setCheckinTime(resultSet.getString("checkin_time"));
				ixPoiHotel.setCheckoutTime(resultSet.getString("checkout_time"));
				ixPoiHotel.setRoomCount(resultSet.getInt("room_count"));
				ixPoiHotel.setRoomType(resultSet.getString("room_type"));
				ixPoiHotel.setRoomPrice(resultSet.getString("room_price"));
				ixPoiHotel.setBreakfast(resultSet.getInt("breakfast"));
				ixPoiHotel.setService(resultSet.getString("service"));
				ixPoiHotel.setParking(resultSet.getInt("parking"));
				ixPoiHotel.setLongDescription(resultSet.getString("long_description"));
				ixPoiHotel.setOpenHour(resultSet.getString("open_hour"));
				ixPoiHotel.setRowId(resultSet.getString("row_id"));
				rows.add(ixPoiHotel);
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
	
}
