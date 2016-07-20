package com.navinfo.dataservice.dao.glm.operator.poi.index;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiNameFlag;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiNameTone;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

/**
 * POI名称表操作类
 * 
 * @author zhangxiaolong
 * 
 */
public class IxPoiNameOperator extends AbstractOperator {

	private IxPoiName ixPoiName;

	public IxPoiNameOperator(Connection conn, IxPoiName ixPoiName) {
		super(conn);

		this.ixPoiName = ixPoiName;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		ixPoiName.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(ixPoiName.tableName());

		sb.append("(NAME_ID, POI_PID, NAME_GROUPID, NAME_CLASS, NAME_TYPE, LANG_CODE, NAME, NAME_PHONETIC, KEYWORDS, NIDB_PID,U_DATE,U_RECORD,ROW_ID) values (");

		sb.append(ixPoiName.getPid());

		sb.append("," + ixPoiName.getPoiPid());

		sb.append("," + ixPoiName.getNameGroupid());

		sb.append("," + ixPoiName.getNameClass());

		sb.append("," + ixPoiName.getNameType());

		if (StringUtils.isNotEmpty(ixPoiName.getLangCode())) {
			sb.append(",'" + ixPoiName.getLangCode() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoiName.getName())) {
			sb.append(",'" + ixPoiName.getName() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoiName.getNamePhonetic())) {
			sb.append(",'" + ixPoiName.getNamePhonetic() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoiName.getKeywords())) {
			sb.append(",'" + ixPoiName.getKeywords() + "'");
		} else {
			sb.append(",null");
		}

		if (StringUtils.isNotEmpty(ixPoiName.getNidbPid())) {
			sb.append(",'" + ixPoiName.getNidbPid() + "'");
		} else {
			sb.append(",null");
		}

		sb.append(",'" + StringUtils.getCurrentTime() + "'");

		sb.append(",1,'" + ixPoiName.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : ixPoiName.getNameFlags()) {
			IxPoiNameFlagOperator op = new IxPoiNameFlagOperator(conn,
					(IxPoiNameFlag) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : ixPoiName.getNameTones()) {
			IxPoiNameToneOperator op = new IxPoiNameToneOperator(conn,
					(IxPoiNameTone) r);

			op.insertRow2Sql(stmt);
		}

	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + ixPoiName.tableName()
				+ " set u_record=3,u_date='" + StringUtils.getCurrentTime()
				+ "',");

		Set<Entry<String, Object>> set = ixPoiName.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = ixPoiName.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(ixPoiName);

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
		sb.append(" where name_id= " + ixPoiName.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + ixPoiName.tableName()
				+ " set u_record=2,u_date= '" + StringUtils.getCurrentTime()
				+ "'  where name_id=" + ixPoiName.getPid();

		stmt.addBatch(sql);

		for (IRow r : ixPoiName.getNameTones()) {
			IxPoiNameToneOperator op = new IxPoiNameToneOperator(conn,
					(IxPoiNameTone) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : ixPoiName.getNameFlags()) {
			IxPoiNameFlagOperator op = new IxPoiNameFlagOperator(conn,
					(IxPoiNameFlag) r);

			op.deleteRow2Sql(stmt);
		}
	}

}
