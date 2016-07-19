package com.navinfo.dataservice.dao.glm.operator.lu;

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
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceName;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;

public class LuFaceNameOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private LuFaceName luFaceName;

	public LuFaceNameOperator(Connection conn, LuFaceName luFaceName) {
		super(conn);

		this.luFaceName = luFaceName;
	}


	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		luFaceName.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(luFaceName.tableName());

		sb.append("(name_id, face_pid, name_groupid, lang_code, name, phonetic, src_flag, u_record, row_id) values (");

		sb.append(luFaceName.getNameId());

		sb.append("," + luFaceName.getFacePid());

		sb.append("," + luFaceName.getNameGroupid());

		sb.append("," + luFaceName.getLangCode());

		sb.append("," + luFaceName.getName());

		sb.append("," + luFaceName.getPhonetic());

		sb.append("," + luFaceName.getSrcFlag());

		sb.append(",1,'" + luFaceName.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + luFaceName.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = luFaceName.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = luFaceName.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(luFaceName);

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
		sb.append(" where row_id=hextoraw('" + luFaceName.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + luFaceName.tableName()
				+ " set u_record=2 where row_id=hextoraw('"
				+ luFaceName.rowId() + "')";

		stmt.addBatch(sql);
	}

}
