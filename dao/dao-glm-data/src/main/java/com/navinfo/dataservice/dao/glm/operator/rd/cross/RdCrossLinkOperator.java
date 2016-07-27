package com.navinfo.dataservice.dao.glm.operator.rd.cross;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdCrossLinkOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdCrossLinkOperator.class);

	private RdCrossLink link;

	public RdCrossLinkOperator(Connection conn, RdCrossLink link) {
		super(conn);

		this.link = link;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		link.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(link.tableName());

		sb.append("(pid, link_pid, u_record, row_id) values (");

		sb.append(link.getPid());

		sb.append("," + link.getLinkPid());

		sb.append(",1,'" + link.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + link.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = link.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = link.getClass().getDeclaredField(column);

			field.setAccessible(true);

			column = StringUtils.toColumnName(column);

			Object value = field.get(link);

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
		sb.append(" where row_id=hextoraw('" + link.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + link.tableName()
				+ " set u_record=2 where row_id=hextoraw('" + link.rowId()
				+ "')";

		stmt.addBatch(sql);
	}

}
