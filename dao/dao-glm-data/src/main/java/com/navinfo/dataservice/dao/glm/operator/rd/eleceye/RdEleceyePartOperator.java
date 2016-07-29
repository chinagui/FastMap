package com.navinfo.dataservice.dao.glm.operator.rd.eleceye;

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
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePart;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @Title: RdEleceyePartOperator.java
 * @Prject: dao-glm-data
 * @Package: com.navinfo.dataservice.dao.glm.operator.rd.eleceye
 * @Description: 数据库操作(区间测速电子眼组成)
 * @author zhangyt
 * @date: 2016年7月20日 下午5:44:10
 * @version: v1.0
 *
 */
public class RdEleceyePartOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdEleceyePartOperator.class);

	private RdEleceyePart part;

	public RdEleceyePartOperator(Connection conn, RdEleceyePart part) {
		super(conn);
		this.part = part;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		part.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(part.tableName());

		sb.append("(group_id,eleceye_pid,u_record,row_id) values (");

		sb.append(part.getGroupId());

		sb.append("," + part.getEleceyePid());

		sb.append(",1,'" + part.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + part.tableName() + " set u_record = 2 where row_id = '" + part.rowId() + "'";

		stmt.addBatch(sql);
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + part.tableName() + " set u_record = 3,");

		Set<Entry<String, Object>> set = part.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = part.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(part);

			column = StringUtils.toColumnName(column);

			if (value instanceof String || value == null) {

				if (!StringUtils.isStringSame(String.valueOf(value), String.valueOf(columnValue))) {

					if (columnValue == null) {
						sb.append(column + "=null,");
					} else {
						sb.append(column + "='" + String.valueOf(columnValue) + "',");
					}

				}

			} else if (value instanceof Double) {

				if (Double.parseDouble(String.valueOf(value)) != Double.parseDouble(String.valueOf(columnValue))) {
					sb.append(column + "=" + Double.parseDouble(String.valueOf(columnValue)) + ",");
				}

			} else if (value instanceof Integer) {

				if (Integer.parseInt(String.valueOf(value)) != Integer.parseInt(String.valueOf(columnValue))) {
					sb.append(column + "=" + Integer.parseInt(String.valueOf(columnValue)) + ",");
				}

			} else if (value instanceof Geometry) {
				// 先降级转WKT

				String oldWkt = GeoTranslator.jts2Wkt((Geometry) value, 0.00001, 5);

				String newWkt = Geojson.geojson2Wkt(columnValue.toString());

				if (!StringUtils.isStringSame(oldWkt, newWkt)) {
					sb.append("geometry=sdo_geometry('" + String.valueOf(newWkt) + "',8307),");
				}
			}
		}
		sb.append(" where row_id = hextoraw('" + part.rowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql.toString());
	}

}
