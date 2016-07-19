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

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdLinkNameOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdLinkNameOperator.class);

	

	private RdLinkName name;

	public RdLinkNameOperator(Connection conn, RdLinkName name) {
		super(conn);

		this.name = name;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		name.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder(
				"insert into rd_link_name(link_pid,name_groupid,seq_num,name_class,name_type,input_time,src_flag,route_att,code,u_record,row_id) values (");

		sb.append(name.getLinkPid());

		sb.append(",");

		sb.append(name.getNameGroupid());

		sb.append(",");

		sb.append(name.getSeqNum());

		sb.append(",");

		sb.append(name.getNameClass());

		sb.append(",");

		sb.append(name.getNameType());
		
		sb.append(",");
		
		sb.append(name.getInputTime());
		
		sb.append(",");
		
		sb.append(name.getSrcFlag());
		
		sb.append(",");
		
		sb.append(name.getRouteAtt());
		
		sb.append(",");
		
		sb.append(name.getCode());

		sb.append(",1,'" + name.getRowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql( Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + name.tableName()
				+ " set u_record=3,");

			Set<Entry<String, Object>> set = name.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();
				
				if ("name".equals(column)){
					continue;
				}

				Object columnValue = en.getValue();

				Field field = name.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(name);

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
			sb.append(" where row_id=hextoraw('" + name.getRowId());
			
			sb.append("')");

			String sql = sb.toString();

			sql = sql.replace(", where", " where");

			stmt.addBatch(sql);
		}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update rd_link_name set u_record=2 where row_id = hextoraw('"
				+ name.getRowId() + "')";

		stmt.addBatch(sql);
	}

}
