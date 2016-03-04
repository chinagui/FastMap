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
import com.navinfo.dataservice.FosEngine.edit.model.IOperator;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.commons.util.UuidUtils;

public class RdBranchRealimageOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdBranchRealimageOperator.class);

	private Connection conn;

	private RdBranchRealimage realimage;

	public RdBranchRealimageOperator(Connection conn, RdBranchRealimage realimage) {
		this.conn = conn;

		this.realimage = realimage;
	}

	@Override
	public void insertRow() throws Exception {

		realimage.setRowId(UuidUtils.genUuid());

		String sql = "insert into "
				+ realimage.tableName()
				+ " (branch_pid, image_type, real_code, arrow_code, u_record, row_id) values "
				+ "(:1,:2,:3,:4,:5,:6)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, realimage.getBranchPid());

			pstmt.setInt(2, realimage.getImageType());

			pstmt.setString(3, realimage.getRealCode());

			pstmt.setString(4, realimage.getArrowCode());

			pstmt.setInt(5, 1);

			pstmt.setString(6, realimage.rowId());

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

		StringBuilder sb = new StringBuilder("update " + realimage.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = realimage.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = realimage.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(realimage);

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

				}
			}
			sb.append(" where row_id=hextoraw('" + realimage.getRowId());

			sb.append(realimage.getRowId());

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

		String sql = "update " + realimage.tableName()
				+ " set u_record=? where row_id=hextoraw(?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setString(2, realimage.rowId());

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

		realimage.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(realimage.tableName());

		sb.append("(branch_pid, image_type, real_code, arrow_code, u_record, row_id) values (");

		sb.append(realimage.getBranchPid());

		sb.append("," + realimage.getImageType());

		if (realimage.getRealCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + realimage.getRealCode() + "'");
		}

		if (realimage.getArrowCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + realimage.getArrowCode() + "'");
		}
		
		sb.append(",1,'" + realimage.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {
		
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + realimage.tableName()
				+ " set u_record=2 where row_id=hextoraw('" + realimage.rowId() + "')";

		stmt.addBatch(sql);
	}

}
