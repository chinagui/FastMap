package com.navinfo.dataservice.dao.glm.operator.poi.index;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;

/**
 * POI父子关系子表 操作
 * 
 * @author luyao
 * 
 */
public class IxPoiChildrenOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private IxPoiChildren ixPoiChildren;

	public IxPoiChildrenOperator(Connection conn, IxPoiChildren ixPoiChildren)
			throws Exception {
		super(conn);
		this.ixPoiChildren = ixPoiChildren;
		IxPoiOperator operator = new IxPoiOperator(conn,
				new IxPoiSelector(conn).loadRowIdByPid(
						ixPoiChildren.getChildPoiPid(), false));
		operator.upatePoiStatus();
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiChildren.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoiChildren.tableName());

		sb.append("(group_id, child_poi_pid, relation_type, row_id,u_date,u_record) values (");

		sb.append(ixPoiChildren.getGroupId());

		sb.append("," + ixPoiChildren.getChildPoiPid());

		sb.append("," + ixPoiChildren.getRelationType() + "");

		sb.append(",'" + ixPoiChildren.getRowId() + "'");

		sb.append(",'" + StringUtils.getCurrentTime() + "'");

		sb.append(",'1')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiChildren.tableName() + " set u_record=3,u_date='"
				+ StringUtils.getCurrentTime() + "',");

		Set<Entry<String, Object>> set = ixPoiChildren.changedFields()
				.entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiChildren.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiChildren);

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
		sb.append(" where row_id=hextoraw('" + ixPoiChildren.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiChildren.tableName()
				+ " set u_record=2,u_date='" + StringUtils.getCurrentTime()
				+ "' where row_id=hextoraw('" + ixPoiChildren.rowId() + "')";

		stmt.addBatch(sql);

	}

}
