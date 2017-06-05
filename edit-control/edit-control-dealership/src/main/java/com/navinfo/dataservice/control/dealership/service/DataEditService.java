package com.navinfo.dataservice.control.dealership.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.edit.upload.EditJson;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.search.IxPoiSearch;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiColumnStatusSelector;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportorCommand;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

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
				"SELECT COUNT(*) FROM IX_DEALERSHIP_RESULT WHERE USERID = %d AND WORKFLOW_STATUS = %d AND DEAL_STAUTS = %d AND CHAIN = %s FOR UPDATE NOWAIT;",
				userId, 3, 1, chainCode);
		int count = run.queryForInt(conn, haveDataSql);

		if (count >= 50)
			return 0;

		String queryListSql = String.format(
				"SELECT RESULT_ID FROM IX_DEALERSHIP_RESULT WHERE USERID = %d AND WORKFLOW_STATUS = %d AND DEAL_STAUTS = %d AND CHAIN = %s AND ROWNUM <= %d FOR UPDATE NOWAIT;",
				0, 3, 1, chainCode, 50 - count);
		List<Integer> resultID = ExecuteQuery(queryListSql, conn);

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
		//待作业，待提交→内页录入作业3；已提交→出品9
		int flowStatus = 3;
		if(dealStatus == 3 || dealStatus == 2) flowStatus = 9;
		
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
	private List<Integer> ExecuteQuery(String sql, Connection conn) throws Exception {
		List<Integer> resultID = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				int value = resultSet.getInt(1);
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
			//man库
			mancon = DBConnector.getInstance().getManConnection();
			//代理店数据库
			con = DBConnector.getInstance().getDealershipConnection();
	
			//品牌表赋值为3
			editChainStatus(chainCode, con);

			List<Integer> resultIdList = getResultId(chainCode, con);
			for(int resultId : resultIdList){
				int regionId = getRegionId(resultId, con);
				int dailyDbId = getDailyDbId(regionId, mancon);
				
				dailycon = DBConnector.getInstance().getConnectionById(dailyDbId);

				int workflow_status = getWorkflowStatus(resultId, con);
				
				if(1 == workflow_status){
					//调用差分一致业务逻辑
					editResultCaseStatusSame(resultId, con);
				}else if(2 == workflow_status){
					//表内批表外
					insideEditOutside(resultId, chainCode, con, dailycon, userId, dailyDbId);
					//清空关联POI
					clearRelevancePoi(resultId, con);
				}else{
					return "chainCode对应的workflow_status为："+ workflow_status;
				}
				//根据RESULT表维护SOURCE表
				resultMaintainSource(resultId, con);
			}
			
			return "success ";
		}catch(Exception e){
			e.printStackTrace();
			DbUtils.rollbackAndClose(con);
			DbUtils.rollbackAndClose(mancon);
			DbUtils.rollbackAndClose(dailycon);
		}finally{
			DbUtils.commitAndClose(con);
			DbUtils.commitAndClose(mancon);
			DbUtils.commitAndClose(dailycon);
		}
		return null;
	}
	
	/**
	 * 获取reginID
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public static int getRegionId(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.region_id from IX_DEALERSHIP_RESULT t where t.RESULT_ID ="+resultId;
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					int regionId = 0;
					if (rs.next()) {
						regionId = rs.getInt("region_id");
					}
					return regionId;
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
	public static int getDailyDbId(int regionId, Connection mancon) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.daily_db_id from REGION t where t.region_id =" + regionId;
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					int dailyDbId = 0;
					if (rs.next()) {
						dailyDbId = rs.getInt("daily_db_id");
					}
					return dailyDbId;
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
	public static void editChainStatus(String chainCode, Connection con) throws Exception{
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
	public static int getWorkflowStatus(int resultId, Connection con) throws Exception{
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
	public static List<Integer> getResultId(String chainCode, Connection con) throws Exception{
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
	public static void editResultCaseStatusSame(int resultId, Connection con) throws Exception{
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
	public static void insideEditOutside(int resultId, String chainCode, Connection con, Connection dailycon, long userId, int dailyDbId) throws Exception{
		//根据chainCode查询对应外业采集POI_ID
		String poiNumber = getResultPoiNumber(resultId, con);
		if(poiNumber == null){
			return;
		}
		//IX_POI数据
		Map<String, Object> resultKindCode = getResultKindCode(poiNumber, dailycon);
		//元数据库中数据
		Map<String, Object> metaKindCode = getMetaKindCode(chainCode);
		String dailyPoiChain = resultKindCode.get("poi_chain").toString();
		String dailytPoiCode = resultKindCode.get("poi_kind_code").toString();
		String MetaPoiChain = metaKindCode.get("poi_chain").toString();
		String MetaPoiCode = metaKindCode.get("poi_kind_code").toString();
		if(dailyPoiChain.equals(MetaPoiChain) && dailytPoiCode.equals(MetaPoiCode)){
			String MetaKindChain = metaKindCode.get("r_kind_chain").toString();
			String MetaKind = metaKindCode.get("r_kind").toString();
			//调用POI分类和品牌赋值方法
			editResultTableBrands(resultId, MetaKindChain, MetaKind, con);
			//调用生成POI履历
			JSONObject json = prepareDeepControlData(resultKindCode, dailyDbId);
			producePOIDRecord(json, dailycon, userId);
			int poiStatus = getPoiStatus(resultId, con);
			if(poiStatus == 0){
				//POI状态修改为已提交3
				String pid = resultKindCode.get("pid").toString();
				updatePoiStatus(pid, dailycon);
			}
			//清空关联POI作业属性
			int matchMethod = getMatchMethodFromResult(resultId, con);
			if(matchMethod == 1){
				clearRelevancePoi(resultId, con);
			}
			
		}
		
	}
	
	/**
	 * 准备调用生成poi履历的数据
	 * @param map
	 * @param result
	 * 
	 * */
	public static JSONObject prepareDeepControlData(Map<String, Object> poiMap, int dailyDbId){
		
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
	public static void producePOIDRecord(JSONObject json, Connection dailycon, long userId) throws Exception{
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
	public static int getMatchMethodFromResult(int resultId, Connection con) throws Exception{
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
	public static void updatePoiStatus(String pid, Connection dailycon) throws Exception{
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
	public static Map<String, Object> getResultKindCode(String poiNumber, Connection dailycon) throws Exception{
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
	public static Map<String, Object> getMetaKindCode(String chainCode) throws SQLException{
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
			return null;
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
	public static void editResultTableBrands(int resultId, String brand, String kindCode, Connection con) throws Exception{
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
	public static int getPoiStatus(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select t.deal_status from IX_DEALERSHIP_RESULT t where t.RESULT_ID ="+resultId;
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					int result  = 0;
					if (rs.next()) {
						result = rs.getInt("deal_status");
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
	 * 获取POI对应number
	 * @param chainCode
	 * @param con
	 * @return poiStatus
	 * @throws Exception 
	 * @author songhe
	 * */
	public static String getResultPoiNumber(int resultId, Connection con) throws Exception{
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
	public static void resultMaintainSource(int resultId, Connection con) throws Exception{
		try{
			//查询对应resultID数据
			int sourceId = getResultTable(resultId, con);
			if(sourceId != -1){
				updateSource(con, resultId, sourceId);
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
	public static int getResultTable(int resultId, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.SOURCE_ID from IX_DEALERSHIP_RESULT t where t.RESULT_ID ="+resultId;
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next() && rs.getInt("SOURCE_ID") != 0) {
						int sourceId = rs.getInt("SOURCE_ID");
						return sourceId;
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
	 * 给source表赋值
	 * @param con
	 * @param resulId
	 * @param sourceId
	 * @throws Exception 
	 * @author songhe
	 * */
	public static void updateSource(Connection con, int resulId, int sourceId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "update IX_DEALERSHIP_SOURCE s set "
					+ "(s.PROVINCE,s.CITY,s.PROJECT,s.KIND_CODE,s.CHAIN,s.NAME,s.NAME_SHORT,s.ADDRESS,s.TEL_SALE, "
					+ "s.TEL_SERVICE,s.TEL_OTHER,s.POST_CODE,s.NAME_ENG,s.ADDRESS_ENG,s.PROVIDE_DATE,"
					+ "s.IS_DELETED,s.FB_SOURCE,s.FB_CONTENT,s.FB_AUDIT_REMARK,s.FB_DATE,s.CFM_POI_NUM,s.CFM_MEMO,s.DEAL_CFM_DATE,s.POI_KIND_CODE,s.POI_CHAIN,"
					+ "s.POI_NAME,s.POI_NAME_SHORT,s.POI_ADDRESS,s.POI_POST_CODE,s.POI_X_DISPLAY,s.POI_Y_DISPLAY,"
					+ "s.POI_X_GUIDE,s.POI_Y_GUIDE,s.GEOMETRY)"
					+ "=(select t.PROVINCE,t.CITY,t.PROJECT,t.KIND_CODE,t.CHAIN,t.NAME,t.NAME_SHORT,t.ADDRESS,t.TEL_SALE, "
					+ "t.TEL_SERVICE,t.TEL_OTHER,t.POST_CODE,t.NAME_ENG,t.ADDRESS_ENG,t.PROVIDE_DATE,"
					+ "t.IS_DELETED,t.FB_SOURCE,t.FB_CONTENT,t.FB_AUDIT_REMARK,t.FB_DATE,t.CFM_POI_NUM,t.CFM_MEMO,t.DEAL_CFM_DATE,t.POI_KIND_CODE,t.POI_CHAIN,t.POI_NAME,"
					+ "t.POI_NAME_SHORT,t.POI_ADDRESS,t.POI_POST_CODE,t.POI_X_DISPLAY,t.POI_Y_DISPLAY,t.POI_X_GUIDE,"
					+ "t.POI_Y_GUIDE,t.GEOMETRY from IX_DEALERSHIP_RESULT t where t.RESULT_ID = "+resulId+")" + "where s.SOURCE_ID = "+sourceId;
			run.execute(con, sql);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 清空关联poi Dao
	 * @param con
	 * @param 
	 * @author songhe
	 * */
	public static void clearRelevancePoi(int resultId, Connection con) throws Exception{
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
	public void clearRelatedPoi(int resultId) throws SQLException{
		Connection con = null;
		try{
			//代理店数据库
			con = DBConnector.getInstance().getDealershipConnection();
			clearRelevancePoi(resultId, con);
		}catch(Exception e){
			e.printStackTrace();
			DbUtils.rollbackAndClose(con);
		}finally{
			DbUtils.commitAndClose(con);
		}
	}
	
	/**
	 * 保存数据
	 * 
	 * @param parameter
	 * @param userId
	 */
	public String saveDataService(JSONObject parameter, long userId) throws Exception {
	
	public static void updateIxDealershipResult(IxDealershipResult bean)throws ServiceException{
		Connection conn = null;
        Connection poiConn = null;
        Connection dealershipConn = null;
        JSONObject result = null;
        String log="保存成功";
        
        List<Integer> pids = new ArrayList<Integer>();
        
		try{
            JSONObject dealershipInfo = JSONObject.fromObject(parameter.getString("dealershipInfo"));
            int wkfStatus= dealershipInfo.getInt("wkfStatus");
            int dealershipDbId= dealershipInfo.getInt("dbId");
            int resultId = dealershipInfo.getInt("resultId");
            String cfmMemo = dealershipInfo.getString("cfmMemo");
            dealershipConn = DBConnector.getInstance().getConnectionById(dealershipDbId);
			
          //审核意见为内业录入
            if(wkfStatus==3){
            	JSONObject poiData = JSONObject.fromObject(parameter.getString("poiData"));
            	int poiDbId = poiData.getInt("dbId");
                int objId = poiData.getInt("objId");
                String poiNum = poiData.getString("poiNum");

                poiConn = DBConnector.getInstance().getConnectionById(poiDbId);
                
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
    			
            }
            
            //审核意见为转外业、转客户
            if(wkfStatus==4||wkfStatus==5){
            	//更新IX_DEALERSHIP_RESULT.cfm_Memo
            	updateResultDealStatus(wkfStatus,resultId,cfmMemo,dealershipConn);
    			//更新IX_DEALERSHIP_RESULT.workflow_status=4|5，且写履历
            	
            }
            //不代理
        	if(wkfStatus==6){
        		//更新IX_DEALERSHIP_RESULT.deal_status＝2及cfm_Memo
    			updateResultDealStatus(wkfStatus,resultId,cfmMemo,dealershipConn);
    			
    			//更新IX_DEALERSHIP_RESULT.workflow_status=6，且写履历
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

}
