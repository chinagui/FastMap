package com.navinfo.dataservice.dao.glm.operator.ad.geo;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminPart;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.gsc.RdGscLinkOperator;

public class AdAdminGroupOperator extends AbstractOperator {
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);
	private AdAdminGroup adminGroup;

	public AdAdminGroupOperator(Connection conn, AdAdminGroup adminGroup) {
		super(conn);
		this.adminGroup = adminGroup;
	}
	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		adminGroup.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adminGroup.tableName());

		sb.append("(group_id, region_id_up,u_record,row_id) values (");

		sb.append(adminGroup.pid());
		sb.append("," + adminGroup.getRegionIdUp());

		sb.append(",1,'" + adminGroup.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + adminGroup.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = adminGroup.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = adminGroup.getClass().getDeclaredField(column);

			field.setAccessible(true);

			column = StringUtils.toColumnName(column);

			Object value = field.get(adminGroup);

			if (value instanceof String || value == null) {

				if (!StringUtils.isStringSame(String.valueOf(value),
						String.valueOf(columnValue))) {

					if (columnValue == null) {
						sb.append(column + "=null,");
					} else {
						sb.append(column + "='" + String.valueOf(columnValue)
								+ "',");
					}

				}

			} else if (value instanceof Double) {

				if (Double.parseDouble(String.valueOf(value)) != Double
						.parseDouble(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Double.parseDouble(String.valueOf(columnValue))
							+ ",");
				}

			} else if (value instanceof Integer) {

				if (Integer.parseInt(String.valueOf(value)) != Integer
						.parseInt(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Integer.parseInt(String.valueOf(columnValue))
							+ ",");
				}

			}
		}
		sb.append(" where group_id =" + adminGroup.pid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adminGroup.tableName()
				+ " set u_record=2 where group_id=" + adminGroup.pid();
		stmt.addBatch(sql);
		for (IRow row : adminGroup.getParts()) {
			AdAdminPartOperator op = new AdAdminPartOperator(conn,
					(AdAdminPart) row);
			op.deleteRow2Sql(stmt);
		}

	}

}
