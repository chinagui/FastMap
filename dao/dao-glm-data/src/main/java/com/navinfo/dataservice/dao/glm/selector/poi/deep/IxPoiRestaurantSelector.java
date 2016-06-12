package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
/**
 * 索引:POI 深度信息(餐饮类)查询接口
 * @author zhaokk
 *
 */
public class IxPoiRestaurantSelector implements ISelector {
	private Connection conn;

	public IxPoiRestaurantSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiRestaurant ixPoiRestaurant = new IxPoiRestaurant();

		StringBuilder sb = new StringBuilder(
				"select * from " + ixPoiRestaurant.tableName() + " WHERE  restaurant_id= :1 and  u_record !=2");

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
				this.setAttr(ixPoiRestaurant, resultSet);
				return ixPoiRestaurant;
			} else {
				throw new Exception("对应"+ixPoiRestaurant.tableName()+"数据不存在不存在!");
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
				"select * from ix_poi_restaurant WHERE restaurant_id  = :1 and  u_record !=2");

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
				IxPoiRestaurant ixPoiRestaurant= new IxPoiRestaurant();
				this.setAttr(ixPoiRestaurant, resultSet);
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
	private void setAttr(IxPoiRestaurant ixPoiRestaurant,ResultSet resultSet) throws SQLException{
		ixPoiRestaurant.setPid(resultSet.getInt("restaurant_id"));
		ixPoiRestaurant.setPoiPid(resultSet.getInt("poi_pid"));
		ixPoiRestaurant.setFoodType(resultSet.getString("food_type"));
		ixPoiRestaurant.setCreditCard(resultSet.getString("credit_card"));
		ixPoiRestaurant.setAvgCost(resultSet.getInt("avg_cost"));
		ixPoiRestaurant.setParking(resultSet.getInt("parking"));
		ixPoiRestaurant.setLongDescription(resultSet.getString("long_description"));
		ixPoiRestaurant.setLongDescripEng(resultSet.getString("long_descrip_eng"));
		ixPoiRestaurant.setOpenHour(resultSet.getString("open_hour"));
		ixPoiRestaurant.setOpenHourEng(resultSet.getString("open_hour_eng"));
		ixPoiRestaurant.setTelephone(resultSet.getString("telephone"));
		ixPoiRestaurant.setAddress(resultSet.getString("address"));
		ixPoiRestaurant.setCity(resultSet.getString("city"));
		ixPoiRestaurant.setPhotoName(resultSet.getString("photo_name"));
		ixPoiRestaurant.setTravelguideFlag(resultSet.getInt("travelguide_flag"));
		ixPoiRestaurant.setRowId(resultSet.getString("row_id"));

	}
	
}
