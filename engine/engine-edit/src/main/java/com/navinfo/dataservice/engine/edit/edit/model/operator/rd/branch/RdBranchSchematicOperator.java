package com.navinfo.dataservice.engine.edit.edit.model.operator.rd.branch;

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

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.engine.edit.edit.model.IOperator;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.branch.RdBranchSchematic;

public class RdBranchSchematicOperator implements IOperator {

	private static Logger logger = Logger
			.getLogger(RdBranchSchematicOperator.class);

	private Connection conn;

	private RdBranchSchematic schematic;

	public RdBranchSchematicOperator(Connection conn,
			RdBranchSchematic schematic) {
		this.conn = conn;

		this.schematic = schematic;
	}

	@Override
	public void insertRow() throws Exception {

		schematic.setRowId(UuidUtils.genUuid());

		String sql = "insert into "
				+ schematic.tableName()
				+ " (schematic_id, branch_pid, schematic_code, arrow_code, memo, u_record, row_id) values "
				+ "(:1,:2,:3,:4,:5,:6,:7)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, schematic.getBranchPid());

			pstmt.setInt(2, schematic.getBranchPid());

			pstmt.setString(3, schematic.getSchematicCode());

			pstmt.setString(4, schematic.getArrowCode());
			
			pstmt.setString(5, schematic.getMemo());

			pstmt.setInt(6, 1);

			pstmt.setString(7, schematic.rowId());

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

		StringBuilder sb = new StringBuilder("update " + schematic.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = schematic.changedFields()
					.entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = schematic.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(schematic);

				column = StringUtils.toColumnName(column);

				if (value instanceof String || value==null ) {

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

				} else if (value instanceof JSONObject) {
					if (!StringUtils.isStringSame(value.toString(),
							String.valueOf(columnValue))) {
						sb.append("geometry=sdo_geometry('"
								+ String.valueOf(columnValue) + "',8307),");
					}
				}
			}
			sb.append(" where schematic_id=" + schematic.getPid());

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

		String sql = "update " + schematic.tableName()
				+ " set u_record=? where schematic_id=?";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setInt(2, schematic.getPid());

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

		schematic.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(schematic.tableName());

		sb.append("(schematic_id, branch_pid, schematic_code, arrow_code, memo, u_record, row_id) values (");

		sb.append(schematic.getPid());

		sb.append("," + schematic.getBranchPid());

		if (schematic.getSchematicCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + schematic.getSchematicCode() + "'");
		}

		if (schematic.getArrowCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + schematic.getArrowCode() + "'");
		}

		if (schematic.getMemo() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + schematic.getMemo() + "'");
		}

		sb.append(",1,'" + schematic.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + schematic.tableName()
				+ " set u_record=2 where schematic_id=" + schematic.getPid();

		stmt.addBatch(sql);
	}

}
