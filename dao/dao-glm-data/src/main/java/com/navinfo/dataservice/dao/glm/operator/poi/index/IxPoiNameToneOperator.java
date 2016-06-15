package com.navinfo.dataservice.dao.glm.operator.poi.index;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiNameTone;

/**
 * POI名称语音语调表 操作类
 * @author zhangxiaolong
 *
 */
public class IxPoiNameToneOperator implements IOperator {

	private Connection conn;

	private IxPoiNameTone ixPoiNameTone;

	public IxPoiNameToneOperator(Connection conn, IxPoiNameTone ixPoiNameTone) {
		this.conn = conn;

		this.ixPoiNameTone = ixPoiNameTone;
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
		StringBuilder sb = new StringBuilder("update " + ixPoiNameTone.tableName() + " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ixPoiNameTone.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoiNameTone.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(ixPoiNameTone);

				column = StringUtils.toColumnName(column);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value), String.valueOf(columnValue))) {

						if (columnValue == null) {
							sb.append(column + "=null,");
						} else {
							sb.append(column + "='" + String.valueOf(columnValue) + "',");
						}

					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double.parseDouble(String.valueOf(columnValue))) {
						sb.append(column + "=" + Double.parseDouble(String.valueOf(columnValue)) + ",");
					}

				} else if (value instanceof Integer) {

					if (Integer.parseInt(String.valueOf(value)) != Integer.parseInt(String.valueOf(columnValue))) {
						sb.append(column + "=" + Integer.parseInt(String.valueOf(columnValue)) + ",");
					}

				}
			}
			sb.append(" where name_id= " + ixPoiNameTone.getNameId());

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
		String sql = "update " + ixPoiNameTone.tableName() + " set u_record=? where row_id=hextoraw(?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setString(2, ixPoiNameTone.rowId());

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
		ixPoiNameTone.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoiNameTone.tableName());

		sb.append("(NAME_ID, TONE_A, TONE_B, LH_A, LH_B, JYUTP, MEMO,U_RECORD, ROW_ID) values (");

		sb.append(ixPoiNameTone.getNameId());

		sb.append(",'" + ixPoiNameTone.getToneA()+"'");

		sb.append(",'" + ixPoiNameTone.getToneB()+"'");

		sb.append(",'" + ixPoiNameTone.getLhA()+"'");

		sb.append(",'" + ixPoiNameTone.getLhB()+"'");

		sb.append(",'" + ixPoiNameTone.getJyutp()+"'");

		sb.append(",'" + ixPoiNameTone.getMemo()+"'");

		sb.append(",1,'" + ixPoiNameTone.rowId() + "')");
		
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt) throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiNameTone.tableName() + " set u_record=2 where name_id="
				+ ixPoiNameTone.getNameId();

		stmt.addBatch(sql);
	}

}
