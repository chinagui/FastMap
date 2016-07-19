package com.navinfo.dataservice.dao.glm.operator.ad.geo;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminDetail;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.gsc.RdGscLinkOperator;

public class AdAdminDetailOperator extends AbstractOperator {
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);

	private AdAdminDetail adminDetail;

	public AdAdminDetailOperator(Connection conn, AdAdminDetail adminDetail) {
		super(conn);
		this.adminDetail = adminDetail;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		adminDetail.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adminDetail.tableName());

		sb.append("(admin_id, city_name, city_name_eng, city_intr, city_intr_eng, "
				+ "country, photo_name, audio_file,reserved,memo,u_record,row_id) values (");

		sb.append(adminDetail.getPid());

		if (adminDetail.getCityName() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getCityName() + "'");
		}
		if (adminDetail.getCityNameEng() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getCityNameEng() + "'");
		}
		if (adminDetail.getCityIntr() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getCityIntr() + "'");
		}
		if (adminDetail.getCityIntrEng() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getCityIntrEng() + "'");
		}
		if (adminDetail.getCountry() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getCountry() + "'");
		}
		if (adminDetail.getPhotoName() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getPhotoName() + "'");
		}
		if (adminDetail.getAudioFile() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getAudioFile() + "'");
		}
		if (adminDetail.getReserved() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getReserved() + "'");
		}
		if (adminDetail.getMemo() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getMemo() + "'");
		}
		sb.append(",1,'" + adminDetail.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update "
				+ adminDetail.tableName() + " set u_record=3,");

		Set<Entry<String, Object>> set = adminDetail.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = adminDetail.getClass().getDeclaredField(column);

			field.setAccessible(true);

			column = StringUtils.toColumnName(column);

			Object value = field.get(adminDetail);

			if (value instanceof String || value == null) {

				if (!StringUtils.isStringSame(String.valueOf(value),
						String.valueOf(columnValue))) {

					if (columnValue == null) {
						sb.append(column + "=null,");
					} else {
						sb.append(column + "='" + String.valueOf(columnValue)
								+ "',");
					}

				}

			} else if (value instanceof Double) {

				if (Double.parseDouble(String.valueOf(value)) != Double
						.parseDouble(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Double.parseDouble(String.valueOf(columnValue))
							+ ",");
				}

			} else if (value instanceof Integer) {

				if (Integer.parseInt(String.valueOf(value)) != Integer
						.parseInt(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Integer.parseInt(String.valueOf(columnValue))
							+ ",");
				}

			}
		}
		sb.append(" where admin_id =" + adminDetail.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adminDetail.tableName()
				+ " set u_record=2 where admin_id=" + adminDetail.getPid();

		stmt.addBatch(sql);

	}

}
