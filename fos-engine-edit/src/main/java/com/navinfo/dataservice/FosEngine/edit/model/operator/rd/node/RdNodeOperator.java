package com.navinfo.dataservice.FosEngine.edit.model.operator.rd.node;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.util.StringUtils;
import com.navinfo.dataservice.FosEngine.edit.model.IOperator;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeForm;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeMesh;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeName;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.vividsolutions.jts.geom.Geometry;

public class RdNodeOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdNodeOperator.class);

	private Connection conn;

	private RdNode node;

	public RdNodeOperator(Connection conn, RdNode node) {
		this.conn = conn;

		this.node = node;
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

		StringBuilder sb = new StringBuilder("update " + node.tableName()
				+ " set ");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = node.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = node.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				Object value = field.get(node);

				column = StringUtils.toColumnName(column);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value),
							String.valueOf(columnValue))) {
						sb.append(column + "='" + String.valueOf(columnValue)
								+ "',");
					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double
							.parseDouble(String.valueOf(columnValue))) {
						sb.append(column
								+ "="
								+ Double.parseDouble(String
										.valueOf(columnValue)) + ",");
					}

				} else if (value instanceof Integer) {

					if (Integer.parseInt(String.valueOf(value)) != Integer
							.parseInt(String.valueOf(columnValue))) {
						sb.append(column + "="
								+ Integer.parseInt(String.valueOf(columnValue))
								+ ",");
					}

				} else if (value instanceof Geometry) {
					// 先降级转WKT

					String oldWkt = GeoTranslator.jts2Wkt((Geometry) value,
							0.00001, 5);

					String newWkt = Geojson.geojson2Wkt(columnValue.toString());

					if (!StringUtils.isStringSame(oldWkt, newWkt)) {
						sb.append("geometry=sdo_geometry('"
								+ String.valueOf(newWkt) + "',8307),");
					}
				}
			}
			sb.append(" where node_pid=" + node.getPid());

			String sql = sb.toString();

			sql = sql.replace(", where", " where");

			pstmt = conn.prepareStatement(sql);

			pstmt.executeUpdate();

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

		node.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(node.tableName());

		sb.append("(node_pid, kind, geometry, adas_flag, edit_flag, dif_groupid, "
				+ "src_flag, digital_level, reserved, u_record, row_id) values (");

		sb.append(node.getPid());

		sb.append("," + node.getKind());

		String wkt = GeoTranslator.jts2Wkt(node.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + node.getAdasFlag());

		sb.append("," + node.getEditFlag());

		if (node.getDifGroupid() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + node.getDifGroupid() + "'");
		}

		sb.append("," + node.getSrcFlag());

		sb.append("," + node.getDigitalLevel());

		if (node.getReserved() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + node.getReserved() + "'");
		}

		sb.append(",1,'" + node.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : node.getNames()) {
			RdNodeNameOperator op = new RdNodeNameOperator(conn, (RdNodeName) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : node.getForms()) {
			RdNodeFormOperator op = new RdNodeFormOperator(conn, (RdNodeForm) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : node.getMeshes()) {
			RdNodeMeshOperator op = new RdNodeMeshOperator(conn, (RdNodeMesh) r);

			op.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + node.tableName()
				+ " set u_record=2 where node_pid=" + node.getPid();

		stmt.addBatch(sql);

		for (IRow r : node.getNames()) {
			RdNodeNameOperator op = new RdNodeNameOperator(conn, (RdNodeName) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : node.getForms()) {
			RdNodeFormOperator op = new RdNodeFormOperator(conn, (RdNodeForm) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : node.getMeshes()) {
			RdNodeMeshOperator op = new RdNodeMeshOperator(conn, (RdNodeMesh) r);

			op.deleteRow2Sql(stmt);
		}
	}

}
