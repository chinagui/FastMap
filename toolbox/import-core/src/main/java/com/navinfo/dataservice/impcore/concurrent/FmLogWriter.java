package com.navinfo.dataservice.impcore.concurrent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.oracle.MyPoolGuardConnectionWrapper;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.impcore.flushbylog.EditLog;
import com.navinfo.dataservice.impcore.flushbylog.ILogWriteListener;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

/**
 * 
 * @ClassName: FmLogWriter
 * @author xiaoxiaowen4127
 * @date 2017年9月18日
 * @Description: LogWriter.java
 */
public class FmLogWriter {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private static WKT wktUtil = new WKT();
	protected Connection targetDbConn;
	protected ILogWriteListener listener;
	protected boolean physicalDelete=false;
	protected boolean ignoreAddError = false;
	protected boolean ignoreUpdateError = false;
	protected boolean ignoreDelError = false;
	protected Collection<String> ignoreExceptionKeys;

	/**
	 * 
	 * @param conn
	 * @param listener
	 */
	public FmLogWriter(Connection conn,ILogWriteListener listener) {
		this.targetDbConn = conn;
		this.listener=listener;
	}
	
	/**
	 * 暂未实现
	 * @param logs
	 */
	private void write(Collection<EditLog> logs){
		
	}

	public void write(EditLog editLog)
			throws Exception {
		int op_tp = editLog.getOpType();
		if (op_tp == 1) {// 新增
			insertData(editLog);
		} else if (op_tp == 3) { // 修改
			updateData(editLog);
		} else if (op_tp == 2) { // 删除
			if(physicalDelete){
				PhysicalDeleteData(editLog);
			}else{
				deleteData(editLog);
			}
		}
	}

	/**
	 * ??????
	 */
	static final Set includeFiledsSet = new HashSet();
	static {
		String[] includeFiledsArr = new String[] { "pid", "kind_code",
				"geometry", "x_guide", "y_guide", "link_pid", "side",
				"name_groupid", "road_flag", "pmesh_id", "admin_real",
				"importance", "chain", "airport_code", "access_flag",
				"open_24h", "mesh_id_5k", "mesh_id", "region_id", "post_code",
				"edit_flag", "dif_groupid", "reserved", "state", "field_state",
				"label", "type", "address_flag", "ex_priority", "edition_flag",
				"poi_memo", "old_blockcode", "old_name", "old_address",
				"old_kind", "poi_num", "log", "task_id", "data_version",
				"field_task_id", "verified_flag", "collect_time",
				"geo_adjust_flag", "full_attr_flag", "old_x_guide",
				"old_y_guide", "u_record", "u_fields", "u_date", "row_id",
				"level", "sports_venue", "indoor", "vip_flag","trunk_flag" };
		includeFiledsSet.addAll(Arrays.asList(includeFiledsArr));
	}

	private boolean isFieldInIncludeFiledsSet(String field) {
		if (includeFiledsSet.contains(field.toUpperCase())
				|| includeFiledsSet.contains(field.toLowerCase())) {
			return true;
		} else {
			return false;
		}
	}

	private int insertData(EditLog editLog) throws Exception {
		listener.preInsert();
		StringBuilder sb = new StringBuilder("insert into ");
		PreparedStatement pstmt = null;
		try {
			String newValue = editLog.getNewValue();
			JSONObject json = JSONObject.fromObject(newValue);
			String tableName = editLog.getTableName().toLowerCase();

			sb.append(tableName);
			sb.append(" (");
			Iterator<String> it = json.keys();
			int keySize = json.keySet().size();
			int tmpPos = 0;
			while (it.hasNext()) {
				String filed = it.next();
				if ("IX_POI".equalsIgnoreCase(tableName)
						&& !isFieldInIncludeFiledsSet(filed)) {
					continue;
				}

				if (isExcludeField(filed, tableName)) {
					continue;
				}
				filed = unescapeField(filed);
				boolean isOracleKey = isOracleKey(filed);
				if (0 == tmpPos) {
					if (isOracleKey) {
						sb.append("\"" + filed.toUpperCase() + "\"");
					} else {
						sb.append(filed);
					}

				} else {
					if (isOracleKey) {
						sb.append(",\"" + filed.toUpperCase() + "\"");
					} else {
						sb.append("," + filed);
					}

				}
				tmpPos++;

			}
			boolean hasUrecord = false;
			if (sb.indexOf(",U_RECORD") != -1) {
				hasUrecord = true;
			}
			if (hasUrecord) {
				sb.append(") ");
			} else {
				sb.append(",U_RECORD) ");
			}
			sb.append("values(");
			this.log.debug("json" + json);

			tmpPos = 0;
			int i = 0;
			for (Object key : json.keySet()) {
				if ("IX_POI".equalsIgnoreCase(tableName)
						&& !isFieldInIncludeFiledsSet(key.toString())) {
					continue;
				}
				if (isExcludeField(key.toString(), tableName)) {
					continue;
				}
				if (i == 0) {
					sb.append("?");
				} else {
					sb.append(",?");
				}
				i++;
			}
			if (hasUrecord) {
				sb.append(") ");
			} else {
				sb.append(",1)");
			}

			it = json.keys();

			tmpPos = 0;
			this.log.debug(sb);
			pstmt = this.targetDbConn.prepareStatement(sb.toString());

			while (it.hasNext()) {
				String keyName = it.next();
				// if("IX_POI".equalsIgnoreCase(tableName)&&
				// !includeFiledsSet.contains(keyName) ){
				// continue;
				// }
				if ("IX_POI".equalsIgnoreCase(tableName)
						&& !isFieldInIncludeFiledsSet(keyName)) {
					continue;
				}
				if (isExcludeField(keyName.toString(), tableName)) {
					continue;
				}
				tmpPos++;

				Object valObj = json.get(keyName);

				if (!"geometry".equalsIgnoreCase(keyName)) {

					if (tableName.equals("ck_exception")) {

						if ("create_date".equalsIgnoreCase(keyName)
								|| "update_date".equalsIgnoreCase(keyName)) {
							Timestamp ts = new Timestamp(
									DateUtils.stringToLong(valObj.toString(),
											"yyyy-MM-dd HH:mm:ss"));

							pstmt.setTimestamp(tmpPos, ts);
						} else {
							pstmt.setObject(tmpPos, valObj);
						}
					} else {
						// 临时处理办法
						if (valObj == null
								|| (valObj instanceof net.sf.json.JSONNull)) {
							pstmt.setObject(tmpPos, "");
						} else {
							pstmt.setObject(tmpPos, valObj);
						}
					}
				} else {

					if (tableName.equalsIgnoreCase("ck_exception")) {
						pstmt.setObject(tmpPos, valObj);
					} else {
						JGeometry jg = wktUtil.toJGeometry(valObj.toString()
								.getBytes());

						jg.setSRID(8307);
						pstmt.setObject(tmpPos, ConnectionUtil
								.createSTRUCTFromJGeometry(jg, targetDbConn));

					}
				}

			}
			int result = pstmt.executeUpdate();
			return result;
		} catch (Exception e) {
			listener.insertFail(editLog, e.getMessage());
			if(ignoreAddError){
				log.warn("已忽略新增类型错误："+e.getMessage());
			}else if(isIgnoreableException(e)){
				log.warn("已忽略指定错误类型："+e.getMessage());
			}else{
				log.error(e.getMessage(),e);
				throw e;
			}
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
		return 0;
	}
	private boolean isIgnoreableException(Exception e){
		if(ignoreExceptionKeys!=null){
			for(String key:ignoreExceptionKeys){
				if(e.getMessage().indexOf(key)>-1){
					return true;
				}
			}
		}
		return false;
	}

	private boolean isExcludeField(String filed, String tableName) {
		return "changed_fields".equalsIgnoreCase(filed)
				|| "status".equalsIgnoreCase(filed)
				|| "rd_gate_condition_map".equalsIgnoreCase(filed)
				|| ("ix_poi_restaurant".endsWith(tableName) && "mesh"
						.equalsIgnoreCase(filed))
				|| ("ad_face_topo".endsWith(tableName) && "mesh"
						.equalsIgnoreCase(filed))
				|| ("ad_face_topo".endsWith(tableName) && "mesh"
						.equalsIgnoreCase(filed))
				|| ("ad_link".endsWith(tableName) && "mesh"
						.equalsIgnoreCase(filed));
	}

	// 特殊字段进行转换
	private String unescapeField(String filed) {
		if ("name_group_id".equalsIgnoreCase(filed)) {
			filed = "name_groupid";
		}
		if ("open_2_4h".equalsIgnoreCase(filed)) {
			filed = "open_24h";
		}
		if ("p_mesh_id".equalsIgnoreCase(filed)) {
			filed = "pmesh_id";
		}
		return filed;
	}

	private boolean isOracleKey(String filed) {
		return "level".equalsIgnoreCase(filed) || "log".equalsIgnoreCase(filed)
				|| "label".equalsIgnoreCase(filed)
				|| "tag".equalsIgnoreCase(filed)
				|| "type".equalsIgnoreCase(filed)
				|| "current".equalsIgnoreCase(filed)
				|| "mode".equalsIgnoreCase(filed);
	}

	private int updateData(EditLog editLog) throws Exception {

		listener.preUpdate();
		PreparedStatement pstmt = null;

		StringBuilder sb = new StringBuilder("update ");

		try {
			String newValue = editLog.getNewValue();

			JSONObject json = JSONObject.fromObject(newValue);

			String tableName = editLog.getTableName().toLowerCase();

			sb.append(tableName);

			sb.append(" set ");

			Iterator<String> it = json.keys();

			int keySize = json.keySet().size();

			int tmpPos = 0;

			while (it.hasNext()) {
				String keyName = it.next();
				keyName = unescapeField(keyName);
				if (isOracleKey(keyName)) {
					sb.append("\"" + keyName.toUpperCase() + "\"");
				} else {
					sb.append(keyName);
				}

				sb.append("=:");

				sb.append(++tmpPos);

				if (tmpPos < keySize) {

					sb.append(",");
				}
			}
			if (keySize == 0 || StringUtils.endsWith(sb.toString(), ",")) {
				sb.append("u_record=3 where row_id = hextoraw('");
			} else {
				sb.append(",u_record=3 where row_id = hextoraw('");
			}

			sb.append(editLog.getTableRowId());

			sb.append("')");

			it = json.keys();

			tmpPos = 0;
			this.log.debug(sb);
			pstmt = this.targetDbConn.prepareStatement(sb.toString());

			while (it.hasNext()) {
				tmpPos++;

				String keyName = it.next();

				Object valObj = json.get(keyName);

				if (!"geometry".equalsIgnoreCase(keyName)) {

					// 临时处理办法
					if (valObj == null
							|| (valObj instanceof net.sf.json.JSONNull)) {
						pstmt.setObject(tmpPos, "");
					} else {
						pstmt.setObject(tmpPos, valObj);
					}
					// pstmt.setObject(tmpPos, valObj);
				} else {

					if (tableName.equalsIgnoreCase("ck_exception")) {
						pstmt.setObject(tmpPos, valObj);
					} else {
						JGeometry jg = wktUtil.toJGeometry(valObj.toString()
								.getBytes());

						jg.setSRID(8307);
						pstmt.setObject(tmpPos, ConnectionUtil
								.createSTRUCTFromJGeometry(jg, targetDbConn));
					}
				}

			}
			int result = pstmt.executeUpdate();
			return result;
		} catch (Exception e) {
			listener.updateFailed(editLog, e.getMessage());
			if(ignoreUpdateError){
				log.warn("已忽略修改类型错误："+e.getMessage());
			}else if(isIgnoreableException(e)){
				log.warn("已忽略指定错误类型："+e.getMessage());
			}else{
				log.error(e.getMessage(),e);
				throw e;
			}
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
		return 0;
	}

	private int deleteData(EditLog editLog) throws Exception {
		listener.preDelete();
		PreparedStatement pstmt = null;
		try {
			String sql = "update " + editLog.getTableName()
					+ " set u_record = 2 where row_id =hextoraw('"
					+ editLog.getTableRowId() + "')";
			this.log.debug(sql);
			pstmt = this.targetDbConn.prepareStatement(sql);
			int result = pstmt.executeUpdate();
			return result;
		} catch (Exception e) {
			listener.updateFailed(editLog, e.getMessage());
			if(ignoreDelError){
				log.warn("已忽略删除类型错误："+e.getMessage());
			}else if(isIgnoreableException(e)){
				log.warn("已忽略指定错误类型："+e.getMessage());
			}else{
				log.error(e.getMessage(),e);
				throw e;
			}
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
		 return 0;

	}

	private int PhysicalDeleteData(EditLog editLog) throws Exception {

		PreparedStatement pstmt = null;
		try {
			String sql = "delete from " + editLog.getTableName()
					+ " where row_id =hextoraw('" + editLog.getTableRowId()
					+ "')";
			this.log.debug(sql);
			pstmt = this.targetDbConn.prepareStatement(sql);
			int result = pstmt.executeUpdate();
			return result;
		}catch (Exception e) {
			listener.updateFailed(editLog, e.getMessage());
			if(ignoreDelError){
				log.warn("已忽略删除类型错误："+e.getMessage());
			}else if(isIgnoreableException(e)){
				log.warn("已忽略指定错误类型："+e.getMessage());
			}else{
				log.error(e.getMessage(),e);
				throw e;
			}
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
		return 0;

	}

	private int getDataByRowId(EditLog editLog) throws Exception {

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		int count = 0;

		try {
			String sql = "select  count(1) count from "
					+ editLog.getTableName() + " where row_id =hextoraw('"
					+ editLog.getTableRowId() + "')";
			this.log.debug(sql);
			pstmt = this.targetDbConn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				count = resultSet.getInt("count");
			}
		} finally {

			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return count;

	}

	public static void main(String[] args) {
		JSONObject json = JSONObject.fromObject("{\"key1\":null}");
		for (Object key : json.keySet()) {
			System.out.println("key:" + key + ",class:"
					+ json.get(key).getClass().getName());
		}
	}
}
