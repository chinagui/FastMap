package com.navinfo.dataservice.dao.glm.operator.rd.gsc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;


public class RdGscLinkOperator implements IOperator{
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);

	private Connection conn;
	private RdGscLink gscLink;
	public RdGscLinkOperator(Connection conn, RdGscLink gscLink) {
		this.conn = conn;
		this.gscLink = gscLink;
	}


	@Override
	public void insertRow() throws Exception {

		gscLink.setRowId(UuidUtils.genUuid());

		String sql = "insert into " + gscLink.tableName()
				+ " (pid, zlevel, link_pid, table_name, shp_seq_num, "
				+ "start_end, u_record, row_id) values "
				+ "(?,?,?,?,?,?,?,?,?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, gscLink.getPid());

			pstmt.setInt(2, gscLink.getLinkPid());

			pstmt.setString(3, gscLink.getTableName());

			pstmt.setInt(4, gscLink.getShpSeqNum());

			pstmt.setInt(5, gscLink.getStartEnd());

			pstmt.setInt(6,1);

			pstmt.setString(7, gscLink.rowId());

			pstmt.executeUpdate();

		} catch (Exception e) {
			
			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
				
			}

		}
		
	}

	@Override
	public void updateRow() throws Exception {

		StringBuilder sb = new StringBuilder("update " + gscLink.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = gscLink.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = gscLink.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(gscLink);

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
			sb.append(" where row_id=hextoraw('" + gscLink.getRowId());
			
			sb.append("')");

			String sql = sb.toString();

			sql = sql.replace(", where", " where");

			pstmt = conn.prepareStatement(sql);

			pstmt.executeUpdate();

		} catch (Exception e) {
			
			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
				
			}

		}
		
	}

	@Override
	public void deleteRow() throws Exception {
		String sql = "update " + gscLink.tableName()
				+ " set u_record=? where pid=?";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setInt(2, gscLink.getPid());

			pstmt.executeUpdate();

		} catch (Exception e) {
			
			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
				
			}

		}
		
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {
		gscLink.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(gscLink.tableName());

		sb.append("(pid, zlevel, link_pid, table_name, shp_seq_num, "
				+ "start_end, u_record, row_id) values (");

		sb.append(gscLink.getPid());

		sb.append("," + gscLink.getZlevel());

		sb.append("," + gscLink.getLinkPid());

		if (gscLink.tableName() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + gscLink.getTableName() + "'");
		}
		sb.append("," + gscLink.getShpSeqNum());
		sb.append("," + gscLink.getStartEnd());
		sb.append(",1,'" + gscLink.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("update " + gscLink.tableName()
				+ " set u_record=3,");

		for (int i = 0; i < fieldNames.size(); i++) {

			if (i > 0) {
				sb.append(",");
			}

			String column = StringUtils.toColumnName(fieldNames.get(i));

			sb.append(column);

			sb.append("=");

			Field field = gscLink.getClass().getDeclaredField(fieldNames.get(i));

			Object value = field.get(gscLink);

			sb.append(value);

		}

		sb.append(" where pid =");

		sb.append(gscLink.getPid());

		stmt.addBatch(sb.toString());
		
	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + gscLink.tableName()
				+ " set u_record=2 where pid=" + gscLink.getPid();

		stmt.addBatch(sql);
		
	}

}
