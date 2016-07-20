package com.navinfo.dataservice.dao.glm.operator.rd.gsc;

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
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdGscLinkOperator extends AbstractOperator {
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);

	private RdGscLink gscLink;

	public RdGscLinkOperator(Connection conn, RdGscLink gscLink) {
		super(conn);
		this.gscLink = gscLink;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		gscLink.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(gscLink.tableName());

		sb.append("(pid, zlevel, link_pid, table_name, shp_seq_num, "
				+ "start_end, u_record, row_id) values (");

		sb.append(gscLink.getPid());

		sb.append("," + gscLink.getZlevel());

		sb.append("," + gscLink.getLinkPid());

		if (gscLink.tableName() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + gscLink.getTableName().toUpperCase() + "'");
		}
		sb.append("," + gscLink.getShpSeqNum());
		sb.append("," + gscLink.getStartEnd());
		sb.append(",1,'" + gscLink.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {

		StringBuilder sb = new StringBuilder("update " + gscLink.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = gscLink.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = gscLink.getClass().getDeclaredField(column);

			field.setAccessible(true);

			column = StringUtils.toColumnName(column);

			Object value = field.get(gscLink);

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
		sb.append(" where row_id=hextoraw('" + gscLink.getRowId());

		sb.append("')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + gscLink.tableName()
				+ " set u_record=2 where pid=" + gscLink.getPid();

		stmt.addBatch(sql);

	}

}
