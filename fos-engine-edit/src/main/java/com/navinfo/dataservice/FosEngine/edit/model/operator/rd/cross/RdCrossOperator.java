package com.navinfo.dataservice.FosEngine.edit.model.operator.rd.cross;

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
import com.navinfo.dataservice.FosEngine.edit.model.IOperator;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossNode;
import com.navinfo.dataservice.commons.util.UuidUtils;

public class RdCrossOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdCrossOperator.class);

	private Connection conn;

	private RdCross cross;

	public RdCrossOperator(Connection conn, RdCross cross) {
		this.conn = conn;

		this.cross = cross;
	}

	@Override
	public void insertRow() throws Exception {

		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			this.insertRow2Sql(stmt);

			stmt.executeBatch();

		} catch (Exception e) {
			
			throw e;

		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				
			}

		}
	}

	@Override
	public void updateRow() throws Exception {

		StringBuilder sb = new StringBuilder("update " + cross.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = cross.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = cross.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(cross);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value),
							String.valueOf(columnValue))) {
						
						if(columnValue==null){
							sb.append(column + "=null,");
						}
						else{
							sb.append(column + "='" + String.valueOf(columnValue)
									+ "',");
						}
						
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
			sb.append(" where pid=" + cross.getPid());

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

		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			this.deleteRow2Sql(stmt);

			stmt.executeBatch();

		} catch (Exception e) {
			
			throw e;

		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				
			}

		}
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		cross.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(cross.tableName());

		sb.append("(pid, type, signal, electroeye, kg_flag, u_record, row_id) values (");

		sb.append(cross.getPid());

		sb.append("," + cross.getType());

		sb.append("," + cross.getSignal());

		sb.append("," + cross.getElectroeye());

		sb.append("," + cross.getKgFlag());

		sb.append(",1,'" + cross.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : cross.getLinks()) {
			RdCrossLinkOperator op = new RdCrossLinkOperator(conn,
					(RdCrossLink) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : cross.getNodes()) {
			RdCrossNodeOperator op = new RdCrossNodeOperator(conn,
					(RdCrossNode) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : cross.getNames()) {
			RdCrossNameOperator op = new RdCrossNameOperator(conn,
					(RdCrossName) r);

			op.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + cross.tableName()
				+ " set u_record=2 where pid=" + cross.getPid();

		stmt.addBatch(sql);

		for (IRow r : cross.getLinks()) {
			RdCrossLinkOperator op = new RdCrossLinkOperator(conn,
					(RdCrossLink) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : cross.getNodes()) {
			RdCrossNodeOperator op = new RdCrossNodeOperator(conn,
					(RdCrossNode) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : cross.getNames()) {
			RdCrossNameOperator op = new RdCrossNameOperator(conn,
					(RdCrossName) r);

			op.deleteRow2Sql(stmt);
		}
	}

}
