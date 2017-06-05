package com.navinfo.dataservice.control.dealership.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.search.IxPoiSearch;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiColumnStatusSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 代理店数据编辑类
 * 
 * @author jicaihua
 *
 */
public class DataEditService {

	private QueryRunner run = new QueryRunner();

	/**
	 * 申请数据
	 * 
	 * @param chainCode
	 * @param conn
	 * @param useId
	 */
	public int applyDataService(String chainCode, Connection conn, long userId) throws Exception {
		String haveDataSql = String.format(
				"SELECT COUNT(*) FROM IX_DEALERSHIP_RESULT WHERE USERID = %d AND WORKFLOW_STATUS = %d AND DEAL_STAUTS = %d AND CHAIN = %s FOR UPDATE NOWAIT;",
				userId, 3, 1, chainCode);
		int count = run.queryForInt(conn, haveDataSql);

		if (count >= 50)
			return 0;

		String queryListSql = String.format(
				"SELECT RESULT_ID FROM IX_DEALERSHIP_RESULT WHERE USERID = %d AND WORKFLOW_STATUS = %d AND DEAL_STAUTS = %d AND CHAIN = %s AND ROWNUM <= %d FOR UPDATE NOWAIT;",
				0, 3, 1, chainCode, 50 - count);
		List<Object> resultID = ExecuteQuery(queryListSql, conn);

		String updateSql = "UPDATE IX_DEALERSHIP_RESULT SET USER_ID = " + userId + " ,DEAL_STATUS = " + 1
				+ " WHERE RESULT_ID IN (" + StringUtils.join(resultID, ",") + ")";
		run.execute(conn, updateSql);

		return 50 - count;
	}

	/**
	 * 开始作业：加载分配数据列表
	 * 
	 * @param chainCode
	 * @param conn
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public JSONArray startWorkService(String chainCode, Connection conn, long userId, int dealStatus) throws Exception {
		// 待作业，待提交→内页录入作业3；已提交→出品9
		int flowStatus = 3;
		if (dealStatus == 3 || dealStatus == 2)
			flowStatus = 9;

		String queryListSql = String.format(
				"SELECT RESULT_ID,NAME,KIND_CODE,WORKFLOW_STATUS,DEAL_SRC_DIFF FROM IX_DEALERSHIP_RESULT WHERE USERID = %d AND WORKFLOW_STATUS = %d AND DEAL_STAUTS = %d AND CHAIN = %s FOR UPDATE NOWAIT;",
				userId, flowStatus, dealStatus, chainCode);
		List<Map<String, Object>> resultCol = ExecuteQueryForDetail(queryListSql, conn);

		JSONArray result = new JSONArray();

		for (Map<String, Object> item : resultCol) {
			JSONObject obj = new JSONObject();
			obj.put("resultId", item.get("RESULT_ID"));
			obj.put("name", item.get("NAME"));
			obj.put("kindCode", item.get("KIND_CODE"));
			obj.put("workflowStatus", item.get("WORKFLOW_STATUS"));
			obj.put("dealSrcDiff", item.get("DEAL_SRC_DIFF"));

			// TODO:checkErrorNum需要计算
			String queryPoi = String.format(
					"SELECT CFM_POI_NUM FROM IX_DEALERSHIP_RESULT WHERE RESULT_ID = %d AND CFM_STATUS = %d",
					item.get("RESULT_ID"), 2);
			String poiPid = run.queryForString(conn, queryPoi);
			obj.put("checkErrorNum", GetCheckResultCount(poiPid, conn));
			result.add(obj);
		}
		return result;
	}

	private Integer GetCheckResultCount(String poiPid, Connection conn) throws Exception {
		if (poiPid.isEmpty())
			return 0;

		String checkSqlStr = String.format(
				"SELECT COUNT(*) FROM NI_VAL_EXCEPTION NE,CK_RESULT_OBJECT CK WHERE NE.MD5_CODE = CK.MD5_CODE AND CK.PID = %d",
				Integer.valueOf(poiPid));
		int count = run.queryForInt(conn, checkSqlStr);

		return count;
	}

	public JSONObject diffDetailService(int resultId, Connection conn) throws Exception {
		Collection<Integer> resultIds = new ArrayList<>();
		resultIds.add(resultId);

		Map<Integer, IxDealershipResult> dealership = IxDealershipResultSelector.getByResultIds(conn, resultIds);
		IxDealershipResult corresDealership = dealership.get(resultId);

		// dealership_result中最匹配的五个poi
		List<String> matchPoiNums = getMatchPoiNum(corresDealership);
		List<IxPoi> matchPois = new ArrayList<>();

		int regionDbId = corresDealership.getRegionId();
		Connection connPoi = DBConnector.getInstance().getConnectionById(regionDbId);
		IxPoiSelector poiSelector = new IxPoiSelector(connPoi);

		// dealership_source中是否已存在的cfm_poi_num
		String querySourceSql = String.format("SELECT CFM_POI_NUM FROM IX_DEALERSHIP_SOUORCE WHERE CFM_POI_NUM IN (%s)",
				StringUtils.join(matchPoiNums, ','));
		List<Object> adoptedPoiNum = ExecuteQuery(querySourceSql, conn);

		for (Object poiNum : matchPoiNums) {
			String queryPoiPid=String.format("SELECT PID FROM IX_POI WHERE POI_NUM = %s", (String)poiNum);
			int poiPid=run.queryForInt(connPoi, queryPoiPid);
			IxPoi poi = (IxPoi) poiSelector.loadById(poiPid, false);
			matchPois.add(poi);
		}
		
		JSONObject result=componentJsonData(corresDealership,matchPois,adoptedPoiNum,connPoi,conn);
		return result;
	}

	private JSONObject componentJsonData(IxDealershipResult dealership, List<IxPoi> matchPoi, List<Object> adoptedPoiNums,
			Connection conn,Connection connDealership) throws Exception {
		JSONObject result = new JSONObject();

		// dealership部分
		JSONObject dealershipJson = new JSONObject();
		dealershipJson.put("name", dealership.getName());
		dealershipJson.put("nameShort", dealership.getNameShort());
		dealershipJson.put("address", dealership.getAddress());
		dealershipJson.put("kindCode", dealership.getKindCode());
		dealershipJson.put("chainName", dealership.getChain());
		dealershipJson.put("telSale", dealership.getTelSale());
		dealershipJson.put("telService", dealership.getTelService());
		dealershipJson.put("telOther", dealership.getTelOther());
		dealershipJson.put("postCode", dealership.getPostCode());
		dealershipJson.put("cfmMemo", dealership.getCfmMemo());
		dealershipJson.put("fbContent", dealership.getFbContent());
		dealershipJson.put("matchMethod", dealership.getMatchMethod());
		dealershipJson.put("resultId", dealership.getResultId());
		dealershipJson.put("dbId", dealership.getRegionId());

		String sourcesql = String.format("SELECT CFM_MEMO FROM IX_DEALERSHIP_SOUORCE WHERE SOURCE_ID = %d",
				dealership.getSourceId());
		String sourceCfmMemo = run.queryForString(connDealership, sourcesql);
		dealershipJson.put("sourceCfmMemo", sourceCfmMemo);
		dealershipJson.put("workflowStatus", dealership.getWorkflowStatus());
		result.put("dealership", dealershipJson);

		// 匹配poi部分
		JSONArray poiJson = new JSONArray();
		for (IxPoi poi : matchPoi) {
			String poiName_1 = String.format(
					"SELECT NAME FROM IX_POI_NAME WHERE NAME_CLASS=1 AND NAME_TYPE=1 AND (LANG_CODE = 'CHI' OR LANG_CODE = 'CHT') AND POI_PID=%d",
					poi.getPid());
			String poiName_2 = String.format(
					"SELECT NAME FROM IX_POI_NAME WHERE NAME_CLASS=3 AND NAME_TYPE=1 AND (LANG_CODE = 'CHI' OR LANG_CODE = 'CHT')  AND POI_PID=%d",
					poi.getPid());
			String poiContact_sale = String.format(
					"SELECT CONTACT C1 FROM IX_POI_CONTACT WHERE CONTACT_DEPART=8 AND CONTACT_TYPE=1 AND POI_PID = %d",
					poi.getPid());
			String poiContact_repair = String.format(
					"SELECT CONTACT C2 FROM IX_POI_CONTACT WHERE CONTACT_DEPART=16 AND CONTACT_TYPE=1 AND POI_PID = %d",
					poi.getPid());
			String poiContact_other = String.format(
					"SELECT CONTACT C3 FROM IX_POI_CONTACT WHERE CONTACT_DEPART=32 AND CONTACT_TYPE=1 AND POI_PID = %d",
					poi.getPid());
			String poiContact_special = String.format(
					"SELECT CONTACT C4 FROM IX_POI_CONTACT WHERE CONTACT_TYPE=3 AND POI_PID = %d", poi.getPid());
			String poiAddress = String.format(
					"SELECT FULLNAME FROM IX_POI_ADDRESS WHERE POI_PID=%d AND (LANG_CODE = 'CHI' OR LANG_CODE = 'CHT')",
					poi.getPid());

			JSONObject obj = new JSONObject();
			int value = 0;
			obj.put("poiNum", value);
			obj.put("pid", poi.getPid());
			obj.put("name", run.queryForString(conn, poiName_1));
			obj.put("nameAlias", run.queryForString(conn, poiName_2));
			obj.put("address", run.queryForString(conn, poiAddress));
			obj.put("kindCode", poi.getKindCode());
			obj.put("chain", poi.getChain());
			obj.put("telSale", run.queryForString(conn, poiContact_sale));
			obj.put("telService", run.queryForString(conn, poiContact_repair));
			obj.put("telOther", run.queryForString(conn, poiContact_other));
			obj.put("telSpecial", run.queryForString(conn, poiContact_special));
			obj.put("postCode", poi.getPostCode());
		}
		result.put("pois", poiJson);

		// usedPid部分
		result.put("usedPoiPids", adoptedPoiNums);
		return result;
	}

	/**
	 * 获取代理店匹配的poi_num
	 * 
	 * @param corresDealership
	 * @return
	 */
	private List<String> getMatchPoiNum(IxDealershipResult corresDealership) {
		List<String> result = new ArrayList<>();

		result.add(corresDealership.getPoiNum1());
		result.add(corresDealership.getPoiNum2());
		result.add(corresDealership.getPoiNum3());
		result.add(corresDealership.getPoiNum4());
		result.add(corresDealership.getPoiNum5());

		return result;
	}

	/**
	 * 传入sql，返回需要的详细信息（RESULT_ID,NAME,KIND_CODE,WORKFLOW_STATUS,DEAL_SRC_DIFF）集合
	 * 
	 * @param sql
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> ExecuteQueryForDetail(String sql, Connection conn) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				Map<String, Object> detail = new HashMap<>();

				detail.put("RESULT_ID", resultSet.getInt(1));
				detail.put("NAME", resultSet.getString(2));
				detail.put("KIND_CODE", resultSet.getString(3));
				detail.put("WORKFLOW_STATUS", resultSet.getInt(4));
				detail.put("DEAL_SRC_DIFF", resultSet.getInt(5));

				result.add(detail);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return result;
	}

	/**
	 * 传入sql，返回代理店号码集合
	 * 
	 * @param sql
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private List<Object> ExecuteQuery(String sql, Connection conn) throws Exception {
		List<Object> resultID = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				Object value = resultSet.getObject(1);
				resultID.add(value);
			} // while
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return resultID;
	}
}
