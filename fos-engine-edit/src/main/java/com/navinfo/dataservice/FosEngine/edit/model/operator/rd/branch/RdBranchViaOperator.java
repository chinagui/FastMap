package com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.util.StringUtils;
import com.navinfo.dataservice.FosEngine.comm.util.UuidUtils;
import com.navinfo.dataservice.FosEngine.edit.model.IOperator;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchVia;

public class RdBranchViaOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdBranchViaOperator.class);

	private Connection conn;

	private RdBranchVia via;

	public RdBranchViaOperator(Connection conn, RdBranchVia via) {
		this.conn = conn;

		this.via = via;
	}

	@Override
	public void insertRow() throws Exception {

		via.setRowId(UuidUtils.genUuid());

		String sql = "insert into "
				+ via.tableName()
				+ " (branch_pid, link_pid, group_id, seq_num, u_record, row_id) values "
				+ "(:1,:2,:3,:4,:5,:6)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, via.getBranchPid());

			pstmt.setInt(2, via.getLinkPid());

			pstmt.setInt(3, via.getGroupId());

			pstmt.setInt(4, via.getSeqNum());

			pstmt.setInt(5, 1);

			pstmt.setString(6, via.rowId());

			pstmt.executeUpdate();

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}
	}

	@Override
	public void updateRow() throws Exception {

		StringBuilder sb = new StringBuilder("update " + via.tableName()
				+ " set ");

		PreparedStatement pstmt = null;

		try {

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

				if (value instanceof String) {

					if (!StringUtils.isStringSame(String.valueOf(value),
							String.valueOf(columnValue))) {
						sb.append(column + "='" + String.valueOf(columnValue)
								+ "',");
					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double
							.parseDouble(String.valueOf(columnValue))) {
						sb.append(column
								+ "="
								+ Double.parseDouble(String
										.valueOf(columnValue)) + ",");
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
			sb.append(" where row_id='" + via.getRowId());

			sb.append(via.getRowId());

			sb.append("'");

			String sql = sb.toString();

			sql = sql.replace(", where", " where");

			pstmt = conn.prepareStatement(sql);

			pstmt.executeUpdate();

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}
	}

	@Override
	public void deleteRow() throws Exception {

		String sql = "update " + via.tableName()
				+ " set u_record=? where row_id=?";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setString(2, via.rowId());

			pstmt.executeUpdate();

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		via.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(via.tableName());

		sb.append("(branch_pid, link_pid, group_id, seq_num, u_record, row_id) values (");

		sb.append(via.getBranchPid());

		sb.append("," + via.getLinkPid());

		sb.append("," + via.getGroupId());

		sb.append("," + via.getSeqNum());

		sb.append(",1,'" + via.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

		StringBuilder sb = new StringBuilder("update " + via.tableName()
				+ " set ");

		for (int i = 0; i < fieldNames.size(); i++) {

			if (i > 0) {
				sb.append(",");
			}

			String column = StringUtils.toColumnName(fieldNames.get(i));

			sb.append(column);

			sb.append("=");

			Field field = via.getClass().getDeclaredField(fieldNames.get(i));

			Object value = field.get(via);

			sb.append(value);

		}

		sb.append(" where row_id=");

		sb.append(via.rowId());

		stmt.addBatch(sb.toString());
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + via.tableName()
				+ " set u_record=2 where row_id='" + via.rowId() + "'";

		stmt.addBatch(sql);
	}

}
