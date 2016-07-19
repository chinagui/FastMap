package com.navinfo.dataservice.dao.glm.operator.ad.zone;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class ZoneLinkKindOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(ZoneLinkKindOperator.class);

	private ZoneLinkKind zoneLinkKind;

	public ZoneLinkKindOperator(Connection conn, ZoneLinkKind zoneLinkKind) {
		super(conn);

		this.zoneLinkKind = zoneLinkKind;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		zoneLinkKind.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(zoneLinkKind.tableName());

		sb.append("(link_pid, kind,form, u_record, row_id) values (");

		sb.append(zoneLinkKind.getLinkPid());

		sb.append("," + zoneLinkKind.getKind());
		sb.append("," + zoneLinkKind.getForm());

		sb.append(",1,'" + zoneLinkKind.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ zoneLinkKind.tableName() + " set u_record=3,");

		Set<Entry<String, Object>> set = zoneLinkKind.changedFields()
				.entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = zoneLinkKind.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(zoneLinkKind);

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

			}
		}
		sb.append(" where row_id=hextoraw('" + zoneLinkKind.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + zoneLinkKind.tableName()
				+ " set u_record=2 where row_id=hextoraw('"
				+ zoneLinkKind.rowId() + "')";

		stmt.addBatch(sql);
	}

}
