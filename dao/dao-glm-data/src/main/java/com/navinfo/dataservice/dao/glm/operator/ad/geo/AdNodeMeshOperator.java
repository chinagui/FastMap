package com.navinfo.dataservice.dao.glm.operator.ad.geo;

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
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;

public class AdNodeMeshOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private AdNodeMesh adNodeMesh;

	public AdNodeMeshOperator(Connection conn, AdNodeMesh adNodeMesh) {
		super(conn);

		this.adNodeMesh = adNodeMesh;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		adNodeMesh.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adNodeMesh.tableName());

		sb.append("(node_pid, mesh_id, u_record, row_id) values (");

		sb.append(adNodeMesh.getNodePid());

		sb.append("," + adNodeMesh.getMeshId());

		sb.append(",1,'" + adNodeMesh.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + adNodeMesh.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = adNodeMesh.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = adNodeMesh.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(adNodeMesh);

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
		sb.append(" where row_id=hextoraw('" + adNodeMesh.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adNodeMesh.tableName()
				+ " set u_record=2 where row_id=hextoraw('"
				+ adNodeMesh.rowId() + "')";

		stmt.addBatch(sql);
	}

}
