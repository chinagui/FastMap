package com.navinfo.dataservice.dao.glm.operator.poi.index;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiIcon;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.ad.geo.AdFaceTopoOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI图标(3DICON)表 操作
 * 
 * @author luyao
 * 
 */
public class IxPoiIconOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private IxPoiIcon ixPoiIcon;

	public IxPoiIconOperator(Connection conn, IxPoiIcon ixPoiIcon) {
		super(conn);

		this.ixPoiIcon = ixPoiIcon;
	}


	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiIcon.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoiIcon.tableName());

		sb.append("(rel_id, poi_pid,icon_name, geometry, manage_code, client_flag, row_id,u_date,u_record) values (");

		sb.append(ixPoiIcon.getPid());

		sb.append("," + ixPoiIcon.getPoiPid());

		if (StringUtils.isNotEmpty(ixPoiIcon.getIconName())) {
			sb.append(",'" + ixPoiIcon.getIconName() + "'");
		} else {
			sb.append(",null");
		}

		String wkt = GeoTranslator.jts2Wkt(ixPoiIcon.getGeometry(), 0.00001, 5);

		sb.append(",sdo_geometry('" + wkt + "',8307)");

		if (StringUtils.isNotEmpty(ixPoiIcon.getManageCode())) {
			sb.append(",'" + ixPoiIcon.getManageCode() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoiIcon.getClientFlag())) {
			sb.append(",'" + ixPoiIcon.getClientFlag() + "'");
		} else {
			sb.append(",null");
		}

		sb.append(",'" + ixPoiIcon.getRowId() + "'");

		sb.append(",'" + StringUtils.getCurrentTime() + "'");

		sb.append(",'1')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + ixPoiIcon.tableName()
				+ " set u_record=3,u_date= '" + StringUtils.getCurrentTime()
				+ "',");

		Set<Entry<String, Object>> set = ixPoiIcon.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiIcon.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiIcon);

			column = StringUtils.toColumnName(column);

			if (value instanceof String || value == null) {

				if (!StringUtils.isStringSame(String.valueOf(value),
						String.valueOf(columnValue))) {

					if (columnValue == null) {
						sb.append(column + "=null,");
					} else {
						sb.append(column + "='" + String.valueOf(columnValue)
								+ "',");
					}
					this.setChanged(true);
				}

			} else if (value instanceof Double) {

				if (Double.parseDouble(String.valueOf(value)) != Double
						.parseDouble(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Double.parseDouble(String.valueOf(columnValue))
							+ ",");

					this.setChanged(true);
				}

			} else if (value instanceof Integer) {

				if (Integer.parseInt(String.valueOf(value)) != Integer
						.parseInt(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Integer.parseInt(String.valueOf(columnValue))
							+ ",");

					this.setChanged(true);
				}

			} else if (value instanceof Geometry) {
				// 先降级转WKT

				String oldWkt = GeoTranslator.jts2Wkt((Geometry) value,
						0.00001, 5);

				String newWkt = Geojson.geojson2Wkt(columnValue.toString());

				if (!StringUtils.isStringSame(oldWkt, newWkt)) {
					sb.append("geometry=sdo_geometry('"
							+ String.valueOf(newWkt) + "',8307),");

					this.setChanged(true);
				}
			}
		}
		sb.append(" where rel_id=" + ixPoiIcon.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiIcon.tableName()
				+ " set u_record=2,u_date= '" + StringUtils.getCurrentTime()
				+ "' where rel_id=" + ixPoiIcon.getPid();

		stmt.addBatch(sql);
	}

}
