package com.navinfo.dataservice.dao.glm.operator.ad.zone;

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
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;
/**
 * 
 * */

public class ZoneFaceOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private Connection conn;

	private ZoneFace  zoneFace;

	public ZoneFaceOperator(Connection conn,  ZoneFace  zoneFace) {
		this.conn = conn;

		this.zoneFace = zoneFace;
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
		StringBuilder sb = new StringBuilder("update " + zoneFace.tableName() + " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = zoneFace.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			boolean isChanged = false;

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = zoneFace.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(zoneFace);

				column = StringUtils.toColumnName(column);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value), String.valueOf(columnValue))) {

						if (columnValue == null) {
							sb.append(column + "=null,");
						} else {
							sb.append(column + "='" + String.valueOf(columnValue) + "',");
						}
						isChanged = true;
					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double.parseDouble(String.valueOf(columnValue))) {
						sb.append(column + "=" + Double.parseDouble(String.valueOf(columnValue)) + ",");

						isChanged = true;
					}

				} else if (value instanceof Integer) {

					if (Integer.parseInt(String.valueOf(value)) != Integer.parseInt(String.valueOf(columnValue))) {
						sb.append(column + "=" + Integer.parseInt(String.valueOf(columnValue)) + ",");

						isChanged = true;
					}

				} else if (value instanceof Geometry) {
					// 先降级转WKT

					String oldWkt = GeoTranslator.jts2Wkt((Geometry) value, 0.00001, 5);

					String newWkt = Geojson.geojson2Wkt(columnValue.toString());

					if (!StringUtils.isStringSame(oldWkt, newWkt)) {
						sb.append("geometry=sdo_geometry('" + String.valueOf(newWkt) + "',8307),");

						isChanged = true;
					}
				}
			}
			sb.append(" where face_pid=" + zoneFace.getPid());

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
		zoneFace.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(zoneFace.tableName());

		sb.append(
				"(face_pid, region_id, geometry, area, perimeter, mesh_id, " + "edit_flag, u_record, row_id) values (");

		sb.append(zoneFace.getPid());

		sb.append("," + zoneFace.getRegionId());

		String wkt = GeoTranslator.jts2Wkt(zoneFace.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + zoneFace.getArea());

		sb.append("," + zoneFace.getPerimeter());

		sb.append("," + zoneFace.getMeshId());

		sb.append("," + zoneFace.getEditFlag());

		sb.append(",1,'" + zoneFace.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : zoneFace.getFaceTopos()) {
			ZoneFaceTopoOperator ap = new ZoneFaceTopoOperator(conn, (ZoneFaceTopo) r);

			ap.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt) throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + zoneFace.tableName() + " set u_record=2 where face_pid=" + zoneFace.getPid();

		stmt.addBatch(sql);

		for (IRow r : zoneFace.getFaceTopos()) {
			ZoneFaceTopoOperator ap = new ZoneFaceTopoOperator(conn, (ZoneFaceTopo) r);

			ap.deleteRow2Sql(stmt);
		}
	}

}
