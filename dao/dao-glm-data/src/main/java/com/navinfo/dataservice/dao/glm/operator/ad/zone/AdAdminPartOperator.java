package com.navinfo.dataservice.dao.glm.operator.ad.zone;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminDetail;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminPart;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.operator.rd.gsc.RdGscLinkOperator;

public class AdAdminPartOperator implements IOperator {
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);

	private Connection conn;
	private AdAdminPart adminPart;
	public AdAdminPartOperator(Connection conn, AdAdminPart adminPart) {
		this.conn = conn;
		this.adminPart = adminPart;
	}
	@Override
	public void insertRow() throws Exception {
		adminPart.setRowId(UuidUtils.genUuid());

		String sql = "insert into " + adminPart.tableName()
				+ " (group_id, region_id_down,u_record,row_id) values "
				+ "(?,?,?,?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, adminPart.getGroupId());

			pstmt.setInt(2, adminPart.getGroupIdDown());
			pstmt.setInt(3, 1);
			pstmt.setString(4, adminPart.rowId());
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
		StringBuilder sb = new StringBuilder("update " + adminPart.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = adminPart.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = adminPart.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(adminPart);

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
			sb.append(" where row_id=hextoraw('" + adminPart.rowId());

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
		String sql = "update " + adminPart.tableName()
				+ " set u_record=? where row_id=hextoraw(?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setString(2, adminPart.rowId());

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
		adminPart.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adminPart.tableName());

		sb.append("(group_id, region_id_down,u_record,row_id) values (");

		sb.append(adminPart.getGroupId());
		sb.append("," + adminPart.getGroupIdDown());
		sb.append(",1,'" + adminPart.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("update " + adminPart.tableName()
				+ " set u_record=3,");

		for (int i = 0; i < fieldNames.size(); i++) {

			if (i > 0) {
				sb.append(",");
			}

			String column = StringUtils.toColumnName(fieldNames.get(i));

			sb.append(column);

			sb.append("=");

			Field field = adminPart.getClass().getDeclaredField(fieldNames.get(i));

			Object value = field.get(adminPart);

			sb.append(value);

		}

		sb.append(" where row_id=hextoraw('"+adminPart.getRowId()+"')");

		stmt.addBatch(sb.toString());

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adminPart.tableName()
				+ " set u_record=2 where row_id=hextoraw('" + adminPart.getRowId() + "')";
		stmt.addBatch(sql);

	}

}
