package com.navinfo.dataservice.dao.glm.operator.rd.restrict;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdRestrictionOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(RdRestrictionOperator.class);

	private RdRestriction restrict;

	public RdRestrictionOperator(Connection conn, RdRestriction restrict) {
		super(conn);

		this.restrict = restrict;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		restrict.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(restrict.tableName());

		sb.append("(pid, in_link_pid, node_pid, restric_info, kg_flag, u_record, row_id) values (");

		sb.append(restrict.getPid());

		sb.append("," + restrict.getInLinkPid());

		sb.append("," + restrict.getNodePid());

		if (restrict.getRestricInfo() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + restrict.getRestricInfo() + "'");
		}

		sb.append("," + restrict.getKgFlag());

		sb.append(",1,'" + restrict.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : restrict.getDetails()) {
			RdRestrictionDetailOperator op = new RdRestrictionDetailOperator(
					conn, (RdRestrictionDetail) r);

			op.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + restrict.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = restrict.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = restrict.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(restrict);

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
		sb.append(" where pid=" + restrict.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + restrict.tableName()
				+ " set u_record=2 where pid=" + restrict.getPid();

		stmt.addBatch(sql);

		for (IRow r : restrict.getDetails()) {
			RdRestrictionDetailOperator op = new RdRestrictionDetailOperator(
					conn, (RdRestrictionDetail) r);

			op.deleteRow2Sql(stmt);
		}
	}

}
