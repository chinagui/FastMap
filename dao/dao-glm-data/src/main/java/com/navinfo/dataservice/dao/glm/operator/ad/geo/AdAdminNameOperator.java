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
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminName;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.gsc.RdGscLinkOperator;

public class AdAdminNameOperator extends AbstractOperator {
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);
	private AdAdminName adminName;

	public AdAdminNameOperator(Connection conn, AdAdminName adminName) {
		super(conn);
		this.adminName = adminName;
	}

	
	
	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		adminName.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adminName.tableName());

		sb.append("(name_id, region_id,name_groupid,lang_code,name_class,name,"
				+ "phonetic,src_flag,u_record,row_id) values (");

		sb.append(adminName.getPid());
		sb.append("," + adminName.getRegionId());
		sb.append("," + adminName.getNameGroupId());
		if (adminName.getLangCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminName.getLangCode() + "'");
		}
		sb.append("," + adminName.getNameClass());
		if (adminName.getName() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminName.getName() + "'");
		}
		if (adminName.getPhonetic() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminName.getPhonetic() + "'");
		}
		sb.append("," + adminName.getSrcFlag());
		sb.append(",1,'" + adminName.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + adminName.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = adminName.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = adminName.getClass().getDeclaredField(column);

			field.setAccessible(true);

			column = StringUtils.toColumnName(column);

			Object value = field.get(adminName);

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
		sb.append(" where name_id =" + adminName.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adminName.tableName()
				+ " set u_record=2 where name_id=" + adminName.getPid();

		stmt.addBatch(sql);

	}

}
