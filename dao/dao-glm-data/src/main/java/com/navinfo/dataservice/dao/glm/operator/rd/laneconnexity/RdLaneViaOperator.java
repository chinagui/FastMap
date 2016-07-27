package com.navinfo.dataservice.dao.glm.operator.rd.laneconnexity;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdLaneViaOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdLaneViaOperator.class);

	private RdLaneVia via;

	public RdLaneViaOperator(Connection conn, RdLaneVia via) {
		super(conn);

		this.via = via;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		via.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(via.tableName());

		sb.append("(topology_id, link_pid, group_id, seq_num, u_record, row_id) values (");

		sb.append(via.getTopologyId());

		sb.append("," + via.getLinkPid());

		sb.append("," + via.getGroupId());

		sb.append("," + via.getSeqNum());

		sb.append(",1,'" + via.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {

		StringBuilder sb = new StringBuilder("update " + via.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = via.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = via.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(via);

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
		sb.append(" where row_id=hextoraw('" + via.getRowId());

		sb.append(via.getRowId());

		sb.append("')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + via.tableName()
				+ " set u_record=2 where row_id=hextoraw('" + via.rowId()
				+ "')";

		stmt.addBatch(sql);
	}

	// 维护经过线方向
	public List<Entry<Integer, RdLaneVia>> repaireViaDirect(
			List<Entry<Integer, RdLaneVia>> vias, int preSNodePid,
			int preENodePid, int linkPid) {
		List<Entry<Integer, RdLaneVia>> newVias = new ArrayList<Entry<Integer, RdLaneVia>>();

		for (Entry<Integer, RdLaneVia> entry : vias) {

			RdLaneVia v = entry.getValue();

			if (v.getLinkPid() == linkPid) {

				if (preSNodePid != 0 && preENodePid != 0) {
					if (v.igetsNodePid() == preSNodePid
							|| v.igetsNodePid() == preENodePid) {

					} else {
						int tempPid = v.igetsNodePid();

						v.isetsNodePid(v.igeteNodePid());

						v.iseteNodePid(tempPid);
					}
				} else {
					if (v.igeteNodePid() == v.igetInNodePid()) {
						int tempPid = v.igetsNodePid();

						v.isetsNodePid(v.igeteNodePid());

						v.iseteNodePid(tempPid);
					}
				}
			}

			newVias.add(new AbstractMap.SimpleEntry(entry.getKey(), v));
		}

		return newVias;
	}

}
