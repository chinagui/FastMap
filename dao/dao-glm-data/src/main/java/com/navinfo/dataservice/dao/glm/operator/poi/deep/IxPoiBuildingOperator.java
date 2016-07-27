package com.navinfo.dataservice.dao.glm.operator.poi.deep;

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
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI 深度信息(建筑物和租户的楼层信息) 操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiBuildingOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(IxPoiBuildingOperator.class);

	private IxPoiBuilding ixPoiBuilding;

	public IxPoiBuildingOperator(Connection conn, IxPoiBuilding ixPoiBuilding) {
		super(conn);
		this.ixPoiBuilding = ixPoiBuilding;
	}

	
	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiBuilding.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiBuilding.tableName());
		sb.append("(poi_pid, floor_used, floor_empty, memo, u_date,u_record, row_id) values (");

		sb.append(ixPoiBuilding.getPoiPid());
		if (StringUtils.isNotEmpty(ixPoiBuilding.getFloorUsed())) {
			sb.append(",'" + ixPoiBuilding.getFloorUsed() + "'");
		} else {
			sb.append(", null ");
		}

		if (StringUtils.isNotEmpty(ixPoiBuilding.getFloorEmpty())) {
			sb.append(",'" + ixPoiBuilding.getFloorEmpty() + "'");
		} else {
			sb.append(", null ");
		}

		if (StringUtils.isNotEmpty(ixPoiBuilding.getMemo())) {
			sb.append(",'" + ixPoiBuilding.getMemo() + "'");
		} else {
			sb.append(", null ");
		}
		sb.append(",'" + StringUtils.getCurrentTime() + "'");
		sb.append(",1,'" + ixPoiBuilding.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiBuilding.tableName() + " set u_record=3,u_date='"
				+ StringUtils.getCurrentTime() + "',");

		Set<Entry<String, Object>> set = ixPoiBuilding.changedFields()
				.entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiBuilding.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiBuilding);

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

			}
		}
		sb.append(" where row_id=hextoraw('" + ixPoiBuilding.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + ixPoiBuilding.tableName()
				+ " set u_record=2 ,u_date='" + StringUtils.getCurrentTime()
				+ "' where row_id=hextoraw('" + ixPoiBuilding.rowId() + "')";
		stmt.addBatch(sql);
	}

}
