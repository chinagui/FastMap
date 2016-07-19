package com.navinfo.dataservice.dao.glm.operator.lu;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;

import com.navinfo.dataservice.dao.glm.model.lu.LuNodeMesh;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class LuNodeMeshOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(LuNodeMeshOperator.class);

	private LuNodeMesh luNodeMesh;

	public LuNodeMeshOperator(Connection conn, LuNodeMesh luNodeMesh) {
		super(conn);

		this.luNodeMesh = luNodeMesh;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		luNodeMesh.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(luNodeMesh.tableName());

		sb.append("(node_pid, mesh_id, u_record, row_id) values (");

		sb.append(luNodeMesh.getNodePid());

		sb.append("," + luNodeMesh.getMeshId());

		sb.append(",1,'" + luNodeMesh.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + luNodeMesh.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = luNodeMesh.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = luNodeMesh.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(luNodeMesh);

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
		sb.append(" where row_id=hextoraw('" + luNodeMesh.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + luNodeMesh.tableName()
				+ " set u_record=2 where row_id=hextoraw('"
				+ luNodeMesh.rowId() + "')";

		stmt.addBatch(sql);
	}

}
