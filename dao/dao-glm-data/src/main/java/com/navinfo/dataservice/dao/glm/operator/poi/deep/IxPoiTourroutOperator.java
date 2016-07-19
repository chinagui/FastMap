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
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAdvertisement;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAttraction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiIntroduction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiTourroute;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI 深度信息(旅游线路类)操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiTourroutOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(IxPoiTourroutOperator.class);

	private IxPoiTourroute ixPoiTourroute;

	public IxPoiTourroutOperator(Connection conn, IxPoiTourroute ixPoiTourroute) {
		super(conn);
		this.ixPoiTourroute = ixPoiTourroute;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiTourroute.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiTourroute.tableName());
		sb.append("(tour_id, tour_name, tour_name_eng, tour_intr, tour_intr_eng, tour_type, tour_type_eng, tour_x, tour_y, tour_len, trail_time, visit_time, poi_pid, reserved, memo, u_date,u_record, row_id) values (");
		sb.append(ixPoiTourroute.getPid());
		if (StringUtils.isNotEmpty(ixPoiTourroute.getTourName())) {
			sb.append(",'" + ixPoiTourroute.getTourName() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiTourroute.getTourNameEng())) {
			sb.append(",'" + ixPoiTourroute.getTourNameEng() + "'");
		} else {
			sb.append(", null ");
		}

		if (StringUtils.isNotEmpty(ixPoiTourroute.getTourIntr())) {
			sb.append(",'" + ixPoiTourroute.getTourIntr() + "'");
		} else {
			sb.append(", null ");
		}

		if (StringUtils.isNotEmpty(ixPoiTourroute.getTourIntrEng())) {
			sb.append(",'" + ixPoiTourroute.getTourIntrEng() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiTourroute.getTourType())) {
			sb.append(",'" + ixPoiTourroute.getTourType() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiTourroute.getTourTypeEng())) {
			sb.append(",'" + ixPoiTourroute.getTourTypeEng() + "'");
		} else {
			sb.append(", null ");
		}

		sb.append("," + ixPoiTourroute.getTourX());
		sb.append("," + ixPoiTourroute.getTourY());
		sb.append("," + ixPoiTourroute.getTourLen());
		if (StringUtils.isNotEmpty(ixPoiTourroute.getTrailTime())) {
			sb.append(",'" + ixPoiTourroute.getTrailTime() + "'");
		} else {
			sb.append(", null ");
		}

		if (StringUtils.isNotEmpty(ixPoiTourroute.getVisitTime())) {
			sb.append(",'" + ixPoiTourroute.getVisitTime() + "'");
		} else {
			sb.append(", null ");
		}

		if (StringUtils.isNotEmpty(ixPoiTourroute.getPoiPid())) {
			sb.append(",'" + ixPoiTourroute.getPoiPid() + "'");
		} else {
			sb.append(", null ");
		}

		if (StringUtils.isNotEmpty(ixPoiTourroute.getReserved())) {
			sb.append(",'" + ixPoiTourroute.getReserved() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiTourroute.getMemo())) {
			sb.append(",'" + ixPoiTourroute.getMemo() + "'");
		} else {
			sb.append(", null ");
		}

		sb.append("," + ixPoiTourroute.getTravelguideFlag());
		sb.append(",'" + StringUtils.getCurrentTime() + "'");
		sb.append(",1,'" + ixPoiTourroute.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiTourroute.tableName() + " set u_record=3,u_date='"
				+ StringUtils.getCurrentTime() + "',");

		Set<Entry<String, Object>> set = ixPoiTourroute.changedFields()
				.entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiTourroute.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiTourroute);

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
		sb.append(" where tour_id    =" + ixPoiTourroute.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiTourroute.tableName()
				+ " set u_record=2 ,u_date='" + StringUtils.getCurrentTime()
				+ "' where   tour_id      =" + ixPoiTourroute.getPid();
		stmt.addBatch(sql);
	}

}
