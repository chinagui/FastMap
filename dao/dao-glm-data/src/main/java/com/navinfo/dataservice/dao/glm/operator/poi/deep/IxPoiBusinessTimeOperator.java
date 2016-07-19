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
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBusinessTime;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 索引:POI 深度信息(开放或营业时间) 操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiBusinessTimeOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(IxPoiBusinessTimeOperator.class);

	private IxPoiBusinessTime ixPoiBusinessTime;

	public IxPoiBusinessTimeOperator(Connection conn,
			IxPoiBusinessTime ixPoiBusinessTime) {
		super(conn);
		this.ixPoiBusinessTime = ixPoiBusinessTime;
	}

	
	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiBusinessTime.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiBusinessTime.tableName());
		sb.append("(poi_pid, mon_srt, mon_end, week_in_year_srt, week_in_year_end, week_in_month_srt, week_in_month_end, valid_week, day_srt, day_end, time_srt, time_dur, reserved, memo, u_date,u_record, row_id) values (");
		sb.append(ixPoiBusinessTime.getPoiPid());
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getMonSrt())) {
			sb.append(",'" + ixPoiBusinessTime.getMonSrt() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getMonEnd())) {
			sb.append(",'" + ixPoiBusinessTime.getMonEnd() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getWeekInYearSrt())) {
			sb.append(",'" + ixPoiBusinessTime.getWeekInYearSrt() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getWeekInYearEnd())) {
			sb.append(",'" + ixPoiBusinessTime.getWeekInYearEnd() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getWeekInMonthSrt())) {
			sb.append(",'" + ixPoiBusinessTime.getWeekInMonthSrt() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getWeekInMonthEnd())) {
			sb.append(",'" + ixPoiBusinessTime.getWeekInMonthEnd() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getVaildWeek())) {
			sb.append(",'" + ixPoiBusinessTime.getVaildWeek() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getDaySrt())) {
			sb.append(",'" + ixPoiBusinessTime.getDaySrt() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getDayEnd())) {
			sb.append(",'" + ixPoiBusinessTime.getDayEnd() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getTimeSrt())) {
			sb.append(",'" + ixPoiBusinessTime.getTimeSrt() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getTimeDue())) {
			sb.append(",'" + ixPoiBusinessTime.getTimeDue() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getReserved())) {
			sb.append(",'" + ixPoiBusinessTime.getReserved() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiBusinessTime.getMemo())) {
			sb.append(",'" + ixPoiBusinessTime.getMemo() + "'");
		} else {
			sb.append(", null ");
		}
		sb.append(",'" + StringUtils.getCurrentTime() + "'");
		sb.append(",1,'" + ixPoiBusinessTime.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiBusinessTime.tableName() + " set u_record=3,u_date='"
				+ StringUtils.getCurrentTime() + "',");

		Set<Entry<String, Object>> set = ixPoiBusinessTime.changedFields()
				.entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiBusinessTime.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiBusinessTime);

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
		sb.append(" where row_id=hextoraw('" + ixPoiBusinessTime.getRowId()
				+ "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + ixPoiBusinessTime.tableName()
				+ " set u_record=2 ,u_date='" + StringUtils.getCurrentTime()
				+ "' where row_id=hextoraw('" + ixPoiBusinessTime.rowId()
				+ "')";
		stmt.addBatch(sql);
	}

}
