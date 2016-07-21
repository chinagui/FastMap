package com.navinfo.dataservice.dao.glm.operator.rd.eleceye;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.vividsolutions.jts.geom.Geometry;

public class RdElectroniceyeOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdEleceyePartOperator.class);

	private RdElectroniceye eleceye;

	public RdElectroniceyeOperator(Connection conn, RdElectroniceye eleceye) {
		super(conn);
		this.eleceye = eleceye;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		eleceye.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(eleceye.tableName());
		sb.append(
				"(pid,link_pid,direct,kind,location,angle,speed_limit,verified_flag,mesh_id,geometry,src_flag,creation_date,high_violation,u_record,row_id) values (");
		sb.append(eleceye.getPid());
		sb.append("," + eleceye.getLinkPid());
		sb.append("," + eleceye.getDirect());
		sb.append("," + eleceye.getKind());
		sb.append("," + eleceye.getLocation());
		sb.append("," + eleceye.getAngle());
		sb.append("," + eleceye.getSpeedLimit());
		sb.append("," + eleceye.getVerifiedFlag());
		sb.append("," + eleceye.getMeshId());
		String wkt = GeoTranslator.jts2Wkt(eleceye.getGeometry(), 0.00001, 5);
		sb.append(",sdo_geometry('" + wkt + "',8307)");
		sb.append("," + eleceye.getSrcFlag());
		Date creationDate = eleceye.getCreationDate();
		if (null != creationDate) {
			String format = "YYYYMMDDHHMMSS";
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			sb.append(",to_date(\"" + sdf.format(creationDate) + "\",\"" + format + "\"");
		} else {
			sb.append("," + creationDate);
		}
		sb.append("," + eleceye.getHighViolation());
		sb.append(",1,'" + eleceye.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + eleceye.tableName() + " set u_record = 2 where pid = " + eleceye.pid();

		stmt.addBatch(sql);
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + eleceye.tableName() + " set u_record = 3,");

		Set<Entry<String, Object>> set = eleceye.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = eleceye.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(eleceye);

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

			} else if (value instanceof Geometry) {
				// 先降级转WKT

				String oldWkt = GeoTranslator.jts2Wkt((Geometry) value, 0.00001, 5);

				String newWkt = Geojson.geojson2Wkt(columnValue.toString());

				if (!StringUtils.isStringSame(oldWkt, newWkt)) {
					sb.append("geometry=sdo_geometry('" + String.valueOf(newWkt) + "',8307),");
				}
			}
		}
		sb.append(" where row_id = '" + eleceye.rowId() + "'");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql.toString());
	}

}
