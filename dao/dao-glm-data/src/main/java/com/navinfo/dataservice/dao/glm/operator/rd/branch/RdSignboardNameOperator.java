package com.navinfo.dataservice.dao.glm.operator.rd.branch;

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
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboardName;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdSignboardNameOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdSignboardNameOperator.class);

	

	private RdSignboardName name;

	public RdSignboardNameOperator(Connection conn, RdSignboardName name) {
		super(conn);

		this.name = name;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		name.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(name.tableName());

		sb.append("(name_id, seq_num, name_groupid, signboard_id, name_class, lang_code, code_type, name, "
				+ "phonetic, src_flag, voice_file, u_record, row_id) values (");

		sb.append(name.getPid());

		sb.append("," + name.getSeqNum());

		sb.append("," + name.getNameGroupid());

		sb.append("," + name.getSignboardId());

		sb.append("," + name.getNameClass());

		if (name.getLangCode() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + name.getLangCode() + "'");
		}

		sb.append("," + name.getCodeType());

		if (name.getName() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + name.getName() + "'");
		}

		if (name.getPhonetic() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + name.getPhonetic() + "'");
		}

		sb.append("," + name.getSrcFlag());

		if (name.getVoiceFile() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + name.getVoiceFile() + "'");
		}

		sb.append(",1,'" + name.rowId() + "')");

		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(Statement stmt)
			throws Exception {

		StringBuilder sb = new StringBuilder("update " + name.tableName()
				+ " set u_record=3,");


			Set<Entry<String, Object>> set = name.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = name.getClass().getDeclaredField(column);

				field.setAccessible(true);

				Object value = field.get(name);

				column = StringUtils.toColumnName(column);

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
			sb.append(" where name_id=" + name.getPid());

			String sql = sb.toString();

			sql = sql.replace(", where", " where");
			stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + name.tableName()
				+ " set u_record=2 where name_id=" + name.getPid();

		stmt.addBatch(sql);
	}

}
