package com.navinfo.dataservice.dao.glm.operator.ad.geo;

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
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

public class AdFaceOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private Connection conn;

	private AdFace adFace;

	public AdFaceOperator(Connection conn, AdFace adFace) {
		this.conn = conn;

		this.adFace = adFace;
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
		StringBuilder sb = new StringBuilder("update " + adFace.tableName() + " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = adFace.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			boolean isChanged = false;

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = adFace.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(adFace);

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
			sb.append(" where face_pid=" + adFace.getPid());

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
		adFace.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adFace.tableName());

		sb.append(
				"(face_pid, region_id, geometry, area, perimeter, mesh_id, " + "edit_flag, u_record, row_id) values (");

		sb.append(adFace.getPid());

		sb.append("," + adFace.getRegionId());

		String wkt = GeoTranslator.jts2Wkt(adFace.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + adFace.getArea());

		sb.append("," + adFace.getPerimeter());

		sb.append("," + adFace.getMeshId());

		sb.append("," + adFace.getEditFlag());

		sb.append(",1,'" + adFace.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : adFace.getFaceTopos()) {
			AdFaceTopoOperator ap = new AdFaceTopoOperator(conn, (AdFaceTopo) r);

			ap.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt) throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adFace.tableName() + " set u_record=2 where face_pid=" + adFace.getPid();

		stmt.addBatch(sql);

		for (IRow r : adFace.getFaceTopos()) {
			AdFaceTopoOperator ap = new AdFaceTopoOperator(conn, (AdFaceTopo) r);

			ap.deleteRow2Sql(stmt);
		}
	}

}
