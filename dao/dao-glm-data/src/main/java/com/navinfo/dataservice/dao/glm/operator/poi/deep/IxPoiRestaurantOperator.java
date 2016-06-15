package com.navinfo.dataservice.dao.glm.operator.poi.deep;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAdvertisement;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAttraction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiIntroduction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI 深度信息(餐饮类)操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiRestaurantOperator implements IOperator {

	private static Logger logger = Logger
			.getLogger(IxPoiRestaurantOperator.class);

	private Connection conn;
	private IxPoiRestaurant ixPoiRestaurant;

	public IxPoiRestaurantOperator(Connection conn, IxPoiRestaurant ixPoiRestaurant) {
		this.conn = conn;
		this.ixPoiRestaurant = ixPoiRestaurant;
	}

	@Override
	public void insertRow() throws Exception {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			this.insertRow2Sql(stmt);

			stmt.executeBatch();

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {

			}

		}

	}

	@Override
	public void updateRow() throws Exception {
		StringBuilder sb = new StringBuilder("update " + ixPoiRestaurant.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ixPoiRestaurant.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			boolean isChanged = false;

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoiRestaurant.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(ixPoiRestaurant);

				column = StringUtils.toColumnName(column);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value),
							String.valueOf(columnValue))) {

						if (columnValue == null) {
							sb.append(column + "=null,");
						} else {
							sb.append(column + "='"
									+ String.valueOf(columnValue) + "',");
						}
						isChanged = true;
					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double
							.parseDouble(String.valueOf(columnValue))) {
						sb.append(column
								+ "="
								+ Double.parseDouble(String
										.valueOf(columnValue)) + ",");

						isChanged = true;
					}

				} else if (value instanceof Integer) {

					if (Integer.parseInt(String.valueOf(value)) != Integer
							.parseInt(String.valueOf(columnValue))) {
						sb.append(column + "="
								+ Integer.parseInt(String.valueOf(columnValue))
								+ ",");

						isChanged = true;
					}

				} 
			}
			sb.append(" where restaurant_id    =" + ixPoiRestaurant.getPid());

			String sql = sb.toString();

			sql = sql.replace(", where", " where");

			if (isChanged) {

				pstmt = conn.prepareStatement(sql);

				pstmt.executeUpdate();

			}

		} catch (Exception e) {
			logger.debug("");
			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

	}

	@Override
	public void deleteRow() throws Exception {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			this.deleteRow2Sql(stmt);

			stmt.executeBatch();

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {

			}
		}

	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiRestaurant.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiRestaurant.tableName());
		sb.append("(restaurant_id, poi_pid, food_type, credit_card, avg_cost, parking, long_description, long_descrip_eng, open_hour, open_hour_eng, telephone, address, city, photo_name, travelguide_flag, u_record, row_id) values (");
		sb.append(ixPoiRestaurant.getPid());
		sb.append("," + ixPoiRestaurant.getPoiPid());
		sb.append(",'" + ixPoiRestaurant.getCreditCard()+"'");
		sb.append(",'" + ixPoiRestaurant.getFoodType()+"'");
		sb.append(",'" + ixPoiRestaurant.getCreditCard()+"'");
		sb.append("," + ixPoiRestaurant.getAvgCost());
		sb.append("," + ixPoiRestaurant.getParking());
		sb.append(",'" + ixPoiRestaurant.getLongDescription()+"'");
		sb.append(",'" + ixPoiRestaurant.getLongDescripEng()+"'");
		sb.append(",'" + ixPoiRestaurant.getOpenHour()+"'");
		sb.append(",'" + ixPoiRestaurant.getOpenHourEng()+"'");
		sb.append(",'" + ixPoiRestaurant.getTelephone()+"'");
		sb.append(",'" + ixPoiRestaurant.getAddress()+"'");
		sb.append(",'" + ixPoiRestaurant.getCity()+"'");
		sb.append(",'" + ixPoiRestaurant.getPhotoName()+"'");
		sb.append("," + ixPoiRestaurant.getTravelguideFlag());
		sb.append(",1,'" + ixPoiRestaurant.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiRestaurant.tableName() + " set u_record=2 where   restaurant_id      =" + ixPoiRestaurant.getPid();
		stmt.addBatch(sql);
	}

}
