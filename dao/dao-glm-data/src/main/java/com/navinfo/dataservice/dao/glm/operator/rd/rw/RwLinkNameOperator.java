package com.navinfo.dataservice.dao.glm.operator.rd.rw;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLinkName;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

/**
 * 铁路线name操作类
 * 
 * @author zhangxiaolong
 * 
 */
public class RwLinkNameOperator extends AbstractOperator {

	private RwLinkName rwLinkName;

	public RwLinkNameOperator(Connection conn, RwLinkName rwLinkName) {
		super(conn);

		this.rwLinkName = rwLinkName;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		rwLinkName.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(rwLinkName.tableName());

		sb.append("(LINK_PID, NAME_GROUPID, U_RECORD, ROW_ID) values (");

		sb.append(rwLinkName.getLinkPid());

		sb.append("," + rwLinkName.getNameGroupid());

		sb.append(",1,'" + rwLinkName.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + rwLinkName.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = rwLinkName.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = rwLinkName.getClass().getDeclaredField(column);

			field.setAccessible(true);

			column = StringUtils.toColumnName(column);

			Object value = field.get(rwLinkName);

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
		sb.append(" where row_id=hextoraw('" + rwLinkName.getRowId());

		sb.append("')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + rwLinkName.tableName()
				+ " set u_record=2 where row_id=hextoraw('"
				+ rwLinkName.getRowId() + "')";

		stmt.addBatch(sql);
	}

}
