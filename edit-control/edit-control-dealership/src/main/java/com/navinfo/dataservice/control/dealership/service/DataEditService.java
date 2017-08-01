package com.navinfo.dataservice.control.dealership.service;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.edit.upload.EditJson;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.control.dealership.service.excelModel.AddChainDataEntity;
import com.navinfo.dataservice.control.dealership.service.utils.InputStreamUtils;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportorCommand;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

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

		try {
			String queryListSql = String.format(
					"SELECT RESULT_ID FROM IX_DEALERSHIP_RESULT WHERE USER_ID = %d AND WORKFLOW_STATUS = %d AND DEAL_STATUS = %d AND CHAIN = '%s' AND ROWNUM <= %d",
					0, 3, 0, chainCode, 50 - count);
			List<Object> resultID = ExecuteQuery(queryListSql, conn);

			if (resultID.size() == 0)
				return 0;

			String updateSql = "UPDATE IX_DEALERSHIP_RESULT SET USER_ID = " + userId + " ,DEAL_STATUS = " + 1
					+ " WHERE RESULT_ID IN (" + StringUtils.join(resultID, ",") + ")";
			run.execute(conn, updateSql);
			conn.commit();

			return resultID.size();
		} catch (Exception e) {
			conn.rollback();
			log.error("申请数据：" + e.toString());
			throw new Exception(e);
		}
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
		/*int flowStatus = 3;
		if (dealStatus == 3 || dealStatus == 2)
			flowStatus = 9;
*/
		Connection manconn = null;
		JSONArray result = new JSONArray();

		try {
			manconn = DBConnector.getInstance().getManConnection();
			String queryListSql = String.format(
					"SELECT RESULT_ID,NAME,KIND_CODE,WORKFLOW_STATUS,DEAL_SRC_DIFF,REGION_ID FROM IX_DEALERSHIP_RESULT WHERE USER_ID = %d AND DEAL_STATUS = %d AND CHAIN = '%s'",
					userId, dealStatus, chainCode);
			List<Map<String, Object>> resultCol = ExecuteQueryForDetail(queryListSql, conn);

			for (Map<String, Object> item : resultCol) {
				int checkErrorNum = 0;
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
						int dbId = getDailyDbId((Integer) item.get("REGION_ID"), manconn);
						checkErrorNum = GetCheckErrorNum(dbId,poiNum);
					}
				}
				
				objMap.put("checkErrorNum", checkErrorNum);
				JSONObject obj = JSONObject.fromObject(objMap);
				result.add(obj);
			}//for
		} catch (Exception e) {
			log.error("开始作业，加载作业数据列表：" + e.toString());
			throw e;
		} finally {
			if (manconn != null) {
				DBUtils.closeConnection(manconn);
			}
		}
		return result;
	}

	private int GetCheckErrorNum(int dbId, String poiNum) throws Exception {
		Connection poiconn = null;
		int checkErrorNum = 0;
		try {
			poiconn = DBConnector.getInstance().getConnectionById(dbId);
			String queryPoiPid = String.format("SELECT PID FROM IX_POI WHERE POI_NUM = '%s'", poiNum);
			int poiPid = run.queryForInt(poiconn, queryPoiPid);
			NiValExceptionSelector selector = new NiValExceptionSelector(poiconn);
			List<String> checkRuleList=selector.loadByOperationName("DEALERSHIP_SAVE");
			JSONArray checkResultsArr = selector.poiCheckResultList(poiPid,checkRuleList);
			checkErrorNum = checkResultsArr.size();
		} catch (Exception e) {
			throw e;
		} finally {
			if (poiconn != null) {
				DBUtils.closeConnection(poiconn);
			}
		}
		return checkErrorNum;
	}

	/**
	 * 详细数据列表
	 * @param resultId
	 * @param conn
	 * @return
	 * @throws Exception
	 */
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
		Connection mancon = null;
		Connection connPoi = null;

		try {
			mancon = DBConnector.getInstance().getManConnection();
			int regionDbId = corresDealership.getRegionId();
			int dbId = getDailyDbId(regionDbId, mancon);
			connPoi = DBConnector.getInstance().getConnectionById(dbId);
			IxPoiSelector poiSelector = new IxPoiSelector(connPoi);

			// dealership_source中是否已存在的cfm_poi_num
			String querySourceSql = String.format(
					"SELECT CFM_POI_NUM FROM IX_DEALERSHIP_SOURCE WHERE CFM_POI_NUM IN (%s)",
					StringUtils.join(matchPoiNums, ','));
			List<Object> adoptedPoiNum = new ArrayList<>();
			List<Integer> adoptedPoiPid = new ArrayList<>();
			List<String> repeatedPoiNum = new ArrayList<>();
			int cfmPoiPid=0;
			if (matchPoiNums.size() != 0) {
				adoptedPoiNum = ExecuteQuery(querySourceSql, conn);
			}
			

			for (String poiNum : matchPoiNums) {
				if(repeatedPoiNum.contains(poiNum)){
					continue;
				}
				repeatedPoiNum.add(poiNum);
				
				String queryPoiPid = String.format("SELECT PID FROM IX_POI WHERE POI_NUM = %s AND U_RECORD <> 2",
						poiNum);
				int poiPid = run.queryForInt(connPoi, queryPoiPid);

				if (adoptedPoiNum.contains((Object) poiNum.replace("'", ""))
						&& !corresDealership.getCfmPoiNum().equals(poiNum.replace("'", ""))) {
					adoptedPoiPid.add(poiPid);
				}
				
				if(corresDealership.getCfmPoiNum().equals(poiNum.replace("'", ""))){
					cfmPoiPid=poiPid;
				}
				
				if (poiPid < 0)
					continue;

				IxPoi poi = (IxPoi) poiSelector.loadById(poiPid, false);
				matchPois.add(poi);
			}

			JSONArray poiArray = IxDealershipResultOperator.componentPoiData(matchPois, connPoi);
			JSONObject result = componentJsonData(corresDealership, poiArray, adoptedPoiPid, conn, dbId);
			
			//返回cfm_poi_num检查log
			JSONObject log=new JSONObject();
			
			if(cfmPoiPid!=0){
				NiValExceptionSelector selector = new NiValExceptionSelector(connPoi);
				List<String> checkRuleList = selector.loadByOperationName("DEALERSHIP_SAVE");
				JSONArray checkResultsArr = selector.poiCheckResultList(cfmPoiPid,checkRuleList);
				
				log.put("data", checkResultsArr);
				log.put("total", checkResultsArr.size());
			}
			result.put("log", log);
			
			return result;
		} catch (Exception e) {
			log.error("详细数据：" + e.toString());
			throw e;
		} finally {
			if (connPoi != null) {
				DBUtils.closeConnection(connPoi);
			}
			if (mancon != null) {
				DBUtils.closeConnection(mancon);
			}
		}
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
			Connection connDealership, int dbId) throws Exception {
		JSONObject result = new JSONObject();

		// dealership部分
		Map<String, Object> dealershipMap = new HashMap<>();
		dealershipMap.put("name", dealership.getName() == null ? "" : dealership.getName());
		dealershipMap.put("nameShort", dealership.getNameShort() == null ? "" : dealership.getNameShort());
		dealershipMap.put("address", dealership.getAddress() == null ? "" : dealership.getAddress());
		dealershipMap.put("kindCode", dealership.getKindCode() == null ? "" : dealership.getKindCode());
		dealershipMap.put("chain", dealership.getChain() == null?"":dealership.getChain());
		dealershipMap.put("telSale", dealership.getTelSale() == null ? "" : dealership.getTelSale());
		dealershipMap.put("telService", dealership.getTelService() == null ? "" : dealership.getTelService());
		dealershipMap.put("telOther", dealership.getTelOther() == null ? "" : dealership.getTelOther());
		dealershipMap.put("postCode", dealership.getPostCode() == null ? "" : dealership.getPostCode());
		dealershipMap.put("cfmMemo", dealership.getCfmMemo() == null ? "" : dealership.getCfmMemo());
		dealershipMap.put("fbContent", dealership.getFbContent() == null ? "" : dealership.getFbContent());
		dealershipMap.put("matchMethod", dealership.getMatchMethod());
		dealershipMap.put("resultId", dealership.getResultId());
		dealershipMap.put("dbId", dbId);
		dealershipMap.put("cfmPoiNum", dealership.getCfmPoiNum() == null ? "" : dealership.getCfmPoiNum());
		dealershipMap.put("cfmIsAdopted", dealership.getCfmIsAdopted());
		dealershipMap.put("dealSrcDiff", dealership.getDealSrcDiff());
		dealershipMap.put("dealStatus", dealership.getDealStatus());
		

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
		if(corresDealership.getCfmPoiNum() != null){
			result.add("'" + corresDealership.getCfmPoiNum() + "'");
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
						//workflow_status=2需删除状态时，workflow_status赋值为9，deal_status赋值为3，change by jch 20170717
						updateWorkFlowStatus(resultId, con);
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
	 * */
	public void resultMaintainSource(int resultId, Connection con) throws Exception{
		try{
			//查询对应resultID数据
			Map<String, Object> dataMap =  getResultTable(resultId, con);
			if(dataMap == null){
				return;
			}
			int sourceId = Integer.parseInt(String.valueOf(dataMap.get("sourceId")));
			log.info("sourceId:"+sourceId);
			if(sourceId != 0){
				int dealSrcDiff = Integer.parseInt(String.valueOf(dataMap.get("dealSrcDiff")));
				updateSource(con, resultId, sourceId, dealSrcDiff);
			}else{
				insertSource(con, resultId);
			}
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 获取result表数据
	 * @param con
	 * @throws Exception 
	 * */
	public Map<String, Object> getResultTable(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.DEAL_SRC_DIFF,t.SOURCE_ID from IX_DEALERSHIP_RESULT t where t.RESULT_ID ="+resultId;
			ResultSetHandler<Map<String, Object>> rs = new ResultSetHandler<Map<String, Object>>() {
				@Override
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String, Object> map = new HashMap();
					if (rs.next()) {
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
	 * 根据result维护sorce表，插入数据
	 * @param con
	 * @param resulId
	 * @throws Exception 
	 * 
	 * */
	public void insertSource(Connection con, int resulId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "insert into IX_DEALERSHIP_SOURCE s  "
					+ "(s.SOURCE_ID,s.PROVINCE,s.POI_TEL,s.CITY,s.PROJECT,s.KIND_CODE,s.CHAIN,s.NAME,s.NAME_SHORT,s.ADDRESS,"
					+ "s.TEL_SALE, s.TEL_SERVICE,s.TEL_OTHER,s.POST_CODE,s.NAME_ENG,s.ADDRESS_ENG,s.PROVIDE_DATE,s.FB_SOURCE,s.FB_CONTENT,s.FB_AUDIT_REMARK,"
					+ "s.FB_DATE,s.CFM_POI_NUM,s.CFM_MEMO,s.DEAL_CFM_DATE,s.POI_KIND_CODE,s.POI_CHAIN,s.POI_NAME,s.POI_NAME_SHORT,s.POI_ADDRESS,s.POI_POST_CODE,"
					+ "s.POI_X_DISPLAY,s.POI_Y_DISPLAY,s.POI_X_GUIDE,s.POI_Y_GUIDE,s.GEOMETRY) "
					+ "(select (SOURCE_SEQ.NEXTVAL),t.PROVINCE,t.POI_TEL,t.CITY,t.PROJECT,t.KIND_CODE,t.CHAIN,t.NAME,t.NAME_SHORT,t.ADDRESS,"
					+ "t.TEL_SALE, t.TEL_SERVICE,t.TEL_OTHER,t.POST_CODE,t.NAME_ENG,t.ADDRESS_ENG,t.PROVIDE_DATE,t.FB_SOURCE,t.FB_CONTENT,t.FB_AUDIT_REMARK,"
					+ "t.FB_DATE,t.CFM_POI_NUM,t.CFM_MEMO,t.DEAL_CFM_DATE,t.POI_KIND_CODE,t.POI_CHAIN,t.POI_NAME,t.POI_NAME_SHORT,t.POI_ADDRESS,t.POI_POST_CODE,"
					+ "t.POI_X_DISPLAY,t.POI_Y_DISPLAY,t.POI_X_GUIDE,t.POI_Y_GUIDE,t.GEOMETRY from IX_DEALERSHIP_RESULT t where t.RESULT_ID = "+resulId+")";
			
			log.info("根据result插入source的sql："+sql);
			run.execute(con, sql);
		}catch(Exception e){
			throw e;
		}
	}
	
	
	/**
	 * 根据result维护sorce--更新
	 * @param con
	 * @param resulId
	 * @param sourceId
	 * @param dealSrcDiff 更新策略标识
	 * @throws Exception 
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
	 * @param con
	 * @param 
	 * */
	public void updateWorkFlowStatus(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "update IX_DEALERSHIP_RESULT t set t.WORKFLOW_STATUS = 9,t.DEAL_STATUS=3 where t.RESULT_ID = "+ resultId;
			log.info("清空关联poiSQL："+sql);
			run.execute(con, sql);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 清空关联poi作业属性
	 * @param con
	 * @param 
	 * */
	public void clearRelevancePoi(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "update IX_DEALERSHIP_RESULT t set t.CFM_POI_NUM = '', t.CFM_IS_ADOPTED = 0 where t.RESULT_ID = "+ resultId;
			log.info("清空关联poiSQL："+sql);
			run.execute(con, sql);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 清空关联POI接口
	 * @param resultId
	 * @return 执行结果msg
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
	public OperationResult saveDataService(JSONObject parameter, long userId) throws Exception {
	
        Connection poiConn = null;
        Connection dealershipConn = null;
        JSONObject result = null;
        
        List<Integer> pids = new ArrayList<Integer>();
        
		try{
            JSONObject dealershipInfo = JSONObject.fromObject(parameter.getString("dealershipInfo"));
            int wkfStatus= dealershipInfo.getInt("workflowStatus");
            int resultId = dealershipInfo.getInt("resultId");
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
                    if(isOccupied(poiNum ,resultId,dealershipConn)){
                    	throw new Exception("该poi已被占用，不可用");
                    }
                    //需判断采纳POI是否已被使用
                    if(haveUsed(poiNum ,resultId,dealershipConn)){
                    	throw new Exception("该poi已被使用，不可用");
                    }
            	}
            	
                int mesh=0;
                JSONObject data = JSONObject.fromObject(poiData.getString("data"));
                if(data.containsKey("geometry")){
                	Set<String> meshes = new HashSet<String>();
                	Geometry geom = GeoTranslator.geojson2Jts(data.getJSONObject("geometry"), 1, 5);
                	meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geom);
                	Iterator<String> it = meshes.iterator();
            		if(it.hasNext()){
            			mesh= Integer.parseInt(it.next()) ;
            		}
            		if(mesh!=0){
            			data.put("meshId", mesh);
            			poiData.put("data", data);
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
    			updateResultDealStatus(dealershipConn,dealershipInfo);
    			
    			//更新IX_DEALERSHIP_RESULT.workflow_status=3，且写履历
    			updateResultWkfStatus(9,resultId,dealershipConn,userId);
    			return operationResult;
            }
            
            //审核意见为转外业、转客户
            if(wkfStatus==4||wkfStatus==5){
            	//更新IX_DEALERSHIP_RESULT.cfm_Memo
            	updateResultDealStatus(dealershipConn,dealershipInfo);
    			//更新IX_DEALERSHIP_RESULT.workflow_status=4|5，且写履历
            	updateResultWkfStatus(wkfStatus,resultId,dealershipConn,userId);
            }
            //不代理
        	if(wkfStatus==6){
        		//更新IX_DEALERSHIP_RESULT.deal_status＝2及cfm_Memo
    			updateResultDealStatus(dealershipConn,dealershipInfo);
    			
    			//更新IX_DEALERSHIP_RESULT.workflow_status=9，且写履历
    			updateResultWkfStatus(9,resultId,dealershipConn,userId);
        	}
 
		}catch(Exception e){
            DbUtils.rollback(dealershipConn);
            DbUtils.rollback(poiConn);
            throw e;
		}finally{
            DbUtils.commitAndClose(dealershipConn);
            DbUtils.commitAndClose(poiConn);
		}
		return null;
	}
	
	public boolean isOccupied(String poiNum ,int resultId, Connection conn ) throws Exception {
		QueryRunner run = new QueryRunner();

		String sql = String.format(
				"SELECT COUNT(1) FROM IX_DEALERSHIP_RESULT r WHERE r.deal_status=2 AND r.cfm_poi_num='%s' AND r.CFM_IS_ADOPTED=2 AND r.result_id<>%d ",poiNum,resultId);
		int count = run.queryForInt(conn, sql);

		if (count > 0){return true;}
		
		return false;
}
	public boolean haveUsed(String poiNum ,int resultId, Connection conn ) throws Exception {
		QueryRunner run = new QueryRunner();

		String sql = String.format(
				"select count(1) from IX_DEALERSHIP_SOURCE s,IX_DEALERSHIP_RESULT r where s.source_id<>r.source_id and s.cfm_poi_num='%s' and  r.result_id=%d ",poiNum,resultId);
		int count = run.queryForInt(conn, sql);

		if (count > 0){return true;}
		
		return false;
	}
	
	public void updateResultDealStatus(Connection conn,JSONObject dealershipInfo) throws Exception {
		QueryRunner run = new QueryRunner();
		String sql="";
		if (dealershipInfo.getInt("workflowStatus")==4||dealershipInfo.getInt("workflowStatus")==5){
			sql = "UPDATE IX_DEALERSHIP_RESULT r SET R.cfm_Memo=:1,R.cfm_status=1 WHERE r.RESULT_ID=:2 ";
		}else if(dealershipInfo.getInt("workflowStatus")==3){
			sql="UPDATE IX_DEALERSHIP_RESULT r SET r.deal_status＝2,r.cfm_Memo=:1,r.cfm_poi_num=:2,r.CFM_IS_ADOPTED=:3,r.POI_KIND_CODE=:4,r.POI_CHAIN=:5,r.POI_NAME=:6,r.POI_NAME_SHORT=:7,r.POI_ADDRESS=:8,r.POI_TEL=:9,r.POI_POST_CODE=:10,r.POI_X_DISPLAY=:11,r.POI_Y_DISPLAY=:12,r.POI_X_GUIDE=:13,r.POI_Y_GUIDE=:14,r.GEOMETRY=sdo_geometry(:15  , 8307) WHERE r.RESULT_ID=:16 ";
		}else{
			String dealCfmDate = DateUtils.longToString(System.currentTimeMillis(), DateUtils.DATE_COMPACTED_FORMAT);
			sql="UPDATE IX_DEALERSHIP_RESULT r SET r.deal_status＝2,r.match_method=0,r.deal_cfm_date=" + dealCfmDate +",r.cfm_Memo=:1,r.cfm_poi_num=:2,r.CFM_IS_ADOPTED=:3,r.POI_KIND_CODE=:4,r.POI_CHAIN=:5,r.POI_NAME=:6,r.POI_NAME_SHORT=:7,r.POI_ADDRESS=:8,r.POI_TEL=:9,r.POI_POST_CODE=:10,r.POI_X_DISPLAY=:11,r.POI_Y_DISPLAY=:12,r.POI_X_GUIDE=:13,r.POI_Y_GUIDE=:14,r.GEOMETRY=sdo_geometry(:15  , 8307) WHERE r.RESULT_ID=:16 ";
		}
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			System.out.println(sql);
			System.out.println(dealershipInfo.getInt("cfmIsAdopted"));
			pstmt = conn.prepareStatement(sql);
			if (dealershipInfo.getInt("workflowStatus")==4||dealershipInfo.getInt("workflowStatus")==5){
				pstmt.setString(1,dealershipInfo.getString("cfmMemo"));
				pstmt.setInt(2, dealershipInfo.getInt("resultId"));
			}else{
				pstmt.setString(1,dealershipInfo.getString("cfmMemo"));
				pstmt.setString(2, dealershipInfo.getString("cfmPoiNum"));
				pstmt.setInt(3,dealershipInfo.getInt("cfmIsAdopted"));
				pstmt.setString(4, dealershipInfo.getString("poiKindCode"));
				pstmt.setString(5,dealershipInfo.getString("poiChain"));
				pstmt.setString(6,dealershipInfo.getString("poiName"));
				pstmt.setString(7,dealershipInfo.getString("poiNameShort"));
				pstmt.setString(8,dealershipInfo.getString("poiAddress"));
				pstmt.setString(9,dealershipInfo.getString("poiTel"));
				pstmt.setString(10,dealershipInfo.getString("poiPostCode"));
				pstmt.setDouble(11, dealershipInfo.getDouble("poiXDisplay"));
				pstmt.setDouble(12, dealershipInfo.getDouble("poiYDisplay"));
				pstmt.setDouble(13, dealershipInfo.getDouble("poiXGuide"));
				pstmt.setDouble(14, dealershipInfo.getDouble("poiYGuide"));
				String wkt = "POINT(" +dealershipInfo.getDouble("poiYDisplay") + " " +  dealershipInfo.getDouble("poiXDisplay") + ")";
				pstmt.setString(15,wkt);
				pstmt.setInt(16, dealershipInfo.getInt("resultId"));
			}
			  pstmt.executeUpdate();
		}catch(Exception e){
			throw new Exception(e.getMessage(),e);
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
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
			List<IxDealershipResult> resultList = null;
			if(StringUtils.isNotBlank(chainCode)){
				resultList = IxDealershipResultSelector.getResultIdListByChain(chainCode,conn,userId);//根据chain得到待提交差分结果列表
			}
			if(resultList!=null&&!resultList.isEmpty()){
				
				
				for (IxDealershipResult result : resultList) {
					Connection regionConn = null;
					Connection mancon = null;
					try {
						String poiNum = result.getCfmPoiNum();
						
						mancon = DBConnector.getInstance().getManConnection();
						int dbId = getDailyDbId(result.getRegionId(), mancon);
						regionConn = DBConnector.getInstance().getConnectionById(dbId);
						List<String> dealerShipCheckRuleList = getDealerShipCheckRule();//查询代理店检查项
						int count = queryCKLogByPoiNum(poiNum,dealerShipCheckRuleList,regionConn);//查询该pid下有无错误log
						if(count==0){
							IxDealershipResult noLogResult = IxDealershipResultSelector.
									getIxDealershipResultById(result.getResultId(),conn);//根据resultId主键查询IxDealershipResult
							updatePoiStatusByPoiNum(poiNum,regionConn);//修改poi状态为3 已提交
							Integer resultId = result.getResultId();
							IxDealershipResultSelector.updateResultDealStatus(resultId,3,conn);//更新RESULT.DEAL_STATUS＝3（已提交）
							Integer sourceId = IxDealershipSourceSelector.saveOrUpdateSourceByResult(noLogResult,conn);//同步根据RESULT更新SOURCE表
							IxDealershipResultSelector.updateResultSourceId(resultId,sourceId,conn);
						}
						
						if(StringUtils.isNotBlank(poiNum)){//执行批处理
							List<Long> pidList = new ArrayList<>();
							pidList.add(selectPidByPoiNum(poiNum, regionConn).longValue());
							Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadAllLog(regionConn, pidList);
							Set<String> tabNames = getChangeTableSet(logs);
							// 获取poi对象
							Map<Long, BasicObj> objs = null;
							if (tabNames == null || tabNames.size() == 0) {
								objs = ObjBatchSelector.selectByPids(regionConn, ObjectName.IX_POI, tabNames, true, pidList, false, false);
							} else {
								objs = ObjBatchSelector.selectByPids(regionConn, ObjectName.IX_POI, tabNames, false, pidList, false, false);
							}
							// 将poi对象与履历合并起来
							ObjHisLogParser.parse(objs, logs);
							OperationResult operationResult = new OperationResult();
							operationResult.putAll(objs.values());

							BatchCommand batchCommand = new BatchCommand();
							batchCommand.setOperationName("BATCH_DEALERSHIP_RELEASE");
							Batch batch = new Batch(regionConn, operationResult);
							batch.operate(batchCommand);
							System.out.println(batch.getName());
							batch.persistChangeLog(OperationSegment.SG_ROW, 0);
						}
						
						
						
					} catch (Exception e) {
						e.printStackTrace();
						throw e;
					} finally{
						DbUtils.commitAndCloseQuietly(mancon);
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
	 * 查询代理店检查规则
	 * @return
	 * @throws Exception
	 */
	private List<String>  getDealerShipCheckRule() throws Exception{
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		Connection conn = null;
		try {
			
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			StringBuilder sb = new StringBuilder();
			sb.append(" SELECT check_id FROM check_operation_plus WHERE operation_code = 'DEALERSHIP_SAVE'");
			
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			List<String> dealerShipCheckRuleList = new ArrayList<>();
			while(resultSet.next()){
				dealerShipCheckRuleList.add(resultSet.getString(1));
			}
			return dealerShipCheckRuleList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
			
	/**
	 * 根据poiNum查找pid
	 * @param poiNum
	 * @param conn
	 * @return 
	 * @throws Exception
	 */
	private Integer selectPidByPoiNum(String poiNum,Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" select pid from ix_poi where poi_num = '"+poiNum+"'");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			if(resultSet.next()){
				return resultSet.getInt(1);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return null;
		
	}
	
	
	/**
	 * 提交时更新poi状态从0改为为3
	 * 如果没有对应的poi_edit_status记录，则插入一条
	 * @param poiNum
	 * @param conn
	 * @throws Exception
	 */
	private void updatePoiStatusByPoiNum(String poiNum,Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" MERGE INTO poi_edit_status t1 ");
		sb.append(" USING (SELECT p.pid FROM ix_poi p,poi_edit_status pe  WHERE p.pid = pe.pid(+) AND p.POI_NUM = :1) T2 ");
		sb.append(" ON (t1.pid = t2.pid ) ");
		sb.append(" WHEN MATCHED THEN  UPDATE SET STATUS = 3 WHERE STATUS  = 0 ");
		sb.append(" WHEN NOT MATCHED THEN INSERT (pid,status) VALUES(t2.pid,3)");
		
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
	private int queryCKLogByPoiNum(String poiNum,List<String> dealerShipCheckRuleList, Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT COUNT(1)");
		sb.append(" FROM CK_RESULT_OBJECT CO, NI_VAL_EXCEPTION NE,IX_POI P");
		sb.append(" WHERE CO.MD5_CODE = NE.MD5_CODE");
		sb.append(" AND CO.TABLE_NAME = 'IX_POI'");
		sb.append(" AND CO.PID = P.PID");
		sb.append(" AND NE.RULEID IN (");
		for (String dealerShipCheckRule : dealerShipCheckRuleList) {
			sb.append("'"+dealerShipCheckRule+"',");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(") AND P.POI_NUM = :1");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1,poiNum);
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
				if(map.get("fbAuditRemark").toString().equals("舍弃")){
					ixDealershipResult.setWorkflowStatus(9);
					ixDealershipResult.setDealStatus(3);
				}else{
					ixDealershipResult.setWorkflowStatus(3);
				}
				ixDealershipResult.setCfmStatus(3);
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
	
	public void closeChainService(Connection conn, String chainCode) throws Exception {
		if (chainCode == null || chainCode.isEmpty()) {
			throw new Exception("品牌为空，无需关闭！");
		}

		try {
			String sql = String.format(
					"SELECT COUNT(*) FROM IX_DEALERSHIP_RESULT WHERE (WORKFLOW_STATUS <> 9 OR DEAL_STATUS <>3) AND CHAIN = '%s'",
					chainCode);
			int leftChainResult = run.queryForInt(conn, sql);

			if (leftChainResult != 0) {
				throw new Exception(String.format("品牌%s存在未作业数据，无法关闭该品牌！", chainCode));
			}

			String updateSql = String.format("UPDATE IX_DEALERSHIP_CHAIN SET CHAIN_STATUS = 2 WHERE CHAIN_CODE = '%s'",
					chainCode);
			run.execute(conn, updateSql);
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			log.error("关闭品牌：" + e.toString());
			throw e;
		}
	}
	
	/**
	 * @param chainCode
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> queryChainDetail(String chainCode) throws ServiceException {
		Connection conn = null;
		try{
			//获取代理店数据库连接
			conn=DBConnector.getInstance().getDealershipConnection();
			Map<String,Object> result = IxDealershipChainOperator.getByChainCode(conn, chainCode);
			return result;
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 关闭作业
	 * @param userId
	 * @param resultIds
	 * @throws Exception 
	 */
	public void closeWork(long userId, JSONArray resultIds) throws Exception {
		Connection conn = null;
		try{
			//获取代理店数据库连接
			conn=DBConnector.getInstance().getDealershipConnection();
			List<Integer> resultIdList = (List<Integer>) JSONArray.toCollection(resultIds);
			IxDealershipResultSelector.updateResultStatusWhenCloseWork(resultIdList, conn);//当关闭作业时更新result相应的状态

			batchInsertDealershipHistory(3,9,resultIdList,userId);//批量插入履历
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	
	/**
	 * 批量生成代理店履历
	 * @param conn
	 * @param operRate 1新增2	删除3	修改
	 * @param newValue 新值
	 * @param resultIdList
	 * @throws Exception 
	 */
	private void batchInsertDealershipHistory(int operRate,int newValue, List<Integer> resultIdList,long userId) throws Exception {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getDealershipConnection();
			
			Date nowTime = new Date(System.currentTimeMillis());
			String u_date = DateUtils.formatDate(nowTime);
			String sql = "insert into IX_DEALERSHIP_HISTORY  t (t.history_id,t.result_id,t.field_name,t.u_record,t.old_value,t.new_value,t.u_date,t.user_id) "
					+ "VALUES (HISTORY_SEQ.NEXTVAL,?,'workflow_status',?,?,?,'"+u_date+"',?)";
			
			Object[][] param = new Object[resultIdList.size()][];

			for (int i = 0; i < resultIdList.size(); i++) {
				Integer resultId = resultIdList.get(i);
				int oldWorkflowValue= getWorkflowStatus(resultId, conn);
				Object[] obj = new Object[] {resultId,operRate,oldWorkflowValue,newValue,userId};
				param[i] = obj;
			}

			if (param.length!=0) {
				run.batch(conn, sql, param);
				log.info("批量插入代理店履历成功！");
			}
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 编辑查询
	 * @param jsonObj
	 * @return
	 * @throws Exception
	 */
	public JSONArray queryByCon(JSONObject jsonObj) throws Exception {
		Connection conn = null;
		try {
			String poiNum = jsonObj.getString("poiNum");
			String name = jsonObj.getString("name");
			String address = jsonObj.getString("address");
			String telephone = jsonObj.getString("telephone");
			String location = jsonObj.getString("location");
			String proCode = jsonObj.getString("proCode");
			Integer resultId = jsonObj.getInt("resultId");
			Integer dbId = jsonObj.getInt("dbId");
			
			conn =  DBConnector.getInstance().getConnectionById(dbId);
			List<IxPoi> poiList = queryPidListByCon(conn,poiNum,name,address,telephone,location,proCode,resultId);
			JSONArray poiArray = IxDealershipResultOperator.componentPoiData(poiList, null);
			return poiArray;
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 根据查询条件过滤pidList
	 * @param conn
	 * @param poiNum
	 * @param name
	 * @param address
	 * @param telephone
	 * @param location
	 * @param proCode
	 * @param resultId
	 * @return
	 * @throws Exception
	 */
	private List<IxPoi> queryPidListByCon(Connection conn,String poiNum, String name, String address, String telephone,
			String location, String proCode, Integer resultId) throws Exception {
		try {
			PreparedStatement pstmt = null;
			ResultSet rs = null;
		    IxPoiSelector poiSelector = new IxPoiSelector(conn);
			StringBuilder sb = new StringBuilder();
			boolean flag = false;
			Geometry point = null;
			if(StringUtils.isNotBlank(poiNum)){//①输入条件包含POI_NUM时，仅根据POI_NUM查询，其它条件不作为查询条件；
				sb.append("SELECT PID FROM IX_POI WHERE POI_NUM = :1 ");
				flag = true;
			}else {
				if(StringUtils.isNotBlank(location)){//②输入条件不包含POI_NUM且包含poi(x,y)显示坐标时，则根据POI显示坐标关联2公里和输入名称、地址、或者电话进行查询(接口参数中坐标为POI显示坐标)；
					String xLocation = location.substring(0,location.indexOf(","));
					String yLocation = location.substring(location.indexOf(",")+1,location.length());
					String wkt = "POINT(" +xLocation + " " + yLocation + ")";
					point = new WKTReader().read(wkt);
					sb.append("SELECT DISTINCT p.pid FROM ix_poi p ");
					assembleQueryPidListCon(sb, name, address, telephone);//针对高级查询组装条件
					point = point.buffer(GeometryUtils.convert2Degree(2000));
				}else{
					if (StringUtils.isNotBlank(proCode)) {//③输入条件不包含POI_NUM且不包含poi(x,y)显示坐标且包含省份时，根据省份或者省份确定范围，根据代理店坐标关联名称、地址或者电话进行查询，此种情况不进行2公里范围检索；
						sb.append("SELECT DISTINCT p.pid FROM ix_poi p,ad_admin ad ");
						assembleQueryPidListCon(sb, name, address, telephone);//针对高级查询组装条件
						sb.append("AND p.region_id = ad.region_id AND ad.admin_id LIKE '"+proCode+"%' ");
						point = IxDealershipResultSelector.getGeometryByResultId(resultId);
					}else{//④输入条件不包含POI_NUM、不包含poi(x,y)显示坐标，不包含省份，根据名称或地址或电话关联代理店坐标2公里范围查询；
						sb.append("SELECT DISTINCT p.pid FROM ix_poi p ");
						assembleQueryPidListCon(sb, name, address, telephone);//针对高级查询组装条件
						point = IxDealershipResultSelector.getGeometryByResultId(resultId);
						String wkt = GeoTranslator.jts2Wkt(point,0.00001, 5);
						point = new WKTReader().read(wkt);
						point = point.buffer(GeometryUtils.convert2Degree(2000));//2公里扩圈
					}
				}
				
			}
			
			pstmt = conn.prepareStatement(sb.toString());
			if(flag){
				pstmt.setString(1, poiNum);
			}else{
			    String wkt = GeoTranslator.jts2Wkt(point,0.00001, 5);
				Clob geom = ConnectionUtil.createClob(conn);			
				geom.setString(1, wkt);
			    pstmt.setClob(1,geom);
			}
			rs = pstmt.executeQuery();

			List<IxPoi> poiList = new ArrayList<>();
			while(rs.next()){
				try {
					IxPoi poi = (IxPoi) poiSelector.loadById(rs.getInt("pid"), false);
					poiList.add(poi);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			return poiList;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	
	/**
	 * 针对高级查询组装条件
	 * @param sb
	 * @param name
	 * @param address
	 * @param telephone
	 */
	public void assembleQueryPidListCon(StringBuilder sb,String name,String address,String telephone){
		if(StringUtils.isNotBlank(name)){
			sb.append(", ix_poi_name pn ");
		}
		if(StringUtils.isNotBlank(address)){
			sb.append(", ix_poi_address pa ");
		}
		if(StringUtils.isNotBlank(telephone)){
			sb.append(", ix_poi_contact pc ");
		}
		sb.append("WHERE sdo_within_distance(p.geometry, sdo_geometry(:1  , 8307), 'mask=anyinteract') = 'TRUE' ");
		if(StringUtils.isNotBlank(name)){
			sb.append("AND p.pid = pn.poi_pid AND pn.name_class = 1 AND pn.name_type  = 2 AND pn.lang_code = 'CHI'"
					+ " AND pn.name LIKE '%"+name+"%' ");
		}
		if(StringUtils.isNotBlank(address)){
			sb.append("AND p.pid = pa.poi_pid AND pa.lang_code = 'CHI' AND pa.fullname LIKE '%"+address+"%' ");
		}
		if(StringUtils.isNotBlank(telephone)){
			sb.append("AND p.pid = pc.poi_pid AND pc.contact LIKE '%"+telephone+"%' ");
		}
	};
	
	
	/**
	 * 补充增量数据
	 * @param request
	 * @param userId
	 * @throws Exception 
	 */
	public Map<String, Object> addChainData(HttpServletRequest request, long userId) throws Exception {
		//excel文件上传到服务器		
		String filePath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.uploadPath)+"/dealership/addChainData";  //服务器部署路径 /data/resources/upload

		log.info("补充增量数据的文件上传到服务器指定位置"+filePath);
		JSONObject returnParam = InputStreamUtils.request2File(request, filePath);
		String localFile = returnParam.getString("filePath");
		
//		String localFile = "F:/1.xlsx";
		log.info("文件已上传至" + localFile);
		//导入补充增量数据excel
		List<Map<String, Object>> addDataMaps = new ArrayList<>();
		try{
			addDataMaps = impAddDataExcel(localFile);
		}catch(Exception e){
			log.error("解析excel表格出现错误。原因为："+e);
			throw new Exception("解析excel表格出错，请检查表格内是否有空行");
		}
		//代理店_补充数据上传增加文件检查（6879）
		addChainDataCheck(addDataMaps);
		
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getDealershipConnection();
			//这里校验并处理数据
			checkImpAddData(conn, addDataMaps);
			log.info("文件内容符合要求，已经成功上传");
			AddChainDataEntity addChainDataEntity = new AddChainDataEntity();
			String chainCode = null;
			int resultId = 0;
			Map<String, Object> resultMap = new HashMap<>();
			List<Integer> resultIdList = new ArrayList<>();
			List<String> chainCodeList = new ArrayList<>();
			EditIxDealershipResult editIxDealershipResult = new EditIxDealershipResult();
			boolean flag = false;
			for(Map<String, Object> map : addDataMaps){
				chainCode = map.get("chain").toString();
				
				chainCodeList.add(chainCode);
				if(StringUtils.isBlank(map.get("number").toString())){
					log.info("开始新增无序号的数据");
					editIxDealershipResult.editIxDealershipResult(conn, addChainDataEntity, "insert", map, userId);
					HashSet<Integer> resultIds = queryResultIdsForNoNumber(conn);
					addResultId2ResultIdList(resultIdList, resultIds);
					flag = true;
					continue;
				}
//				resultId = Integer.parseInt(map.get("number").toString());
//				resultIdList.add(resultId);
				String history = map.get("history").toString();
				addChainDataEntity.setResultId(resultId);
				//新增
				if("3".equals(history)){
					editIxDealershipResult.editIxDealershipResult(conn, addChainDataEntity, "insert", map, userId);
					HashSet<Integer> resultIds = queryResultIdsForNoNumber(conn);
					addResultId2ResultIdList(resultIdList, resultIds);
					flag = true;
					log.info("补充增量数据新增完成");
				}
				//上传的resultID在库中不存在，异常
				if(!map.containsKey("dealStatus") && !map.containsKey("workFlowStatus")){
					throw new Exception("resultId:"+resultId+"在数据库中不存在");
				}
				//判断后执行新增或者更新
				if("1".equals(history) || "2".equals(history) || "4".equals(history)){
					if("3".equals(map.get("dealStatus").toString()) && "9".equals(map.get("workFlowStatus").toString())){
						editIxDealershipResult.editIxDealershipResult(conn, addChainDataEntity, "insert", map, userId);
						HashSet<Integer> resultIds = queryResultIdsForNoNumber(conn);
						addResultId2ResultIdList(resultIdList, resultIds);
						flag = true;
						log.info("补充增量数据新增完成");
					}else{
						editIxDealershipResult.editIxDealershipResult(conn ,addChainDataEntity, "update", map, userId);
						log.info("补充增量数据更新完成");
					}
				}
			}
			
			chainCodeList = removeDuplicate(chainCodeList);
			log.info("开始根据chain:"+chainCode+"修改对应的品牌状态");
//			if(chainCodeList.size() > 0){
//				updateStatusByChain(conn, chainCodeList);
//			}
//			updateReulteData(conn, resultIdList);
			resultMap.put("resultIdList", resultIdList);
			resultMap.put("chainCodeList", chainCodeList);
			resultMap.put("flag", flag);
			return resultMap;
		}catch(Exception e){
			DbUtils.rollback(conn);
			throw new ServiceException(e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private void addResultId2ResultIdList(List<Integer> resultIdList,HashSet<Integer> resultIds) {
		Iterator<Integer> it = resultIds.iterator();
		if (it.hasNext()){
			resultIdList.add(it.next());
		}
	}
	
	//代理店_补充数据上传增加文件检查（6879）
	private void addChainDataCheck(List<Map<String, Object>> list) throws Exception {
		MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String, List<String>> dataMap = metadataApi.scPointAdminareaDataMap();
		Map<String, Integer> chainStatusMap = null;
		List<Integer> historyList = Arrays.asList(1,2,3,4);
		List<String> districtList = null;
		List<String> kindCodeList = null;
		Connection metaConn = null;
		Connection dealershipConn = null;
		try{
			metaConn = DBConnector.getInstance().getMetaConnection();
			dealershipConn = DBConnector.getInstance().getDealershipConnection();
			QueryRunner run = new QueryRunner();
			String districtSql = "SELECT DISTRICT FROM SC_POINT_ADMINAREA WHERE REMARK = '1'";
			String kindCodeSql = "SELECT KIND_CODE FROM SC_POINT_POICODE_NEW";
			String chainStatusSql = "SELECT CHAIN_CODE, CHAIN_STATUS FROM IX_DEALERSHIP_CHAIN";
			districtList = run.query(metaConn, districtSql, new ResultSetHandler<List<String>>(){
				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> list = new ArrayList<>();
					while(rs.next()){
						list.add(rs.getString("DISTRICT"));
					}
					return list;
				}
			});
			kindCodeList = run.query(metaConn, kindCodeSql, new ResultSetHandler<List<String>>(){
				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> list = new ArrayList<>();
					while(rs.next()){
						list.add(rs.getString("KIND_CODE"));
					}
					return list;
				}
			});
			chainStatusMap = run.query(dealershipConn, chainStatusSql, new ResultSetHandler<Map<String, Integer>>(){
				@Override
				public Map<String, Integer> handle(ResultSet rs) throws SQLException {
					Map<String, Integer> map = new HashMap<>();
					while(rs.next()){
						map.put(rs.getString("CHAIN_CODE"), rs.getInt("CHAIN_STATUS"));
					}
					return map;
				}
			});
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(metaConn);
			DbUtils.rollbackAndCloseQuietly(dealershipConn);
			log.error(e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(metaConn);
			DbUtils.closeQuietly(dealershipConn);
		}
		
		for (int i = 0; i < list.size(); i++) {
			String province = list.get(i).get("province").toString();
			String city = list.get(i).get("city").toString();
			String project = list.get(i).get("project").toString();
			String kindCode = list.get(i).get("kindCode").toString();
			String chain = list.get(i).get("chain").toString();
			String name = list.get(i).get("name").toString();
			String address = list.get(i).get("address").toString();
			String his = list.get(i).get("history").toString();
			
			if(StringUtils.isEmpty(province) || !dataMap.get("province").contains(province)){
				throw new ServiceException("第" + (i+2) + "行中：省份为空或在SC_POINT_ADMINAREA表PROVINCE中不存在");
			}
			if(!(!StringUtils.isEmpty(city) && (dataMap.get("city").contains(city) || districtList.contains(city)))){
				throw new ServiceException("第" + (i+2) + "行中：城市为空或在SC_POINT_ADMINAREA表PROVINCE和字段REMARK为1的DISTRICT中不存在");
			}
			if(StringUtils.isEmpty(project)){
				throw new ServiceException("第" + (i+2) + "行中：项目为空");
			}
			if(StringUtils.isEmpty(kindCode) || !kindCodeList.contains(kindCode)){
				throw new ServiceException("第" + (i+2) + "行中：代理店分类为空或不在表SC_POINT_POICODE_NEW中对应的KIND_CODE的值域内");
			}
			if(StringUtils.isEmpty(chain) || chainStatusMap.get(chain) != 1){
				throw new ServiceException("代理店品牌为空或代理店品牌表中状态不是作业中");
			}
			if(StringUtils.isEmpty(name) || !com.navinfo.dataservice.commons.util.ExcelReader.h2f(name).equals(name)){
				throw new ServiceException("第" + (i+2) + "行中：厂商提供名称为空或不是全角");
			}
			if(StringUtils.isEmpty(address) || !com.navinfo.dataservice.commons.util.ExcelReader.h2f(address).equals(address)){
				throw new ServiceException("第" + (i+2) + "行中：厂商提供地址为空或不是全角");
			}
			if(StringUtils.isEmpty(his)){
				throw new ServiceException("第" + (i+2) + "行中：变更履历为空");
			} else {
				if(!historyList.contains(Integer.valueOf(his))){
					throw new ServiceException("第" + (i+2) + "行中：变更履历值不在{1,2,3,4}范围内");
				}
			}
		}
	}
	
	/**
	 * 查询无序号数据当前事物插入的resultIds
	 * @param Connection
	 * @throws Exception 
	 * 
	 * */
	public HashSet<Integer> queryResultIdsForNoNumber(Connection conn) throws Exception{
		try {
			QueryRunner run = new QueryRunner();
			String sql = "SELECT RESULT_SEQ.CURRVAL AS ID FROM DUAL";
			ResultSetHandler<HashSet<Integer>> rs = new ResultSetHandler<HashSet<Integer>>() {
				@Override
				public HashSet<Integer> handle(ResultSet rs) throws SQLException {
					HashSet<Integer> hs = new HashSet<Integer>();
					if(rs.next()){
						hs.add(rs.getInt("id"));
					}
					return hs;
				}
			};
			return run.query(conn, sql, rs);
		}catch(Exception e){
			log.error(e);
			throw e;
		}
	}
	
	/**
	 * 去除重复的chain
	 * 
	 * 
	 * */
    public static List<String> removeDuplicate(List list){       
        for(int i = 0; i < list.size() - 1; i ++) {       
            for(int j = list.size() - 1; j > i; j --) {       
                 if (list.get(j).equals(list.get(i))) {       
                    list.remove(j);       
                  }        
              }        
          }        
          return list;       
      }  
	
	/**
	 * @param 增量数据upFile
	 * @return
	 * @throws Exception 
	 */
	private List<Map<String, Object>> impAddDataExcel(String upFile) throws Exception {
		ExcelReader excleReader = new ExcelReader(upFile);
		Map<String,String> excelHeader = new HashMap<String,String>();
		excelHeader.put("序号", "number");
		excelHeader.put("FID", "fid");
		excelHeader.put("省份", "province");
		excelHeader.put("城市", "city");
		excelHeader.put("项目", "project");
		excelHeader.put("代理店分类", "kindCode");
		excelHeader.put("代理店品牌", "chain");
		excelHeader.put("厂商提供名称", "name");
		excelHeader.put("厂商提供简称", "nameShort");
		excelHeader.put("厂商提供地址", "address");
		excelHeader.put("厂商提供电话（销售）", "telSale");
		excelHeader.put("厂商提供电话（维修）", "telService");
		excelHeader.put("厂商提供电话（其他）", "telOther");		
		excelHeader.put("厂商提供邮编", "postCode");
		excelHeader.put("厂商提供英文名称", "nameEng");
		excelHeader.put("厂商提供英文地址", "addressEng");
		
		excelHeader.put("一览表提供时间", "provideOldSourceTime");
		excelHeader.put("一览表确认时间", "confirmOldSourceTime");
		excelHeader.put("四维确认备注", "NAVConfirmRemark");
		excelHeader.put("负责人反馈结果", "feedbackResult");
		excelHeader.put("解决人", "resolvePerson");
		excelHeader.put("解决时间", "resolveTime");
		excelHeader.put("四维差分结果", "NAVResult");
		excelHeader.put("一览表作业状态", "sourceWorkStatus");
		excelHeader.put("变更履历", "history");
		
		List<Map<String, Object>> sources = excleReader.readExcelContent(excelHeader);
		log.info("导入补充增量数据完成" + upFile);
		return sources;
	}
	
	/**
	 * @param 增量数据内容校验工厂
	 * @return
	 * @throws Exception 
	 */
	public void checkImpAddData(Connection conn, List<Map<String, Object>> addDataMaps) throws Exception{
		List<Integer> dealStatusList = Arrays.asList(0,1,2);
		for(Map<String, Object> addDataMap : addDataMaps){
			try{
				String resultId = addDataMap.get("number").toString();
				String history = addDataMap.get("history").toString();
				log.info("上传的增量数据序号即resultID为：" + resultId+"上传的变更履历为：" + history);
				//若补充数据上传文件中“序号”没有值且文件中“变更履历”的值必须为3(新增)，可以上传，否则不可上传文件
				if(StringUtils.isBlank(resultId) && !"3".equals(history)){
					throw new Exception("序号为："+resultId+"变更履历为："+history+",文件不可上传！");
				}
				if(StringUtils.isBlank(resultId)){
					continue;
				}
				Map<String, Object> statusMap = getStatusByResultId(conn, resultId);
				//对应的resultId在result表中没有数据
				if(statusMap == null){
					continue;
				}
				String workFlowStatus = null;
				int dealStatus = 0;
				//若补充数据上传文件中“序号”有值，且文件中“变更履历”的值不为3(非新增)，
				//且上传文件中“序号”在RESULT表result_id中存在,且(workflow_status=9 and deal_status<>3),则该文件不可以上传
				if(StringUtils.isNotBlank(resultId) && !"3".equals(history)){
					workFlowStatus = statusMap.get("workFlowStatus").toString();
					dealStatus = (int) statusMap.get("dealStatus");
					if(2 == dealStatus){
						throw new Exception("序号为："+resultId+"，履历为："+history+"dealStatus:"+dealStatus+"的文件不可上传！");
					}
				}
				//若补充数据上传文件中“序号”有值，且文件中“变更履历”的值为2(删除)，
				//且传文件中“序号”在RESULT表result_id中存在，且代理点状态为{0,1,2}，则文件不可以上传
				if(StringUtils.isNotBlank(resultId) && "2".equals(history)){
					workFlowStatus = statusMap.get("workFlowStatus").toString();
					dealStatus = (int) statusMap.get("dealStatus");
//					if("9".equals(workFlowStatus)){
//						throw new Exception("序号为："+resultId+"，履历为："+history+"workFlowStatus:"+workFlowStatus+"的文件不可上传！");
//					}
					if(dealStatusList.contains(dealStatus)){
						throw new Exception("序号为："+resultId+"，履历为："+history+"dealStatus:"+dealStatus+"的文件不可上传！");
					}
				}
				addDataMap.put("workFlowStatus", workFlowStatus);
				addDataMap.put("dealStatus", dealStatus);
			}catch(Exception e){
				log.error(e);
				throw e;
			}
		}
	}
	
	/**
	 * @param 根据resultID获取工艺状态和代理店状态
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> getStatusByResultId(Connection conn, String resultId) throws Exception{
		try {
			QueryRunner run = new QueryRunner();
			String sql = "select t.WORKFLOW_STATUS,t.DEAL_STATUS from IX_DEALERSHIP_RESULT t where t.RESULT_ID ="+resultId;
			ResultSetHandler<Map<String, Object>> rs = new ResultSetHandler<Map<String, Object>>() {
				@Override
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						Map<String, Object> result = new HashMap<>();
						result.put("workFlowStatus", rs.getInt("WORKFLOW_STATUS"));
						result.put("dealStatus", rs.getInt("DEAL_STATUS"));
						return result;
					}
					return null;
				}
			};
			return run.query(conn, sql, rs);
		}catch(Exception e){
			log.error(e);
			throw e;
		}
	}
	
	/**
	 * 更新代理店状态
	 * @param conn
	 * @param chain
	 * @param resultId
	 * 
	 * */
	public void updateStatusByChain(Connection conn, List<String> chains) throws SQLException{
		QueryRunner run = new QueryRunner();
		StringBuffer sb = new StringBuffer();
		for(String chainCode : chains){
			sb.append(chainCode+",");
		}
		String chain = sb.toString();
		chain = chain.substring(0, chain.length() - 1);
		String updateChain = "update IX_DEALERSHIP_CHAIN t set t.chain_status = 1, t.chain_weight = 1 where t.chain_code in ('"+chain+"')";
		log.info("updateChain:"+updateChain);
		run.execute(conn, updateChain);
	}
	
	/**
	 * 更新代理店查分结果
	 * @param conn
	 * @param chain
	 * @param resultId
	 * 
	 * */
	public void updateReulteData(Connection conn, List<Integer> resultId) throws SQLException{
		QueryRunner run = new QueryRunner();
		if(resultId.size() > 0){
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < resultId.size(); i++){
				sb.append(resultId.get(i));
				sb.append(",");
			}
			String resultIds = sb.toString().substring(0, sb.length() -1);
			String updateResult = "update IX_DEALERSHIP_RESULT r set r.deal_src_diff = 2 where r.result_id in("+resultIds+")";
			log.info("updateResult:"+updateResult);
			run.execute(conn, updateResult);
		}
	}
	
	
	/**
	 * 返回poi信息，用于保存编辑时比对，外业是否修改过库中poi信息
	 * 返回属性：官方原始中文名称，中文别名，中文地址，邮编，分类，品牌，等级，特殊电话，维修电话，销售电话，其它电话
	 * 注：每种电话可能有多个，以逗号分隔返回
	 * @param poiNum
	 * @return
	 * @throws Exception
	 */
	public JSONObject loadPoiForConflict(JSONObject jsonIn) throws Exception {
		String poiNum=jsonIn.getString("poiNum");
		int dbId=jsonIn.getInt("dbId");
		Connection conn =null;	
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		JSONObject jsonObj=new JSONObject();

		try {
			conn = DBConnector.getInstance().getConnectionById(dbId);
			StringBuilder sb = new StringBuilder();
			sb.append(" SELECT I.KIND_CODE, I.CHAIN, I.POST_CODE,I.\"LEVEL\", P1.NAME, (SELECT NAME FROM IX_POI_NAME WHERE POI_PID = I.PID ");
			sb.append(" AND NAME_CLASS = 3 AND NAME_TYPE = 1 AND U_RECORD <> 2 AND LANG_CODE IN ('CHI', 'CHT')) SHORT_NAME,A.FULLNAME");
			sb.append(" FROM IX_POI I, IX_POI_NAME P1, IX_POI_ADDRESS A");
			sb.append(" WHERE I.POI_NUM =:1");
			sb.append(" AND I.PID = P1.POI_PID");
			sb.append(" AND P1.U_RECORD <> 2");
			sb.append(" AND P1.NAME_CLASS = 1");
			sb.append(" AND P1.NAME_TYPE = 1");
			sb.append(" AND P1.LANG_CODE IN ('CHI', 'CHT')");
			sb.append(" AND I.PID = A.POI_PID");		
			sb.append(" AND A.U_RECORD <> 2");
			sb.append(" AND A.LANG_CODE IN ('CHI', 'CHT')");
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, poiNum);
		    
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				jsonObj.put("postCode", resultSet.getString("POST_CODE")!=null?resultSet.getString("POST_CODE"):"");
				jsonObj.put("kindCode", resultSet.getString("KIND_CODE")!=null?resultSet.getString("KIND_CODE"):"");
				jsonObj.put("nameShort", resultSet.getString("SHORT_NAME")!=null?resultSet.getString("SHORT_NAME"):"");
				jsonObj.put("address",resultSet.getString("FULLNAME")!=null?resultSet.getString("FULLNAME"):"");
				jsonObj.put("chain", resultSet.getString("CHAIN")!=null?resultSet.getString("CHAIN"):"");
				jsonObj.put("name", resultSet.getString("NAME")!=null?resultSet.getString("NAME"):"");
				jsonObj.put("level", resultSet.getString("LEVEL")!=null?resultSet.getString("LEVEL"):"");
			}
			
			StringBuilder sbTel = new StringBuilder();
			sbTel.append(" SELECT C.CONTACT, C.CONTACT_DEPART, C.CONTACT_TYPE");
			sbTel.append(" FROM IX_POI I, IX_POI_CONTACT C");
			sbTel.append(" WHERE I.POI_NUM =:1");
			sbTel.append(" AND I.PID = C.POI_PID");
			sbTel.append(" AND C.CONTACT_TYPE IN (1,2,3,4) AND C.CONTACT_DEPART IN (0, 16, 8)");
			sbTel.append(" AND C.U_RECORD <> 2");
			
			pstmt = conn.prepareStatement(sbTel.toString());
			pstmt.setString(1, poiNum);
			resultSet = pstmt.executeQuery();
			
			String telOther="";
			String telSale="";
			String telService="";
			String telSpecial="";
			String splitChar=";";
			while(resultSet.next()) {
				if (resultSet.getInt("CONTACT_DEPART")==0&&resultSet.getInt("CONTACT_TYPE")!=3){
					if ("".equals(telOther)){telOther= resultSet.getString("CONTACT");}
					else{telOther+=splitChar+resultSet.getString("CONTACT");}
				}
				if (resultSet.getInt("CONTACT_DEPART")==16){
					if ("".equals(telService)){telService= resultSet.getString("CONTACT");}
					else{telService+=splitChar+resultSet.getString("CONTACT");}
				}
				if (resultSet.getInt("CONTACT_DEPART")==8){
					if ("".equals(telSale)){telSale= resultSet.getString("CONTACT");}
					else{telSale+=splitChar+resultSet.getString("CONTACT");}
				}
				if (resultSet.getInt("CONTACT_TYPE")==3 && resultSet.getInt("CONTACT_DEPART")==0){
					if ("".equals(telSpecial)){telSpecial= resultSet.getString("CONTACT");}
					else{telSpecial+=splitChar+resultSet.getString("CONTACT");}
				}
			}
			jsonObj.put("telOther", telOther);
			jsonObj.put("telSale", telSale);
			jsonObj.put("telService", telService);
			jsonObj.put("telSpecial", telSpecial);
			return jsonObj;
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	public int runDealershipCheck(JSONObject jsonReq,OperationResult opResult) throws Exception{
		log.info("start runDealershipCheck");
		int resultCount=0;
		Connection conn=null;
    	try{
    		 JSONObject dealershipInfo = JSONObject.fromObject(jsonReq.getString("dealershipInfo"));
             int wkfStatus= dealershipInfo.getInt("workflowStatus");
             if (wkfStatus!=3){
            	 return resultCount;
             }
    		JSONObject poiData = JSONObject.fromObject(jsonReq.getString("poiData"));
        	int poiDbId = poiData.getInt("dbId");
        	BasicObj obj=opResult.getAllObjs().get(0);
        	com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi ixPoi = (com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi) obj.getMainrow();
        	int objPid = (int) ixPoi.getPid();
        
    	conn=DBConnector.getInstance().getConnectionById(poiDbId);
		log.info("要检查的数据pid:"+objPid);
		log.info("获取要检查的数据的履历");
		Collection<Long> objPids = new ArrayList<Long>();
		objPids.add(Long.parseLong(String.valueOf(objPid)));
		Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadByRowEditStatus(conn, objPids);
		Set<String> tabNames=getChangeTableSet(logs);
		log.info("加载检查对象");		
		Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, false,
				objPids, false, false);
		log.info("加载将poi对象与履历合并起来对象");
		ObjHisLogParser.parse(objs, logs);
		log.info("执行检查");
		//构造检查参数，执行检查
		OperationResult operationResult=new OperationResult();
		Map<String,Map<Long,BasicObj>> objsMap=new HashMap<String, Map<Long,BasicObj>>();
		objsMap.put(ObjectName.IX_POI, objs);
		operationResult.putAll(objsMap);
	
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setOperationName("DEALERSHIP_SAVE");
		
		// 清理检查结果
		log.info("start 清理检查结果");
		DeepCoreControl deepControl = new DeepCoreControl();
		List<Integer> pidIntList=new ArrayList<Integer>();
		pidIntList.add(objPid);
		deepControl.cleanExByCkRule(conn, pidIntList, checkCommand.getRuleIdList(), ObjectName.IX_POI);
		log.info("end 清理检查结果");
		
		Check check=new Check(conn, operationResult);
		check.operate(checkCommand);
		
		//查询检查结果数量
		NiValExceptionSelector selector = new NiValExceptionSelector(conn);
		JSONArray checkResultsArr = selector.poiCheckResultList(objPid,checkCommand.getRuleIdList());
		resultCount=checkResultsArr.size();
		log.info("查询poi检查结果数量:" +resultCount);
		log.info("end runDealershipCheck");
		return resultCount;	
    	}
    	catch (Exception e) {
    		log.error("执行代理店检查发生错误", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 分析履历，将履历中涉及的变更过的子表集合返回
	 * @param logs
	 * @return [IX_POI_NAME,IX_POI_ADDRESS]
	 */
	private Set<String> getChangeTableSet(Map<Long, List<LogDetail>> logs) {
		Set<String> subtables=new HashSet<String>();
		if(logs==null || logs.size()==0){return subtables;}
		String mainTable="IX_POI";
		for(Long objId:logs.keySet()){
			List<LogDetail> logList = logs.get(objId);
			for(LogDetail logTmp:logList){
				String tableName = logTmp.getTbNm();
				if(!mainTable.equals(tableName)){subtables.add(tableName);}
			}
		}
		return subtables;
	}
}
