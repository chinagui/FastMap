package com.navinfo.dataservice.dao.glm.operator.poi.index;

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
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiNameFlag;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

/**
 * POI名称标识表操作类
 * 
 * @author zhangxiaolong
 * 
 */
public class IxPoiNameFlagOperator extends AbstractOperator {

	private IxPoiNameFlag ixPoiNameFlag;

	public IxPoiNameFlagOperator(Connection conn, IxPoiNameFlag ixPoiNameFlag) {
		super(conn);

		this.ixPoiNameFlag = ixPoiNameFlag;
	}

	
	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiNameFlag.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoiNameFlag.tableName());

		sb.append("(NAME_ID, FLAG_CODE,U_DATE,U_RECORD, ROW_ID) values (");

		sb.append(ixPoiNameFlag.getNameId());

		if (StringUtils.isNotEmpty(ixPoiNameFlag.getFlagCode())) {
			sb.append(",'" + ixPoiNameFlag.getFlagCode() + "'");
		} else {
			sb.append(",null");
		}

		sb.append(",'" + StringUtils.getCurrentTime() + "'");

		sb.append(",1,'" + ixPoiNameFlag.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiNameFlag.tableName() + " set u_record=3,u_date= '"
				+ StringUtils.getCurrentTime() + "',");

		Set<Entry<String, Object>> set = ixPoiNameFlag.changedFields()
				.entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiNameFlag.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiNameFlag);

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
		sb.append(" where name_id= " + ixPoiNameFlag.getNameId());

		sb.append("')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiNameFlag.tableName()
				+ " set u_record=2,u_date= '" + StringUtils.getCurrentTime()
				+ "'  where name_id=" + ixPoiNameFlag.getNameId();

		stmt.addBatch(sql);
	}

}
