package com.navinfo.dataservice.dao.glm.operator.rd.link;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdLinkSpeedlimitOperator extends AbstractOperator {


	

	private RdLinkSpeedlimit limit;

	public RdLinkSpeedlimitOperator(Connection conn, RdLinkSpeedlimit limit) {
		super(conn);

		this.limit = limit;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		limit.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder(
				"insert into rd_link_speedlimit(link_pid," +
				"speed_type,from_speed_limit,to_speed_limit,speed_class,from_limit_src,to_limit_src,speed_dependent,time_domain,speed_class_work," +
				"u_record,row_id)" +
				" values (");

		sb.append(limit.getLinkPid());

		sb.append(",");

		sb.append(limit.getSpeedType());
		
		sb.append(",");
		
		sb.append(limit.getFromSpeedLimit());
		
		sb.append(",");
		
		sb.append(limit.getToSpeedLimit());
		
		sb.append(",");
		
		sb.append(limit.getSpeedClass());
		
		sb.append(",");
		
		sb.append(limit.getFromLimitSrc());
		
		sb.append(",");
		
		sb.append(limit.getToLimitSrc());
		
		sb.append(",");
		
		sb.append(limit.getSpeedDependent());
		
		sb.append(",");
		
		if (limit.getTimeDomain() == null) {
			sb.append("null");
		} else {
			sb.append("'" + limit.getTimeDomain() + "'");
		}
		
		sb.append(",");
		
		sb.append(limit.getSpeedClassWork());
		
		sb.append(",");
		
		sb.append(1);
		
		sb.append(",");
		
		sb.append("'");
		
		sb.append(limit.getRowId());
		
		sb.append("'");
		
		sb.append(")");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {

		StringBuilder sb = new StringBuilder("update " + limit.tableName()
				+ " set u_record=3,");


			Set<Entry<String, Object>> set = limit.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = limit.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(limit);

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
						this.setChanged(true);
						
					}

				} else if (value instanceof Double) {

					if (Double.parseDouble(String.valueOf(value)) != Double
							.parseDouble(String.valueOf(columnValue))) {
						sb.append(column
								+ "="
								+ Double.parseDouble(String
										.valueOf(columnValue)) + ",");
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

				} else if (value instanceof JSONObject) {
					if (!StringUtils.isStringSame(value.toString(),
							String.valueOf(columnValue))) {
						sb.append("geometry=sdo_geometry('"
								+ String.valueOf(columnValue) + "',8307),");
						this.setChanged(true);
					}
				}
			}
			sb.append(" where row_id=hextoraw('" + limit.getRowId());
			
			sb.append("')");

			String sql = sb.toString();

			sql = sql.replace(", where", " where");
			stmt.addBatch(sql);
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update rd_link_speedlimit set u_record=2 where row_id = hextoraw('"
				+ limit.getRowId() + "')";

		stmt.addBatch(sql);
	}

}
