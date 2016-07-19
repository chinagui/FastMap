package com.navinfo.dataservice.dao.glm.operator.rd.laneconnexity;

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
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdLaneConnexityOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(RdLaneConnexityOperator.class);

	private RdLaneConnexity connexity;

	public RdLaneConnexityOperator(Connection conn, RdLaneConnexity connexity) {
		super(conn);

		this.connexity = connexity;
	}
	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		connexity.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(connexity.tableName());

		sb.append("(pid, in_link_pid, node_pid, lane_info, conflict_flag, kg_flag, lane_num, left_extend, right_extend, src_flag, u_record, row_id) values (");

		sb.append(connexity.getPid());

		sb.append("," + connexity.getInLinkPid());

		sb.append("," + connexity.getNodePid());

		if (connexity.getLaneInfo() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + connexity.getLaneInfo() + "'");
		}

		sb.append("," + connexity.getConflictFlag());

		sb.append("," + connexity.getKgFlag());

		sb.append("," + connexity.getLaneNum());

		sb.append("," + connexity.getLeftExtend());

		sb.append("," + connexity.getRightExtend());

		sb.append("," + connexity.getSrcFlag());

		sb.append(",1,'" + connexity.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : connexity.getTopos()) {
			RdLaneTopologyOperator op = new RdLaneTopologyOperator(conn,
					(RdLaneTopology) r);

			op.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + connexity.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = connexity.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = connexity.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(connexity);

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
		sb.append(" where pid=" + connexity.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + connexity.tableName()
				+ " set u_record=2 where pid=" + connexity.getPid();

		stmt.addBatch(sql);

		for (IRow r : connexity.getTopos()) {
			RdLaneTopologyOperator op = new RdLaneTopologyOperator(conn,
					(RdLaneTopology) r);

			op.deleteRow2Sql(stmt);
		}
	}

}
