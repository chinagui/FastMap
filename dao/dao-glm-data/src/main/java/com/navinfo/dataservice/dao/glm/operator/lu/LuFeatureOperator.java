package com.navinfo.dataservice.dao.glm.operator.lu;

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
import com.navinfo.dataservice.dao.glm.model.lu.LuFeature;

import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.vividsolutions.jts.geom.Geometry;

public class LuFeatureOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(LuFeatureOperator.class);

	private LuFeature luFeature;

	public LuFeatureOperator(Connection conn, LuFeature luFeature) {
		super(conn);

		this.luFeature = luFeature;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		luFeature.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(luFeature.tableName());

		sb.append("(feature_pid, u_record, row_id) values (");

		sb.append(luFeature.getPid());

		sb.append(",1,'" + luFeature.rowId() + "')");

		stmt.addBatch(sb.toString());

	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + luFeature.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = luFeature.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = luFeature.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(luFeature);

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
		sb.append(" where feature_pid =" + luFeature.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + luFeature.tableName()
				+ " set u_record=2 where feature_pid =" + luFeature.getPid();

		stmt.addBatch(sql);

	}

}
