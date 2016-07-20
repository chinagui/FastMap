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
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;

/**
 * 索引:POI与照片关系表 操作
 * 
 * @author luyao
 * 
 */
public class IxPoiPhotoOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private IxPoiPhoto ixPoiPhoto;

	public IxPoiPhotoOperator(Connection conn, IxPoiPhoto ixPoiPhoto) {
		super(conn);

		this.ixPoiPhoto = ixPoiPhoto;
	}



	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiPhoto.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoiPhoto.tableName());

		sb.append("(poi_pid, photo_id,pid,status,memo,tag, row_id,u_date,u_record) values (");

		sb.append(ixPoiPhoto.getPoiPid());

		sb.append("," + ixPoiPhoto.getPhotoId());

		if (StringUtils.isNotEmpty(ixPoiPhoto.getPid())) {

			sb.append(",'" + ixPoiPhoto.getPid() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoiPhoto.getStatus())) {

			sb.append(",'" + ixPoiPhoto.getStatus() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoiPhoto.getMemo())) {

			sb.append(",'" + ixPoiPhoto.getMemo() + "'");
		} else {
			sb.append(",null");
		}

		sb.append("," + ixPoiPhoto.getTag());

		sb.append(",'" + ixPoiPhoto.getRowId() + "'");

		sb.append(",'" + StringUtils.getCurrentTime() + "'");

		sb.append(",'1')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + ixPoiPhoto.tableName()
				+ " set u_record=3,u_date= '" + StringUtils.getCurrentTime()
				+ "' ,");

		Set<Entry<String, Object>> set = ixPoiPhoto.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiPhoto.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiPhoto);

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
		sb.append(" where row_id=hextoraw('" + ixPoiPhoto.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiPhoto.tableName()
				+ " set u_record=2,u_date= '" + StringUtils.getCurrentTime()
				+ "' where row_id=hextoraw('" + ixPoiPhoto.rowId() + "')";

		stmt.addBatch(sql);

	}

}
