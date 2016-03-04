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
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkForm;
import com.navinfo.dataservice.commons.util.UuidUtils;

public class RdLinkFormOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdLinkFormOperator.class);

	private Connection conn;

	private RdLinkForm form;

	public RdLinkFormOperator(Connection conn, RdLinkForm form) {
		this.conn = conn;

		this.form = form;
	}

	@Override
	public void insertRow() throws Exception {
		form.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder(
				"insert into rd_link_form(link_pid,form_of_way,u_record,row_id) values (:1,:2,1,:3)");

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, form.getLinkPid());

			pstmt.setInt(2, form.getFormOfWay());

			pstmt.setString(3, form.getRowId());

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

				column = StringUtils.toColumnName(column);

				Object value = field.get(form);

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
			sb.append(" where row_id=hextoraw('" + form.getRowId());
			
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

		String sql = "update rd_link_form set u_record=2 where row_id=hextoraw(?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, form.getRowId());

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

		StringBuilder sb = new StringBuilder("insert into rd_link_form(");

		sb.append("link_pid,form_of_way,u_record,row_id");

		sb.append(") values (");

		sb.append(form.getLinkPid());

		sb.append(",");

		sb.append(form.getFormOfWay());

		sb.append(",1,'" + form.getRowId() + "')");

		stmt.addBatch(sb.toString());

	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update rd_link_form set u_record=2 where row_id = hextoraw('"
				+ form.getRowId() + "')";

		stmt.addBatch(sql);

	}

}
