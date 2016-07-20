package com.navinfo.dataservice.dao.glm.operator.lu;

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
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkMesh;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.vividsolutions.jts.geom.Geometry;

public class LuLinkOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(LuLinkOperator.class);

	private LuLink luLink;

	public LuLinkOperator(Connection conn, LuLink luLink) {
		super(conn);

		this.luLink = luLink;
	}


	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		luLink.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(luLink.tableName());

		sb.append("(link_pid, s_node_pid, e_node_pid, geometry, length, edit_flag, u_record, row_id) values (");

		sb.append(luLink.getPid());

		sb.append("," + luLink.getsNodePid());

		sb.append("," + luLink.geteNodePid());

		String wkt = GeoTranslator.jts2Wkt(luLink.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + luLink.getLength());

		sb.append("," + luLink.getEditFlag());

		sb.append(",1,'" + luLink.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : luLink.getMeshes()) {
			LuLinkMeshOperator ap = new LuLinkMeshOperator(conn, (LuLinkMesh) r);

			ap.insertRow2Sql(stmt);
		}

		for (IRow r : luLink.getLinkKinds()) {
			LuLinkKindOperator ap = new LuLinkKindOperator(conn, (LuLinkKind) r);

			ap.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + luLink.tableName()
				+ " set u_record=3,");

			Set<Entry<String, Object>> set = luLink.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();



			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = luLink.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(luLink);

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
						this.setChanged(true);
					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double
							.parseDouble(String.valueOf(columnValue))) {
						sb.append(column
								+ "="
								+ Double.parseDouble(String
										.valueOf(columnValue)) + ",");

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
			sb.append(" where link_pid=" + luLink.getPid());

			String sql = sb.toString();

			sql = sql.replace(", where", " where");
			stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + luLink.tableName()
				+ " set u_record=2 where link_pid=" + luLink.getPid();

		stmt.addBatch(sql);

		for (IRow r : luLink.getMeshes()) {
			LuLinkMeshOperator ap = new LuLinkMeshOperator(conn, (LuLinkMesh) r);

			ap.deleteRow2Sql(stmt);
		}

		for (IRow r : luLink.getLinkKinds()) {
			LuLinkKindOperator ap = new LuLinkKindOperator(conn, (LuLinkKind) r);

			ap.insertRow2Sql(stmt);
		}
	}

}
