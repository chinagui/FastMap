package com.navinfo.dataservice.dao.glm.operator.ad.geo;

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
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

public class AdLinkOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private AdLink adLink;

	public AdLinkOperator(Connection conn, AdLink adLink) {
		super(conn);

		this.adLink = adLink;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		adLink.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adLink.tableName());

		sb.append("(link_pid, s_node_pid, e_node_pid, kind, form, geometry, "
				+ "length,scale,edit_flag, u_record, row_id) values (");

		sb.append(adLink.getPid());

		sb.append("," + adLink.getsNodePid());

		sb.append("," + adLink.geteNodePid());

		sb.append("," + adLink.getKind());

		sb.append("," + adLink.getForm());

		String wkt = GeoTranslator.jts2Wkt(adLink.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + adLink.getLength());

		sb.append("," + adLink.getScale());

		sb.append("," + adLink.getEditFlag());

		sb.append(",1,'" + adLink.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : adLink.getMeshes()) {
			AdLinkMeshOperator ap = new AdLinkMeshOperator(conn, (AdLinkMesh) r);
			ap.insertRow2Sql(stmt);
		}

	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + adLink.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = adLink.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = adLink.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(adLink);

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
		sb.append(" where link_pid=" + adLink.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adLink.tableName()
				+ " set u_record=2 where link_pid=" + adLink.getPid();

		stmt.addBatch(sql);

		for (IRow r : adLink.getMeshes()) {
			AdLinkMeshOperator ap = new AdLinkMeshOperator(conn, (AdLinkMesh) r);

			ap.deleteRow2Sql(stmt);
		}

	}

}
