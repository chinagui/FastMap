package com.navinfo.dataservice.dao.glm.operator.ad.zone;

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
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminPart;
import com.navinfo.dataservice.dao.glm.operator.rd.gsc.RdGscLinkOperator;

public class AdAdminGroupOperator implements IOperator {
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);

	private Connection conn;
	private AdAdminGroup adminGroup;
	public AdAdminGroupOperator(Connection conn, AdAdminGroup adminGroup) {
		this.conn = conn;
		this.adminGroup = adminGroup;
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
		StringBuilder sb = new StringBuilder("update " + adminGroup.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = adminGroup.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = adminGroup.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(adminGroup);

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
			sb.append(" where group_id =" + adminGroup.pid());

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
		adminGroup.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adminGroup.tableName());

		sb.append("(group_id, region_id_up,u_record,row_id) values (");

		sb.append(adminGroup.pid());
		sb.append("," + adminGroup.getRegionIdUp());
		
		sb.append(",1,'" + adminGroup.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("update " + adminGroup.tableName()
				+ " set u_record=3,");

		for (int i = 0; i < fieldNames.size(); i++) {

			if (i > 0) {
				sb.append(",");
			}

			String column = StringUtils.toColumnName(fieldNames.get(i));

			sb.append(column);

			sb.append("=");

			Field field = adminGroup.getClass().getDeclaredField(fieldNames.get(i));

			Object value = field.get(adminGroup);

			sb.append(value);

		}

		sb.append(" where group_id =");

		sb.append(adminGroup.pid());

		stmt.addBatch(sb.toString());

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adminGroup.tableName()
				+ " set u_record=2 where group_id=" + adminGroup.pid();
		stmt.addBatch(sql);
		for(IRow row : adminGroup.getParts()){
			AdAdminPartOperator op = new AdAdminPartOperator(conn,(AdAdminPart)row);
			op.deleteRow2Sql(stmt);
		}

		

	}

}
