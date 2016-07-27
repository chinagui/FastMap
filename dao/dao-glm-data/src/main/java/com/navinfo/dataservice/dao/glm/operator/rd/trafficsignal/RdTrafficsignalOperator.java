/**
 * 
 */
package com.navinfo.dataservice.dao.glm.operator.rd.trafficsignal;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

/**
 * @ClassName: RdTrafficsignalOperator
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午6:00:05
 * @Description: TODO
 */
public class RdTrafficsignalOperator extends AbstractOperator {

	private RdTrafficsignal rdTrafficsignal;

	/**
	 * @param conn
	 */
	public RdTrafficsignalOperator(Connection conn, RdTrafficsignal rdTrafficsignal) {
		super(conn);

		this.rdTrafficsignal = rdTrafficsignal;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		rdTrafficsignal.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(rdTrafficsignal.tableName());

		sb.append("(PID, NODE_PID, LINK_PID, LOCATION, FLAG, TYPE, KG_FLAG, U_RECORD, ROW_ID) values (");

		sb.append(rdTrafficsignal.getPid());

		sb.append("," + rdTrafficsignal.getNodePid());

		sb.append("," + rdTrafficsignal.getLinkPid());

		sb.append("," + rdTrafficsignal.getLocation());

		sb.append("," + rdTrafficsignal.getFlag());

		sb.append("," + rdTrafficsignal.getType());

		sb.append("," + rdTrafficsignal.getKgFlag());

		sb.append(",1,'" + rdTrafficsignal.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + rdTrafficsignal.tableName() + " set u_record=3,");

		Set<Entry<String, Object>> set = rdTrafficsignal.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = rdTrafficsignal.getClass().getDeclaredField(column);

			field.setAccessible(true);

			column = StringUtils.toColumnName(column);

			Object value = field.get(rdTrafficsignal);

			if (value instanceof String || value == null) {

				if (!StringUtils.isStringSame(String.valueOf(value), String.valueOf(columnValue))) {

					if (columnValue == null) {
						sb.append(column + "=null,");
					} else {
						sb.append(column + "='" + String.valueOf(columnValue) + "',");
					}
					this.setChanged(true);

				}

			} else if (value instanceof Double) {

				if (Double.parseDouble(String.valueOf(value)) != Double.parseDouble(String.valueOf(columnValue))) {
					sb.append(column + "=" + Double.parseDouble(String.valueOf(columnValue)) + ",");
					this.setChanged(true);
				}

			} else if (value instanceof Integer) {

				if (Integer.parseInt(String.valueOf(value)) != Integer.parseInt(String.valueOf(columnValue))) {
					sb.append(column + "=" + Integer.parseInt(String.valueOf(columnValue)) + ",");
					this.setChanged(true);
				}

			}
		}
		sb.append(" where pid=" + rdTrafficsignal.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + rdTrafficsignal.tableName() + " set u_record=2 where pid=" + rdTrafficsignal.getPid();

		stmt.addBatch(sql);
	}

}
