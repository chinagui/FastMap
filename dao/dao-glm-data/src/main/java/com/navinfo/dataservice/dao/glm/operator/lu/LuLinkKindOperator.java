package com.navinfo.dataservice.dao.glm.operator.lu;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class LuLinkKindOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(LuLinkKindOperator.class);

	private LuLinkKind luLinkKind;

	public LuLinkKindOperator(Connection conn, LuLinkKind luLinkKind) {
		super(conn);

		this.luLinkKind = luLinkKind;
	}




	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		luLinkKind.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(luLinkKind.tableName());

		sb.append("(link_pid, kind, u_record, row_id) values (");

		sb.append(luLinkKind.getLinkPid());

		sb.append("," + luLinkKind.getKind());

		sb.append(",1,'" + luLinkKind.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + luLinkKind.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = luLinkKind.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = luLinkKind.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(luLinkKind);

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
		sb.append(" where row_id=hextoraw('" + luLinkKind.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + luLinkKind.tableName()
				+ " set u_record=2 where row_id=hextoraw('"
				+ luLinkKind.rowId() + "')";

		stmt.addBatch(sql);
	}

}
