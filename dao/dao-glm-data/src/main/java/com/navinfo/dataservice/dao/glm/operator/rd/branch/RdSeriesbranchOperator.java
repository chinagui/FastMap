package com.navinfo.dataservice.dao.glm.operator.rd.branch;

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
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdSeriesbranchOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(RdSeriesbranchOperator.class);

	private RdSeriesbranch seriesbranch;

	public RdSeriesbranchOperator(Connection conn, RdSeriesbranch seriesbranch) {
		super(conn);

		this.seriesbranch = seriesbranch;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		seriesbranch.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(seriesbranch.tableName());

		sb.append("(branch_pid, type, voice_dir, pattern_code, arrow_code, arrow_flag, u_record, row_id) values (");

		sb.append(seriesbranch.getBranchPid());

		sb.append("," + seriesbranch.getType());

		sb.append("," + seriesbranch.getVoiceDir());

		if (seriesbranch.getPatternCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + seriesbranch.getPatternCode() + "'");
		}

		if (seriesbranch.getArrowCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + seriesbranch.getArrowCode() + "'");
		}

		sb.append("," + seriesbranch.getArrowFlag());

		sb.append(",1,'" + seriesbranch.rowId() + "')");

		System.out.println(sb.toString());

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {

		StringBuilder sb = new StringBuilder("update "
				+ seriesbranch.tableName() + " set u_record=3,");

		Set<Entry<String, Object>> set = seriesbranch.changedFields()
				.entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = seriesbranch.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(seriesbranch);

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
		sb.append(" where row_id=hextoraw('" + seriesbranch.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + seriesbranch.tableName()
				+ " set u_record=2 where row_id=hextoraw('"
				+ seriesbranch.rowId() + "')";

		stmt.addBatch(sql);
	}

}
