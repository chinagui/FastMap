package com.navinfo.dataservice.engine.edit.edit.model.operator.rd.cross;

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
import com.navinfo.dataservice.engine.edit.edit.model.IOperator;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.cross.RdCrossName;

public class RdCrossNameOperator implements IOperator {
	private static Logger logger = Logger.getLogger(RdCrossNameOperator.class);

	private Connection conn;

	private RdCrossName name;

	public RdCrossNameOperator(Connection conn, RdCrossName name) {
		this.conn = conn;

		this.name = name;
	}

	@Override
	public void insertRow() throws Exception {

		name.setRowId(UuidUtils.genUuid());

		String sql = "insert into " + name.tableName()
				+ " (name_id, pid, name_groupid, lang_code, name, "
				+ "phonetic, src_flag, u_record, row_id) values "
				+ "(?,?,?,?,?,?,?,?,?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, name.getNameId());

			pstmt.setInt(2, name.getPid());

			pstmt.setInt(3, name.getNameGroupid());

			pstmt.setString(4, name.getLangCode());

			pstmt.setString(5, name.getName());

			pstmt.setString(6, name.getPhonetic());

			pstmt.setInt(7, name.getSrcFlag());

			pstmt.setInt(8, 1);

			pstmt.setString(9, name.rowId());

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

		StringBuilder sb = new StringBuilder("update " + name.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = name.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = name.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(name);

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
			sb.append(" where name_id=" + name.getNameId());

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
				+ " set u_record=? where name_id=?";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setInt(2, name.getNameId());

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

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(name.tableName());

		sb.append("(name_id, pid, name_groupid, lang_code, name, "
				+ "phonetic, src_flag, u_record, row_id) values (");

		sb.append(name.getNameId());

		sb.append("," + name.getPid());

		sb.append("," + name.getNameGroupid());

		if (name.getLangCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + name.getLangCode() + "'");
		}

		if (name.getName() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + name.getName() + "'");
		}

		if (name.getPhonetic() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + name.getPhonetic() + "'");
		}

		sb.append("," + name.getSrcFlag());

		sb.append(",1,'" + name.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

		StringBuilder sb = new StringBuilder("update " + name.tableName()
				+ " set u_record=3,");

		for (int i = 0; i < fieldNames.size(); i++) {

			if (i > 0) {
				sb.append(",");
			}

			String column = StringUtils.toColumnName(fieldNames.get(i));

			sb.append(column);

			sb.append("=");

			Field field = name.getClass().getDeclaredField(fieldNames.get(i));

			Object value = field.get(name);

			sb.append(value);

		}

		sb.append(" where name_id=");

		sb.append(name.getNameId());

		stmt.addBatch(sb.toString());
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + name.tableName()
				+ " set u_record=2 where name_id=" + name.getNameId();

		stmt.addBatch(sql);
	}

}
