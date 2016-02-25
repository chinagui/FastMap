package com.navinfo.dataservice.FosEngine.edit.model.operator.rd.restrict;

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
import com.navinfo.dataservice.FosEngine.edit.model.IOperator;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.commons.util.UuidUtils;

public class RdRestrictionConditionOperator implements IOperator {

	private static Logger logger = Logger
			.getLogger(RdRestrictionConditionOperator.class);

	private Connection conn;

	private RdRestrictionCondition condition;

	public RdRestrictionConditionOperator(Connection conn,
			RdRestrictionCondition condition) {
		this.conn = conn;

		this.condition = condition;
	}

	@Override
	public void insertRow() throws Exception {

		condition.setRowId(UuidUtils.genUuid());

		String sql = "insert into "
				+ condition.tableName()
				+ " (detail_id, time_domain, vehicle, res_trailer, res_weigh, res_axle_load, "
				+ "res_axle_count, res_out, u_record, row_id) values "
				+ "(?,?,?,?,?,?,?,?,?,?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, condition.getDetailId());

			pstmt.setString(2, condition.getTimeDomain());

			pstmt.setInt(3, condition.getVehicle());

			pstmt.setInt(4, condition.getResTrailer());

			pstmt.setDouble(5, condition.getResWeigh());

			pstmt.setDouble(6, condition.getResAxleLoad());

			pstmt.setInt(7, condition.getResAxleCount());

			pstmt.setInt(8, condition.getResOut());

			pstmt.setInt(9, 1);

			pstmt.setString(10, condition.rowId());

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

		StringBuilder sb = new StringBuilder("update " + condition.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = condition.changedFields()
					.entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = condition.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				Object value = field.get(condition);

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

				}
			}
			sb.append(" where row_id='" + condition.getRowId() + "'");

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

		String sql = "update " + condition.tableName()
				+ " set u_record=? where row_id=?";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setString(2, condition.rowId());

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

		condition.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(condition.tableName());

		sb.append("(detail_id, time_domain, vehicle, res_trailer, res_weigh, res_axle_load, "
				+ "res_axle_count, res_out, u_record, row_id) values (");

		sb.append(condition.getDetailId());

		if (condition.getTimeDomain() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + condition.getTimeDomain() + "'");
		}

		sb.append("," + condition.getVehicle());

		sb.append("," + condition.getResTrailer());

		sb.append("," + condition.getResWeigh());

		sb.append("," + condition.getResAxleLoad());

		sb.append("," + condition.getResAxleCount());

		sb.append("," + condition.getResOut());

		sb.append(",1,'" + condition.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

		StringBuilder sb = new StringBuilder("update " + condition.tableName()
				+ " set u_record=3,");

		for (int i = 0; i < fieldNames.size(); i++) {

			if (i > 0) {
				sb.append(",");
			}

			String column = StringUtils.toColumnName(fieldNames.get(i));

			sb.append(column);

			sb.append("=");

			Field field = condition.getClass().getDeclaredField(
					fieldNames.get(i));

			Object value = field.get(condition);

			sb.append(value);

		}

		sb.append(" where row_id=");

		sb.append(condition.rowId());

		stmt.addBatch(sb.toString());
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + condition.tableName()
				+ " set u_record=2 where row_id='" + condition.rowId() + "'";

		stmt.addBatch(sql);
	}

}
