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
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePair;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @Title: RdEleceyePairOperator.java
 * @Prject: dao-glm-data
 * @Package: com.navinfo.dataservice.dao.glm.operator.rd.eleceye
 * @Description: 数据库操作(区间测速电子眼)
 * @author zhangyt
 * @date: 2016年7月20日 下午5:42:36
 * @version: v1.0
 *
 */
public class RdEleceyePairOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdEleceyePairOperator.class);

	private RdEleceyePair pair;

	public RdEleceyePairOperator(Connection conn, RdEleceyePair pair) {
		super(conn);
		this.pair = pair;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		pair.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(pair.tableName());

		sb.append("(group_id,u_record,row_id) values (");

		sb.append(pair.getPid());

		sb.append(",1,'" + pair.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + pair.tableName() + " set u_record = 2 where group_id = " + pair.getPid();

		stmt.addBatch(sql);
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + pair.tableName() + " set u_record = 3,");

		Set<Entry<String, Object>> set = pair.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = pair.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(pair);

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
		sb.append(" where pid=" + pair.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql.toString());
	}

}
