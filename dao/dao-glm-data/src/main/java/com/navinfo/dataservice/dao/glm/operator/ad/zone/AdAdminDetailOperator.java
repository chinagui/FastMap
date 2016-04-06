package com.navinfo.dataservice.dao.glm.operator.ad.zone;

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
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminDetail;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.operator.rd.gsc.RdGscLinkOperator;

public class AdAdminDetailOperator implements IOperator {
	private static Logger logger = Logger.getLogger(RdGscLinkOperator.class);

	private Connection conn;
	private AdAdminDetail adminDetail;
	public AdAdminDetailOperator(Connection conn, AdAdminDetail adminDetail) {
		this.conn = conn;
		this.adminDetail = adminDetail;
	}
	@Override
	public void insertRow() throws Exception {
		adminDetail.setRowId(UuidUtils.genUuid());

		String sql = "insert into " + adminDetail.tableName()
				+ " (admin_id, city_name, city_name_eng, city_intr, city_intr_eng, "
				+ "country, photo_name, audio_file,reserved,memo,u_record,row_id) values "
				+ "(?,?,?,?,?,?,?,?,?,?,?,?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, adminDetail.pid());

			pstmt.setString(2, adminDetail.getCityName());

			pstmt.setString(3, adminDetail.getCityNameEng());

			pstmt.setString(4, adminDetail.getCityIntr());

			pstmt.setString(5, adminDetail.getCityIntrEng());

			pstmt.setString(6,adminDetail.getCountry());

			pstmt.setString(7, adminDetail.getPhotoName());
			pstmt.setString(8, adminDetail.getAudioFile());
			pstmt.setString(9, adminDetail.getReserved());
			pstmt.setString(10, adminDetail.getMemo());
			pstmt.setInt(11, 1);
			pstmt.setString(12, adminDetail.rowId());
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
		StringBuilder sb = new StringBuilder("update " + adminDetail.tableName()
				+ " set u_record=3,");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = adminDetail.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = adminDetail.getClass().getDeclaredField(column);
				
				field.setAccessible(true);

				column = StringUtils.toColumnName(column);

				Object value = field.get(adminDetail);

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
			sb.append(" where admin_id =" + adminDetail.getPid());

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
		String sql = "update " + adminDetail.tableName()
				+ " set u_record=? where admin_id=?";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setInt(2, adminDetail.getPid());

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
		adminDetail.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(adminDetail.tableName());

		sb.append("(admin_id, city_name, city_name_eng, city_intr, city_intr_eng, "
				+ "country, photo_name, audio_file,reserved,memo,u_record,row_id) values (");

		sb.append(adminDetail.getPid());

		if (adminDetail.getCityName() == null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getCityName() + "'");
		}
		if (adminDetail.getCityNameEng()== null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getCityNameEng() + "'");
		}
		if (adminDetail.getCityIntr()== null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getCityIntr() + "'");
		}
		if (adminDetail.getCityIntrEng()== null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getCityIntrEng() + "'");
		}
		if (adminDetail.getCountry()== null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getCountry() + "'");
		}
		if (adminDetail.getPhotoName()== null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getPhotoName() + "'");
		}
		if (adminDetail.getAudioFile()== null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getAudioFile() + "'");
		}
		if (adminDetail.getReserved()== null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getReserved() + "'");
		}
		if (adminDetail.getMemo()== null) {
			sb.append(",null");
		} else {
			sb.append(",'" + adminDetail.getMemo() + "'");
		}
		sb.append(",1,'" + adminDetail.rowId() + "')");
		stmt.addBatch(sb.toString());
	}

	@Override
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception {
		StringBuilder sb = new StringBuilder("update " + adminDetail.tableName()
				+ " set u_record=3,");

		for (int i = 0; i < fieldNames.size(); i++) {

			if (i > 0) {
				sb.append(",");
			}

			String column = StringUtils.toColumnName(fieldNames.get(i));

			sb.append(column);

			sb.append("=");

			Field field = adminDetail.getClass().getDeclaredField(fieldNames.get(i));

			Object value = field.get(adminDetail);

			sb.append(value);

		}

		sb.append(" where admin_id =");

		sb.append(adminDetail.getPid());

		stmt.addBatch(sb.toString());

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {
		String sql = "update " + adminDetail.tableName()
				+ " set u_record=2 where admin_id=" + adminDetail.getPid();

		stmt.addBatch(sql);

	}

}
