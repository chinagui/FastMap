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
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminName;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.operator.rd.gsc.RdGscLinkOperator;

public class AdAdminNameOperator implements IOperator {
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);

	private Connection conn;
	private AdAdminName adminName;
	public AdAdminNameOperator(Connection conn, AdAdminName adminName) {
		this.conn = conn;
		this.adminName = adminName;
	}
	@Override
	public void insertRow() throws Exception {
		adminName.setRowId(UuidUtils.genUuid());

		String sql = "insert into " + adminName.tableName()
				+ " (name_id, region_id,name_groupid,lang_code,name_class,name," +
				"phonetic,src_flag,u_record,row_id) values "
				+ "(?,?,?,?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, adminName.getPid());

			pstmt.setInt(2, adminName.getRegionId());
			pstmt.setInt(3, adminName.getNameGroupId());
			pstmt.setString(4, adminName.getLangCode());
			pstmt.setInt(5, adminName.getNameClass());
			pstmt.setString(6, adminName.getName());
			pstmt.setString(7, adminName.getPhonetic());
			pstmt.setInt(7, adminName.getSrcFlag());
			pstmt.setInt(8, 1);
			pstmt.setString(9, adminName.rowId());
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
		StringBuilder sb = new StringBuilder("update " + adminName.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = adminName.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = adminName.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(adminName);

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
			sb.append(" where name_id =" + adminName.getPid());

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
		String sql = "update " + adminName.tableName()
				+ " set u_record=? where name_id=?";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setInt(2, adminName.getPid());

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
		adminName.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adminName.tableName());

		sb.append("(name_id, region_id,name_groupid,lang_code,name_class,name," +
				"phonetic,src_flag,u_record,row_id) values (");

		sb.append(adminName.getPid());
		sb.append("," + adminName.getRegionId());
		sb.append("," + adminName.getNameGroupId());
		if (adminName.getLangCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminName.getLangCode() + "'");
		}
		sb.append("," + adminName.getNameClass());
		if (adminName.getName() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminName.getName() + "'");
		}
		if (adminName.getPhonetic() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminName.getPhonetic() + "'");
		}
		sb.append("," + adminName.getSrcFlag());
		sb.append(",1,'" + adminName.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("update " + adminName.tableName()
				+ " set u_record=3,");

		for (int i = 0; i < fieldNames.size(); i++) {

			if (i > 0) {
				sb.append(",");
			}

			String column = StringUtils.toColumnName(fieldNames.get(i));

			sb.append(column);

			sb.append("=");

			Field field = adminName.getClass().getDeclaredField(fieldNames.get(i));

			Object value = field.get(adminName);

			sb.append(value);

		}

		sb.append(" where name_id =");

		sb.append(adminName.getPid());

		stmt.addBatch(sb.toString());

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adminName.tableName()
				+ " set u_record=2 where group_id=" + adminName.getPid();

		stmt.addBatch(sql);

	}

}
