package com.navinfo.dataservice.dao.glm.operator.ad.geo;

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
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.branch.RdBranchOperator;

public class AdFaceTopoOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private AdFaceTopo adFaceTopo;

	public AdFaceTopoOperator(Connection conn, AdFaceTopo adFaceTopo) {
		super(conn);

		this.adFaceTopo = adFaceTopo;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		adFaceTopo.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adFaceTopo.tableName());

		sb.append("(face_pid, seq_num,link_pid, u_record, row_id) values (");

		sb.append(adFaceTopo.getFacePid());

		sb.append("," + adFaceTopo.getSeqNum());

		sb.append("," + adFaceTopo.getLinkPid());

		sb.append(",1,'" + adFaceTopo.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + adFaceTopo.tableName()
				+ " set u_record=3,");
		Set<Entry<String, Object>> set = adFaceTopo.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = adFaceTopo.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(adFaceTopo);

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
		sb.append(" where row_id=hextoraw('" + adFaceTopo.getRowId() + "')");

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adFaceTopo.tableName()
				+ " set u_record=2 where row_id=hextoraw('"
				+ adFaceTopo.rowId() + "')";

		stmt.addBatch(sql);
	}

}
