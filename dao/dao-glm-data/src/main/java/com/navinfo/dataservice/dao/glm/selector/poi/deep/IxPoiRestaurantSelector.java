package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
/**
 * 索引:POI 深度信息(餐饮类)查询接口
 * @author zhaokk
 *
 */
public class IxPoiRestaurantSelector extends AbstractSelector {
	private Connection conn;

	public IxPoiRestaurantSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoiRestaurant.class);
	}

	public List<IRow> loadByIdForAndroid(int id) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		

		StringBuilder sb = new StringBuilder(
				"select food_type,credit_card,parking,open_hour,avg_cost,row_id from ix_poi_restaurant WHERE poi_pid  = :1 and  u_record !=2");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while(resultSet.next()) {
				IxPoiRestaurant ixPoiRestaurant= new IxPoiRestaurant();
				ixPoiRestaurant.setFoodType(resultSet.getString("food_type"));
				ixPoiRestaurant.setCreditCard(resultSet.getString("credit_card"));
				ixPoiRestaurant.setAvgCost(resultSet.getInt("avg_cost"));
				ixPoiRestaurant.setParking(resultSet.getInt("parking"));
				ixPoiRestaurant.setOpenHour(resultSet.getString("open_hour"));
				ixPoiRestaurant.setRowId(resultSet.getString("row_id"));
				rows.add(ixPoiRestaurant);
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
