package com.navinfo.dataservice.control.dealership.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.util.Hash;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.edit.model.IxDealershipSource;
import com.navinfo.dataservice.api.edit.upload.EditJson;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.control.dealership.diff.DiffService;
import com.navinfo.dataservice.control.dealership.service.excelModel.DiffTableExcel;
import com.navinfo.dataservice.control.dealership.service.utils.InputStreamUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportorCommand;
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
	private DataEditService() {
	}

	private static class SingletonHolder {
		private static final DataEditService INSTANCE = new DataEditService();
	}

	public static DataEditService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private QueryRunner run = new QueryRunner();

	private static Logger log = LoggerRepos.getLogger(DataEditService.class);

	/**
	 * 申请数据
	 * 
	 * @param chainCode
	 * @param conn
	 * @param useId
	 */
	public int applyDataService(String chainCode, Connection conn, long userId) throws Exception {
		String haveDataSql = String.format(
				"SELECT COUNT(*) FROM IX_DEALERSHIP_RESULT WHERE USER_ID = %d AND WORKFLOW_STATUS = %d AND DEAL_STATUS = %d AND CHAIN = '%s'",
				userId, 3, 1, chainCode);
		int count = run.queryForInt(conn, haveDataSql);

		if (count >= 50)
			return 0;

		String queryListSql = String.format(
				"SELECT RESULT_ID FROM IX_DEALERSHIP_RESULT WHERE USER_ID = %d AND WORKFLOW_STATUS = %d AND DEAL_STATUS = %d AND CHAIN = '%s' AND ROWNUM <= %d",
				0, 3, 1, chainCode, 50 - count);
		List<Object> resultID = ExecuteQuery(queryListSql, conn);

		if(resultID.size()==0)
			return 0;
		
		String updateSql = "UPDATE IX_DEALERSHIP_RESULT SET USER_ID = " + userId + " ,DEAL_STATUS = " + 1
				+ " WHERE RESULT_ID IN (" + StringUtils.join(resultID, ",") + ")";
		run.execute(conn, updateSql);

		return resultID.size();
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
	public JSONArray loadWorkListService(String chainCode, Connection conn, long userId, int dealStatus)
			throws Exception {
		DBConnector connector = DBConnector.getInstance();
		// 待作业→内页录入作业3；已提交，待提交→出品9
		int flowStatus = 3;
		int checkErrorNum = 0;
		if (dealStatus == 3 || dealStatus == 2)
			flowStatus = 9;

		String queryListSql = String.format(
				"SELECT RESULT_ID,NAME,KIND_CODE,WORKFLOW_STATUS,DEAL_SRC_DIFF,REGION_ID FROM IX_DEALERSHIP_RESULT WHERE USER_ID = %d AND WORKFLOW_STATUS = %d AND DEAL_STATUS = %d AND CHAIN = '%s'",
				userId, flowStatus, dealStatus, chainCode);
		List<Map<String, Object>> resultCol = ExecuteQueryForDetail(queryListSql, conn);

		JSONArray result = new JSONArray();

		if (resultCol.size() == 0) {
			JSONObject obj = new JSONObject();
			obj.put("resultId", 0);
			obj.put("name", "");
			obj.put("kindCode", "");
			obj.put("workflowStatus", 0);
			obj.put("dealSrcDiff", 0);
			obj.put("checkErrorNum", checkErrorNum);
			result.add(obj);
		}

		for (Map<String, Object> item : resultCol) {
			Map<String, Object> objMap = new HashMap<>();
			objMap.put("resultId", item.get("RESULT_ID"));
			objMap.put("name", item.get("NAME") == null ? "" : item.get("NAME"));
			objMap.put("kindCode", item.get("KIND_CODE") == null ? "" : item.get("KIND_CODE"));
			objMap.put("workflowStatus", item.get("WORKFLOW_STATUS"));
			objMap.put("dealSrcDiff", item.get("DEAL_SRC_DIFF"));

			// TODO:checkErrorNum需要计算
			if (dealStatus == 2) {
				String queryPoi = String.format(
						"SELECT CFM_POI_NUM FROM IX_DEALERSHIP_RESULT WHERE RESULT_ID = %d AND CFM_IS_ADOPTED = %d",
						item.get("RESULT_ID"), 2);
				String poiNum = run.queryForString(conn, queryPoi);
				if (poiNum.isEmpty()) {
					checkErrorNum = 0;
				} else {
					Connection conPoi = connector.getConnectionById((Integer) item.get("REGION_ID"));
					String queryPoiPid = String.format("SELECT PID FROM IX_POI WHERE POI_NUM = '%s'", poiNum);
					int poiPid = run.queryForInt(conPoi, queryPoiPid);
					checkErrorNum = GetCheckResultCount(poiPid, conPoi);
				}
			}
			objMap.put("checkErrorNum", checkErrorNum);

			JSONObject obj = JSONObject.fromObject(objMap);
			result.add(obj);
		}
		return result;
	}

	private Integer GetCheckResultCount(Integer poiPid, Connection conn) throws Exception {
		if (poiPid == 0)
			return 0;

		String checkSqlStr = String.format(
				"SELECT COUNT(*) FROM NI_VAL_EXCEPTION NE,CK_RESULT_OBJECT CK WHERE NE.MD5_CODE = CK.MD5_CODE AND CK.PID = %d",
				poiPid);
		int count = run.queryForInt(conn, checkSqlStr);

		return count;
	}

	public JSONObject diffDetailService(int resultId, Connection conn) throws Exception {
		Collection<Integer> resultIds = new ArrayList<>();
		resultIds.add(resultId);

		Map<Integer, IxDealershipResult> dealership = IxDealershipResultSelector.getByResultIds(conn, resultIds);
		IxDealershipResult corresDealership = dealership.get(resultId);

		if (corresDealership == null)
			return null;

		// dealership_result中最匹配的五个poi
		List<String> matchPoiNums = getMatchPoiNum(corresDealership);
		List<IxPoi> matchPois = new ArrayList<>();

		int regionDbId = corresDealership.getRegionId();
		Connection connPoi = DBConnector.getInstance().getConnectionById(regionDbId);
		IxPoiSelector poiSelector = new IxPoiSelector(connPoi);

		// dealership_source中是否已存在的cfm_poi_num
		String querySourceSql = String.format("SELECT CFM_POI_NUM FROM IX_DEALERSHIP_SOURCE WHERE CFM_POI_NUM IN (%s)",
				StringUtils.join(matchPoiNums, ','));
		List<Object> adoptedPoiNum = new ArrayList<>();
		List<Integer> adoptedPoiPid=new ArrayList<>();
		if (matchPoiNums.size() != 0) {
			adoptedPoiNum = ExecuteQuery(querySourceSql, conn);
		}

		for (String poiNum : matchPoiNums) {
			String queryPoiPid = String.format("SELECT PID FROM IX_POI WHERE POI_NUM = %s AND U_RECORD <> 2", poiNum);
			int poiPid = run.queryForInt(connPoi, queryPoiPid);

			if (adoptedPoiNum.contains((Object) poiNum.replace("'", ""))) {
				adoptedPoiPid.add(poiPid);
			}
			
			if (poiPid < 0)
				continue;

			IxPoi poi = (IxPoi) poiSelector.loadById(poiPid, false);
			matchPois.add(poi);
		}

		JSONArray poiArray = IxDealershipResultOperator.componentPoiData(matchPois);
		JSONObject result = componentJsonData(corresDealership, poiArray, adoptedPoiPid, conn);
		return result;
	}


	/**
	 * dealership和poi共同组成详细加载数据
	 * @param dealership
	 * @param poiJson
	 * @param adoptedPoiNums
	 * @param connDealership
	 * @return
	 * @throws Exception
	 */
	private JSONObject componentJsonData(IxDealershipResult dealership, JSONArray poiJson, List<Integer> adoptedPoiNums,
			Connection connDealership) throws Exception {
		JSONObject result = new JSONObject();

		// dealership部分
		Map<String, Object> dealershipMap = new HashMap<>();
		dealershipMap.put("name", dealership.getName() == null ? "" : dealership.getName());
		dealershipMap.put("nameShort", dealership.getNameShort() == null ? "" : dealership.getNameShort());
		dealershipMap.put("address", dealership.getAddress() == null ? "" : dealership.getAddress());
		dealershipMap.put("kindCode", dealership.getKindCode() == null ? "" : dealership.getKindCode());
		dealershipMap.put("telSale", dealership.getTelSale() == null ? "" : dealership.getTelSale());
		dealershipMap.put("telService", dealership.getTelService() == null ? "" : dealership.getTelService());
		dealershipMap.put("telOther", dealership.getTelOther() == null ? "" : dealership.getTelOther());
		dealershipMap.put("postCode", dealership.getPostCode() == null ? "" : dealership.getPostCode());
		dealershipMap.put("cfmMemo", dealership.getCfmMemo() == null ? "" : dealership.getCfmMemo());
		dealershipMap.put("fbContent", dealership.getFbContent() == null ? "" : dealership.getFbContent());
		dealershipMap.put("matchMethod", dealership.getMatchMethod());
		dealershipMap.put("resultId", dealership.getResultId());
		dealershipMap.put("dbId", dealership.getRegionId());
		dealershipMap.put("cfmPoiNum", dealership.getCfmPoiNum() == null ? "" : dealership.getCfmPoiNum());
		dealershipMap.put("cfmIsAdopted", dealership.getCfmIsAdopted());

		String sourcesql = String.format("SELECT CFM_MEMO FROM IX_DEALERSHIP_SOURCE WHERE SOURCE_ID = %d",
				dealership.getSourceId());
		String sourceCfmMemo = run.queryForString(connDealership, sourcesql);
		dealershipMap.put("sourceCfmMemo", sourceCfmMemo == null ? "" : sourceCfmMemo);
		dealershipMap.put("workflowStatus", dealership.getWorkflowStatus());

		JSONObject dealershipJson = JSONObject.fromObject(dealershipMap);
		result.put("dealership", dealershipJson);

		// pois
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

		if (corresDealership.getPoiNum1() != null) {
			result.add("'" + corresDealership.getPoiNum1() + "'");
		}
		if (corresDealership.getPoiNum2() != null) {
			result.add("'" + corresDealership.getPoiNum2() + "'");
		}
		if (corresDealership.getPoiNum3() != null) {
			result.add("'" + corresDealership.getPoiNum3() + "'");
		}
		if (corresDealership.getPoiNum4() != null) {
			result.add("'" + corresDealership.getPoiNum4() + "'");
		}
		if (corresDealership.getPoiNum5() != null) {
			result.add("'" + corresDealership.getPoiNum5() + "'");
		}

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
				detail.put("REGION_ID", resultSet.getInt(6));

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
	
	/**
	 * 启动录入作业
	 * @param chainCode品牌号
	 * @return 执行结果msg
	 * @author songhe
	 * 
	 * */
	public String startWork(String chainCode, long userId) throws SQLException{
		Connection con = null;
		Connection dailycon = null;
		Connection mancon = null;
		try{
			//代理店数据库
			con = DBConnector.getInstance().getDealershipConnection();
	
			//品牌表赋值为3
			log.info("chainCode:"+chainCode+"对应的数据开始品牌表赋值为3");
			editChainStatus(chainCode, con);

			List<Integer> resultIdList = getResultId(chainCode, con);
			for(int resultId : resultIdList){
				int workflow_status = getWorkflowStatus(resultId, con);
				
				if(1 == workflow_status){
					try{
						//调用差分一致业务逻辑
						log.info(resultId+"开始执行差分一致业务逻辑");
						editResultCaseStatusSame(resultId, con);
						inserDealershipHistory(con,3,resultId,workflow_status,9,userId);
						//根据RESULT表维护SOURCE表
						log.info(resultId+"开始根据RESULT表维护SOURCE表");
						resultMaintainSource(resultId, con);
						con.commit();
					}catch(Exception e){
						con.rollback();
						log.error(e.getMessage());
						continue;
					}
				}else if(2 == workflow_status){
					int regionId = 0;
					int dailyDbId = 0;
					try{
						//man库
						mancon = DBConnector.getInstance().getManConnection();
						regionId = getRegionId(resultId, con);
						dailyDbId = getDailyDbId(regionId, mancon);
						dailycon = DBConnector.getInstance().getConnectionById(dailyDbId);
						//表内批表外
						log.info(resultId+"开始根表内批表外操作");
						insideEditOutside(resultId, chainCode, con, dailycon, userId, dailyDbId);
						//清空关联POI作业属性
						log.info(resultId+"开始清空关联POI");
						clearRelevancePoi(resultId, con);
						inserDealershipHistory(con,3,resultId,workflow_status,9,userId);
						//根据RESULT表维护SOURCE表
						log.info(resultId+"开始根据RESULT表维护SOURCE表");
						resultMaintainSource(resultId, con);
						con.commit();
					}catch(Exception e){
						con.rollback();
						log.error(e.getMessage());
						continue;
					}
				}
			}
			return "success ";
		}catch(Exception e){
			e.printStackTrace();
			DbUtils.rollback(con);
			DbUtils.rollback(mancon);
			DbUtils.rollback(dailycon);
		}finally{
			if(con != null){
				DbUtils.commitAndClose(con);
			}
			if(mancon != null){
				DbUtils.commitAndClose(mancon);
			}
			if(dailycon != null){
				DbUtils.commitAndClose(dailycon);
			}
		}
		return null;
	}
	
	/**
	 * 获取reginID
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public int getRegionId(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.region_id from IX_DEALERSHIP_RESULT t where t.RESULT_ID ="+resultId;
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						int regionId = rs.getInt("region_id");
						return regionId;
					}
					return -1;
				}
			};
			
			return run.query(con, sql, rs);
		}catch(Exception e){
			throw e;
		}
	}
			
	/**
	 * 获取dailyDbId
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public int getDailyDbId(int regionId, Connection mancon) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.daily_db_id from REGION t where t.region_id =" + regionId;
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						int dailyDbId = rs.getInt("daily_db_id");
						return dailyDbId;
					}
					return -1;
				}
			};
			
			return run.query(mancon, sql, rs);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 代理店品牌表状态修改已完成
	 * IX_DEALERSHIP_CHAIN.WORK_STATUS赋值为3
	 * @param con
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public void editChainStatus(String chainCode, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "update IX_DEALERSHIP_CHAIN t set t.work_status = 3 where t.chain_code = '"+chainCode+"'";
			run.execute(con, sql);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 查询表库查分结果
	 * @param con
	 * @return workflow_status
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public int getWorkflowStatus(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select r.workflow_status from IX_DEALERSHIP_RESULT r where r.RESULT_ID = "+resultId;
			
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					int workflow_status = 0;
					if (rs.next()) {
						workflow_status = rs.getInt("workflow_status");
					}
					return workflow_status;
				}
			};
			
			return run.query(con, sql, rs);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 查询对应品牌的resultID
	 * @param con
	 * @return workflow_status
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public List<Integer> getResultId(String chainCode, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select r.RESULT_ID from IX_DEALERSHIP_RESULT r where r.chain = '"+chainCode+"'";
			
			ResultSetHandler<List<Integer>> rs = new ResultSetHandler<List<Integer>>() {
				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> result = new ArrayList<>();;
					while (rs.next()) {
						result.add(rs.getInt("RESULT_ID"));
					}
					return result;
				}
			};
			
			return run.query(con, sql, rs);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 表库查分结果一致，result表中DEAL_STATUS赋值3，WORKFLOW_STATUS赋值9
	 * @param con
	 * @param chainCode
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public void editResultCaseStatusSame(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "update IX_DEALERSHIP_RESULT t set t.deal_status = 3, t.workflow_status = 9 where t.RESULT_ID ="+resultId;
			run.execute(con, sql);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 表内批表外
	 * @throws Exception 
	 * @param chainCode
	 * @param con
	 * @param dailycon
	 * @author songhe
	 * 
	 * */
	public void insideEditOutside(int resultId, String chainCode, Connection con, Connection dailycon, long userId, int dailyDbId) throws Exception{
		//根据chainCode查询对应外业采集POI_ID
		String poiNumber = getResultPoiNumber(resultId, con);
		if(poiNumber == null){
			log.info("resultId" + resultId + "在日库中对应的poiNumber为空");
			return;
		}
		//IX_POI数据
		Map<String, Object> resultKindCode = getResultKindCode(poiNumber, dailycon);
		if(resultKindCode == null || resultKindCode.size() == 0){
			log.info("resultId" + resultId + "在日库中对应的内容为空");
			return;
		}
		//元数据库中数据
		Map<String, Object> metaKindCode = getMetaKindCode(chainCode);
		if(metaKindCode == null || metaKindCode.size() == 0){
			log.info("resultId" + resultId + "在元数据库中对应的内容为空");
			return;
		}
		String dailyPoiChain = resultKindCode.get("poi_chain").toString();
		String dailytPoiCode = resultKindCode.get("poi_kind_code").toString();
		String MetaPoiChain = metaKindCode.get("poi_chain").toString();
		String MetaPoiCode = metaKindCode.get("poi_kind_code").toString();
		if(dailyPoiChain.equals(MetaPoiChain) && dailytPoiCode.equals(MetaPoiCode)){
			String MetaKindChain = metaKindCode.get("r_kind_chain").toString();
			String MetaKind = metaKindCode.get("r_kind").toString();
			//调用POI分类和品牌赋值方法
			log.info(resultId+"调用POI分类和品牌赋值方法");
			editResultTableBrands(resultId, MetaKindChain, MetaKind, con);
			//调用生成POI履历
			log.info(resultId+"调用生成POI履历");
			JSONObject json = prepareDeepControlData(resultKindCode, dailyDbId);
			producePOIDRecord(json, dailycon, userId);
			
			String pid = resultKindCode.get("pid").toString();
			int poiStatus = getPoiStatus(pid, dailycon);
			log.info("resultId:"+resultId+"对应的在元数据库中poiStatus为"+poiStatus);
			if(poiStatus == 0){
				//POI状态修改为已提交3
				log.info(resultId+"resultId对应的POI状态修改为已提交3");
				updatePoiStatus(pid, dailycon);
			}
			//清空关联POI作业属性
			int matchMethod = getMatchMethodFromResult(resultId, con);
			log.info(resultId+"resultId对应的matchMethod值为："+matchMethod);
			if(matchMethod == 1){
				log.info(resultId+"resultId清空关联POI作业属性");
				clearRelevancePoi(resultId, con);
				//生成一条履历
				int workflow_status = getWorkflowStatus(resultId, con);
				inserDealershipHistory(con,3,resultId,workflow_status,9,userId);
			}
		}
	}
	
	/**
	 * 准备调用生成poi履历的数据
	 * @param map
	 * @param result
	 * 
	 * */
	public JSONObject prepareDeepControlData(Map<String, Object> poiMap, int dailyDbId){
		
		Map<String, Object> data = new HashMap<>();
		Map<String, Object> result = new HashMap<>();
		
		String pid = poiMap.get("pid").toString();
		String rowId = poiMap.get("row_id").toString();
		String chain = poiMap.get("poi_chain").toString();
		String kindCode = poiMap.get("poi_kind_code").toString();
		result.put("dbId", dailyDbId);
		result.put("objId", pid);
		result.put("command", "UPDATE");
		result.put("type", "IXPOI");
		data.put("rowId", rowId);
		data.put("chain", chain);
		data.put("kindCode", kindCode);
		data.put("pid", pid);
		data.put("objStatus", "UPDATE");
		data.put("command", "UPDATE");
		result.put("data", data);
		
		JSONObject json = JSONObject.fromObject(result); 
		
		return json;
	}
	
	/**
	 * 生成POI履历
	 * @param map
	 * @param result
	 * @throws Exception 
	 * 
	 * */
	public void producePOIDRecord(JSONObject json, Connection dailycon, long userId) throws Exception{
		try {
            DefaultObjImportor importor = new DefaultObjImportor(dailycon,null);
			EditJson editJson = new EditJson();
			editJson.addJsonPoi(json);
			DefaultObjImportorCommand command = new DefaultObjImportorCommand(editJson);
			importor.operate(command);
			importor.persistChangeLog(OperationSegment.SG_COLUMN, userId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * 获取result表中MATCH_METHOD
	 * @param  chainCode
	 * @param  con
	 * @return result
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public int getMatchMethodFromResult(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select t.match_method from IX_DEALERSHIP_RESULT t where t.RESULT_ID = "+resultId;
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					int result  = 0;
					if (rs.next()) {
						result = rs.getInt("match_method");
					}
					return result;
				}
			};
			return run.query(con, selectSql, rs);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 将POI状态改为“已提交”
	 * @param  pid
	 * @param  dailycon
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public void updatePoiStatus(String pid, Connection dailycon) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "update POI_EDIT_STATUS t set t.status = 3 where t.pid = "+pid;
			run.execute(dailycon, sql);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 查询reginDB中IX_POI中的分类和品牌
	 * @param poiNumber
	 * @param dailycon
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public Map<String, Object> getResultKindCode(String poiNumber, Connection dailycon) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.row_id,t.pid,t.kind_code,t.chain from IX_POI t where t.poi_num ='"+poiNumber+"'";
			ResultSetHandler<Map<String, Object>> rs = new ResultSetHandler<Map<String, Object>>() {
				@Override
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String, Object> result = new HashMap();
					if (rs.next()) {
						result.put("poi_kind_code", rs.getString("kind_code"));
						result.put("poi_chain", rs.getString("chain"));
						result.put("pid", rs.getInt("pid"));
						result.put("row_id", rs.getObject("row_id"));
					}
					return result;
				}
			};
			return run.query(dailycon, sql, rs);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 查询元数据库中的POIKIND和POIKIND_CHAIN
	 * @param con
	 * @param chainCode
	 * @throws SQLException 
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public Map<String, Object> getMetaKindCode(String chainCode) throws SQLException{
		Connection Metacon = null;
		try{
			Metacon = DBConnector.getInstance().getMetaConnection();
			QueryRunner run = new QueryRunner();
			String sql = "select t.r_kind_chain, t.r_kind,t.poikind,t.poikind_chain from SC_POINT_KIND_INNER2OUT t where t.r_kind_chain ='"+chainCode+"'";
			ResultSetHandler<Map<String, Object>> rs = new ResultSetHandler<Map<String, Object>>() {
				@Override
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String, Object> result = new HashMap();
					if (rs.next()) {
						result.put("poi_kind_code", rs.getString("poikind"));
						result.put("poi_chain", rs.getString("poikind_chain"));
						result.put("r_kind_chain", rs.getString("r_kind_chain"));
						result.put("r_kind", rs.getString("r_kind"));
					}
					return result;
				}
			};
			return run.query(Metacon, sql, rs);
		}catch(Exception e){
			DbUtils.rollbackAndClose(Metacon);
			throw e;
		}finally{
			DbUtils.close(Metacon);
		}
	}
	
	/**
	 * POI分类和品牌赋值方法
	 * @param MetaKindChain
	 * @param MetaKind
	 * @throws Exception 
	 * @author songhe
	 * */
	public void editResultTableBrands(int resultId, String brand, String kindCode, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "update IX_DEALERSHIP_RESULT t "
					+ "set t.poi_kind_code = '"+kindCode+"', t.poi_chain = '"+brand+"' where t.RESULT_ID ="+resultId;
			run.execute(con, sql);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 获取POI状态
	 * @param chainCode
	 * @param con
	 * @return poiStatus
	 * @throws Exception 
	 * @author songhe
	 * */
	public int getPoiStatus(String pid, Connection dailycon) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select t.status from POI_EDIT_STATUS t where t.pid ="+pid;
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					int result  = 0;
					if (rs.next()) {
						result = rs.getInt("status");
					}
					return result;
				}
			};
			return run.query(dailycon, selectSql, rs);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 获取POI对应number
	 * @param chainCode
	 * @param con
	 * @return poiStatus
	 * @throws Exception 
	 * @author songhe
	 * */
	public String getResultPoiNumber(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select t.cfm_poi_num from IX_DEALERSHIP_RESULT t where t.RESULT_ID ="+resultId+ "and t.IS_DELETED = 0";
			ResultSetHandler<String> rs = new ResultSetHandler<String>() {
				@Override
				public String handle(ResultSet rs) throws SQLException {
					String result  = null;
					if (rs.next()) {
						result = rs.getString("cfm_poi_num");
					}
					return result;
				}
			};
			return run.query(con, selectSql, rs);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * result维护source表
	 * @param con
	 * @throws Exception 
	 * @author songhe
	 * */
	public void resultMaintainSource(int resultId, Connection con) throws Exception{
		try{
			//查询对应resultID数据
			Map<String, Object> dataMap =  getResultTable(resultId, con);
			int sourceId = Integer.parseInt(String.valueOf(dataMap.get("sourceId")));
			log.info("sourceId:"+sourceId);
			if(dataMap != null && sourceId != 0){
				int dealSrcDiff = Integer.parseInt(String.valueOf(dataMap.get("dealSrcDiff")));
				updateSource(con, resultId, sourceId, dealSrcDiff);
			}
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 获取result表数据
	 * @param con
	 * @throws Exception 
	 * @author songhe
	 * */
	public Map<String, Object> getResultTable(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.DEAL_SRC_DIFF,t.SOURCE_ID from IX_DEALERSHIP_RESULT t where t.RESULT_ID ="+resultId;
			ResultSetHandler<Map<String, Object>> rs = new ResultSetHandler<Map<String, Object>>() {
				@Override
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String, Object> map = new HashMap();
					if (rs.next() && rs.getInt("SOURCE_ID") != 0) {
						map.put("sourceId", rs.getInt("SOURCE_ID"));
						map.put("dealSrcDiff", rs.getInt("DEAL_SRC_DIFF"));
						return map;
					}
					return null;
				}
			};
			return run.query(con, sql, rs);
		}catch(Exception e){
			throw e;
			}
	}
	
	/**
	 * 给source表赋值
	 * @param con
	 * @param resulId
	 * @param sourceId
	 * @param dealSrcDiff 更新策略标识
	 * @throws Exception 
	 * @author songhe
	 * */
	public void updateSource(Connection con, int resulId, int sourceId, int dealSrcDiff) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = null;
			String isDeletedSql = null;
			if(dealSrcDiff == 2){
				sql = "update IX_DEALERSHIP_SOURCE s set "
						+ "(s.FB_SOURCE,s.FB_CONTENT,s.FB_AUDIT_REMARK,s.FB_DATE,s.CFM_POI_NUM,s.CFM_MEMO,s.DEAL_CFM_DATE,s.POI_KIND_CODE,s.POI_CHAIN,"
						+ "s.POI_NAME,s.POI_NAME_SHORT,s.POI_ADDRESS,s.POI_POST_CODE,s.POI_X_DISPLAY,s.POI_Y_DISPLAY,"
						+ "s.POI_X_GUIDE,s.POI_Y_GUIDE,s.GEOMETRY)"
						+ "=(select t.FB_SOURCE,t.FB_CONTENT,t.FB_AUDIT_REMARK,t.FB_DATE,t.CFM_POI_NUM,t.CFM_MEMO,t.DEAL_CFM_DATE,t.POI_KIND_CODE,t.POI_CHAIN,t.POI_NAME,"
						+ "t.POI_NAME_SHORT,t.POI_ADDRESS,t.POI_POST_CODE,t.POI_X_DISPLAY,t.POI_Y_DISPLAY,t.POI_X_GUIDE,"
						+ "t.POI_Y_GUIDE,t.GEOMETRY from IX_DEALERSHIP_RESULT t where t.RESULT_ID = "+resulId+")" + "where s.SOURCE_ID = "+sourceId;
				isDeletedSql = "update IX_DEALERSHIP_SOURCE s set s.is_deleted = 1 where s.SOURCE_ID = "+sourceId;
			}else{
				sql = "update IX_DEALERSHIP_SOURCE s set "
						+ "(s.PROVINCE,s.POI_TEL,s.CITY,s.PROJECT,s.KIND_CODE,s.CHAIN,s.NAME,s.NAME_SHORT,s.ADDRESS,s.TEL_SALE, "
						+ "s.TEL_SERVICE,s.TEL_OTHER,s.POST_CODE,s.NAME_ENG,s.ADDRESS_ENG,s.PROVIDE_DATE,"
						+ "s.FB_SOURCE,s.FB_CONTENT,s.FB_AUDIT_REMARK,s.FB_DATE,s.CFM_POI_NUM,s.CFM_MEMO,s.DEAL_CFM_DATE,s.POI_KIND_CODE,s.POI_CHAIN,"
						+ "s.POI_NAME,s.POI_NAME_SHORT,s.POI_ADDRESS,s.POI_POST_CODE,s.POI_X_DISPLAY,s.POI_Y_DISPLAY,"
						+ "s.POI_X_GUIDE,s.POI_Y_GUIDE,s.GEOMETRY)"
						+ "=(select t.PROVINCE,t.POI_TEL,t.CITY,t.PROJECT,t.KIND_CODE,t.CHAIN,t.NAME,t.NAME_SHORT,t.ADDRESS,t.TEL_SALE, "
						+ "t.TEL_SERVICE,t.TEL_OTHER,t.POST_CODE,t.NAME_ENG,t.ADDRESS_ENG,t.PROVIDE_DATE,"
						+ "t.FB_SOURCE,t.FB_CONTENT,t.FB_AUDIT_REMARK,t.FB_DATE,t.CFM_POI_NUM,t.CFM_MEMO,t.DEAL_CFM_DATE,t.POI_KIND_CODE,t.POI_CHAIN,t.POI_NAME,"
						+ "t.POI_NAME_SHORT,t.POI_ADDRESS,t.POI_POST_CODE,t.POI_X_DISPLAY,t.POI_Y_DISPLAY,t.POI_X_GUIDE,"
						+ "t.POI_Y_GUIDE,t.GEOMETRY from IX_DEALERSHIP_RESULT t where t.RESULT_ID = "+resulId+")" + "where s.SOURCE_ID = "+sourceId;
				isDeletedSql = "update IX_DEALERSHIP_SOURCE s set s.is_deleted = 0 where s.SOURCE_ID = "+sourceId;
			}
			log.info("dealSrcDiff:" + dealSrcDiff);
			log.info("根据result维护source的sql："+sql);
			run.execute(con, sql);
			run.execute(con, isDeletedSql);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 清空关联poi作业属性
	 * @param con
	 * @param 
	 * @author songhe
	 * */
	public void clearRelevancePoi(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "update IX_DEALERSHIP_RESULT t set t.CFM_POI_NUM = '', t.WORKFLOW_STATUS = 9, t.CFM_IS_ADOPTED = 0 where t.RESULT_ID = "+ resultId;
			run.execute(con, sql);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 清空关联POI接口
	 * @param resultId
	 * @return 执行结果msg
	 * @author songhe
	 * 
	 * */
	public void clearRelatedPoi(int resultId, long userId) throws Exception{
		Connection con = null;
		Connection dailycon = null;
		Connection mancon = null;
		try{
			con = DBConnector.getInstance().getDealershipConnection();
			mancon = DBConnector.getInstance().getManConnection();
			int regionId = getRegionId(resultId, con);
			//异常统一抛出，返回前端异常原因
			if(regionId == -1){
				throw new Exception("resultId:"+resultId+"不存在");
			}
			int dailyDbId = getDailyDbId(regionId, mancon);
			if(dailyDbId == -1){
				throw new Exception("regionId:"+regionId+"对应的dailyDbId为空");
			}
			dailycon = DBConnector.getInstance().getConnectionById(dailyDbId);
			String chainCode = getChainCodeByResultId(resultId, con);
			if(chainCode == null || "".equals(chainCode)){
				throw new Exception("resultId:"+resultId+"对应的chainCode为空");
			}
			//表内批表外
			insideEditOutside(resultId, chainCode, con, dailycon, userId, dailyDbId);
			//根据result维护source表
			resultMaintainSource(resultId, con);
			//清空关联POI作业属性
			int workflow_status = getWorkflowStatus(resultId, con);
			clearRelevancePoi(resultId, con);
			inserDealershipHistory(con,3,resultId,workflow_status,9,userId);
		}catch(Exception e){
			DbUtils.rollback(con);
			DbUtils.rollback(mancon);
			DbUtils.rollback(dailycon);
			throw e;
		}finally{
			if(con != null){
				DbUtils.commitAndClose(con);
			}
			if(mancon != null){
				DbUtils.commitAndClose(mancon);
			}
			if(dailycon != null){
				DbUtils.commitAndClose(dailycon);
			}
		}
	}
	
	/**
	 * 根据resultId获取chain表数据
	 * @param con
	 * @throws Exception 
	 * @author songhe
	 * */
	public String getChainCodeByResultId(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.CHAIN from IX_DEALERSHIP_RESULT t where t.RESULT_ID ="+resultId;
			ResultSetHandler<String> rs = new ResultSetHandler<String>() {
				@Override
				public String handle(ResultSet rs) throws SQLException {
					String chain = null;
					if (rs.next()) {
						chain = rs.getString("CHAIN");
					}
					return chain;
				}
			};
			return run.query(con, sql, rs);
		}catch(Exception e){
			throw e;
			}
	}
	
	/**
	 * 保存数据
	 * 
	 * @param parameter
	 * @param userId
	 */
	public String saveDataService(JSONObject parameter, long userId) throws Exception {
	
        Connection poiConn = null;
        Connection dealershipConn = null;
        JSONObject result = null;
        String log="";
        
        List<Integer> pids = new ArrayList<Integer>();
        
		try{
            JSONObject dealershipInfo = JSONObject.fromObject(parameter.getString("dealershipInfo"));
            int wkfStatus= dealershipInfo.getInt("wkfStatus");
            int resultId = dealershipInfo.getInt("resultId");
            String cfmMemo = dealershipInfo.getString("cfmMemo");
            dealershipConn = DBConnector.getInstance().getDealershipConnection();
			
          //审核意见为内业录入
            if(wkfStatus==3){
            	JSONObject poiData = JSONObject.fromObject(parameter.getString("poiData"));
            	int poiDbId = poiData.getInt("dbId");
            	poiConn = DBConnector.getInstance().getConnectionById(poiDbId);
            	String cmd=poiData.getString("command");
            	if(cmd.equals("UPDATE")){
            		int objId = poiData.getInt("objId");
                    String poiNum = poiData.getString("poiNum");
                    
                    LogReader logRead = new LogReader(poiConn);
                    int sate=logRead.getObjectState(objId, "IX_POI");
                    //需判断采纳POI是否被外业删除,为删除不可保存
                    if(sate==2){
                    	throw new Exception("该poi已被外业删除，不可用");
                    }
                    //需判断采纳POI是否被占用
                    if(isOccupied(poiNum ,dealershipConn)){
                    	throw new Exception("该poi已被占用，不可用");
                    }
                    //需判断采纳POI是否已被使用
                    if(haveUsed(poiNum ,dealershipConn)){
                    	throw new Exception("该poi已被使用，不可用");
                    }
            	}

                //更新POI并且写履历
                DefaultObjImportor importor = new DefaultObjImportor(poiConn,null);
    			EditJson editJson = new EditJson();
    			editJson.addJsonPoi(poiData);
    			DefaultObjImportorCommand command = new DefaultObjImportorCommand(editJson);
    			importor.operate(command);
    			importor.persistChangeLog(OperationSegment.SG_ROW, userId);
    			OperationResult operationResult=importor.getResult();
    			
        		//获取后检查需要执行规则列表
	            //List<String> checkList=getCheckRuleList(poiConn,"dealership");

	            //调用清理检查结果方法，可调用行编和月编的
            	//cleanExByCkRule(poiConn, pids, checkList, "IX_POI");
            
	            //执行检查
	            //CheckCommand checkCommand=new CheckCommand();		
				//checkCommand.setRuleIdList(checkList);
				//Check check=new Check(poiConn,operationResult);
				//check.operate(checkCommand);
    			
    			//更新IX_DEALERSHIP_RESULT.deal_status＝2及cfm_Memo
    			updateResultDealStatus(wkfStatus,resultId,cfmMemo,dealershipConn);
    			
    			//更新IX_DEALERSHIP_RESULT.workflow_status=3，且写履历
    			updateResultWkfStatus(9,resultId,dealershipConn,userId);
            }
            
            //审核意见为转外业、转客户
            if(wkfStatus==4||wkfStatus==5){
            	//更新IX_DEALERSHIP_RESULT.cfm_Memo
            	updateResultDealStatus(wkfStatus,resultId,cfmMemo,dealershipConn);
    			//更新IX_DEALERSHIP_RESULT.workflow_status=4|5，且写履历
            	updateResultWkfStatus(wkfStatus,resultId,dealershipConn,userId);
            }
            //不代理
        	if(wkfStatus==6){
        		//更新IX_DEALERSHIP_RESULT.deal_status＝2及cfm_Memo
    			updateResultDealStatus(wkfStatus,resultId,cfmMemo,dealershipConn);
    			
    			//更新IX_DEALERSHIP_RESULT.workflow_status=9，且写履历
    			updateResultWkfStatus(9,resultId,dealershipConn,userId);
        	}
 
            return log;
		}catch(Exception e){
            DbUtils.rollback(dealershipConn);
            DbUtils.rollback(poiConn);
            throw e;
		}finally{
            DbUtils.commitAndClose(dealershipConn);
            DbUtils.commitAndClose(poiConn);
		}
	}
	
	public boolean isOccupied(String poiNum , Connection conn ) throws Exception {
		QueryRunner run = new QueryRunner();

		String sql = String.format(
				"SELECT COUNT(1) FROM IX_DEALERSHIP_RESULT r WHERE r.deal_status=2 AND r.cfm_poi_num='%s' AND CFM_IS_ADOPTED=2 ",poiNum);
		int count = run.queryForInt(conn, sql);

		if (count > 0){return true;}
		
		return false;
}
	public boolean haveUsed(String poiNum , Connection conn ) throws Exception {
		QueryRunner run = new QueryRunner();

		String sql = String.format(
				"select count(1) from IX_DEALERSHIP_SOURCE s,IX_DEALERSHIP_RESULT r where s.source_id=r.source_id and s.cfm_poi_num='%s' ",poiNum);
		int count = run.queryForInt(conn, sql);

		if (count > 0){return true;}
		
		return false;
	}
	
	public void updateResultDealStatus(int wkfStatus,int resultId,String cfmMemo,Connection conn) throws Exception {
		QueryRunner run = new QueryRunner();
		String sql="";
		if(wkfStatus==4||wkfStatus==5){
			sql = String.format("UPDATE IX_DEALERSHIP_RESULT r SET R.cfm_Memo='%s' WHERE r.RESULT_ID=%d ",cfmMemo,resultId);
		}else{
			sql = String.format("UPDATE IX_DEALERSHIP_RESULT r SET r.deal_status＝2,R.cfm_Memo='%s' WHERE r.RESULT_ID=%d ",cfmMemo,resultId);
		}
		
		run.execute(conn, sql);
	}
	
	public void updateResultWkfStatus(int wkfStatus,int resultId,Connection conn,long userId) throws Exception {
		QueryRunner run = new QueryRunner();
		String sql = String.format("UPDATE IX_DEALERSHIP_RESULT r SET r.WORKFLOW_STATUS＝%d  WHERE r.RESULT_ID=%d ",wkfStatus,resultId);
		run.execute(conn, sql);
		
		int oldWorkflow= getWorkflowStatus(resultId, conn);
		inserDealershipHistory(conn,3,resultId, oldWorkflow, wkfStatus,userId);
	}

	/**
	 * 提交数据
	 * @param chainCode
	 * @param conn
	 * @throws Exception 
	 */
	public void commitDealership(String chainCode, Connection conn,long userId) throws Exception {
		try {
			List<IxDealershipResult> resultList = IxDealershipResultSelector.getResultIdListByChain(chainCode,conn,userId);//根据chain得到待提交差分结果列表
			if(resultList!=null&&!resultList.isEmpty()){
				for (IxDealershipResult result : resultList) {
					Connection regionConn = null;
					try {
						String poiNum = result.getCfmPoiNum();
						regionConn = DBConnector.getInstance().getConnectionById(result.getRegionId());
						int count = queryCKLogByPoiNum(poiNum,"IX_POI",regionConn);//查询该pid下有无错误log
						if(count==0){
							IxDealershipResult noLogResult = IxDealershipResultSelector.
									getIxDealershipResultById(result.getResultId(),conn);//根据resultId主键查询IxDealershipResult
							updatePoiStatusByPoiNum(poiNum,regionConn);//修改poi状态为3 已提交
							IxDealershipResultSelector.updateResultDealStatus(result.getResultId(),conn);//更新RESULT.DEAL_STATUS＝3（已提交）
							IxDealershipSourceSelector.saveOrUpdateSourceByResult(noLogResult,conn);//同步根据RESULT更新SOURCE表
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw e;
					} finally{
						DbUtils.commitAndCloseQuietly(regionConn);
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
			
	/**
	 * 提交时更新poi状态从0改为为3
	 * @param poiNum
	 * @param conn
	 * @throws Exception
	 */
	private void updatePoiStatusByPoiNum(String poiNum,Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" UPDATE POI_EDIT_STATUS SET STATUS = 3 WHERE STATUS = 0");
		sb.append(" AND PID = (SELECT PID FROM IX_POI WHERE POI_NUM = :1)");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, poiNum);
			pstmt.executeUpdate();

			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}

	/**
	 * 查询该pid下有无错误log
	 * @param pid
	 * @param regionConn
	 * @return
	 * @throws Exception 
	 */
	private int queryCKLogByPoiNum(String poiNum,String tbNm, Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT COUNT(1)");
		sb.append(" FROM CK_RESULT_OBJECT CO, NI_VAL_EXCEPTION NE,IX_POI P");
		sb.append(" WHERE CO.MD5_CODE = NE.MD5_CODE");
		sb.append(" AND CO.TABLE_NAME = :1");
		sb.append(" AND CO.PID = P.PID");
		sb.append(" AND P.POI_NUM = :2");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, tbNm);
			pstmt.setString(2,poiNum);
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				return resultSet.getInt(1);
			}

			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return 0;
	}

	/**
	 * 生成代理店履历
	 * @param oldValue --> oldWorkStatus
	 * @param newValue --> newWorkStatus
	 * @param operRate 1新增2	删除3	修改
	 * @param userId
	 * @param con
	 * @throws Exception 
	 * 
	 * */
	public void inserDealershipHistory(Connection con, int operRate, int resultId, int oldValue, int newValue, long userId) throws Exception{
		try{
			Date nowTime = new Date(System.currentTimeMillis());
			String u_date = DateUtils.formatDate(nowTime);
			QueryRunner run = new QueryRunner();
			String sql = "insert into IX_DEALERSHIP_HISTORY  t (t.history_id,t.result_id,t.field_name,t.u_record,t.old_value,t.new_value,t.u_date,t.user_id) "
					+ "VALUES (HISTORY_SEQ.NEXTVAL,"+resultId+",'workflow_status',"+operRate+","+oldValue+","+newValue+",'"+u_date+"',"+userId+")";
			log.info("插入代理店履历："+sql);
			run.execute(con, sql);
		}catch(Exception e){
			throw e;
			}
	}

	/**
	 * @param userId 
	 * @param resultIds
	 * @throws ServiceException 
	 */
	public void passDealership(long userId, JSONArray resultIds) throws ServiceException {
		Connection conn = null;
		try{
			//获取代理店数据库连接
			conn=DBConnector.getInstance().getDealershipConnection();
			Map<Integer,IxDealershipResult> ixDealershipResultMap = IxDealershipResultSelector.getByResultIds(conn, JSONArray.toCollection(resultIds));
			for(IxDealershipResult ixDealershipResult:ixDealershipResultMap.values()){
				ixDealershipResult.setWorkflowStatus(3);
				ixDealershipResult.setCfmStatus(0);
				IxDealershipResultOperator.updateIxDealershipResult(conn, ixDealershipResult, userId);
			}
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @param request
	 * @param userId
	 * @throws Exception 
	 */
	public void impConfirmData(HttpServletRequest request, long userId) throws Exception {
		log.info("start 客户确认导入");
		
		//excel文件上传到服务器		
		//保存文件
		String filePath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.uploadPath)+"/dealership/fullChainExcel";  //服务器部署路径 /data/resources/upload
//		String filePath = "D:\\data\\resources\\upload\\dealership\\fullChainExcel";
		log.info("文件由本地上传到服务器指定位置"+filePath);
		JSONObject returnParam = InputStreamUtils.request2File(request, filePath);
		String localFile=returnParam.getString("filePath");
//		String chainCode = "4007";
		String chainCode = returnParam.getString("chainCode");
		log.info("文件已上传至"+localFile);
		//导入表表差分结果excel
		List<Map<String, Object>> sourceMaps=impConfirmExcel(localFile);
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getDealershipConnection();
			//导入到oracle库中
			for(Map<String, Object> map:sourceMaps){
				IxDealershipResult ixDealershipResult = new IxDealershipResult();
				ixDealershipResult.setResultId(Integer.parseInt(map.get("resultId").toString()));
				ixDealershipResult.setFbAuditRemark(map.get("fbAuditRemark").toString());
				ixDealershipResult.setFbContent(map.get("fbContent").toString());
				ixDealershipResult.setFbDate(map.get("fbDate").toString());
				ixDealershipResult.setCfmMemo(map.get("cfmMemo").toString());
				ixDealershipResult.setFbSource(2);
				IxDealershipResultOperator.updateIxDealershipResult(conn, ixDealershipResult, userId);
			}
			log.info("end 客户确认导入");
		}catch(Exception e){
			log.error("", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException(e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @param upFile
	 * @return
	 * @throws Exception 
	 */
	private List<Map<String, Object>> impConfirmExcel(String upFile) throws Exception {
		log.info("start 导入表表差分结果excel："+upFile);
		ExcelReader excleReader = new ExcelReader(upFile);
		Map<String,String> excelHeader = new HashMap<String,String>();
		
		excelHeader.put("UUID", "resultId");
		excelHeader.put("四维确认备注", "cfmMemo");
		excelHeader.put("负责人反馈结果", "fbContent");
		excelHeader.put("审核意见", "fbAuditRemark");
		excelHeader.put("反馈时间", "fbDate");
		
		List<Map<String, Object>> sources = excleReader.readExcelContent(excelHeader);
		log.info("end 导入客户确认excel："+upFile);
		return sources;
	}
}
