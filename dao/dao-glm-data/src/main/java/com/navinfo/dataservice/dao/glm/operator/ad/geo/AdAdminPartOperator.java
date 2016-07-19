package com.navinfo.dataservice.dao.glm.operator.ad.geo;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminPart;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.dao.glm.operator.rd.gsc.RdGscLinkOperator;

public class AdAdminPartOperator extends AbstractOperator {
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);
	private AdAdminPart adminPart;
	public AdAdminPartOperator(Connection conn, AdAdminPart adminPart) {
		super(conn);
		this.adminPart = adminPart;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		adminPart.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adminPart.tableName());

		sb.append("(group_id, region_id_down,u_record,row_id) values (");

		sb.append(adminPart.getGroupId());
		sb.append("," + adminPart.getRegionIdDown());
		sb.append(",1,'" + adminPart.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("update " + adminPart.tableName()
				+ " set u_record=3,");


			Set<Entry<String, Object>> set = adminPart.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = adminPart.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(adminPart);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value),
							String.valueOf(columnValue))) {
						
						if(columnValue==null){
							sb.append(column + "=null,");
						}
						else{
							sb.append(column + "='" + String.valueOf(columnValue)
									+ "',");
						}
						
					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double
							.parseDouble(String.valueOf(columnValue))) {
						sb.append(column
								+ "="
								+ Double.parseDouble(String
										.valueOf(columnValue)) + ",");
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
			sb.append(" where row_id=hextoraw('" + adminPart.rowId());

			String sql = sb.toString();

			sql = sql.replace(", where", " where");

			stmt.addBatch(sql);

		
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adminPart.tableName()
				+ " set u_record=2 where row_id=hextoraw('" + adminPart.getRowId() + "')";
		stmt.addBatch(sql);

	}

}
