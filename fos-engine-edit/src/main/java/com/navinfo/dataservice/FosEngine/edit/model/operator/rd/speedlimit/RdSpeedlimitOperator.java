package com.navinfo.dataservice.FosEngine.edit.model.operator.rd.speedlimit;

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
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.vividsolutions.jts.geom.Geometry;

public class RdSpeedlimitOperator implements IOperator {

	private static Logger logger = Logger.getLogger(RdSpeedlimitOperator.class);

	private Connection conn;

	private RdSpeedlimit speedlimit;

	public RdSpeedlimitOperator(Connection conn, RdSpeedlimit speedlimit) {
		this.conn = conn;

		this.speedlimit = speedlimit;
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

		StringBuilder sb = new StringBuilder("update " + speedlimit.tableName()
				+ " set ");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = speedlimit.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = speedlimit.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				Object value = field.get(speedlimit);

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

				} else if (value instanceof Geometry) {
					// 先降级转WKT

					String oldWkt = GeoTranslator.jts2Wkt((Geometry) value,
							0.00001, 5);

					String newWkt = Geojson.geojson2Wkt(columnValue.toString());

					if (!StringUtils.isStringSame(oldWkt, newWkt)) {
						sb.append("geometry=sdo_geometry('"
								+ String.valueOf(newWkt) + "',8307),");
					}
				}
			}
			sb.append(" where pid=" + speedlimit.getPid());

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

		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			this.deleteRow2Sql(stmt);

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
	public void insertRow2Sql(Statement stmt) throws Exception {

		speedlimit.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(speedlimit.tableName());

		sb.append("(pid,link_pid,direct,speed_value,speed_type,speed_dependent,speed_flag,limit_src,time_domain,capture_flag,descript,mesh_id,status,ck_status,adja_flag,rec_status_in,rec_status_out,time_descript,geometry,lane_speed_value,u_record,row_id) values (");

		sb.append(speedlimit.getPid());

		sb.append("," + speedlimit.getLinkPid());
		
		sb.append("," + speedlimit.getDirect());
		
		sb.append("," + speedlimit.getSpeedValue());
		
		sb.append("," + speedlimit.getSpeedType());
		
		sb.append("," + speedlimit.getSpeedDependent());
		
		sb.append("," + speedlimit.getSpeedFlag());
		
		sb.append("," + speedlimit.getLimitSrc());
		
		if (speedlimit.getTimeDomain() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + speedlimit.getTimeDomain() + "'");
		}
		
		sb.append("," + speedlimit.getCaptureFlag());
		
		if (speedlimit.getDescript() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + speedlimit.getDescript() + "'");
		}
		
		sb.append("," + speedlimit.getMeshId());
		
		sb.append("," + speedlimit.getStatus());
		
		sb.append("," + speedlimit.getCkStatus());
		
		sb.append("," + speedlimit.getAdjaFlag());
		
		sb.append("," + speedlimit.getRecStatusIn());
		
		sb.append("," + speedlimit.getRecStatusOut());
		
		if (speedlimit.getTimeDescript() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + speedlimit.getTimeDescript() + "'");
		}
		
		String wkt = GeoTranslator.jts2Wkt(speedlimit.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");
		
		if (speedlimit.getLaneSpeedValue() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + speedlimit.getLaneSpeedValue() + "'");
		}

		sb.append(",1,'" + speedlimit.rowId() + "')");

		stmt.addBatch(sb.toString());

	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + speedlimit.tableName()
				+ " set u_record=2 where pid=" + speedlimit.getPid();

		stmt.addBatch(sql);
	}

}
