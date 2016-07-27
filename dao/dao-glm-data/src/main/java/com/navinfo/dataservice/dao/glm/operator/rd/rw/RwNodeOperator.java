package com.navinfo.dataservice.dao.glm.operator.rd.rw;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNodeMesh;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 铁路点操作类
 * 
 * @author zhangxiaolong
 * 
 */
public class RwNodeOperator extends AbstractOperator {

	private RwNode rwNode;

	public RwNodeOperator(Connection conn, RwNode rwNode) {
		super(conn);

		this.rwNode = rwNode;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		rwNode.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(rwNode.tableName());

		sb.append("(NODE_PID, KIND, FORM, GEOMETRY, EDIT_FLAG, U_RECORD, ROW_ID) values (");

		sb.append(rwNode.getPid());

		sb.append("," + rwNode.getKind());

		sb.append("," + rwNode.getForm());

		String wkt = GeoTranslator.jts2Wkt(rwNode.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + rwNode.getEditFlag());

		sb.append(",1,'" + rwNode.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : rwNode.getMeshes()) {
			RwNodeMeshOperator op = new RwNodeMeshOperator(conn, (RwNodeMesh) r);

			op.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + rwNode.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = rwNode.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = rwNode.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(rwNode);

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
		sb.append(" where node_pid=" + rwNode.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + rwNode.tableName()
				+ " set u_record=2 where node_pid=" + rwNode.getPid();

		stmt.addBatch(sql);

		for (IRow r : rwNode.getMeshes()) {
			RwNodeMeshOperator op = new RwNodeMeshOperator(conn, (RwNodeMesh) r);

			op.deleteRow2Sql(stmt);
		}
	}

}
