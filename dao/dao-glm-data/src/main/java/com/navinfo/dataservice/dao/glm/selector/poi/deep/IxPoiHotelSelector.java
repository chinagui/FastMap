package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
/**
 * 索引:POI 深度信息(住宿酒店类)查询接口
 * @author zhaokk
 *
 */
public class IxPoiHotelSelector implements ISelector {
	private Connection conn;

	public IxPoiHotelSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiHotel ixPoiHotel = new IxPoiHotel();

		StringBuilder sb = new StringBuilder(
				"select * from " + ixPoiHotel.tableName() + " WHERE  hotel_id= :1 and  u_record !=2");

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
				this.setAttr(ixPoiHotel, resultSet);
				return ixPoiHotel;
			} else {
				throw new Exception("对应"+ixPoiHotel.tableName()+"数据不存在不存在!");
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
				"select * from ix_poi_hotel WHERE poi_pid  = :1 and  u_record !=2");

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
				IxPoiHotel ixPoiHotel = new IxPoiHotel();
				this.setAttr(ixPoiHotel, resultSet);
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
 
	private void setAttr(IxPoiHotel ixPoiHotel,ResultSet resultSet) throws SQLException{
		ixPoiHotel.setPid(resultSet.getInt("hotel_id"));
		ixPoiHotel.setPoiPid(resultSet.getInt("poi_pid"));
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
		ixPoiHotel.setLongDescripEng(resultSet.getString("long_descrip_eng"));
		ixPoiHotel.setOpenHour(resultSet.getString("open_hour"));
		ixPoiHotel.setOpenHourEng(resultSet.getString("open_hour_eng"));
		ixPoiHotel.setTelephone(resultSet.getString("telephone"));
		ixPoiHotel.setAddress(resultSet.getString("address"));
		ixPoiHotel.setCity(resultSet.getString("city"));
		ixPoiHotel.setPhotoName(resultSet.getString("photo_name"));
		ixPoiHotel.setTravelguideFlag(resultSet.getInt("travelguide_flag"));
		ixPoiHotel.setRowId(resultSet.getString("row_id"));
		ixPoiHotel.setuDate(resultSet.getString("u_date"));
	}
	
}
