package com.navinfo.dataservice.dao.glm.operator;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.vividsolutions.jts.geom.Geometry;

/***
 * 基础操作类
 * 
 * @author zhaokk
 * 
 */

public class BasicOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(BasicOperator.class);
	private static final String M_PID = "pid";
	private static final String M_PRIMARYKEY = "primaryKey";
	private static final String M_ROW_ID = "row_id";
	private static final String M_STRING = "class java.lang.String";
	private static final String M_GEOMETRY = "class com.vividsolutions.jts.geom.Geometry";
	private static final String M_DATE = "class java.util.Date";
	private static final String M_U_DATE = "u_date";
	private static final String M_U_RECORD = "u_record";
	private IRow row;

	public BasicOperator(Connection conn, IRow row) throws Exception {
		super(conn);
		this.row = row;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		this.generateInsertSql(stmt, row);
		this.modifyPoiStatus();
	}

	/***
	 * 自动组装新增语句
	 * 
	 * @param stmt
	 * @param row
	 * @throws Exception
	 */
	public void generateInsertSql(Statement stmt, IRow row) throws Exception {
		Class<?> c = row.getClass();
		Field[] fields = c.getDeclaredFields();
		StringBuilder key = new StringBuilder();
		StringBuilder value = new StringBuilder();
		key.append("insert into ");
		key.append(row.tableName());
		key.append("(");
		value.append(" values ( ");
		for (Field f : fields) {
			if (isBasicType(f)) {
				if (f.getModifiers() == 4) {
					continue;
				}
				f.setAccessible(true);
				Object oj = f.get(row);
				if (oj instanceof Integer) {
					oj = Integer.parseInt(String.valueOf(oj));
				}
				if (oj instanceof Double) {
					oj = Double.parseDouble(String.valueOf(oj));
				}
				if (oj instanceof String) {
					oj = "'" + String.valueOf(oj) + "'";
				}
				if (oj instanceof Geometry) {
					String wkt = GeoTranslator.jts2Wkt((Geometry) oj, 0.00001, 5);

					oj = ("sdo_geometry('" + wkt + "',8307)");
				}
				String name = f.getName().toString();
				if ((StringUtils.toColumnName(name).equals(M_U_RECORD)))
					continue;
				if (row instanceof IxPoi) {
					if ((StringUtils.toColumnName(name).equals("status")))
						continue;
				}
				if (StringUtils.toColumnName(name).equals(M_U_DATE)) {
					key.append(M_U_DATE + ",");
					value.append("'" + StringUtils.getCurrentTime() + "',");
				} else if (StringUtils.toColumnName(name).equals(M_PID) && row instanceof IObj) {
					try {
						key.append(c.getMethod(M_PRIMARYKEY).invoke(row) + ",");
						value.append(oj + ",");

					} catch (Exception e) {
						if (e instanceof NoSuchMethodException) {
							key.append(StringUtils.toColumnName(name) + ",");
							value.append(oj + ",");
						}
					}

				} else if (StringUtils.toColumnName(name).equals(M_ROW_ID)) {
					key.append(M_ROW_ID + ",");
					row.setRowId(UuidUtils.genUuid());
					value.append("'" + row.rowId() + "',");
				} else if(row instanceof IxPoiPhoto && name.equals("fccPid"))
				{
					key.append("pid,");
					value.append(oj + ",");
				}
				else {
					key.append(StringUtils.toColumnName(name) + ",");
					value.append(oj + ",");
				}

			}

		}
		key.append(M_U_RECORD + ")");
		value.append(1 + ")");
		System.out.println(key + " " + value);
		stmt.addBatch(key.append(value).toString());
		if (row.children() != null) {
			List<List<IRow>> lists = row.children();
			for (List<IRow> list : lists) {
				for (IRow iRow : list) {
					this.generateInsertSql(stmt, iRow);
				}
			}
		}
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + row.tableName() + " set u_record=3 ");
		this.addConditionForPoi(sb);
		Set<Entry<String, Object>> set = row.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = row.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(row);

			column = StringUtils.toColumnName(column);
			
			if (value instanceof String || value == null) {
				
				if (!StringUtils.isStringSame(String.valueOf(value), String.valueOf(columnValue))) {
					if(row instanceof IxPoiPhoto && column.equals("fccPid"))
					{
						column = "pid";
					}
					if (columnValue == null) {
						sb.append(column + "=null,");
					} else {
						sb.append(column + "='" + String.valueOf(columnValue) + "',");
					}
					this.setChanged(true);
				}

			} else if (value instanceof Double) {

				if (Double.parseDouble(String.valueOf(value)) != Double.parseDouble(String.valueOf(columnValue))) {
					sb.append(column + "=" + Double.parseDouble(String.valueOf(columnValue)) + ",");

					this.setChanged(true);
				}

			} else if (value instanceof Long) {

				if (Long.parseLong(String.valueOf(value)) != Long.parseLong(String.valueOf(columnValue))) {
					sb.append(column + "=" + Long.parseLong(String.valueOf(columnValue)) + ",");

					this.setChanged(true);
				}

			} else if (value instanceof Integer) {

				if (Integer.parseInt(String.valueOf(value)) != Integer.parseInt(String.valueOf(columnValue))) {
					sb.append(column + "=" + Integer.parseInt(String.valueOf(columnValue)) + ",");

					this.setChanged(true);
				}

			} else if (value instanceof Geometry) {
				// 先降级转WKT

				String oldWkt = GeoTranslator.jts2Wkt((Geometry) value, 0.00001, 5);

				String newWkt = Geojson.geojson2Wkt(columnValue.toString());

				if (!StringUtils.isStringSame(oldWkt, newWkt)) {
					sb.append("geometry=sdo_geometry('" + String.valueOf(newWkt) + "',8307),");

					this.setChanged(true);
				}
			}
		}
		sb.append(getWhereForTable(row));
		String sql = sb.toString();
		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);
		this.modifyPoiStatus();

	}

	public String getWhereForTable(IRow row) {
		try {
			if (row.getClass().getMethod(M_PRIMARYKEY).invoke(row) != null) {
				return "" + row.getClass().getMethod("M_PRIMARYKEY").invoke(row) + " = "
						+ row.getClass().getMethod(M_PID).invoke(row) + "";
			} else {
				return " where row_id=hextoraw('" + row.rowId() + "')";
			}
		} catch (Exception e) {
			if (e instanceof NoSuchMethodException) {
				return " where row_id=hextoraw('" + row.rowId() + "')";
			}
		}

		return "";
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		this.generateDeleteSql(stmt, row);
		this.modifyPoiStatus();
	}

	/***
	 * 自动组装删除语句
	 * 
	 * @param stmt
	 * @param row
	 * @throws Exception
	 */
	private void generateDeleteSql(Statement stmt, IRow row) throws Exception {
		StringBuilder sb = new StringBuilder("update " + row.tableName() + " set u_record=2 ");
		this.addConditionForPoi(sb);
		sb.append(getWhereForTable(row));
		String sql = sb.toString();
		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);
		if (row.children() != null) {
			List<List<IRow>> lists = row.children();
			for (List<IRow> list : lists) {
				for (IRow iRow : list) {
					this.generateDeleteSql(stmt, iRow);
				}
			}
		}
	}

	/**
	 * 判断模型基本类型
	 * 
	 * @param f
	 * @return
	 */
	public static boolean isBasicType(Field f) {
		if (f.getGenericType().toString().equals(M_STRING)) {
			return true;
		}
		if (f.getType() == Integer.TYPE) {
			return true;
		}
		if (f.getType() == Double.TYPE) {
			return true;
		}
		if (f.getGenericType().toString().equals(M_GEOMETRY)) {
			return true;
		}
		if (f.getGenericType().toString().equals(M_DATE)) {
			return true;
		}
		if (f.getType() == Float.TYPE) {
			return true;
		}
		if (f.getType() == Short.TYPE) {
			return true;
		}
		if (f.getType() == Long.TYPE) {
			return true;
		}

		return false;
	}

	private void addConditionForPoi(StringBuilder sb) {
		if (row instanceof IxPoi || row instanceof IxPoiChildren || row instanceof IxPoiParent) {
			sb.append(",u_date = " + StringUtils.getCurrentTime() + ",");
		} else {
			sb.append(",");
		}

	}

	private void modifyPoiStatus() throws Exception {
		if (row instanceof IxPoi) {
			if (org.apache.commons.lang.StringUtils.isBlank(row.rowId())) {
				row.setRowId(UuidUtils.genUuid());
			}
			upatePoiStatus();
		}
		if (row instanceof IxPoiParent) {
			IxPoiParent ixPoiParent = (IxPoiParent) row;
			row.setRowId(new IxPoiSelector(conn).loadRowIdByPid(ixPoiParent.getParentPoiPid(), false));
			upatePoiStatus();
		}
		if (row instanceof IxPoiChildren) {
			IxPoiChildren ixPoiParent = (IxPoiChildren) row;
			row.setRowId(new IxPoiSelector(conn).loadRowIdByPid(ixPoiParent.getChildPoiPid(), false));
			upatePoiStatus();

		}

	}

	/**
	 * poi操作修改poi状态为已作业，鲜度信息为0 zhaokk sourceFlag 0 web 1 Android
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void upatePoiStatus() throws Exception {
		StringBuilder sb = new StringBuilder(" MERGE INTO poi_edit_status T1 ");
		sb.append(" USING (SELECT '" + row.rowId() + "' as a, 2 as b,0 as c FROM dual) T2 ");
		sb.append(" ON ( T1.row_id=T2.a) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c ");
		sb.append(" WHEN NOT MATCHED THEN ");
		sb.append(" INSERT (T1.row_id,T1.status,T1.fresh_verified) VALUES(T2.a,T2.b,T2.c)");
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
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

	public static void main(String[] args) {
		AdFace adFace = new AdFace();
		adFace.setArea(2332);
		Field[] fields = adFace.getClass().getDeclaredFields();
		for (Field f : fields) {
			System.out.println(f.getModifiers());
			// f.setAccessible(true);

			System.out.println(f);
			System.out.println();
		}
	}
}
