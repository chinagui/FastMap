package com.navinfo.dataservice.control.row.pointaddress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Title: PointAddressService
 * @Package: com.navinfo.dataservice.control.row.pointaddress
 * @Description:
 * @Author: LittleDog
 * @Date: 2017年9月26日
 * @Version: V1.0
 */
public class PointAddressService {

	private static final Logger logger = LoggerRepos.getLogger(PointAddressService.class);

	private PointAddressService() {
	}

	private static class SingletonHolder {
		private static final PointAddressService INSTANCE = new PointAddressService();
	}

	public static PointAddressService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * 获取点门牌列表
	 * @param parameter
	 * @return
	 * @throws Exception
	 */
	public JSONObject getPointAddressList(String parameter) throws Exception {

		Connection conn = null;
		JSONObject resultJson = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");
			int subtaskId = jsonReq.getInt("subtaskId");
			int status = jsonReq.getInt("status");
			int pageNum = jsonReq.getInt("pageNum");
			int pageSize = jsonReq.getInt("pageSize");

			if (pageNum == 0 && pageSize == 0) {
				pageNum = 1;
				pageSize = 999999999;
			}

			String pidName = "";
			if (jsonReq.containsKey("pidName")) {
				pidName = jsonReq.getString("pidName");
			}
			conn = DBConnector.getInstance().getConnectionById(dbId);
			resultJson = loadPointAddressList(conn, pidName, status, subtaskId, pageSize, pageNum);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

		} finally {
			DbUtils.closeQuietly(conn);
		}
		return resultJson;
	}
	
	/**
	 * 
	 * @param conn
	 * @param pidName
	 * @param type
	 * @param subtaskId
	 * @param pageSize
	 * @param pageNum
	 * @return
	 * @throws Exception
	 */
	private JSONObject loadPointAddressList(Connection conn, String pidName, int status, int subtaskId, int pageSize, int pageNum) throws Exception {

		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();

		Map<Long, JSONObject> objs = new LinkedHashMap<Long, JSONObject>();

		int total = 0;
		int startRow = (pageNum - 1) * pageSize + 1;

		int endRow = pageNum * pageSize;
		NiValExceptionSelector selector = new NiValExceptionSelector(conn);
		List<String> checkRuleList = selector.loadByOperationName("POINTADDRESS_ROW_COMMIT");
		
		String ckRules = "('";
		ckRules += StringUtils.join(checkRuleList.toArray(), "','") + "')";
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(" SELECT * ");
		builder.append(" FROM (SELECT C.*, ROWNUM RN ");
		builder.append(" FROM (SELECT COUNT(1) OVER(PARTITION BY 1) TOTAL, ");
		builder.append(" IP.PID, IP.DPR_NAME, IP.DP_NAME, IP.MEMO, DS.FRESH_VERIFIED AS FRESHNESS_VEFICATION, DS.RAW_FIELDS, ");
		builder.append(" (SELECT COUNT(N.RULEID) FROM NI_VAL_EXCEPTION N, CK_RESULT_OBJECT C WHERE N.MD5_CODE = C.MD5_CODE AND IP.PID = C.PID(+) ");
		builder.append(" AND C.TABLE_NAME = 'IX_POINTADDRESS' AND N.RULEID IN " + ckRules + " ) AS ERRORCOUNT ");
		builder.append(" FROM IX_POINTADDRESS IP, (SELECT * FROM IX_POINTADDRESS_NAME WHERE LANG_CODE = 'CHI') IPN, DAY_EDIT_STATUS DS ");
		builder.append(" WHERE IP.PID = IPN.PID(+) AND IP.PID = DS.PID ");
		
		builder.append(" AND DS.WORK_TYPE = 1 AND DS.ELEMENT = 1 AND DS.STATUS = " + status + " ");
		builder.append(" AND (DS.QUICK_SUBTASK_ID = " + subtaskId + " OR DS.MEDIUM_SUBTASK_ID = " + subtaskId + " ) ");

		if (!pidName.isEmpty()) {
			Pattern pattern = Pattern.compile("[0-9]*");
			Matcher isNum = pattern.matcher(pidName);
			if (isNum.matches()) {
				builder.append(" AND (IP.PID = " + pidName + " OR IPN.FULLNAME LIKE '%" + pidName + "%') ");
			} else {
				if (StringUtils.isNotBlank(pidName)) {
					builder.append(" AND IPN.FULLNAME LIKE '%" + pidName + "%'");
				}
			}
		}
		
		builder.append(" ) C");
		
		builder.append(" WHERE ROWNUM <= :1 ) ");
		builder.append(" WHERE RN >= :2 ");
		
		logger.info(builder.toString());
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(builder.toString());
			pstmt.setInt(1, endRow);
			pstmt.setInt(2, startRow);
			
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				if (total == 0) {
					total = resultSet.getInt("TOTAL");
				}
				JSONObject json = new JSONObject();
				long pid = resultSet.getLong("PID");
				json.put("pid", pid);
				json.put("dprName", resultSet.getString("DPR_NAME") == null ? "" : resultSet.getString("DPR_NAME"));
				json.put("dpName", resultSet.getString("DP_NAME") == null ? "" : resultSet.getString("DP_NAME"));
				
				String memo = "";
				if (StringUtils.isNotEmpty(resultSet.getString("MEMO"))) {
					memo = resultSet.getString("MEMO");
				}
				json.put("memo", memo);

				json.put("freshnessVefication", resultSet.getInt("FRESHNESS_VEFICATION"));
				
				String rawFields = "";
				if (StringUtils.isNotEmpty(resultSet.getString("RAW_FIELDS"))) {
					rawFields = resultSet.getString("RAW_FIELDS");
				}
				json.put("rawFields", rawFields);
				
				json.put("errorCount", resultSet.getInt("ERRORCOUNT"));
				json.put("collectTime", "");

				objs.put(pid, json);
			}
			result.put("total", total);

			if (objs.size() > 0) {
				LogReader logRead = new LogReader(conn);
				Map<Long, Integer> objStatus = logRead.getObjectState(objs.keySet(), ObjectName.IX_POINTADDRESS);
				
				for (Entry<Long, JSONObject> entry : objs.entrySet()) {
					Integer statusRet = objStatus.get(entry.getKey());
					JSONObject jo = entry.getValue();
					jo.put("status", statusRet == null ? 0 : statusRet);
					array.add(jo);
				}
			}
			result.put("rows", array);
			return result;
		} catch (Exception e) {
			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
}
