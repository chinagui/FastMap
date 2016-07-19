package com.navinfo.dataservice.dao.glm.operator.ad.zone;

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
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkMesh;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

public class ZoneLinkOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private ZoneLink zoneLink;

	public ZoneLinkOperator(Connection conn, ZoneLink zoneLink) {
		super(conn);
		this.zoneLink = zoneLink;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		zoneLink.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(zoneLink.tableName());

		sb.append("(link_pid, s_node_pid, e_node_pid, geometry, "
				+ "length,scale,edit_flag, u_record, row_id) values (");

		sb.append(zoneLink.getPid());

		sb.append("," + zoneLink.getsNodePid());

		sb.append("," + zoneLink.geteNodePid());

		String wkt = GeoTranslator.jts2Wkt(zoneLink.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + zoneLink.getLength());

		sb.append("," + zoneLink.getScale());

		sb.append("," + zoneLink.getEditFlag());

		sb.append(",1,'" + zoneLink.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : zoneLink.getMeshes()) {
			ZoneLinkMeshOperator ap = new ZoneLinkMeshOperator(conn,
					(ZoneLinkMesh) r);
			ap.insertRow2Sql(stmt);
		}
		for (IRow r : zoneLink.getKinds()) {
			ZoneLinkKindOperator ap = new ZoneLinkKindOperator(conn,
					(ZoneLinkKind) r);
			ap.insertRow2Sql(stmt);
		}

	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + zoneLink.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = zoneLink.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = zoneLink.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(zoneLink);

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
		sb.append(" where link_pid=" + zoneLink.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + zoneLink.tableName()
				+ " set u_record=2 where link_pid=" + zoneLink.getPid();

		stmt.addBatch(sql);

		for (IRow r : zoneLink.getMeshes()) {
			ZoneLinkMeshOperator ap = new ZoneLinkMeshOperator(conn,
					(ZoneLinkMesh) r);

			ap.deleteRow2Sql(stmt);
		}

	}

}
