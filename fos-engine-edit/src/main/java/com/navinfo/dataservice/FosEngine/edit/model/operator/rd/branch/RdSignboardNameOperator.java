package com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch;

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
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSignboardName;
import com.navinfo.dataservice.commons.util.UuidUtils;

public class RdSignboardNameOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdSignboardNameOperator.class);

	private Connection conn;

	private RdSignboardName name;

	public RdSignboardNameOperator(Connection conn, RdSignboardName name) {
		this.conn = conn;

		this.name = name;
	}

	@Override
	public void insertRow() throws Exception {

		name.setRowId(UuidUtils.genUuid());

		String sql = "insert into "
				+ name.tableName()
				+ " (name_id, seq_num, name_groupid, signboard_id, name_class, lang_code, code_type, name, "
				+ "phonetic, src_flag, voice_file, u_record, row_id) values "
				+ "(:1,:2,:3,:4,:5,:6,:7,:8,:9,:10,:11,:12,:13)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, name.getPid());

			pstmt.setInt(2, name.getSeqNum());

			pstmt.setInt(3, name.getNameGroupid());

			pstmt.setInt(4, name.getSignboardId());

			pstmt.setInt(5, name.getNameClass());

			pstmt.setString(6, name.getLangCode());

			pstmt.setInt(7, name.getCodeType());

			pstmt.setString(8, name.getName());

			pstmt.setString(9, name.getPhonetic());

			pstmt.setInt(10, name.getSrcFlag());

			pstmt.setString(11, name.getVoiceFile());

			pstmt.setInt(12, 1);

			pstmt.setString(13, name.rowId());

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
				+ " set ");

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

				Object value = field.get(name);

				column = StringUtils.toColumnName(column);

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
			sb.append(" where name_id=" + name.getPid());

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

			pstmt.setInt(2, name.getPid());

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

		sb.append("(name_id, seq_num, name_groupid, signboard_id, name_class, lang_code, code_type, name, "
				+ "phonetic, src_flag, voice_file, u_record, row_id) values (");

		sb.append(name.getPid());

		sb.append("," + name.getSeqNum());

		sb.append("," + name.getNameGroupid());

		sb.append("," + name.getSignboardId());

		sb.append("," + name.getNameClass());

		if (name.getLangCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + name.getLangCode() + "'");
		}

		sb.append("," + name.getCodeType());

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

		if (name.getVoiceFile() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + name.getVoiceFile() + "'");
		}

		sb.append(",1,'" + name.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

		StringBuilder sb = new StringBuilder("update " + name.tableName()
				+ " set ");

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

		sb.append(name.getPid());

		stmt.addBatch(sb.toString());
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + name.tableName()
				+ " set u_record=2 where name_id=" + name.getPid();

		stmt.addBatch(sql);
	}

}
