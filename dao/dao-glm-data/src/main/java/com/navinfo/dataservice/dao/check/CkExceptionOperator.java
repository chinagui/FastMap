package com.navinfo.dataservice.dao.check;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.constant.CheckConstant;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.pidservice.LogPidService;

public class CkExceptionOperator {


	private Connection conn;

	public CkExceptionOperator(Connection conn) {

		this.conn = conn;
	}

	/**
	 * 新增检查结果到数据库
	 * 
	 * @param ck
	 * @throws Exception
	 */
	public void insert(CkException ck) throws Exception {

		String sql = "insert into ck_exception"
				+ " (exception_id, rule_id, task_name, status, group_id, rank, situation, information, "
				+ "suggestion, geometry, targets, addition_info, memo, mesh_id, scope_flag, province_name, map_scale, "
				+ "reserved, extended, task_id, qa_task_id, qa_status, worker, qa_worker, memo_1, memo_2, memo_3, create_date, update_date) values "
				+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		PreparedStatement pstmt = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, new LogPidService(conn).generateExceptionPid());

			pstmt.setString(2, ck.getRuleId());

			pstmt.setString(3, ck.getTaskName());

			pstmt.setInt(4, ck.getStatus());

			pstmt.setInt(5, ck.getGroupId());

			pstmt.setInt(6, ck.getRank());

			pstmt.setString(7, ck.getSituation());

			pstmt.setString(8, ck.getInformation());

			pstmt.setString(9, ck.getSuggestion());

			pstmt.setString(10, ck.getGeometry());

			pstmt.setString(11, ck.getTargets());

			pstmt.setString(12, ck.getAdditionInfo());

			pstmt.setString(13, ck.getMemo());

			pstmt.setInt(14, ck.getMeshId());

			pstmt.setInt(15, ck.getScopeFlag());

			pstmt.setString(16, ck.getProvinceName());

			pstmt.setInt(17, ck.getMapScale());

			pstmt.setString(18, ck.getReserved());

			pstmt.setString(19, ck.getExtended());

			pstmt.setString(20, ck.getTaskId());

			pstmt.setString(21, ck.getQaTaskId());

			pstmt.setInt(22, ck.getQaStatus());

			pstmt.setString(23, ck.getWorker());

			pstmt.setString(24, ck.getQaWorker());

			pstmt.setString(25, ck.getMemo1());

			pstmt.setString(26, ck.getMemo2());

			pstmt.setString(27, ck.getMemo3());

			Date date = new Date(new java.util.Date().getTime());

			ck.setUpdateDate(date.toString());

			ck.setCreateDate(date.toString());

			pstmt.setDate(28, date);

			pstmt.setDate(29, date);

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

	/**
	 * 更新检查结果到数据库
	 * 
	 * @param ck
	 * @throws Exception
	 */
	public void update(CkException ck) throws Exception {

		StringBuilder sb = new StringBuilder("update ck_exception set ");

		PreparedStatement pstmt = null;

		try {

			Set<Entry<String, Object>> set = ck.changedFields().entrySet();

			Iterator<Entry<String, Object>> it = set.iterator();

			while (it.hasNext()) {
				Entry<String, Object> en = it.next();

				String column = en.getKey();

				Object columnValue = en.getValue();

				Field field = this.getClass().getDeclaredField(column);

				column = StringUtils.toColumnName(column);

				Object value = field.get(this);

				if (value instanceof String || value == null) {

					if (!StringUtils.isStringSame(String.valueOf(value),
							String.valueOf(columnValue))) {
						sb.append(column + "='" + String.valueOf(columnValue)
								+ "',");
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

				} else if (value instanceof JSONObject) {
					if (!StringUtils.isStringSame(value.toString(),
							String.valueOf(columnValue))) {
						sb.append("geometry=sdo_geometry('"
								+ String.valueOf(columnValue) + "',8307),");
					}
				}
			}

			sb.append("update_date=sysdate");

			sb.append(" where exception_id=" + ck.getExceptionId());

			String sql = sb.toString();

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

	/**
	 * 删除一条检查结果
	 * 
	 * @param ck
	 * @throws Exception
	 */
	public void delete(CkException ck) throws Exception {

	}

	public JSONArray check(int pid, ObjType type) throws Exception {
		JSONArray array = new JSONArray();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try{
			switch (type) {
			case RDRESTRICTION:
				String sql = "select column_value rule_id from table(package_check.fun_check(:1))";
	
				pstmt = conn.prepareStatement(sql);
	
				pstmt.setInt(1, pid);
	
				resultSet = pstmt.executeQuery();
	
				while (resultSet.next()) {
					String ruleId = resultSet.getString("rule_id");
	
					String message = CheckConstant.getCheckMessage(ruleId);
	
					CkException ck = new CkException();
	
					ck.setRuleId(ruleId);
					
					ck.setTargets("RD_RESTRICTION :"+pid);
	
					ck.setInformation(message);
	
					this.insert(ck);
	
					array.add(ck.Serialize(ObjLevel.BRIEF));
				}
	
			default:
				break;
	
			}
		}catch (Exception e) {
			throw e;
		}finally {
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(resultSet);
		}
		return array;
	}
}
