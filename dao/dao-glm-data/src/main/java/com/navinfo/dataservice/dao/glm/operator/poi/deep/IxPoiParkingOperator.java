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
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiIntroduction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI 深度信息(停车场类) 操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiParkingOperator implements IOperator {

	private static Logger logger = Logger.getLogger(IxPoiParkingOperator.class);

	private Connection conn;
	private IxPoiParking ixPoiParking;

	public IxPoiParkingOperator(Connection conn, IxPoiParking ixPoiParking) {
		this.conn = conn;
		this.ixPoiParking = ixPoiParking;
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
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiParking.tableName() + " set u_record=3,u_date='"
				+ StringUtils.getCurrentTime() + "',");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ixPoiParking.changedFields()
					.entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			boolean isChanged = false;

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoiParking.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(ixPoiParking);

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
			sb.append(" where parking_id   =" + ixPoiParking.getPid());

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
		ixPoiParking.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiParking.tableName());
		sb.append("(parking_id, poi_pid, parking_type, toll_std, toll_des, toll_way, payment, remark, source, open_tiime, total_num, work_time, res_high, res_width, res_weigh, certificate, vehicle, photo_name, have_specialplace, women_num,  handicap_num, mini_num, vip_num, u_date,u_record, row_id) values (");
		sb.append(ixPoiParking.getPid());
		sb.append("," + ixPoiParking.getPoiPid());
		if (ixPoiParking.getParkingType() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiParking.getParkingType() + "'");
		}
		if (ixPoiParking.getTollStd() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiParking.getTollStd() + "'");
		}
		if (ixPoiParking.getTollStd() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiParking.getTollDes() + "'");
		}
		if (ixPoiParking.getTollWay() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiParking.getTollWay() + "'");
		}
		if (ixPoiParking.getPayment() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiParking.getPayment() + "'");
		}
		if (ixPoiParking.getRemark() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiParking.getRemark() + "'");
		}
		if (ixPoiParking.getSource() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiParking.getSource() + "'");
		}
		if (ixPoiParking.getOpenTiime() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiParking.getOpenTiime() + "'");
		}
		sb.append("," + ixPoiParking.getTotalNum());
		if (ixPoiParking.getWorkTime() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiParking.getWorkTime() + "'");
		}
		sb.append("," + ixPoiParking.getResHigh());
		sb.append("," + ixPoiParking.getResWidth());
		sb.append("," + ixPoiParking.getResWeigh());
		sb.append("," + ixPoiParking.getCertificate());
		sb.append("," + ixPoiParking.getVehicle());
		if (ixPoiParking.getPhotoName() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiParking.getPhotoName() + "'");
		}
		if (ixPoiParking.getHaveSpecialplace() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + ixPoiParking.getHaveSpecialplace() + "'");
		}
		sb.append("," + ixPoiParking.getWomenNum());
		sb.append("," + ixPoiParking.getHandicapNum());

		sb.append("," + ixPoiParking.getMiniNum());
		sb.append("," + ixPoiParking.getVipNum());
		sb.append(",'" + StringUtils.getCurrentTime() + "'");
		sb.append(",1,'" + ixPoiParking.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiParking.tableName()
				+ " set u_record=2 ,u_date='" + StringUtils.getCurrentTime()
				+ "' where parking_id   =" + ixPoiParking.getPid();
		stmt.addBatch(sql);
	}

}
