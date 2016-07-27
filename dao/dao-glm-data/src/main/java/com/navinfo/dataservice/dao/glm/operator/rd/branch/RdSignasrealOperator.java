package com.navinfo.dataservice.dao.glm.operator.rd.branch;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignasreal;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdSignasrealOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdSignasrealOperator.class);

	private RdSignasreal signasreal;

	public RdSignasrealOperator(Connection conn, RdSignasreal signasreal) {
		super(conn);

		this.signasreal = signasreal;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		signasreal.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(signasreal.tableName());

		sb.append("(signboard_id, branch_pid, svgfile_code, arrow_code, memo, u_record, row_id) values (");

		sb.append(signasreal.getPid());

		sb.append("," + signasreal.getBranchPid());

		if (signasreal.getSvgfileCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + signasreal.getSvgfileCode() + "'");
		}

		if (signasreal.getArrowCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + signasreal.getArrowCode() + "'");
		}

		if (signasreal.getMemo() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + signasreal.getMemo() + "'");
		}

		sb.append(",1,'" + signasreal.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + signasreal.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = signasreal.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = signasreal.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(signasreal);

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

			} else if (value instanceof JSONObject) {
				if (!StringUtils.isStringSame(value.toString(),
						String.valueOf(columnValue))) {
					sb.append("geometry=sdo_geometry('"
							+ String.valueOf(columnValue) + "',8307),");
					this.setChanged(true);
				}
			}
		}
		sb.append(" where signboard_id=" + signasreal.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + signasreal.tableName()
				+ " set u_record=2 where signboard_id=" + signasreal.getPid();

		stmt.addBatch(sql);
	}

}
