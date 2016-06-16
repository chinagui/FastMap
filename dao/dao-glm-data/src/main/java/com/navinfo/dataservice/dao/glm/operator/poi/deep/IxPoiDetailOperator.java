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
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI 通用深度信息表 操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiDetailOperator implements IOperator {

	private static Logger logger = Logger
			.getLogger(IxPoiDetailOperator.class);

	private Connection conn;
	private IxPoiDetail ixPoiDetail;

	public IxPoiDetailOperator(Connection conn, IxPoiDetail ixPoiDetail) {
		this.conn = conn;
		this.ixPoiDetail = ixPoiDetail;
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
		StringBuilder sb = new StringBuilder("update " + ixPoiDetail.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ixPoiDetail.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			boolean isChanged = false;

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoiDetail.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(ixPoiDetail);

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
			sb.append(" where row_id=hextoraw('" + ixPoiDetail.getRowId() + "')");

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
		ixPoiDetail.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiDetail.tableName());
		sb.append("(poi_pid, web_site, fax, star_hotel, brief_desc, adver_flag, photo_name, reserved, memo, hw_entryexit, paycard, cardtype, hospital_class, u_record, row_id) values (");
		sb.append(ixPoiDetail.getPoiPid());
		sb.append(",'" + ixPoiDetail.getWebsite()+"'");
		sb.append(",'" + ixPoiDetail.getFax()+"'");
		sb.append(",'"  + ixPoiDetail.getStarHotel()+"'");
		sb.append(",'"  + ixPoiDetail.getBriefDesc()+"'");
		sb.append(","  + ixPoiDetail.getAdverFlag());
		sb.append(",'"  + ixPoiDetail.getPhotoName()+"'");
		sb.append(",'"  + ixPoiDetail.getReserved()+"'");
		sb.append(",'"  + ixPoiDetail.getMemo()+"'");
		sb.append(","  + ixPoiDetail.getHwEntryExit());
		sb.append(","  + ixPoiDetail.getPayCard());
		sb.append(",'"  + ixPoiDetail.getCardType()+"'");
		sb.append(","  + ixPoiDetail.getHospitalClass());
		sb.append(",1,'" + ixPoiDetail.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		
		String sql = "update " + ixPoiDetail.tableName() + " set u_record=2 where row_id=hextoraw('" + ixPoiDetail.rowId()
				+ "')";
		stmt.addBatch(sql);
	}

}
