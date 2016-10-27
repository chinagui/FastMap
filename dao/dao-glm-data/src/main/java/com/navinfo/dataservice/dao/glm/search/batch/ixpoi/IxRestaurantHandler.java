package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;

public class IxRestaurantHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> restaurantMap = new HashMap<Long, List<IRow>>();
		try {
			while (rs.next()){
				List<IRow> restaurantList = new ArrayList<IRow>();
				IxPoiRestaurant ixPoiRestaurant= new IxPoiRestaurant();
				ixPoiRestaurant.setFoodType(rs.getString("food_type"));
				ixPoiRestaurant.setCreditCard(rs.getString("credit_card"));
				ixPoiRestaurant.setAvgCost(rs.getInt("avg_cost"));
				ixPoiRestaurant.setParking(rs.getInt("parking"));
				ixPoiRestaurant.setOpenHour(rs.getString("open_hour"));
				ixPoiRestaurant.setRowId(rs.getString("row_id"));
				
				if (restaurantMap.containsKey(rs.getLong("poi_pid"))) {
					restaurantList = restaurantMap.get(rs.getLong("poi_pid"));
					restaurantList.add(ixPoiRestaurant);
					restaurantMap.put(rs.getLong("poi_pid"), restaurantList);
				} else {
					restaurantList.add(ixPoiRestaurant);
					restaurantMap.put(rs.getLong("poi_pid"), restaurantList);
				}
			}
			return restaurantMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
