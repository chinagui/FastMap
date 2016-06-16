package com.navinfo.dataservice.dao.glm.operator.poi.index;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiEntryimage;

/**
 * POI入口概略图表操作类
 * 
 * @author zhangxiaolong
 *
 */
public class IxPoiEntryImageOperator implements IOperator {

	private Connection conn;

	private IxPoiEntryimage ixPoiEntryimage;

	public IxPoiEntryImageOperator(Connection conn, IxPoiEntryimage ixPoiEntryimage) {
		this.conn = conn;

		this.ixPoiEntryimage = ixPoiEntryimage;
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
		StringBuilder sb = new StringBuilder("update " + ixPoiEntryimage.tableName() + " set u_record=3,u_date="+StringUtils.getCurrentTime()+",");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ixPoiEntryimage.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = ixPoiEntryimage.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(ixPoiEntryimage);

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
			sb.append(" where poi_pid= " + ixPoiEntryimage.getPoiPid());

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
		String sql = "update " + ixPoiEntryimage.tableName() + " set u_record=:1 where poi_pid=:2";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setInt(2, ixPoiEntryimage.getPoiPid());

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
		ixPoiEntryimage.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoiEntryimage.tableName());

		sb.append(
				"(POI_PID, IMAGE_CODE, X_PIXEL_R4, Y_PIXEL_R4, X_PIXEL_R5, Y_PIXEL_R5, X_PIXEL_35, Y_PIXEL_35, MEMO, MAIN_POI_PID, U_RECORD, ROW_ID) values (");

		sb.append(ixPoiEntryimage.getPoiPid());

		sb.append(",'" + ixPoiEntryimage.getImageCode() + "'");

		sb.append("," + ixPoiEntryimage.getxPixelR4());

		sb.append("," + ixPoiEntryimage.getyPixelR4());

		sb.append("," + ixPoiEntryimage.getxPixelR5());

		sb.append("," + ixPoiEntryimage.getyPixelR5());

		sb.append("," + ixPoiEntryimage.getxPixel35());

		sb.append("," + ixPoiEntryimage.getyPixel35());

		sb.append(",'" + ixPoiEntryimage.getMemo() + "'");

		sb.append("," + ixPoiEntryimage.getMainPoiPid());

		sb.append(",1,'" + ixPoiEntryimage.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt) throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiEntryimage.tableName() + " set u_record=2 where row_id=hextoraw('"
				+ ixPoiEntryimage.rowId() + "')";

		stmt.addBatch(sql);
	}

}
