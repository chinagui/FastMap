package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;

public class IxHotelHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> hotelMap = new HashMap<Long, List<IRow>>();
		try {
			while (rs.next()){
				List<IRow> hotelList = new ArrayList<IRow>();
				IxPoiHotel ixPoiHotel = new IxPoiHotel();
				ixPoiHotel.setCreditCard(rs.getString("credit_card"));
				ixPoiHotel.setRating(rs.getInt("rating"));
				ixPoiHotel.setCheckinTime(rs.getString("checkin_time"));
				ixPoiHotel.setCheckoutTime(rs.getString("checkout_time"));
				ixPoiHotel.setRoomCount(rs.getInt("room_count"));
				ixPoiHotel.setRoomType(rs.getString("room_type"));
				ixPoiHotel.setRoomPrice(rs.getString("room_price"));
				ixPoiHotel.setBreakfast(rs.getInt("breakfast"));
				ixPoiHotel.setService(rs.getString("service"));
				ixPoiHotel.setParking(rs.getInt("parking"));
				ixPoiHotel.setLongDescription(rs.getString("long_description"));
				ixPoiHotel.setOpenHour(rs.getString("open_hour"));
				ixPoiHotel.setRowId(rs.getString("row_id"));
				
				if (hotelMap.containsKey(rs.getLong("poi_pid"))) {
					hotelList = hotelMap.get(rs.getLong("poi_pid"));
					hotelList.add(ixPoiHotel);
					hotelMap.put(rs.getLong("poi_pid"), hotelList);
				} else {
					hotelList.add(ixPoiHotel);
					hotelMap.put(rs.getLong("poi_pid"), hotelList);
				}
			}
			return hotelMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
