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
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminDetail;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminName;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminPart;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.operator.rd.gsc.RdGscLinkOperator;

public class AdAdminOperator implements IOperator {
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);

	private Connection conn;
	private AdAdmin admin;
	public AdAdminOperator(Connection conn, AdAdmin admin) {
		this.conn = conn;
		this.admin = admin;
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
		StringBuilder sb = new StringBuilder("update " + admin.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = admin.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = admin.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(admin);

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
			sb.append(" where group_id =" + admin.pid());

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
		admin.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(admin.tableName());

		sb.append("(region_id, admin_id,extend_id,admin_type," +
				"capital,population,geometry,link_pid,name_groupId,side," +
				"road_flag,pmesh_id,jis_code,mesh_id,edit_flag,memo,u_record,row_id) values (");

		sb.append(admin.pid());
		sb.append("," + admin.getAdminId());
		sb.append("," + admin.getExtendId());
		sb.append("," + admin.getAdminId());
		sb.append("," + admin.getAdminType());
		sb.append("," + admin.getCapital());
		sb.append("," + admin.getCapital());
		if (admin.getPopulation() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + admin.getPopulation() + "'");
		}
		sb.append("," + admin.getGeometry());
		sb.append("," + admin.getLinkPid());
		sb.append("," + admin.getNameGroupId());
		sb.append("," + admin.getSide());
		sb.append("," + admin.getRoadFlag());
		sb.append("," + admin.getpMeshId());
		sb.append("," + admin.getJisCode());
		sb.append("," + admin.getMeshId());
		sb.append("," + admin.getEditFlag());
		if (admin.getMemo() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + admin.getMemo()+ "'");
		}
		sb.append(",1,'" + admin.rowId() + "')");
		stmt.addBatch(sb.toString());
		for(IRow row :admin.getGroups()){
			AdAdminGroupOperator op = new AdAdminGroupOperator(conn, (AdAdminGroup)row);
			op.insertRow2Sql(stmt);
		}
		for (IRow row :admin.getNames()){
			AdAdminNameOperator  op = new AdAdminNameOperator(conn, (AdAdminName)row);
			op.insertRow2Sql(stmt);
		}
		for (IRow row :admin.getDetails()){
			AdAdminDetailOperator  op = new AdAdminDetailOperator(conn, (AdAdminDetail)row);
			op.insertRow2Sql(stmt);
		}
			
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("update " + admin.tableName()
				+ " set u_record=3,");

		for (int i = 0; i < fieldNames.size(); i++) {

			if (i > 0) {
				sb.append(",");
			}

			String column = StringUtils.toColumnName(fieldNames.get(i));

			sb.append(column);

			sb.append("=");

			Field field = admin.getClass().getDeclaredField(fieldNames.get(i));

			Object value = field.get(admin);

			sb.append(value);

		}

		sb.append(" where group_id =");

		sb.append(admin.pid());

		stmt.addBatch(sb.toString());

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + admin.tableName()
				+ " set u_record=2 where group_id=" + admin.pid();
		stmt.addBatch(sql);
		for(IRow row :admin.getGroups()){
			AdAdminGroupOperator op = new AdAdminGroupOperator(conn, (AdAdminGroup)row);
			op.deleteRow2Sql(stmt);
		}
		for (IRow row :admin.getNames()){
			AdAdminNameOperator  op = new AdAdminNameOperator(conn, (AdAdminName)row);
			op.deleteRow2Sql(stmt);
		}
		for (IRow row :admin.getDetails()){
			AdAdminDetailOperator  op = new AdAdminDetailOperator(conn, (AdAdminDetail)row);
			op.deleteRow2Sql(stmt);
		}

		

	}

}
