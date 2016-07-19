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
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdRestrictionConditionOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(RdRestrictionConditionOperator.class);

	private RdRestrictionCondition condition;

	public RdRestrictionConditionOperator(Connection conn,
			RdRestrictionCondition condition) {
		super(conn);

		this.condition = condition;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		condition.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(condition.tableName());

		sb.append("(detail_id, time_domain, vehicle, res_trailer, res_weigh, res_axle_load, "
				+ "res_axle_count, res_out, u_record, row_id) values (");

		sb.append(condition.getDetailId());

		if (condition.getTimeDomain() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + condition.getTimeDomain() + "'");
		}

		sb.append("," + condition.getVehicle());

		sb.append("," + condition.getResTrailer());

		sb.append("," + condition.getResWeigh());

		sb.append("," + condition.getResAxleLoad());

		sb.append("," + condition.getResAxleCount());

		sb.append("," + condition.getResOut());

		sb.append(",1,'" + condition.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {

		StringBuilder sb = new StringBuilder("update " + condition.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = condition.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = condition.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(condition);

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

			} else if (value instanceof Long) {

				if (Long.parseLong(String.valueOf(value)) != Long
						.parseLong(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Long.parseLong(String.valueOf(columnValue)) + ",");
					this.setChanged(true);
				}

			}
		}
		sb.append(" where row_id=hextoraw('" + condition.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + condition.tableName()
				+ " set u_record=2 where row_id=hextoraw('" + condition.rowId()
				+ "')";

		stmt.addBatch(sql);
	}

}
