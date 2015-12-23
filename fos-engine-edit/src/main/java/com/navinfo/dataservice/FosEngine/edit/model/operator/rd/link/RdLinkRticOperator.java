package com.navinfo.dataservice.FosEngine.edit.model.operator.rd.link;

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
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkRtic;
import com.navinfo.dataservice.commons.util.UuidUtils;

public class RdLinkRticOperator implements IOperator {

	private static Logger logger = Logger
			.getLogger(RdLinkRticOperator.class);

	private Connection conn;

	private RdLinkRtic rtic;

	public RdLinkRticOperator(Connection conn, RdLinkRtic rtic) {
		this.conn = conn;

		this.rtic = rtic;
	}

	@Override
	public void insertRow() throws Exception {

		rtic.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder(
				"insert into rd_link_rtic(link_pid,code,rank,rtic_dir,updown_flag,range_type,u_record,row_id) values (:1,:2,:3,:4,:5,:6,1,:7)");

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, rtic.getLinkPid());

			pstmt.setInt(2, rtic.getCode());

			pstmt.setInt(3, rtic.getRank());

			pstmt.setInt(4, rtic.getRticDir());

			pstmt.setInt(5, rtic.getUpdownFlag());

			pstmt.setInt(6, rtic.getRangeType());

			pstmt.setString(7, rtic.getRowId());

			pstmt.execute();
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

		StringBuilder sb = new StringBuilder("update " + rtic.tableName()
				+ " set ");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = rtic.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = rtic.getClass().getDeclaredField(column);

				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(rtic);

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

				} else if (value instanceof JSONObject) {
					if (!StringUtils.isStringSame(value.toString(),
							String.valueOf(columnValue))) {
						sb.append("geometry=sdo_geometry('"
								+ String.valueOf(columnValue) + "',8307),");
					}
				}
			}
			sb.append(" where row_id='" + rtic.rowId() + "'");

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

		String sql = "update " + rtic.tableName()
				+ " set u_record=2 where row_id=?";

		PreparedStatement pstmt = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, rtic.getRowId());

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

		rtic.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder(
				"insert into rd_link_rtic(link_pid,code,rank,rtic_dir,updown_flag,range_type,u_record,row_id) values (");

		sb.append(rtic.getLinkPid());

		sb.append(",");

		sb.append(rtic.getCode());

		sb.append(",");

		sb.append(rtic.getRank());

		sb.append(",");

		sb.append(rtic.getRticDir());

		sb.append(",");

		sb.append(rtic.getUpdownFlag());

		sb.append(",");

		sb.append(rtic.getRangeType());

		sb.append(",1,'" + rtic.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> columns, Statement stmt) {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update rd_link_rtic set u_record=2 where row_id = '"
				+ rtic.getRowId() + "'";

		stmt.addBatch(sql);
	}

}
