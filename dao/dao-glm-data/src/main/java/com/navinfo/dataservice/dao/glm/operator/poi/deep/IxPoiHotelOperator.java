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
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI 深度信息(住宿酒店类)操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiHotelOperator implements IOperator {

	private static Logger logger = Logger.getLogger(IxPoiHotelOperator.class);

	private Connection conn;
	private IxPoiHotel ixPoiHotel;

	public IxPoiHotelOperator(Connection conn, IxPoiHotel ixPoiHotel) {
		this.conn = conn;
		this.ixPoiHotel = ixPoiHotel;
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
		StringBuilder sb = new StringBuilder("update " + ixPoiHotel.tableName()
				+ " set u_record=3,u_date=" + StringUtils.getCurrentTime()
				+ ",");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ixPoiHotel.changedFields()
					.entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			boolean isChanged = false;

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoiHotel.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(ixPoiHotel);

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
			sb.append(" where hotel_id   =" + ixPoiHotel.getPid());

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
		ixPoiHotel.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiHotel.tableName());
		sb.append("(hotel_id, poi_pid, credit_card, rating, checkin_time, checkout_time, room_count, room_type, room_price, breakfast, service, parking, long_description, long_descrip_eng, open_hour, open_hour_eng, telephone, address, city, photo_name, travelguide_flag, u_date,u_record, row_id) values (");
		sb.append(ixPoiHotel.getPid());
		sb.append(",'" + ixPoiHotel.getPoiPid() + "'");
		sb.append(",'" + ixPoiHotel.getCreditCard() + "'");
		sb.append("," + ixPoiHotel.getRating());
		sb.append(",'" + ixPoiHotel.getCheckinTime() + "'");
		sb.append(",'" + ixPoiHotel.getCheckoutTime() + "'");
		sb.append("," + ixPoiHotel.getRoomCount());
		sb.append(",'" + ixPoiHotel.getRoomType() + "'");
		sb.append(",'" + ixPoiHotel.getRoomPrice() + "'");
		sb.append("," + ixPoiHotel.getBreakfast());
		sb.append(",'" + ixPoiHotel.getService() + "'");
		sb.append("," + ixPoiHotel.getParking());
		sb.append(",'" + ixPoiHotel.getLongDescription() + "'");
		sb.append(",'" + ixPoiHotel.getLongDescripEng() + "'");
		sb.append(",'" + ixPoiHotel.getOpenHour() + "'");
		sb.append(",'" + ixPoiHotel.getOpenHourEng() + "'");
		sb.append(",'" + ixPoiHotel.getTelephone() + "'");
		sb.append(",'" + ixPoiHotel.getAddress() + "'");
		sb.append(",'" + ixPoiHotel.getCity() + "'");
		sb.append(",'" + ixPoiHotel.getPhotoName() + "'");
		sb.append("," + ixPoiHotel.getTravelguideFlag());
		sb.append(",'" + StringUtils.getCurrentTime()+"'");
		sb.append(",1,'" + ixPoiHotel.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiHotel.tableName()
				+ " set u_record=2 ,u_date="+StringUtils.getCurrentTime()+" where   hotel_id     ="
				+ ixPoiHotel.getPid();
		stmt.addBatch(sql);
	}

}
