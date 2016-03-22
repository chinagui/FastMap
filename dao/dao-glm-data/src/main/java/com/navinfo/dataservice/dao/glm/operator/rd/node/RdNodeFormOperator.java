package com.navinfo.dataservice.dao.glm.operator.rd.node;

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
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;

public class RdNodeFormOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdNodeFormOperator.class);

	private Connection conn;

	private RdNodeForm form;

	public RdNodeFormOperator(Connection conn, RdNodeForm form) {
		this.conn = conn;

		this.form = form;
	}

	@Override
	public void insertRow() throws Exception {

		form.setRowId(UuidUtils.genUuid());

		String sql = "insert into "
				+ form.tableName()
				+ " (node_pid, form_of_way, auxi_flag, u_record, row_id) values "
				+ "(?,?,?,?,?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, form.getNodePid());

			pstmt.setInt(2, form.getFormOfWay());

			pstmt.setInt(3, form.getAuxiFlag());

			pstmt.setInt(4, 1);

			pstmt.setString(5, form.getRowId());

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

		StringBuilder sb = new StringBuilder("update " + form.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = form.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = form.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				Object value = field.get(form);

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

				} else if (value instanceof JSONObject) {
					if (!StringUtils.isStringSame(value.toString(),
							String.valueOf(columnValue))) {
						sb.append("geometry=sdo_geometry('"
								+ String.valueOf(columnValue) + "',8307),");
					}
				}
			}
			sb.append(" where row_id=hextoraw('" + form.getRowId() + "')");

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

		String sql = "update " + form.tableName()
				+ " set u_record=? where row_id=hextoraw(?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setString(2, form.getRowId());

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

		form.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(form.tableName());

		sb.append("(node_pid, form_of_way, auxi_flag, u_record, row_id) values (");

		sb.append(form.getNodePid());

		sb.append("," + form.getFormOfWay());

		sb.append("," + form.getAuxiFlag());

		sb.append(",1,'" + form.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + form.tableName()
				+ " set u_record=2 where row_id=hextoraw('" + form.getRowId() + "')";

		stmt.addBatch(sql);
	}
}
