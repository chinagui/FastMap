package com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link;

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

import com.navinfo.dataservice.FosEngine.comm.util.StringUtils;
import com.navinfo.dataservice.FosEngine.edit.model.IOperator;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkName;
import com.navinfo.dataservice.commons.util.UuidUtils;

public class RdLinkNameOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdLinkNameOperator.class);

	private Connection conn;

	private RdLinkName name;

	public RdLinkNameOperator(Connection conn, RdLinkName name) {
		this.conn = conn;

		this.name = name;
	}

	@Override
	public void insertRow() throws Exception {

		name.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder(
				"insert into rd_link_name(link_pid,seq_num,name_type,name_class,name_groupid,input_time,src_flag,route_att,code,u_record,row_id) values (:1,:2,:3,:4,:5,:6,:7,:8,:9,1,:10)");

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, name.getLinkPid());

			pstmt.setInt(2, name.getSeqNum());

			pstmt.setInt(3, name.getNameType());

			pstmt.setInt(4, name.getNameClass());

			pstmt.setInt(5, name.getNameGroupid());
			
			pstmt.setString(6, name.getInputTime());
			
			pstmt.setInt(7, name.getSrcFlag());
			
			pstmt.setInt(8, name.getRouteAtt());
			
			pstmt.setInt(9, name.getCode());

			pstmt.setString(10, name.getRowId());

			pstmt.execute();
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

		StringBuilder sb = new StringBuilder("update " + name.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = name.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();
				
				if ("name".equals(column)){
					continue;
				}

				Object columnValue = en.getValue();

				Field field = name.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(name);

				if (value instanceof String || value == null) {

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

				} else if (value instanceof JSONObject) {
					if (!StringUtils.isStringSame(value.toString(),
							String.valueOf(columnValue))) {
						sb.append("geometry=sdo_geometry('"
								+ String.valueOf(columnValue) + "',8307),");
					}
				}
			}
			sb.append(" where row_id='" + name.getRowId());
			
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

		String sql = "update " + name.tableName()
				+ " set u_record=2 where row_id=?";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, name.getRowId());

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

		name.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder(
				"insert into rd_link_name(link_pid,name_groupid,seq_num,name_class,name_type,input_time,src_flag,route_att,code,u_record,row_id) values (");

		sb.append(name.getLinkPid());

		sb.append(",");

		sb.append(name.getNameGroupid());

		sb.append(",");

		sb.append(name.getSeqNum());

		sb.append(",");

		sb.append(name.getNameClass());

		sb.append(",");

		sb.append(name.getNameType());
		
		sb.append(",");
		
		sb.append(name.getInputTime());
		
		sb.append(",");
		
		sb.append(name.getSrcFlag());
		
		sb.append(",");
		
		sb.append(name.getRouteAtt());
		
		sb.append(",");
		
		sb.append(name.getCode());

		sb.append(",1,'" + name.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> columns, Statement stmt) {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update rd_link_name set u_record=2 where row_id = '"
				+ name.getRowId() + "'";

		stmt.addBatch(sql);
	}

}
