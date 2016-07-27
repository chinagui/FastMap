package com.navinfo.dataservice.dao.glm.operator.poi.index;

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
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAudio;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;

/**
 * POI音频表 操作
 * 
 * @author luyao
 * 
 */
public class IxPoiAudioOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private IxPoiAudio ixPoiAudio;

	public IxPoiAudioOperator(Connection conn, IxPoiAudio ixPoiAudio) {
		super(conn);

		this.ixPoiAudio = ixPoiAudio;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiAudio.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoiAudio.tableName());

		sb.append("(poi_pid, audio_id, status,memo, row_id,u_date,u_record) values (");

		sb.append(ixPoiAudio.getPoiPid());

		sb.append("," + ixPoiAudio.getAudioId());

		if (StringUtils.isNotEmpty(ixPoiAudio.getStatus())) {
			sb.append(",'" + ixPoiAudio.getStatus() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoiAudio.getMemo())) {
			sb.append(",'" + ixPoiAudio.getMemo() + "'");
		} else {
			sb.append(",null");
		}

		sb.append(",'" + ixPoiAudio.getRowId() + "'");

		sb.append(",'" + StringUtils.getCurrentTime() + "'");

		sb.append(",'1')");

		stmt.addBatch(sb.toString());

	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + ixPoiAudio.tableName()
				+ " set u_record=3,u_date='" + StringUtils.getCurrentTime()
				+ "',");

		Set<Entry<String, Object>> set = ixPoiAudio.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiAudio.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiAudio);

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
		sb.append(" where row_id=hextoraw('" + ixPoiAudio.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiAudio.tableName()
				+ " set u_record=2,u_date='" + StringUtils.getCurrentTime()
				+ "' where row_id=hextoraw('" + ixPoiAudio.rowId() + "')";

		stmt.addBatch(sql);
	}

}
