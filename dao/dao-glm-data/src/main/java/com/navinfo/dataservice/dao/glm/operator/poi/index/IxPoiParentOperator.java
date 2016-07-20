package com.navinfo.dataservice.dao.glm.operator.poi.index;

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
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;

/**
 * 索引:POI父子关系父表 操作
 * 
 * @author luyao
 * 
 */
public class IxPoiParentOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private IxPoiParent ixPoiParent;

	public IxPoiParentOperator(Connection conn, IxPoiParent ixPoiParent)
			throws Exception {
		super(conn);

		this.ixPoiParent = ixPoiParent;
		IxPoiOperator operator = new IxPoiOperator(conn,
				new IxPoiSelector(conn).loadRowIdByPid(
						ixPoiParent.getParentPoiPid(), false));
		operator.upatePoiStatus();
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiParent.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoiParent.tableName());

		sb.append("(group_id, parent_poi_pid, tenant_flag, memo, row_id,u_date,u_record) values (");

		sb.append(ixPoiParent.getPid());

		sb.append("," + ixPoiParent.getParentPoiPid());

		sb.append("," + ixPoiParent.getTenantFlag());

		if (StringUtils.isNotEmpty(ixPoiParent.getMemo())) {

			sb.append(",'" + ixPoiParent.getMemo() + "'");
		} else {
			sb.append(",null");
		}

		sb.append(",'" + ixPoiParent.getRowId() + "'");

		sb.append(",'" + StringUtils.getCurrentTime() + "'");

		sb.append(",'1')");

		stmt.addBatch(sb.toString());

		for (IRow r : ixPoiParent.getPoiChildrens()) {

			IxPoiChildrenOperator ap = new IxPoiChildrenOperator(conn,
					(IxPoiChildren) r);

			ap.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiParent.tableName() + " set u_record=3,u_date= '"
				+ StringUtils.getCurrentTime() + "' ,");

		Set<Entry<String, Object>> set = ixPoiParent.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiParent.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiParent);

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
		sb.append(" where group_id=" + ixPoiParent.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiParent.tableName()
				+ " set u_record=2,u_date= '" + StringUtils.getCurrentTime()
				+ "'  where group_id=" + ixPoiParent.getPid();

		stmt.addBatch(sql);

		for (IRow r : ixPoiParent.getPoiChildrens()) {

			IxPoiChildrenOperator ixPoiChildrenOperator = new IxPoiChildrenOperator(
					conn, (IxPoiChildren) r);

			ixPoiChildrenOperator.deleteRow2Sql(stmt);
		}

	}

}
