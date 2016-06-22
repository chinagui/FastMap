package com.navinfo.dataservice.dao.glm.operator.poi.index;

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
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
/**
 * 索引:POI联系方式表 操作
 * @author luyao
 *
 */
public class IxPoiContactOperator implements IOperator {
	
	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private Connection conn;

	private IxPoiContact ixPoiContact;

	public IxPoiContactOperator(Connection conn, IxPoiContact ixPoiContact) {
		this.conn = conn;

		this.ixPoiContact = ixPoiContact;
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
		StringBuilder sb = new StringBuilder("update " + ixPoiContact.tableName()
				+ " set u_record=3,u_date="+StringUtils.getCurrentTime()+",");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ixPoiContact.changedFields()
					.entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoiContact.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(ixPoiContact);

				column = StringUtils.toColumnName(column);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value),
							String.valueOf(columnValue))) {

						if (columnValue == null) {
							sb.append(column + "=null,");
						} else {
							sb.append(column + "='"
									+ String.valueOf(columnValue) + "',");
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
			sb.append(" where row_id=hextoraw('" + ixPoiContact.getRowId() + "')");

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
		ixPoiContact.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoiContact.tableName());

		sb.append("(poi_pid, contact_type, contact,contact_depart,priority, row_id,u_date,u_record) values (");

		sb.append(ixPoiContact.getPoiPid());

		sb.append("," + ixPoiContact.getContactType());
		
		if(StringUtils.isNotEmpty(ixPoiContact.getContact()))
		{
			sb.append(",'" + ixPoiContact.getContact()+"'");
		}
		else
		{
			sb.append(",null");
		}
		
		sb.append("," + ixPoiContact.getContactDepart() + "");
		
		sb.append("," + ixPoiContact.getPriority() + "");

		sb.append(",'" + ixPoiContact.getRowId() + "'");
		
		sb.append(",'" + StringUtils.getCurrentTime()+ "'");


		sb.append(",'1')");

		stmt.addBatch(sb.toString());

	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiContact.tableName()
				+ " set u_record=2,u_date="+StringUtils.getCurrentTime()+" where row_id=hextoraw('"
				+ ixPoiContact.rowId() + "')";

		stmt.addBatch(sql);

	}

}
