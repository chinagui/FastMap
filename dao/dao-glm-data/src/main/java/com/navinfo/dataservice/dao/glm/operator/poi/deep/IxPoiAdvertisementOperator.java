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
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAdvertisement;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

/**
 * 索引:POI 深度信息(广告类)操作
 * 
 * @author zhaokk
 * 
 */
public class IxPoiAdvertisementOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(IxPoiAdvertisementOperator.class);

	private IxPoiAdvertisement ixPoiAdvertisement;

	public IxPoiAdvertisementOperator(Connection conn,
			IxPoiAdvertisement ixPoiAdvertisement) {
		super(conn);
		this.ixPoiAdvertisement = ixPoiAdvertisement;
	}


	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiAdvertisement.setRowId(UuidUtils.genUuid());
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(ixPoiAdvertisement.tableName());
		sb.append("(advertise_id, poi_pid, label_text, type, priority,start_time, end_time,u_date, u_record,row_id) values (");
		sb.append(ixPoiAdvertisement.getPid());
		sb.append("," + ixPoiAdvertisement.getPoiPid());
		if (StringUtils.isNotEmpty(ixPoiAdvertisement.getLableText())) {
			sb.append(",'" + ixPoiAdvertisement.getLableText() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiAdvertisement.getType())) {
			sb.append(",'" + ixPoiAdvertisement.getType() + "'");
		} else {
			sb.append(", null ");
		}
		sb.append("," + ixPoiAdvertisement.getPriority());
		if (StringUtils.isNotEmpty(ixPoiAdvertisement.getStartTime())) {
			sb.append(",'" + ixPoiAdvertisement.getStartTime() + "'");
		} else {
			sb.append(", null ");
		}
		if (StringUtils.isNotEmpty(ixPoiAdvertisement.getEndTime())) {
			sb.append(",'" + ixPoiAdvertisement.getEndTime() + "'");
		} else {
			sb.append(", null ");
		}
		sb.append(",'" + StringUtils.getCurrentTime() + "'");
		sb.append(",1,'" + ixPoiAdvertisement.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ ixPoiAdvertisement.tableName() + " set u_record=3,u_date='"
				+ StringUtils.getCurrentTime() + "',");

		Set<Entry<String, Object>> set = ixPoiAdvertisement.changedFields()
				.entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiAdvertisement.getClass()
					.getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiAdvertisement);

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
		sb.append(" where advertise_id=" + ixPoiAdvertisement.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiAdvertisement.tableName()
				+ " set u_record=2,u_date='" + StringUtils.getCurrentTime()
				+ "' where advertise_id   =" + ixPoiAdvertisement.getPid();
		stmt.addBatch(sql);
	}

}
