package com.navinfo.dataservice.dao.glm.operator.rd.link;

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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSidewalk;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdLinkSidewalkOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(RdLinkSidewalkOperator.class);

	private RdLinkSidewalk sidewalk;

	public RdLinkSidewalkOperator(Connection conn, RdLinkSidewalk sidewalk) {
		super(conn);

		this.sidewalk = sidewalk;
	}
	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		sidewalk.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder(
				"insert into rd_link_sidewalk(link_pid,sidewalk_loc,divider_type,work_dir,process_flag,capture_flag,u_record,row_id) values (");

		sb.append(sidewalk.getLinkPid());

		sb.append(",");

		sb.append(sidewalk.getSidewalkLoc());

		sb.append(",");

		sb.append(sidewalk.getDividerType());

		sb.append(",");

		sb.append(sidewalk.getWorkDir());

		sb.append(",");

		sb.append(sidewalk.getProcessFlag());

		sb.append(",");

		sb.append(sidewalk.getCaptureFlag());

		sb.append(",1,'" + sidewalk.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + sidewalk.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = sidewalk.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = sidewalk.getClass().getDeclaredField(column);

			field.setAccessible(true);

			column = StringUtils.toColumnName(column);

			Object value = field.get(sidewalk);

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
		sb.append(" where row_id=hextoraw('" + sidewalk.rowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update rd_link_sidewalk set u_record=2 where row_id = hextoraw('"
				+ sidewalk.getRowId() + "')";

		stmt.addBatch(sql);
	}

}
