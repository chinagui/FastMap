package com.navinfo.dataservice.dao.glm.operator.rd.rw;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLinkName;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 铁路线link操作类
 * @author zhangxiaolong
 *
 */
public class RwLinkOperator implements IOperator {

	private Connection conn;

	private RwLink rwLink;

	public RwLinkOperator(Connection conn, RwLink rwLink) {
		this.conn = conn;

		this.rwLink = rwLink;
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
		StringBuilder sb = new StringBuilder("update " + rwLink.tableName() + " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = rwLink.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			boolean isChanged = false;

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = rwLink.getClass().getDeclaredField(column);

				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(rwLink);

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
			sb.append(" where link_pid=" + rwLink.getPid());

			String sql = sb.toString();

			sql = sql.replace(", where", " where");

			if (isChanged) {
				pstmt = conn.prepareStatement(sql);
				pstmt.executeUpdate();
			}

		} catch (Exception e) {

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
		rwLink.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(rwLink.tableName());

		sb.append(
				"(LINK_PID, FEATURE_PID, S_NODE_PID, E_NODE_PID, KIND, FORM, LENGTH, GEOMETRY, MESH_ID, SCALE, DETAIL_FLAG, EDIT_FLAG, COLOR, U_RECORD, ROW_ID) values (");

		sb.append(rwLink.getPid());

		sb.append("," + rwLink.getFeaturePid());

		sb.append("," + rwLink.getsNodePid());

		sb.append("," + rwLink.geteNodePid());

		sb.append("," + rwLink.getKind());

		sb.append("," + rwLink.getForm());

		sb.append("," + rwLink.getLength());

		String wkt = GeoTranslator.jts2Wkt(rwLink.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + rwLink.getMeshId());

		sb.append("," + rwLink.getScale());

		sb.append("," + rwLink.getDetailFlag());

		sb.append("," + rwLink.getEditFlag());
		
		if(StringUtils.isNotEmpty(rwLink.getColor()))
		{
			sb.append(",'" + rwLink.getColor()+"'");
		}
		else
		{
			sb.append(",null");
		}

		sb.append(",1,'" + rwLink.rowId() + "')");

		stmt.addBatch(sb.toString());

		// 新增rw_link_name
		for (IRow r : rwLink.getNames()) {
			RwLinkNameOperator op = new RwLinkNameOperator(conn, (RwLinkName) r);

			op.insertRow2Sql(stmt);
		}

		// 新增rw_node
		for (IRow r : rwLink.getNodes()) {
			RwNodeOperator op = new RwNodeOperator(conn, (RwNode) r);

			op.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt) throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update rw_link set u_record=2 where link_pid = " + rwLink.getPid();

		stmt.addBatch(sql);

		for (IRow r : rwLink.getNodes()) {
			RwNodeOperator op = new RwNodeOperator(conn, (RwNode) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : rwLink.getNames()) {
			RwLinkNameOperator op = new RwLinkNameOperator(conn, (RwLinkName) r);

			op.deleteRow2Sql(stmt);
		}
	}

}
