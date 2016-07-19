package com.navinfo.dataservice.dao.glm.operator.rd.node;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeName;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.vividsolutions.jts.geom.Geometry;

public class RdNodeOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdNodeOperator.class);

	private RdNode node;

	public RdNodeOperator(Connection conn, RdNode node) {
		super(conn);

		this.node = node;
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
	public void updateRow2Sql(Statement stmt) throws Exception {

		StringBuilder sb = new StringBuilder("update " + node.tableName()
				+ " set u_record=3,");

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

					if (columnValue == null) {
						sb.append(column + "=null,");
					} else {
						sb.append(column + "='" + String.valueOf(columnValue)
								+ "',");
					}
					this.setChanged(true);
				}

			} else if (value instanceof Double) {

				if (Double.parseDouble(String.valueOf(value)) != Double
						.parseDouble(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Double.parseDouble(String.valueOf(columnValue))
							+ ",");

					this.setChanged(true);
				}

			} else if (value instanceof Integer) {

				if (Integer.parseInt(String.valueOf(value)) != Integer
						.parseInt(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Integer.parseInt(String.valueOf(columnValue))
							+ ",");

					this.setChanged(true);
				}

			} else if (value instanceof Geometry) {
				// 先降级转WKT

				String oldWkt = GeoTranslator.jts2Wkt((Geometry) value,
						0.00001, 5);

				String newWkt = Geojson.geojson2Wkt(columnValue.toString());

				if (!StringUtils.isStringSame(oldWkt, newWkt)) {
					sb.append("geometry=sdo_geometry('"
							+ String.valueOf(newWkt) + "',8307),");

					this.setChanged(true);
				}
			}
		}
		sb.append(" where node_pid=" + node.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

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
