package com.navinfo.dataservice.dao.glm.operator.rd.branch;

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
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboard;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboardName;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdSignboardOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdSignboardOperator.class);

	private RdSignboard signboard;

	public RdSignboardOperator(Connection conn, RdSignboard signboard) {
		super(conn);

		this.signboard = signboard;
	}
	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		signboard.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(signboard.tableName());

		sb.append("(signboard_id, branch_pid, arrow_code, backimage_code, u_record, row_id) values (");

		sb.append(signboard.getPid());

		sb.append("," + signboard.getBranchPid());

		if (signboard.getArrowCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + signboard.getArrowCode() + "'");
		}

		if (signboard.getBackimageCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + signboard.getBackimageCode() + "'");
		}

		sb.append(",1,'" + signboard.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : signboard.getNames()) {
			RdSignboardNameOperator op = new RdSignboardNameOperator(conn,
					(RdSignboardName) r);

			op.insertRow2Sql(stmt);
		}

	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + signboard.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = signboard.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = signboard.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(signboard);

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
		sb.append(" where signboard_id=" + signboard.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + signboard.tableName()
				+ " set u_record=2 where signboard_id=" + signboard.getPid();

		stmt.addBatch(sql);

		for (IRow r : signboard.getNames()) {
			RdSignboardNameOperator op = new RdSignboardNameOperator(conn,
					(RdSignboardName) r);

			op.deleteRow2Sql(stmt);
		}
	}

}
