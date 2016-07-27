package com.navinfo.dataservice.dao.glm.operator.poi.deep;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;


import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAdvertisement;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAttraction;

import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

/**
 * 索引:POI 深度信息(景点类) 操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiAttractionOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(IxPoiAttractionOperator.class);

	private IxPoiAttraction ixPoiAttraction;

	public IxPoiAttractionOperator(Connection conn,
			IxPoiAttraction ixPoiAttraction) {
		super(conn);
		this.ixPoiAttraction = ixPoiAttraction;
	}


	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiAttraction.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiAttraction.tableName());
		sb.append("(attraction_id, poi_pid, sight_level, long_description, long_descrip_eng, ticket_price, ticket_price_eng, open_hour, open_hour_eng, telephone, address, city, photo_name, parking, travelguide_flag,u_date,  u_record, row_id) values (");
		sb.append(ixPoiAttraction.getPid());
		sb.append("," + ixPoiAttraction.getPoiPid());
		sb.append("," + ixPoiAttraction.getSightLevel());
		if (StringUtils.isNotEmpty(ixPoiAttraction.getLongDescription())) {
			sb.append(",'" + ixPoiAttraction.getLongDescription() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiAttraction.getLongDescripEng())) {
			sb.append(",'" + ixPoiAttraction.getLongDescripEng() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiAttraction.getTicketPrice())) {
			sb.append(",'" + ixPoiAttraction.getTicketPrice() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiAttraction.getTicketPriceEng())) {
			sb.append(",'" + ixPoiAttraction.getTicketPriceEng() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiAttraction.getOpenHour())) {
			sb.append(",'" + ixPoiAttraction.getOpenHour() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiAttraction.getOpenHourEng())) {
			sb.append(",'" + ixPoiAttraction.getOpenHourEng() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiAttraction.getTelephone())) {
			sb.append(",'" + ixPoiAttraction.getTelephone() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiAttraction.getAddress())) {
			sb.append(",'" + ixPoiAttraction.getAddress() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiAttraction.getCity())) {
			sb.append(",'" + ixPoiAttraction.getCity() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiAttraction.getPhotoName())) {
			sb.append(",'" + ixPoiAttraction.getPhotoName() + "'");
		} else {
			sb.append(", null ");
		}
		sb.append("," + ixPoiAttraction.getParking());
		sb.append("," + ixPoiAttraction.getTravelguideFlag());
		sb.append(",'" + StringUtils.getCurrentTime() + "'");
		sb.append(",1,'" + ixPoiAttraction.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiAttraction.tableName() + " set u_record=3,u_date='"
				+ StringUtils.getCurrentTime() + "',");

		Set<Entry<String, Object>> set = ixPoiAttraction.changedFields()
				.entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiAttraction.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiAttraction);

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
		sb.append(" where attraction_id   =" + ixPoiAttraction.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiAttraction.tableName()
				+ " set u_record=2 ,u_date='" + StringUtils.getCurrentTime()
				+ "' where attraction_id    =" + ixPoiAttraction.getPid();
		stmt.addBatch(sql);
	}

}
