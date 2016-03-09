package com.navinfo.dataservice.FosEngine.edit.model.operator.rd.laneconnexity;

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
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.commons.util.UuidUtils;

public class RdLaneTopologyOperator implements IOperator {

	private static Logger logger = Logger
			.getLogger(RdLaneTopologyOperator.class);

	private Connection conn;

	private RdLaneTopology topo;

	public RdLaneTopologyOperator(Connection conn,
			RdLaneTopology topo) {
		this.conn = conn;

		this.topo = topo;
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

		StringBuilder sb = new StringBuilder("update " + topo.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = topo.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = topo.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				Object value = field.get(topo);

				column = StringUtils.toColumnName(column);

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
			sb.append(" where topology_id=" + topo.getPid());

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

		topo.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(topo.tableName());

		sb.append("(topology_id, connexity_pid, out_link_pid, in_lane_info, bus_lane_info, "
				+ "reach_dir, relationship_type, u_record, row_id) values (");

		sb.append(topo.getPid());

		sb.append("," + topo.getConnexityPid());

		sb.append("," + topo.getOutLinkPid());

		sb.append("," + topo.getInLaneInfo());

		sb.append("," + topo.getBusLaneInfo());

		sb.append("," + topo.getReachDir());

		sb.append("," + topo.getRelationshipType());

		sb.append(",1,'" + topo.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : topo.getVias()) {
			RdLaneViaOperator op = new RdLaneViaOperator(conn,
					(RdLaneVia) r);

			op.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + topo.tableName()
				+ " set u_record=2 where topology_id=" + topo.getPid();

		stmt.addBatch(sql);

		for (IRow r : topo.getVias()) {
			RdLaneViaOperator op = new RdLaneViaOperator(conn,
					(RdLaneVia) r);

			op.deleteRow2Sql(stmt);
		}
	}

}
