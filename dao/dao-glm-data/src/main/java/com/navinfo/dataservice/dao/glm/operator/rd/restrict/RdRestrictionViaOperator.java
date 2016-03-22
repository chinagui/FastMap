package com.navinfo.dataservice.dao.glm.operator.rd.restrict;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;

public class RdRestrictionViaOperator implements IOperator {

	private static Logger logger = Logger
			.getLogger(RdRestrictionViaOperator.class);

	private Connection conn;

	private RdRestrictionVia via;

	public RdRestrictionViaOperator(Connection conn, RdRestrictionVia via) {
		this.conn = conn;

		this.via = via;
	}

	@Override
	public void insertRow() throws Exception {

		via.setRowId(UuidUtils.genUuid());

		String sql = "insert into "
				+ via.tableName()
				+ " (detail_id, link_pid, group_id, seq_num, u_record, row_id) values "
				+ "(?,?,?,?,?,?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, via.getDetailId());

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
				+ " set u_record=3,");

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
			sb.append(" where row_id=hextoraw('" + via.getRowId());
			
			sb.append("')");

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
				+ " set u_record=? where row_id=hextoraw(?)";

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

		sb.append("(detail_id, link_pid, group_id, seq_num, u_record, row_id) values (");

		sb.append(via.getDetailId());

		sb.append("," + via.getLinkPid());

		sb.append("," + via.getGroupId());

		sb.append("," + via.getSeqNum());

		sb.append(",1,'" + via.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {
		
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + via.tableName()
				+ " set u_record=2 where row_id=hextoraw('" + via.rowId() + "')";

		stmt.addBatch(sql);
	}

	// 维护经过线方向
	public List<RdRestrictionVia> repaireViaDirect(List<RdRestrictionVia> vias,
			int preSNodePid, int preENodePid, int linkPid) {
		List<RdRestrictionVia> newVias = new ArrayList<RdRestrictionVia>();

		for (RdRestrictionVia v : vias) {
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

			newVias.add(v);
		}

		return newVias;
	}

}
