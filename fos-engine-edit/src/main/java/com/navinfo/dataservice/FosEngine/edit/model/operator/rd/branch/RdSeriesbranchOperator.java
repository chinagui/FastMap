package com.navinfo.dataservice.FosEngine.edit.model.operator.rd.branch;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.util.StringUtils;
import com.navinfo.dataservice.FosEngine.comm.util.UuidUtils;
import com.navinfo.dataservice.FosEngine.edit.model.IOperator;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSeriesbranch;

public class RdSeriesbranchOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdSeriesbranchOperator.class);

	private Connection conn;

	private RdSeriesbranch seriesbranch;

	public RdSeriesbranchOperator(Connection conn, RdSeriesbranch seriesbranch) {
		this.conn = conn;

		this.seriesbranch = seriesbranch;
	}

	@Override
	public void insertRow() throws Exception {

		seriesbranch.setRowId(UuidUtils.genUuid());

		String sql = "insert into "
				+ seriesbranch.tableName()
				+ " (branch_pid, type, voice_dir, pattern_code, arrow_code, arrow_flag, u_record, row_id) values "
				+ "(:1,:2,:3,:4,:5,:6,:7,:8)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, seriesbranch.getBranchPid());

			pstmt.setInt(2, seriesbranch.getType());
			
			pstmt.setInt(3, seriesbranch.getVoiceDir());

			pstmt.setString(4, seriesbranch.getPatternCode());

			pstmt.setString(5, seriesbranch.getArrowCode());

			pstmt.setInt(6, seriesbranch.getArrowFlag());
			
			pstmt.setInt(7, 1);

			pstmt.setString(8, seriesbranch.rowId());

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

		StringBuilder sb = new StringBuilder("update " + seriesbranch.tableName()
				+ " set ");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = seriesbranch.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = seriesbranch.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(seriesbranch);

				column = StringUtils.toColumnName(column);

				if (value instanceof String) {

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

				}
			}
			sb.append(" where row_id='" + seriesbranch.getRowId());

			sb.append(seriesbranch.getRowId());

			sb.append("'");

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

		String sql = "update " + seriesbranch.tableName()
				+ " set u_record=? where row_id=?";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setString(2, seriesbranch.rowId());

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

		seriesbranch.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(seriesbranch.tableName());

		sb.append("(branch_pid, type, voice_dir, pattern_code, arrow_code, arrow_flag, u_record, row_id) values (");

		sb.append(seriesbranch.getBranchPid());
		
		sb.append(seriesbranch.getType());
		
		sb.append(seriesbranch.getVoiceDir());

		if (seriesbranch.getPatternCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + seriesbranch.getPatternCode() + "'");
		}

		if (seriesbranch.getArrowCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + seriesbranch.getArrowCode() + "'");
		}
		
		sb.append(seriesbranch.getArrowFlag());
		
		sb.append(",1,'" + seriesbranch.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

		StringBuilder sb = new StringBuilder("update " + seriesbranch.tableName()
				+ " set ");

		for (int i = 0; i < fieldNames.size(); i++) {

			if (i > 0) {
				sb.append(",");
			}

			String column = StringUtils.toColumnName(fieldNames.get(i));

			sb.append(column);

			sb.append("=");

			Field field = seriesbranch.getClass().getDeclaredField(fieldNames.get(i));

			Object value = field.get(seriesbranch);

			sb.append(value);

		}

		sb.append(" where row_id=");

		sb.append(seriesbranch.rowId());

		stmt.addBatch(sb.toString());
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + seriesbranch.tableName()
				+ " set u_record=2 where row_id='" + seriesbranch.rowId() + "'";

		stmt.addBatch(sql);
	}

}
