package com.navinfo.dataservice.dao.glm.operator.rd.gsc;

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
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdGscOperator extends AbstractOperator {
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);

	private RdGsc gsc;

	public RdGscOperator(Connection conn, RdGsc gsc) {
		super(conn);
		this.gsc = gsc;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		gsc.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(gsc.tableName());

		sb.append("(pid, geometry,process_flag,u_record, row_id) values (");

		sb.append(gsc.pid());

		String wkt = GeoTranslator.jts2Wkt(gsc.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		sb.append("," + gsc.getProcessFlag());

		sb.append(",1,'" + gsc.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : gsc.getLinks()) {
			RdGscLinkOperator op = new RdGscLinkOperator(conn, (RdGscLink) r);

			op.insertRow2Sql(stmt);
		}

	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + gsc.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = gsc.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = gsc.getClass().getDeclaredField(column);

			field.setAccessible(true);

			column = StringUtils.toColumnName(column);

			Object value = field.get(gsc);

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

			}
		}
		sb.append(" where pid=" + gsc.pid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + gsc.tableName() + " set u_record=2 where pid="
				+ gsc.pid();

		stmt.addBatch(sql);

		for (IRow r : gsc.getLinks()) {
			RdGscLinkOperator op = new RdGscLinkOperator(conn, (RdGscLink) r);

			op.deleteRow2Sql(stmt);
		}

	}

}
