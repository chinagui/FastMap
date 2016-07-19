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
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdBranchRealimageOperator extends AbstractOperator {

	private static Logger logger = Logger
			.getLogger(RdBranchRealimageOperator.class);

	private RdBranchRealimage realimage;

	public RdBranchRealimageOperator(Connection conn,
			RdBranchRealimage realimage) {
		super(conn);

		this.realimage = realimage;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		realimage.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(realimage.tableName());

		sb.append("(branch_pid, image_type, real_code, arrow_code, u_record, row_id) values (");

		sb.append(realimage.getBranchPid());

		sb.append("," + realimage.getImageType());

		if (realimage.getRealCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + realimage.getRealCode() + "'");
		}

		if (realimage.getArrowCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + realimage.getArrowCode() + "'");
		}

		sb.append(",1,'" + realimage.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + realimage.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = realimage.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = realimage.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(realimage);

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
		sb.append(" where row_id=hextoraw('" + realimage.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");
		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + realimage.tableName()
				+ " set u_record=2 where row_id=hextoraw('" + realimage.rowId()
				+ "')";

		stmt.addBatch(sql);
	}

}
