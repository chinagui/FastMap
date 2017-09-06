package com.navinfo.dataservice.engine.man.statics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

@Service
public class StaticsService {
	
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private static class SingletonHolder{
		private static final StaticsService INSTANCE =new StaticsService();
	}

	public static StaticsService getInstance(){
		return SingletonHolder.INSTANCE;
	}

	/**
	 * 根据输入的范围和类型，查询范围内的所有grid的相应的统计信息，并返回grid列表和统计信息。
	 * 
	 * @param wkt
	 * @param type
	 *            0POI, 1ROAD
	 * @param stage
	 *            0采集 1日编 2月编
	 * @return 
	 * @throws Exception 
	 * @throws JSONException 
	 */
	/*public List<GridChangeStatInfo> gridChangeStaticQuery(String wkt, int stage, int type, String date)
			throws JSONException, Exception {
		//通过wkt获取gridIdList
		Geometry geo=GeoTranslator.geojson2Jts(Geojson.wkt2Geojson(wkt));
		Set<String> grids= CompGeometryUtil.geo2GridsWithoutBreak(geo);
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");

		return api.getChangeStatByGrids(grids, type, stage, date);
	}*/
	
	/*public List<Map<String,Object>> blockExpectStatQuery(String wkt) throws JSONException, Exception{
		BlockService service = BlockService.getInstance();
		
		JSONObject json = new JSONObject();
		
		json.put("wkt",wkt);
		
		JSONArray status = new JSONArray();
		
		status.add(0);
		
		status.add(1);
		
		json.put("snapshot", 0);
		
		json.put("planningStatus", status);
		
		List<Map<String,Object>> data = service.listByWkt(json);
		
		Set<Integer> blocks = new HashSet<Integer>();
		
		for(Map<String,Object> map : data){
			int blockId = (int) map.get("blockId");
			
			blocks.add(blockId);
		}
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
		
		Map<Integer,Integer> statusMap = api.getExpectStatusByBlocks(blocks);
		
		for(Map<String,Object> map : data){
			map.put("expectStatus", statusMap.get(map.get("blockId")));
		}
		
		return data;
	}*/
	
	/*public HashMap blockExpectStatQuery(int blockId, int stage) throws JSONException, Exception{
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
	
		HashMap data = new HashMap();
		
		List<BlockExpectStatInfo> poiStat = api.getExpectStatByBlock(blockId, stage, 0);
		
		List<BlockExpectStatInfo> roadStat = api.getExpectStatByBlock(blockId, stage, 1);
		
		data.put("poi", poiStat);
		
		data.put("road", roadStat);
		
		return data;
	}
	
	public List<HashMap<String, Object>> cityExpectStatQuery(String wkt) throws JSONException, Exception{
		CityService service = CityService.getInstance();
		
		JSONObject json = new JSONObject();
		
		json.put("wkt",wkt);
		
		JSONArray status = new JSONArray();
		
		status.add(0);
		
		status.add(1);
		
		json.put("planningStatus", status);
		
		List<HashMap<String, Object>> data = service.queryListByWkt(json);
		
		Set<Integer> citys = new HashSet<Integer>();
		
		for(HashMap<String, Object> map : data){
			int blockId = (int) map.get("cityId");
			
			citys.add(blockId);
		}
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
		
		Map<Integer,Integer> statusMap = api.getExpectStatusByCitys(citys);
		
		for(HashMap map : data){
			map.put("expectStatus", statusMap.get(map.get("cityId")));
		}
		
		return data;
	}*/
	
//	public SubtaskStatInfo subtaskStatQuery(int subtaskId) throws JSONException, Exception{
//		
//		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
//		
//		return api.getStatBySubtask(subtaskId);
//
//	}
	/*
	public Map<String,Object> subtaskStatQuery(final int subtaskId) throws JSONException, Exception{
		
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();

			String selectSql = "SELECT FSOS.SUBTASK_ID, FSOS.PERCENT, FSOS.TOTAL_POI, FSOS.FINISHED_POI, FSOS.TOTAL_ROAD, FSOS.FINISHED_ROAD"
						+ " FROM FM_STAT_OVERVIEW_SUBTASK FSOS"
						+ " WHERE FSOS.SUBTASK_ID = " + subtaskId;
			
			return run.query(conn, selectSql, new ResultSetHandler<Map<String,Object>>() {

				@Override
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> poi = new HashMap<String,Object>();
					poi.put("total", 0);
					poi.put("finish", 0);
					poi.put("working", 0);
					
					Map<String,Object> road = new HashMap<String,Object>();
					road.put("total", 0);
					road.put("finish", 0);
					road.put("working", 0);
					
					Map<String,Object> result = new HashMap<String,Object>();
					result.put("subtaskId", subtaskId);
					result.put("percent", 0);
					result.put("poi", poi);
					result.put("road", road);
					if(rs.next()) {
						poi.put("total", rs.getInt("TOTAL_POI"));
						poi.put("finish", rs.getInt("FINISHED_POI"));
						poi.put("working", rs.getInt("TOTAL_POI") - rs.getInt("FINISHED_POI"));
						
						road.put("total", rs.getInt("TOTAL_ROAD"));
						road.put("finish", rs.getInt("FINISHED_ROAD"));
						road.put("working", rs.getInt("TOTAL_ROAD") - rs.getInt("FINISHED_ROAD"));
						
						result.put("percent", rs.getInt("PERCENT"));
						result.put("poi", poi);
						result.put("road", road);	
					}
					return result;
				}
			});
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}*/
	
/*
	public JSONObject querymonthTaskOverView()throws Exception{
		Connection conn = null;
		PreparedStatement stmtGrid = null;
		PreparedStatement stmtOther = null;
		ResultSet rsGrid = null;
		ResultSet rsOther =null;
		try {	
			conn = DBConnector.getInstance().getManConnection();
			JSONObject gridRoadCuStaticsJson= new JSONObject();
			gridRoadCuStaticsJson.put(1, 0);
			gridRoadCuStaticsJson.put(2, 0);
			
			JSONObject gridRoadJinStaticsJson= new JSONObject();
			gridRoadJinStaticsJson.put(1, 0);
			gridRoadJinStaticsJson.put(2, 0);
			
			JSONObject subtaskStaticsJson= new JSONObject();
			subtaskStaticsJson.put(0, 0);
			subtaskStaticsJson.put(1, 0);
			subtaskStaticsJson.put(2, 0);
			
			JSONObject cityStaticsJson= new JSONObject();
			//1 待分配，2 已分配
			cityStaticsJson.put(1, 0);
			cityStaticsJson.put(2, 0);
			
			JSONObject otherStaticsJson= new JSONObject();
			otherStaticsJson.put(6, 0);
			otherStaticsJson.put(7, 0);	
			
			String gridSql = "WITH NOWGRID AS" + " (SELECT C.CITY_ID, ST.TYPE, COUNT(DISTINCT M.GRID_ID) USEGRID"
					+ "    FROM SUBTASK_GRID_MAPPING M, SUBTASK ST, TASK T, CITY C"
					+ "   WHERE M.SUBTASK_ID = ST.SUBTASK_ID" + "     AND ST.STAGE = 2" + "     AND ST.TYPE IN (8, 9)"
					+ "     AND T.TASK_ID = ST.TASK_ID" + "     AND C.CITY_ID = T.CITY_ID" + "     and t.latest=1"
					+ "   GROUP BY C.CITY_ID, ST.TYPE)," + " CITYGRID AS"
					+ " (SELECT CITY_ID, COUNT(DISTINCT GRID_ID) ALLGRID" + "    FROM GRID"
					+ "   WHERE CITY_ID IS NOT NULL" + "   GROUP BY CITY_ID)" + " SELECT NOWGRID.TYPE,"
					+ "       SUM(CITYGRID.ALLGRID - NOWGRID.USEGRID) NOTUSERGRID,"
					+ "       SUM(NOWGRID.USEGRID) USEGRID" + "  FROM NOWGRID, CITYGRID"
					+ " WHERE NOWGRID.CITY_ID = CITYGRID.CITY_ID" + " GROUP BY NOWGRID.TYPE";
			//“月编总览“子任务个数
			String otherSql = "SELECT 'subtaskCount' DESCP,ST.STATUS, COUNT(1) COUNTNUM" + "  FROM SUBTASK ST"
					+ " WHERE ST.STAGE = 2" + " GROUP BY ST.STATUS" + " UNION ALL"
					//“月编概览”city个数：待分配,已分配"
					+ " SELECT 'cityCount' DESCP,1 TYPE, COUNT(DISTINCT T.CITY_ID) CITYNUM" + "  FROM TASK T"
					+ " WHERE T.LATEST = 1" + "   AND NOT EXISTS (SELECT 1" + "          FROM SUBTASK ST"
					+ "         WHERE ST.TASK_ID = T.TASK_ID" + "           AND ST.TYPE = 10)" + " UNION ALL"
					+ " SELECT 'cityCount' DESCP,2 TYPE, COUNT(DISTINCT T.CITY_ID) " + "  FROM TASK T, SUBTASK ST"
					+ " WHERE T.LATEST = 1" + "   AND ST.TASK_ID = T.TASK_ID" + "   AND ST.TYPE = 10" + "   UNION ALL"
					//POI专项概览”,"代理店"子任务个数"
					+ " SELECT 'otherSubtaskCount' DESCP,ST.TYPE, COUNT(1) SUBNUM" + "  FROM SUBTASK ST"
					+ " WHERE ST.TYPE IN (6, 7)" + " GROUP BY ST.TYPE";
			
			try {
				stmtGrid = conn.prepareStatement(gridSql);
				stmtOther = conn.prepareStatement(otherSql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			rsGrid = stmtGrid.executeQuery();
			rsOther = stmtOther.executeQuery();
		
			while (rsGrid.next()) {
				//道路精编grid
				if(rsGrid.getInt("TYPE")==8){
					gridRoadJinStaticsJson.put(1, rsGrid.getInt("NOTUSERGRID"));
					gridRoadJinStaticsJson.put(2, rsGrid.getInt("USEGRID"));
				}
				//道路粗编grid
				if(rsGrid.getInt("TYPE")==9){
					gridRoadCuStaticsJson.put(1, rsGrid.getInt("NOTUSERGRID"));
					gridRoadCuStaticsJson.put(2, rsGrid.getInt("USEGRID"));
				}
			}
			while (rsOther.next()) {
				String descp=rsOther.getString("DESCP");
				if(descp.equals("subtaskCount")){
					subtaskStaticsJson.put(rsOther.getInt("STATUS"), rsOther.getInt("COUNTNUM"));
				}
				if(descp.equals("cityCount")){
					cityStaticsJson.put(rsOther.getInt("STATUS"), rsOther.getInt("COUNTNUM"));
				}
				if(descp.equals("otherSubtaskCount")){
					otherStaticsJson.put(rsOther.getInt("STATUS"), rsOther.getInt("COUNTNUM"));
			}
			}
			
			JSONObject staticsJson= new JSONObject();
			staticsJson.put("gridRoadCuStatics", gridRoadCuStaticsJson);
			staticsJson.put("gridRoadJinStatics", gridRoadJinStaticsJson);
			staticsJson.put("subtaskStatics", subtaskStaticsJson);
			staticsJson.put("cityStatics", cityStaticsJson);
			staticsJson.put("otherStatics", otherStaticsJson);
			return staticsJson;
			
			} catch (Exception e) {
				DbUtils.rollbackAndCloseQuietly(conn);
				log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
				DbUtils.closeQuietly(stmtGrid);
				DbUtils.closeQuietly(stmtOther);
				DbUtils.closeQuietly(rsGrid);
				DbUtils.closeQuietly(rsOther);
				DbUtils.commitAndCloseQuietly(conn);
				
		}
	}
	
	public JSONObject queryCollectOverView(int groupId) throws Exception {
		Connection conn = null;
		PreparedStatement stmtGrid = null;
		PreparedStatement stmtSubtask = null;
		ResultSet rsGrid = null;
		ResultSet rsSubtask =null;
		try {
			conn = DBConnector.getInstance().getManConnection();

			JSONObject subtaskStaticsJson = new JSONObject();
			subtaskStaticsJson.put(0, 0);
			subtaskStaticsJson.put(1, 0);
			subtaskStaticsJson.put(2, 0);

			// 1 待分配，2 已分配
			JSONObject gridRoadCollectStaticsJson = new JSONObject();
			gridRoadCollectStaticsJson.put(1, 0);
			gridRoadCollectStaticsJson.put(2, 0);

			JSONObject gridPoiCollectStaticsJson = new JSONObject();
			gridPoiCollectStaticsJson.put(1, 0);
			gridPoiCollectStaticsJson.put(2, 0);

			JSONObject gridUnityStaticsJson = new JSONObject();
			gridUnityStaticsJson.put(1, 0);
			gridUnityStaticsJson.put(2, 0);

			String subtaskSql = "SELECT ST.STATUS, COUNT(1) COUNTNUM" + " FROM SUBTASK ST,BLOCK_MAN BM"
					+ " WHERE ST.BLOCK_ID=BM.BLOCK_ID AND BM.COLLECT_GROUP_ID=" + groupId + " AND ST.STAGE = 0 "
					+ " GROUP BY ST.STATUS";

			String gridSql = "WITH GROUPGRID AS" + " (SELECT G.BLOCK_ID, COUNT(G.GRID_ID) BLOCKGRID"
					+ " 		    FROM BLOCK_MAN BM, GRID G" + " 		   WHERE BM.COLLECT_GROUP_ID=" + groupId
					+ " 		     AND BM.STATUS IN (1, 2)" + " 		     AND BM.BLOCK_ID = G.BLOCK_ID"
					+ " 		   GROUP BY G.BLOCK_ID)" + " 		ASSIGNGRID AS"
					+ " 		 (SELECT BM.BLOCK_ID, S.TYPE, COUNT(DISTINCT SG.GRID_ID) ASSIGNCOUNT"
					+ " 		    FROM BLOCK_MAN BM, SUBTASK S, SUBTASK_GRID_MAPPING SG"
					+ " 		   WHERE BM.COLLECT_GROUP_ID = " + groupId + " 		     AND BM.STATUS IN (1, 2)"
					+ " 		     AND BM.BLOCK_ID = S.BLOCK_ID" + " 		     AND S.TYPE IN (0, 1, 2) AND S.STAGE=0"
					+ " 		     AND S.SUBTASK_ID = SG.GRID_ID" + " 		   GROUP BY BM.BLOCK_ID, S.TYPE)"
					+ " 		SELECT ASSIGNGRID.TYPE,"
					+ " 		       (SUM(GROUPGRID.BLOCKGRID) - SUM(ASSIGNGRID.ASSIGNCOUNT) WAITASSIGNGRID, "
					+ "	SUM(ASSIGNGRID.ASSIGNCOUNT) AlREADYASSIGNGRID FROM GROUPGRID, ASSIGNGRID "
					+ "	WHERE GROUPGRID.BLOCK_ID = ASSIGNGRID.BLOCK_ID GROUP BY ASSIGNGRID.TYPE";

			
			try {
				stmtGrid = conn.prepareStatement(gridSql);
				stmtSubtask = conn.prepareStatement(subtaskSql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
}
			rsGrid = stmtGrid.executeQuery();
			rsSubtask = stmtSubtask.executeQuery();

			while (rsGrid.next()) {
				// poi采集
				if (rsGrid.getInt("TYPE") == 0) {
					gridPoiCollectStaticsJson.put(1, rsGrid.getInt("WAITASSIGNGRID"));
					gridPoiCollectStaticsJson.put(2, rsGrid.getInt("AlREADYASSIGNGRID"));
				}
				// 道路采集
				if (rsGrid.getInt("TYPE") == 1) {
					gridRoadCollectStaticsJson.put(1, rsGrid.getInt("WAITASSIGNGRID"));
					gridRoadCollectStaticsJson.put(2, rsGrid.getInt("AlREADYASSIGNGRID"));
				}
				// 一体化采集
				if (rsGrid.getInt("TYPE") == 2) {
					gridUnityStaticsJson.put(1, rsGrid.getInt("WAITASSIGNGRID"));
					gridUnityStaticsJson.put(2, rsGrid.getInt("AlREADYASSIGNGRID"));
				}
			}
			while (rsSubtask.next()) {

				subtaskStaticsJson.put(rsSubtask.getInt("STATUS"), rsSubtask.getInt("COUNTNUM"));
			}

			JSONObject staticsJson = new JSONObject();
			staticsJson.put("subtaskStatics", subtaskStaticsJson);
			staticsJson.put("gridPoiStatics", gridPoiCollectStaticsJson);
			staticsJson.put("gridRoadStatics", gridRoadCollectStaticsJson);
			staticsJson.put("gridUnityStatics", gridUnityStaticsJson);
			return staticsJson;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(stmtGrid);
			DbUtils.closeQuietly(stmtSubtask);
			DbUtils.closeQuietly(rsGrid);
			DbUtils.closeQuietly(rsSubtask);
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public JSONObject queryDayEidtOverView(int groupId) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();

			JSONObject subtaskStaticsJson = new JSONObject();
			subtaskStaticsJson.put(0, 0);
			subtaskStaticsJson.put(1, 0);
			subtaskStaticsJson.put(2, 0);

			// 1 待分配，2 已分配
			JSONObject blockUnityStaticsJson = new JSONObject();
			blockUnityStaticsJson.put(1, 0);
			blockUnityStaticsJson.put(2, 0);

			JSONObject gridPoiCollectStaticsJson = new JSONObject();
			gridPoiCollectStaticsJson.put(1, 0);
			gridPoiCollectStaticsJson.put(2, 0);

			JSONObject gridUnityStaticsJson = new JSONObject();
			gridUnityStaticsJson.put(1, 0);
			gridUnityStaticsJson.put(2, 0);

			JSONObject sourcePoiStaticsJson = new JSONObject();
			sourcePoiStaticsJson.put(2, 0);

			String subtaskSql = "SELECT 'subtask' DESCP,ST.STATUS, COUNT(1) COUNTNUM" + " FROM SUBTASK ST,BLOCK_MAN BM"
					+ " WHERE ST.BLOCK_ID=BM.BLOCK_ID AND BM.COLLECT_GROUP_ID=" + groupId + " AND ST.STAGE = 0 "
					+ " GROUP BY ST.STATUS" + " UNION ALL"
					+ " SELECT 'block' DESCP,1 STATUS, COUNT(DISTINCT T.BLOCK_ID) BLOCKNUM " + "   FROM BlOCK_MAN T "
					+ "  WHERE T.LATEST = 1 AND T.STATUS IN (1,2) AND T.DAY_EDIT_GROUP_ID=" + groupId
					+ "    AND NOT EXISTS (SELECT 1 " + "           FROM SUBTASK ST "
					+ "          WHERE ST.BLOCK_ID = T.BLOCK_ID" + "            AND ST.TYPE = 4) " + "  UNION ALL "
					+ "  SELECT 'block' DESCP,2 STATUS, COUNT(DISTINCT T.BLOCK_ID) BLOCKNUM "
					+ "   FROM BlOCK_MAN T, SUBTASK ST " + "  WHERE T.LATEST = 1 AND T.DAY_EDIT_GROUP_ID=" + groupId
					+ "    AND ST.TASK_ID = T.TASK_ID " + "    AND ST.TYPE = 4" + "    UNION ALL "
					+ "  SELECT 'sourcePoi' DESCP,2 STATUS, COUNT(1) SUBNUM " + "   FROM SUBTASK ST "
					+ "  WHERE ST.TYPE=5 AND ST.EXE_GROUP_ID=" + groupId + "  GROUP BY ST.TYPE";

			String gridSql = "WITH GROUPGRID AS" + " (SELECT G.BLOCK_ID, COUNT(G.GRID_ID) BLOCKGRID"
					+ " 		    FROM BLOCK_MAN BM, GRID G" + " 		   WHERE BM.DAY_EDIT_GROUP_ID=" + groupId
					+ " 		     AND BM.STATUS IN (1, 2)" + " 		     AND BM.BLOCK_ID = G.BLOCK_ID"
					+ " 		   GROUP BY G.BLOCK_ID)" + " 		ASSIGNGRID AS"
					+ " 		 (SELECT BM.BLOCK_ID, S.TYPE, COUNT(DISTINCT SG.GRID_ID) ASSIGNCOUNT"
					+ " 		    FROM BLOCK_MAN BM, SUBTASK S, SUBTASK_GRID_MAPPING SG"
					+ " 		   WHERE BM.DAY_EDIT_GROUP_ID = " + groupId + " 		     AND BM.STATUS IN (1, 2)"
					+ " 		     AND BM.BLOCK_ID = S.BLOCK_ID" + " 		     AND S.TYPE IN (0,2) AND S.STAGE=1"
					+ " 		     AND S.SUBTASK_ID = SG.GRID_ID" + " 		   GROUP BY BM.BLOCK_ID, S.TYPE)"
					+ " 		SELECT ASSIGNGRID.TYPE,"
					+ " 		       (SUM(GROUPGRID.BLOCKGRID) - SUM(ASSIGNGRID.ASSIGNCOUNT) WAITASSIGNGRID, "
					+ "	SUM(ASSIGNGRID.ASSIGNCOUNT) AlREADYASSIGNGRID FROM GROUPGRID, ASSIGNGRID "
					+ "	WHERE GROUPGRID.BLOCK_ID = ASSIGNGRID.BLOCK_ID GROUP BY ASSIGNGRID.TYPE";

			PreparedStatement stmtGrid = null;
			PreparedStatement stmtSubtask = null;
			try {
				stmtGrid = conn.prepareStatement(gridSql);
				stmtSubtask = conn.prepareStatement(subtaskSql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ResultSet rsGrid = stmtGrid.executeQuery();
			ResultSet rsSubtask = stmtSubtask.executeQuery();

			while (rsGrid.next()) {
				// poi采集
				if (rsGrid.getInt("TYPE") == 0) {
					gridPoiCollectStaticsJson.put(1, rsGrid.getInt("WAITASSIGNGRID"));
					gridPoiCollectStaticsJson.put(2, rsGrid.getInt("AlREADYASSIGNGRID"));
				}
				// 一体化采集
				if (rsGrid.getInt("TYPE") == 2) {
					gridUnityStaticsJson.put(1, rsGrid.getInt("WAITASSIGNGRID"));
					gridUnityStaticsJson.put(2, rsGrid.getInt("AlREADYASSIGNGRID"));
				}
			}
			while (rsSubtask.next()) {
				if (rsSubtask.getString("DESCP") == "subtask") {
					subtaskStaticsJson.put(rsSubtask.getInt("STATUS"), rsSubtask.getInt("COUNTNUM"));
				}
				if (rsSubtask.getString("DESCP") == "block") {
					blockUnityStaticsJson.put(rsSubtask.getInt("STATUS"), rsSubtask.getInt("COUNTNUM"));
				}
				if (rsSubtask.getString("DESCP") == "sourcePoi") {
					sourcePoiStaticsJson.put(rsSubtask.getInt("STATUS"), rsSubtask.getInt("COUNTNUM"));
				}

			}

			JSONObject staticsJson = new JSONObject();
			staticsJson.put("subtaskStatics", subtaskStaticsJson);
			staticsJson.put("gridPoiStatics", gridPoiCollectStaticsJson);
			staticsJson.put("blockUnityStatics", blockUnityStaticsJson);
			staticsJson.put("gridUnityStatics", gridUnityStaticsJson);
			staticsJson.put("sourcePoiStatics", sourcePoiStaticsJson);
			return staticsJson;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
*/
	/**
	 * @param groupId
	 * @param stage 
	 * @return
	 * @throws ServiceException 
	 */
	/*public Map<String,Object> queryBlockOverViewByGroup(int groupId, int stage) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "";
			
			if(0==stage){
				//采集
				selectSql = "SELECT DISTINCT BM.BLOCK_MAN_ID,"
						+ "listagg(S.SUBTASK_ID, ',') within GROUP(order by BM.BLOCK_MAN_ID) over(partition by BM.BLOCK_MAN_ID) SUBTASK_ID_lIST,"
						+ "listagg(S.STATUS, ',') within GROUP(order by BM.BLOCK_MAN_ID) over(partition by BM.BLOCK_MAN_ID) SUBTASK_STATUS_LIST,"
						+ "FSOB.COLLECT_PROGRESS PROGRESS,"
						+ "FSOB.COLLECT_DIFF_DATE DIFF_DATE"
						+ " FROM BLOCK B, BLOCK_MAN BM, SUBTASK S,FM_STAT_OVERVIEW_BLOCKMAN FSOB"
						+ " WHERE BM.COLLECT_GROUP_ID = " + groupId
						+ " AND BM.LATEST = 1"
						+ " AND BM.STATUS = 1"
						+ " AND BM.BLOCK_ID = B.BLOCK_ID"
						+ " AND B.PLAN_STATUS IN (1, 2)"
						+ " AND S.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID"
						+ " AND FSOB.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID"
						+ " AND S.STAGE = " + stage
						+ " UNION ALL"
						+ " SELECT DISTINCT BM.BLOCK_MAN_ID,"
						+ " '','',FSOB.DAILY_PROGRESS PROGRESS,FSOB.DAILY_DIFF_DATE DIFF_DATE"
						+ " FROM BLOCK B, BLOCK_MAN BM, SUBTASK S, FM_STAT_OVERVIEW_BLOCKMAN FSOB"
						+ " WHERE BM.COLLECT_GROUP_ID = " + groupId
						+ " AND BM.LATEST = 1"
						+ " AND BM.STATUS = 1"
						+ " AND BM.BLOCK_ID = B.BLOCK_ID"
						+ " AND B.PLAN_STATUS IN (1, 2)"
						+ " AND FSOB.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID"
						+ " AND NOT EXISTS (SELECT 1 FROM SUBTASK S WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID AND S.STAGE = " + stage + ")"
						+ " ORDER BY BLOCK_MAN_ID";
			}else if(1==stage){
				//日编
				selectSql = "SELECT DISTINCT BM.BLOCK_MAN_ID,"
						+ "listagg(S.SUBTASK_ID, ',') within GROUP(order by BM.BLOCK_MAN_ID) over(partition by BM.BLOCK_MAN_ID) SUBTASK_ID_lIST,"
						+ "listagg(S.STATUS, ',') within GROUP(order by BM.BLOCK_MAN_ID) over(partition by BM.BLOCK_MAN_ID) SUBTASK_STATUS_LIST,"
						+ "FSOB.DAILY_PROGRESS PROGRESS,"
						+ "FSOB.DAILY_DIFF_DATE DIFF_DATE"
						+ " FROM BLOCK B, BLOCK_MAN BM, SUBTASK S,FM_STAT_OVERVIEW_BLOCKMAN FSOB"
						+ " WHERE BM.DAY_EDIT_GROUP_ID = " + groupId
						+ " AND BM.LATEST = 1"
						+ " AND BM.STATUS = 1"
						+ " AND BM.BLOCK_ID = B.BLOCK_ID"
						+ " AND B.PLAN_STATUS IN (1, 2)"
						+ " AND S.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID"
						+ " AND FSOB.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID"
						+ " AND S.STAGE = " + stage
						+ " UNION ALL"
						+ " SELECT DISTINCT BM.BLOCK_MAN_ID,"
						+ " '','',FSOB.DAILY_PROGRESS PROGRESS,FSOB.DAILY_DIFF_DATE DIFF_DATE"
						+ " FROM BLOCK B, BLOCK_MAN BM, SUBTASK S, FM_STAT_OVERVIEW_BLOCKMAN FSOB"
						+ " WHERE BM.DAY_EDIT_GROUP_ID = " + groupId
						+ " AND BM.LATEST = 1"
						+ " AND BM.STATUS = 1"
						+ " AND BM.BLOCK_ID = B.BLOCK_ID"
						+ " AND B.PLAN_STATUS IN (1, 2)"
						+ " AND FSOB.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID"
						+ " AND NOT EXISTS (SELECT 1 FROM SUBTASK S WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID AND S.STAGE = " + stage + ")"
						+ " ORDER BY BLOCK_MAN_ID";
			}
			System.out.println("selectSql: "+selectSql);
			Map<String,Object> result = StaticsOperation.queryBlockOverViewByGroup(conn,selectSql);
			return result;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/
	
	/**
	 * @param groupId
	 * @param stage 
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String,Object> queryTaskOverViewByGroup(int groupId) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "WITH T AS (SELECT T.TASK_ID,"
					+ "         T.STATUS,"
					+ "         T.GROUP_ID,"
					+ "         CASE T.STATUS"
					+ "           WHEN 0 THEN 4"
					+ "           ELSE CASE SUM(S.STATUS)"
					+ "              WHEN 0 THEN 3"
					+ "              ELSE 2 END"
					+ "         END TASK_STAT,"
					+ "         0 UNASSIGNED,"
					+ "         NVL(F.DIFF_DATE,0) DIFF_DATE,"
					+ "         NVL(F.PROGRESS,1) PROGRESS"
					+ "    FROM TASK T, SUBTASK S, FM_STAT_OVERVIEW_TASK F"
					+ "   WHERE T.STATUS IN (0, 1)"
					+ "     AND T.TASK_ID = S.TASK_ID"
					+ "     AND S.IS_QUALITY=0"
					+ "     AND T.TASK_ID = F.TASK_ID(+)"
					+ "   GROUP BY T.TASK_ID, T.GROUP_ID, T.STATUS, F.DIFF_DATE, F.PROGRESS"
					+ "  UNION ALL"
					//+ "  --未分配"
					+ "  SELECT T.TASK_ID,"
					+ "         T.STATUS,"
					+ "         T.GROUP_ID,"
					+ "         2           TASK_STAT,"
					+ "         1           UNASSIGNED,"
					+ "         NVL(F.DIFF_DATE,0) DIFF_DATE,"
					+ "         NVL(F.PROGRESS,1) PROGRESS"
					+ "    FROM TASK T, FM_STAT_OVERVIEW_TASK F"
					+ "   WHERE T.STATUS = 1"
					+ "     AND NOT EXISTS"
					+ "   (SELECT 1 FROM SUBTASK S WHERE S.TASK_ID = T.TASK_ID AND S.IS_QUALITY=0)"
					+ "     AND T.TASK_ID = F.TASK_ID(+))"
					+ "SELECT * FROM T WHERE T.GROUP_ID = "+groupId;
			log.info("selectSql: "+selectSql);
			QueryRunner run = new QueryRunner();

			ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> result = new HashMap<String,Object>();
					int total = 0;
					int finished = 0;
					int unFinished = 0;
					
					int unAssigned = 0;
					int ongoing = 0;
					
					int ongoingRegular = 0;
					int ongoingUnexpected = 0;
					int finishedRegular = 0;
					int finishedAdvanced = 0;
					int finishedOverdue = 0;
					while (rs.next()) {
						total+=1;
						int taskStat=rs.getInt("TASK_STAT");
						int unassigned=rs.getInt("UNASSIGNED");
						if(2==taskStat){
							unFinished+=1;
							if(1==unassigned){
								unAssigned+=1;
							}else {
								ongoing+=1;
								if(1==rs.getInt("PROGRESS")){ongoingRegular+=1;}
								else if(2==rs.getInt("PROGRESS")){ongoingUnexpected+=1;}
							}
						}else{
							finished+=1;
							if(0 == rs.getInt("DIFF_DATE")){
								//按时完成
								finishedRegular += 1;
							}else if(0 < rs.getInt("DIFF_DATE")){
								//提前完成
								finishedAdvanced += 1;
							}else if(0 > rs.getInt("DIFF_DATE")){
								//逾期完成
								finishedOverdue += 1;
							}	
						}
					}
					result.put("total", total);
					result.put("finished", finished);
					result.put("unFinished", unFinished);
					
					result.put("unAssigned", unAssigned);
					result.put("ongoing", ongoing);
					
					
					
					Map<String,Integer> ongoingInfo = new HashMap<String,Integer>();
					ongoingInfo.put("ongoingRegular", ongoingRegular);
					ongoingInfo.put("ongoingUnexpected", ongoingUnexpected);
					result.put("ongoingInfo", ongoingInfo);
					
					Map<String,Integer> finishedInfo = new HashMap<String,Integer>();
					finishedInfo.put("finishedRegular", finishedRegular);
					finishedInfo.put("finishedAdvanced", finishedAdvanced);
					finishedInfo.put("finishedOverdue", finishedOverdue);
					result.put("finishedInfo", finishedInfo);

					return result;
				}
	
			};

			return run.query(conn, selectSql,rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @Title: queryGroupOverView
	 * @Description: 查询数据库获取统计详情
	 * @param groupId
	 * @param stage
	 * @return
	 * @throws ServiceException  Map<String,Object>
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年10月19日 上午10:57:59 
	 */
	/*public Map<String,Object> queryGroupOverView(int groupId, int stage) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "";
			
			selectSql = " select t.percent percent,t.plan_date planDate,t.diff_date diffDate,"
						+"t.plan_start_date planStartDate,t.plan_end_date planEndDate,t.actual_start_date actualStartDate,"
						+"t.actual_end_date actualEndDate,t.poi_plan_total poiPlanTotal,t.road_plan_total roadPlanTotal  "
						+" from FM_STAT_OVERVIEW_GROUP t where t.group_id = "+groupId
						+" and t.stage = "+stage ;
			Map<String,Object> result = StaticsOperation.queryGroupOverView(conn,selectSql);
			return result;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/
	
	/**
	 * @param taskId
	 * @param type 
	 * @return
	 * @throws ServiceException 
	 */
	/*public Map<String, Object> queryBlockOverViewByTask(int taskId, int type) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			int unplanned = 0;
			//1常规，2多源，3代理店，4情报
			if(1 == type){
				unplanned = StaticsOperation.queryBlockOverViewByTaskUnplanned(conn,taskId);
			}
			
			Map<String,Object> result = new HashMap<String,Object>();
			
			Map<String,Object> resultTotal = StaticsOperation.queryBlockOverViewByTask(conn,taskId);
			Map<String,Object> resultCollect = StaticsOperation.queryBlockOverViewByTaskCollect(conn,taskId);
			Map<String,Object> resultDaily = StaticsOperation.queryBlockOverViewByTaskDaily(conn,taskId);
			
			result.put("total", (int)resultTotal.get("total"));
			
			result.put("unreleased", (int)resultTotal.get("draft"));
			result.put("ongoing", (int)resultTotal.get("ongoing"));
			result.put("finished", (int)resultTotal.get("finished"));
			result.put("closed", (int)resultTotal.get("closed"));
			
			result.put("draft", (int)resultTotal.get("draft"));
			result.put("unplanned", unplanned);
			
			Map<String,Object> finishedInfo = new HashMap<String,Object>();
			finishedInfo.put("finished", (int)resultTotal.get("closed"));
			finishedInfo.put("finishedRegular", (int)resultTotal.get("finishedRegular"));
			finishedInfo.put("finishedAdvanced", (int)resultTotal.get("finishedAdvanced"));
			finishedInfo.put("finishedOverdue", (int)resultTotal.get("finishedOverdue"));
			
			Map<String,Object> collectInfo = new HashMap<String,Object>();
			collectInfo.put("ongoing", (int)resultCollect.get("ongoing"));
			collectInfo.put("finished", (int)resultCollect.get("finished"));
			collectInfo.put("ongoingUnexpected", (int)resultCollect.get("ongoingUnexpected"));
			collectInfo.put("ongoingRegular", (int)resultCollect.get("ongoingRegular"));
			
			Map<String,Object> dailyInfo = new HashMap<String,Object>();
			dailyInfo.put("ongoing", (int)resultDaily.get("ongoing"));
			dailyInfo.put("finished", (int)resultDaily.get("finished"));
			dailyInfo.put("ongoingUnexpected", (int)resultDaily.get("ongoingUnexpected"));
			dailyInfo.put("ongoingRegular", (int)resultDaily.get("ongoingRegular"));
			
			Map<String,Object> overdueInfo = new HashMap<String,Object>();
			overdueInfo.put("collectOverdue", (int)resultTotal.get("finishedOverdueCollect"));
			overdueInfo.put("dailyOverdue", (int)resultTotal.get("finishedOverdueDaily"));
			
			result.put("finishedInfo", finishedInfo);
			result.put("collectInfo", collectInfo);
			result.put("dailyInfo", dailyInfo);
			result.put("overdueInfo", overdueInfo);

			return result;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/
	
	/**
	 * @param taskId
	 * @param type 
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> queryTaskOverviewByProgram(int programId, int type) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			String selectSql ="";
			if(type==1){
				selectSql = "WITH T AS"
						+ " (SELECT 0             TASK_ID,"
						+ "         P.PROGRAM_ID,"
						+ "         B.BLOCK_ID,"
						+ "         B.PLAN_STATUS,"
						+ "         0             TASK_STAT,"
						+ "         0             TYPE,"
						+ "         0             STATUS,"
						+ "         0             DIFF_DATE,"
						+ "         1             PROGRESS"
						+ "    FROM BLOCK B, PROGRAM P"
						+ "   WHERE B.PLAN_STATUS = 0"
						+ "     AND B.CITY_ID = P.CITY_ID"
						+ "  UNION ALL"
						+ "  SELECT T.TASK_ID,"
						+ "         T.PROGRAM_ID,"
						+ "         B.BLOCK_ID,"
						+ "         B.PLAN_STATUS,"
						+ "         2 TASK_STAT,"
						+ "         T.TYPE,"
						+ "         T.STATUS,"
						+ "         NVL(F.DIFF_DATE,0) DIFF_DATE,"
						+ "         NVL(F.PROGRESS,1) PROGRESS"
						+ "    FROM TASK T, BLOCK B, FM_STAT_OVERVIEW_TASK F"
						+ "   WHERE T.BLOCK_ID = B.BLOCK_ID"
						+ "     AND T.TASK_ID = F.TASK_ID(+)"
						+ "     AND NOT EXISTS"
						+ "   (SELECT 1 FROM SUBTASK S WHERE T.TASK_ID = S.TASK_ID AND S.IS_QUALITY=0)"
						+ "  UNION ALL"
						+ "  SELECT T.TASK_ID,"
						+ "         T.PROGRAM_ID,"
						+ "         B.BLOCK_ID,"
						+ "         B.PLAN_STATUS,"
						+ "         CASE T.STATUS"
						+ "           WHEN 0 THEN 4"
						+ "           ELSE CASE SUM(S.STATUS)"
						+ "              WHEN 0 THEN 3"
						+ "              ELSE 2 END"
						+ "         END TASK_STAT,"
						+ "         T.TYPE,"
						+ "         T.STATUS,"
						+ "         NVL(F.DIFF_DATE,0) DIFF_DATE,"
						+ "         NVL(F.PROGRESS,1) PROGRESS"
						+ "    FROM TASK T, BLOCK B, SUBTASK S, FM_STAT_OVERVIEW_TASK F"
						+ "   WHERE T.BLOCK_ID = B.BLOCK_ID"
						+ "     AND T.TASK_ID = F.TASK_ID(+)"
						+ "     AND T.TASK_ID = S.TASK_ID"
						+ "     AND S.IS_QUALITY=0"
						+ "   GROUP BY T.TASK_ID,"
						+ "            T.PROGRAM_ID,"
						+ "            B.BLOCK_ID,"
						+ "            B.PLAN_STATUS,"
						+ "            T.STATUS,"
						+ "            T.TYPE,"
						+ "            T.STATUS,"
						+ "            F.DIFF_DATE,"
						+ "            F.PROGRESS)"
						+ "SELECT * FROM T WHERE T.PROGRAM_ID = "+programId;
			}else{
				selectSql = "WITH T AS"
					+ " (SELECT T.TASK_ID,"
					+ "         T.PROGRAM_ID,"
					+ "         1 PLAN_STATUS,"
					+ "         2 TASK_STAT,"
					+ "         T.TYPE,"
					+ "         T.STATUS,"
					+ "         NVL(F.DIFF_DATE,0) DIFF_DATE,"
					+ "         NVL(F.PROGRESS,1) PROGRESS"
					+ "    FROM TASK T, FM_STAT_OVERVIEW_TASK F"
					+ "   WHERE T.TASK_ID = F.TASK_ID(+)"
					+ "     AND T.BLOCK_ID=0"
					+ "     AND NOT EXISTS"
					+ "   (SELECT 1 FROM SUBTASK S WHERE T.TASK_ID = S.TASK_ID AND S.IS_QUALITY=0)"
					+ "  UNION ALL"
					+ "  SELECT T.TASK_ID,"
					+ "         T.PROGRAM_ID,"
					+ "         1 PLAN_STATUS,"
					+ "         CASE T.STATUS"
					+ "           WHEN 0 THEN 4"
					+ "           ELSE CASE SUM(S.STATUS)"
					+ "              WHEN 0 THEN 3"
					+ "              ELSE 2 END"
					+ "         END TASK_STAT,"
					+ "         T.TYPE,"
					+ "         T.STATUS,"
					+ "         NVL(F.DIFF_DATE,0) DIFF_DATE,"
					+ "         NVL(F.PROGRESS,1) PROGRESS"
					+ "    FROM TASK T, SUBTASK S, FM_STAT_OVERVIEW_TASK F"
					+ "   WHERE T.TASK_ID = F.TASK_ID(+)"
					+ "     AND T.BLOCK_ID=0"
					+ "     AND S.IS_QUALITY=0"
					+ "     AND T.TASK_ID = S.TASK_ID"
					+ "   GROUP BY T.TASK_ID,"
					+ "            T.PROGRAM_ID,"
					+ "            T.STATUS,"
					+ "            T.TYPE,"
					+ "            T.STATUS,"
					+ "            F.DIFF_DATE,"
					+ "            F.PROGRESS)"
					+ "SELECT * FROM T WHERE T.PROGRAM_ID = "+programId;}

			ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					int total=0;//总量
						
					int unPush=0;//未发布	
					int ongoing=0;//作业中
					int unClosed=0;//已完成
					int closed=0;//已关闭
						
					int collect=0;//采集
					int daily=0;//日编
					int monthly=0;//月编
						
					int draft=0;//草稿
					int unplanned=0;//未规划
						
						//作业中block采集进展
					int ongoingCollectTotal=0;
					int ongoingRegularCollect=0;//进展正常
					int ongoingFinishedCollect=0;//采集完成
					int ongoingUnexpectedCollect=0;//进展异常
					
						//作业中block日编信息
					int ongoingDailyTotal=0;
					int ongoingUnexpectedDaily=0;//进展异常
					int ongoingRegularDaily=0;//进展正常
					int ongoingFinishedDaily=0;//日编完成
					
						//作业中block月编信息
					int ongoingMonthlyTotal=0;
					int ongoingUnexpectedMonthly=0;//进展异常
					int ongoingRegularMonthly=0;//进展正常
					int ongoingFinishedMonthly=0;//月编完成
					
						//完成block逾期统计
					int dailyOverdue=0;//日编逾期
					int collectOverdue=0;//采集逾期
					int monthlyOverdue=0;//月编逾期
					
						//完成block进展统计
					int closedOverdue=0;//逾期完成
					int closedRegular=0;//正常完成
					int closedAdvanced=0;//提前完成
					
					while (rs.next()) {
						total+=1;
						int planStatus=rs.getInt("PLAN_STATUS");
						int status=rs.getInt("STATUS");
						int taskStat=rs.getInt("TASK_STAT");
						int type=rs.getInt("TYPE");
						if(0==type){collect+=1;}//采
						else if(1==type){daily+=1;}//日
						else if(2==type||3==type){monthly+=1;}//月
						if(0==planStatus){//未发布
							unPush+=1;
							unplanned+=1;
						}else if(2==status){//草稿
							unPush+=1;
							draft+=1;
						}else if (0==status) {//关闭
							closed+=1;
							if(0==type&&rs.getInt("DIFF_DATE")<0){collectOverdue+=1;}//采diff_date<0
							else if(1==type&&rs.getInt("DIFF_DATE")<0){dailyOverdue+=1;}//日
							else if((2==type||3==type)&&rs.getInt("DIFF_DATE")<0){monthlyOverdue+=1;}//月
							//按时完成 diff_date=0	 提前完成 diff_date>0 逾期完成 diff_date<0
							if(rs.getInt("DIFF_DATE")==0){closedRegular+=1;}
							else if(rs.getInt("DIFF_DATE")>0){closedAdvanced+=1;}
							else if(rs.getInt("DIFF_DATE")<0){closedOverdue+=1;}
						}else if(2==taskStat){//进行中的任务progress:1正常(实际作业小于预期)，2异常(实际作业等于或大于预期),3完成（percent=100）
							ongoing+=1;
						}else if(3==taskStat){unClosed+=1;}//待关闭
						if(!(0==planStatus||2==status)){
							if(0==type){//采
								ongoingCollectTotal+=1;
								if(0==status){ongoingFinishedCollect+=1;}
								else if(1==rs.getInt("PROGRESS")){ongoingRegularCollect+=1;}
								else if(2==rs.getInt("PROGRESS")){ongoingUnexpectedCollect+=1;}
							}
							else if(1==type){//日
								ongoingDailyTotal+=1;
								if(0==status){ongoingFinishedDaily+=1;}
								else if(1==rs.getInt("PROGRESS")){ongoingRegularDaily+=1;}
								else if(2==rs.getInt("PROGRESS")){ongoingUnexpectedDaily+=1;}
							}
							else if(2==type||3==type){//月
								ongoingMonthlyTotal+=1;
								if(0==status){ongoingFinishedMonthly+=1;}
								else if(1==rs.getInt("PROGRESS")){ongoingRegularMonthly+=1;}
								else if(2==rs.getInt("PROGRESS")){ongoingUnexpectedMonthly+=1;}
							}
						}
					}
					Map<String, Object> map=new HashMap<String, Object>();
					map.put("total", total);
					map.put("unPush", unPush);
					map.put("ongoing", ongoing);
					map.put("unClosed", unClosed);
					map.put("closed", closed);
					map.put("collect", collect);
					map.put("daily", daily);
					map.put("monthly", monthly);
					map.put("draft", draft);
					map.put("unplanned", unplanned);
					Map<String, Integer> ongoingCollectInfo=new HashMap<String, Integer>();
					ongoingCollectInfo.put("ongoingRegularCollect", ongoingRegularCollect);
					ongoingCollectInfo.put("ongoingFinishedCollect", ongoingFinishedCollect);
					ongoingCollectInfo.put("ongoingUnexpectedCollect", ongoingUnexpectedCollect);
					ongoingCollectInfo.put("total", ongoingCollectTotal);
					map.put("ongoingCollectInfo", ongoingCollectInfo);
					Map<String, Integer> ongoingDailyInfo=new HashMap<String, Integer>();
					ongoingDailyInfo.put("ongoingRegularDaily", ongoingRegularDaily);
					ongoingDailyInfo.put("ongoingFinishedDaily", ongoingFinishedDaily);
					ongoingDailyInfo.put("ongoingUnexpectedDaily", ongoingUnexpectedDaily);
					ongoingDailyInfo.put("total", ongoingDailyTotal);
					map.put("ongoingDailyInfo", ongoingDailyInfo);
					Map<String, Integer> ongoingMonthlyInfo=new HashMap<String, Integer>();
					ongoingMonthlyInfo.put("ongoingRegularMonthly", ongoingRegularMonthly);
					ongoingMonthlyInfo.put("ongoingFinishedMonthly", ongoingFinishedMonthly);
					ongoingMonthlyInfo.put("ongoingUnexpectedMonthly", ongoingUnexpectedMonthly);
					ongoingMonthlyInfo.put("total", ongoingMonthlyTotal);
					map.put("ongoingMonthlyInfo", ongoingMonthlyInfo);
					
					Map<String, Integer> overdueInfo=new HashMap<String, Integer>();
					overdueInfo.put("dailyOverdue", dailyOverdue);
					overdueInfo.put("collectOverdue", collectOverdue);
					overdueInfo.put("monthlyOverdue", monthlyOverdue);
					map.put("overdueInfo", overdueInfo);
					
					Map<String, Integer> closedInfo=new HashMap<String, Integer>();
					closedInfo.put("closedOverdue", closedOverdue);
					closedInfo.put("closedAdvanced", closedAdvanced);
					closedInfo.put("closedRegular", closedRegular);
					map.put("closedInfo", closedInfo);
					return map;
				}
	
			};
			log.info("taskByProgram sql:"+selectSql);
			return run.query(conn, selectSql,rsHandler);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @param taskId
	 * @param type 
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> queryTaskOverviewByCity(int cityId) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			String selectSql ="SELECT COUNT(1) total FROM BLOCK WHERE CITY_ID = "+cityId;

			ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					int total=0;//总量
					
					if (rs.next()) {
						total=rs.getInt("total");
					}
					Map<String, Object> map=new HashMap<String, Object>();
					map.put("total", total);
					map.put("unPush", total);
					map.put("ongoing", 0);
					map.put("unClosed", 0);
					map.put("closed", 0);
					map.put("collect", 0);
					map.put("daily", 0);
					map.put("monthly", 0);
					map.put("draft", 0);
					map.put("unplanned", total);
					Map<String, Integer> ongoingCollectInfo=new HashMap<String, Integer>();
					ongoingCollectInfo.put("ongoingRegularCollect", 0);
					ongoingCollectInfo.put("ongoingFinishedCollect", 0);
					ongoingCollectInfo.put("ongoingUnexpectedCollect", 0);
					ongoingCollectInfo.put("total", 0);
					map.put("ongoingCollectInfo", ongoingCollectInfo);
					Map<String, Integer> ongoingDailyInfo=new HashMap<String, Integer>();
					ongoingDailyInfo.put("ongoingRegularDaily", 0);
					ongoingDailyInfo.put("ongoingFinishedDaily", 0);
					ongoingDailyInfo.put("ongoingUnexpectedDaily", 0);
					ongoingDailyInfo.put("total", 0);
					map.put("ongoingDailyInfo", ongoingDailyInfo);
					Map<String, Integer> ongoingMonthlyInfo=new HashMap<String, Integer>();
					ongoingMonthlyInfo.put("ongoingRegularMonthly", 0);
					ongoingMonthlyInfo.put("ongoingFinishedMonthly", 0);
					ongoingMonthlyInfo.put("ongoingUnexpectedMonthly", 0);
					ongoingMonthlyInfo.put("total", 0);
					map.put("ongoingMonthlyInfo", ongoingMonthlyInfo);
					
					Map<String, Integer> overdueInfo=new HashMap<String, Integer>();
					overdueInfo.put("dailyOverdue", 0);
					overdueInfo.put("collectOverdue", 0);
					overdueInfo.put("monthlyOverdue", 0);
					map.put("overdueInfo", overdueInfo);
					
					Map<String, Integer> closedInfo=new HashMap<String, Integer>();
					closedInfo.put("closedOverdue", 0);
					closedInfo.put("closedAdvanced", 0);
					closedInfo.put("closedRegular", 0);
					map.put("closedInfo", closedInfo);
					return map;
				}
	
			};
			log.info("taskByCity sql:"+selectSql);
			return run.query(conn, selectSql,rsHandler);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @param blockManId
	 * @param taskId 
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> querySubtaskOverView(int taskId) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "";
			
			if(0 != taskId){
				selectSql = "SELECT S.SUBTASK_ID,S.STAGE,S.TYPE,S.STATUS,FSOS.PERCENT,NVL(FSOS.DIFF_DATE,0) DIFF_DATE,NVL(FSOS.PROGRESS,1) PROGRESS"
						+ " FROM SUBTASK S,FM_STAT_OVERVIEW_SUBTASK FSOS"
						+ " WHERE S.SUBTASK_ID = FSOS.SUBTASK_ID(+)"
						+ " AND S.IS_QUALITY=0 "
						+ " AND S.TASK_ID = " + taskId;
			}
			
			Map<String,Object> result = StaticsOperation.querySubtaskOverView(conn,selectSql);
			return result;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @param type
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> queryProgramOverView(int type) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "";
			if(1==type){
				//常规
				selectSql = "SELECT 0             PROGRAM_ID,"
						+ "       C.CITY_ID,"
						+ "       C.PLAN_STATUS,"
						+ "       0             TASK_STAT,"
						+ "       0                   COLLECT_STAT,"
						+ "       0                   DAILY_STAT,"
						+ "       0                   MONTHLY_STAT,"
						+ "       0 DIFF_DATE,"
						+ "       1             COLLECT_PROGRESS,"
						+ "       1             DAILY_PROGRESS,"
						+ "       1             MONTHLY_PROGRESS"
						+ "  FROM CITY C"
						+ " WHERE C.PLAN_STATUS IN (0, 1)"
						+ " UNION ALL"
						//+ " --进行中 待分配"
						+ " SELECT P.PROGRAM_ID,"
						+ "       C.CITY_ID,"
						+ "       C.PLAN_STATUS,"
						+ "       2                   TASK_STAT,"
						+ "       0                   COLLECT_STAT,"
						+ "       0                   DAILY_STAT,"
						+ "       0                   MONTHLY_STAT,"
						+ "       NVL(F.DIFF_DATE,0) DIFF_DATE,"
						+ "       NVL(F.COLLECT_PROGRESS,1) COLLECT_PROGRESS,"
						+ "       NVL(F.DAILY_PROGRESS,1) DAILY_PROGRESS,"
						+ "       NVL(F.MONTHLY_PROGRESS,1) MONTHLY_PROGRESS"
						+ "  FROM CITY C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F"
						+ " WHERE C.CITY_ID = P.CITY_ID"
						+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
						+ "   AND P.LATEST = 1"
						+ "   AND P.STATUS = 1"
						+ "   AND NOT EXISTS (SELECT 1"
						+ "          FROM TASK T"
						+ "         WHERE T.PROGRAM_ID = P.PROGRAM_ID"
						+ "           AND T.LATEST = 1)"
						+ " UNION ALL"
						//+ "--进行中SUM(TASK_STATUS)!=0 已完成 SUM(TASK_STATUS)=0 已关闭 STATUS=0"
						+ " SELECT P.PROGRAM_ID,"
						+ "       C.CITY_ID,"
						+ "       C.PLAN_STATUS,"
						+ "       CASE P.STATUS"
						+ "         WHEN 0 THEN 4"
						+ "         ELSE CASE SUM(T.STATUS)"
						+ "            WHEN 0 THEN 3"
						+ "            ELSE 2 END"
						+ "         END TASK_STAT,"
						+ "       CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE 1 END)"
						+ "         WHEN 0 THEN 2"
						+ "         ELSE CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE T.STATUS END)"
						+ "            WHEN 0 THEN 3 ELSE 2 END END COLLECT_STAT,"
						+ "       CASE SUM(CASE T.TYPE WHEN 0 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE 1 END)"
						+ "         WHEN 0 THEN 2 "
						+ "         ELSE CASE SUM(CASE T.TYPE WHEN 0 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE T.STATUS END)"
						+ "            WHEN 0 THEN 3 ELSE 2 END"
						+ "       END DAILY_STAT,"
						+ "       CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 0 THEN 0 ELSE 1 END) "
						+ "         WHEN 0 THEN 2"
						+ "         ELSE CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 0 THEN 0 ELSE T.STATUS END)"
						+ "            WHEN 0 THEN 3 ELSE 2 END"
						+ "       END MONTHLY_STAT,"
						+ "       NVL(F.DIFF_DATE,0) DIFF_DATE,"
						+ "       NVL(F.COLLECT_PROGRESS,1) COLLECT_PROGRESS,"
						+ "       NVL(F.DAILY_PROGRESS,1) DAILY_PROGRESS,"
						+ "       NVL(F.MONTHLY_PROGRESS,1) MONTHLY_PROGRESS"
						+ "  FROM CITY C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F，TASK T"
						+ " WHERE C.CITY_ID = P.CITY_ID"
						+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
						+ "   AND P.STATUS in (0,1)"
						+ "   AND P.LATEST = 1"
						+ "   AND T.PROGRAM_ID = P.PROGRAM_ID"
						+ "   AND T.LATEST = 1"
						+ " GROUP BY P.PROGRAM_ID, C.CITY_ID, C.PLAN_STATUS, P.STATUS,F.DIFF_DATE,"
						+ "F.COLLECT_PROGRESS,F.DAILY_PROGRESS,F.MONTHLY_PROGRESS";
			}else if(4==type){
				//快线项目进行中和已关闭细分时，不判断月编任务状态
				//情报
				selectSql = "SELECT 0             PROGRAM_ID,"
						+ "       C.INFOR_ID,"
						+ "       C.PLAN_STATUS,"
						+ "       0             TASK_STAT,"
						+ "       0                   COLLECT_STAT,"
						+ "       0                   DAILY_STAT,"
						+ "       0                   MONTHLY_STAT,"
						+ "       0 DIFF_DATE,"
						+ "       1             COLLECT_PROGRESS,"
						+ "       1             DAILY_PROGRESS,"
						+ "       1             MONTHLY_PROGRESS"
						+ "  FROM INFOR C"
						+ " WHERE C.PLAN_STATUS IN (0, 1)"
						+ " UNION ALL"
						//+ " --进行中 待分配"
						+ " SELECT P.PROGRAM_ID,"
						+ "       C.INFOR_ID,"
						+ "       C.PLAN_STATUS,"
						+ "       2                   TASK_STAT,"
						+ "       0                   COLLECT_STAT,"
						+ "       0                   DAILY_STAT,"
						+ "       0                   MONTHLY_STAT,"
						+ "       NVL(F.DIFF_DATE,0) DIFF_DATE,"
						+ "       NVL(F.COLLECT_PROGRESS,1) COLLECT_PROGRESS,"
						+ "       NVL(F.DAILY_PROGRESS,1) DAILY_PROGRESS,"
						+ "       NVL(F.MONTHLY_PROGRESS,1) MONTHLY_PROGRESS"
						+ "  FROM INFOR C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F"
						+ " WHERE C.INFOR_ID = P.INFOR_ID"
						+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
						+ "   AND P.LATEST = 1"
						+ "   AND P.STATUS = 1"
						+ "   AND NOT EXISTS (SELECT 1"
						+ "          FROM TASK T"
						+ "         WHERE T.PROGRAM_ID = P.PROGRAM_ID"
						+ "           AND T.LATEST = 1)"
						+ " UNION ALL"
						//+ "--进行中SUM(TASK_STATUS)!=0 已完成 SUM(TASK_STATUS)=0 已关闭 STATUS=0"
						//TASK_STAT 1未规划2进行中3待关闭4已关闭
						+ " SELECT P.PROGRAM_ID,"
						+ "       C.INFOR_ID,"
						+ "       C.PLAN_STATUS,"
						+ "       CASE P.STATUS"
						+ "         WHEN 0 THEN 4"
						+ "         ELSE CASE SUM(CASE T.TYPE WHEN 2 THEN 0 ELSE T.STATUS END)"
						+ "            WHEN 0 THEN 3"
						+ "            ELSE 2 END"
						+ "         END TASK_STAT,"
						+ "       CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE 1 END)"
						+ "         WHEN 0 THEN 2"
						+ "         ELSE CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE T.STATUS END)"
						+ "            WHEN 0 THEN 3 ELSE 2 END END COLLECT_STAT,"
						+ "       CASE SUM(CASE T.TYPE WHEN 0 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE 1 END)"
						+ "         WHEN 0 THEN 2 "
						+ "         ELSE CASE SUM(CASE T.TYPE WHEN 0 THEN 0 WHEN 2 THEN 0 WHEN 3 THEN 0 ELSE T.STATUS END)"
						+ "            WHEN 0 THEN 3 ELSE 2 END"
						+ "       END DAILY_STAT,"
						+ "       CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 0 THEN 0 ELSE 1 END) "
						+ "         WHEN 0 THEN 2"
						+ "         ELSE CASE SUM(CASE T.TYPE WHEN 1 THEN 0 WHEN 0 THEN 0 ELSE T.STATUS END)"
						+ "            WHEN 0 THEN 3 ELSE 2 END"
						+ "       END MONTHLY_STAT,"
						+ "       NVL(F.DIFF_DATE,0) DIFF_DATE,"
						+ "       NVL(F.COLLECT_PROGRESS,1) COLLECT_PROGRESS,"
						+ "       NVL(F.DAILY_PROGRESS,1) DAILY_PROGRESS,"
						+ "       NVL(F.MONTHLY_PROGRESS,1) MONTHLY_PROGRESS"
						+ "  FROM INFOR C, PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F，TASK T"
						+ " WHERE C.INFOR_ID = P.INFOR_ID"
						+ "   AND P.PROGRAM_ID = F.PROGRAM_ID(+)"
						+ "   AND P.STATUS in (1,0)"
						+ "   AND P.LATEST = 1"
						+ "   AND T.PROGRAM_ID = P.PROGRAM_ID"
						+ "   AND T.LATEST = 1"
						//+ "   AND T.TYPE IN (0, 1)"
						+ " GROUP BY P.PROGRAM_ID, C.INFOR_ID, C.PLAN_STATUS, P.STATUS,F.DIFF_DATE,"
						+ "F.COLLECT_PROGRESS,F.DAILY_PROGRESS,F.MONTHLY_PROGRESS";
			}
			log.info("programOverView sql:"+selectSql);
			Map<String,Object> result = StaticsOperation.queryProgramOverView(conn,selectSql);
			return result;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @param taskType
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> queryTaskOverView(int taskType) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "";
			if(1==taskType){
				//常规
				selectSql = "SELECT DISTINCT C.CITY_ID, C.PLAN_STATUS,"
						+ " 0 TASK_ID,"
						+ " 0 STATUS,"
						+ " 0 PERCENT,"
						+ " 0 DIFF_DATE,"
						+ " 1 PROGRESS,"
						+ " 1 COLLECT_PROGRESS,"
						+ " 0 COLLECT_PERCENT,"
						+ " 1 DAILY_PROGRESS,"
						+ " 0 DAILY_PERCENT,"
						+ " 1 MONTHLY_PROGRESS,"
						+ " 0 MONTHLY_PERCENT,"
						+ " '' BLOCK_MAN_ID_LIST,"
						+ " '' BLOCK_MAN_STATUS_LIST"
						+ " FROM CITY C"
						+ " WHERE C.PLAN_STATUS = 0"
						+ " AND C.CITY_ID < 100000"
						+ " UNION ALL"
						+ " SELECT DISTINCT C.CITY_ID"
						+ ", C.PLAN_STATUS"
						+ ", T.TASK_ID, T.STATUS"
						+ ", FSOT.PERCENT, FSOT.DIFF_DATE, FSOT.PROGRESS"
						+ ", FSOT.COLLECT_PROGRESS, FSOT.COLLECT_PERCENT"
						+ ", FSOT.DAILY_PROGRESS, FSOT.DAILY_PERCENT"
						+ ", FSOT.MONTHLY_PROGRESS, FSOT.MONTHLY_PERCENT"
						+ ", listagg(BM.BLOCK_MAN_ID, ',') within GROUP(order by T.TASK_ID) over(partition by T.TASK_ID) BLOCK_MAN_ID_LIST"
						+ ", listagg(BM.STATUS, ',') within GROUP(order by T.TASK_ID) over(partition by T.TASK_ID) BLOCK_MAN_STATUS_LIST"
						+ " FROM CITY C, TASK T, BLOCK_MAN BM, FM_STAT_OVERVIEW_TASK FSOT"
						+ " WHERE C.CITY_ID = T.CITY_ID"
						+ " AND T.TASK_ID = BM.TASK_ID"
						+ " AND T.TASK_TYPE = 1"
						+ " AND T.LATEST = 1"
						+ " AND BM.LATEST = 1"
						+ " AND T.TASK_ID = FSOT.TASK_ID(+)"
						+ " UNION ALL"
						+ " SELECT DISTINCT C.CITY_ID,"
						+ " C.PLAN_STATUS,"
						+ " T.TASK_ID,"
						+ " T.STATUS,"
						+ " FSOT.PERCENT,"
						+ " FSOT.DIFF_DATE,"
						+ " FSOT.PROGRESS,"
						+ " FSOT.COLLECT_PROGRESS,"
						+ " FSOT.COLLECT_PERCENT,"
						+ " FSOT.DAILY_PROGRESS,"
						+ " FSOT.DAILY_PERCENT,"
						+ " FSOT.MONTHLY_PROGRESS,"
						+ " FSOT.MONTHLY_PERCENT,"
						+ " '',"
						+ " ''"
						+ " FROM CITY C, TASK T, FM_STAT_OVERVIEW_TASK FSOT"
						+ " WHERE C.CITY_ID = T.CITY_ID"
						+ " AND T.TASK_TYPE = 1"
						+ " AND T.LATEST = 1"
						+ " AND T.TASK_ID = FSOT.TASK_ID(+)"
						+ " AND NOT EXISTS (SELECT 1 FROM BLOCK_MAN BM WHERE BM.TASK_ID = T.TASK_ID AND BM.LATEST = 1)"
						+ " ORDER BY CITY_ID";
			}else if(4==taskType){
				//情报
				selectSql = "SELECT DISTINCT I.INFOR_ID AS CITY_ID, I.PLAN_STATUS,"
						+ " 0 TASK_ID,"
						+ " 0 STATUS,"
						+ " 0 PERCENT,"
						+ " 0 DIFF_DATE,"
						+ " 1 PROGRESS,"
						+ " 1 COLLECT_PROGRESS,"
						+ " 0 COLLECT_PERCENT,"
						+ " 1 DAILY_PROGRESS,"
						+ " 0 DAILY_PERCENT,"
						+ " 1 MONTHLY_PROGRESS,"
						+ " 0 MONTHLY_PERCENT,"
						+ " '' BLOCK_MAN_ID_LIST,"
						+ " '' BLOCK_MAN_STATUS_LIST"
						+ " FROM INFOR I"
						+ " WHERE I.PLAN_STATUS = 0"
						+ " UNION ALL"
						+ " SELECT DISTINCT I.INFOR_ID AS CITY_ID"
						+ ", I.PLAN_STATUS"
						+ ", T.TASK_ID, T.STATUS"
						+ ", FSOT.PERCENT, FSOT.DIFF_DATE, FSOT.PROGRESS"
						+ ", FSOT.COLLECT_PROGRESS, FSOT.COLLECT_PERCENT"
						+ ", FSOT.DAILY_PROGRESS, FSOT.DAILY_PERCENT"
						+ ", FSOT.MONTHLY_PROGRESS, FSOT.MONTHLY_PERCENT"
						+ ", listagg(BM.BLOCK_MAN_ID, ',') within GROUP(order by T.TASK_ID) over(partition by T.TASK_ID) BLOCK_MAN_ID_LIST"
						+ ", listagg(BM.STATUS, ',') within GROUP(order by T.TASK_ID) over(partition by T.TASK_ID) BLOCK_MAN_STATUS_LIST"
						+ " FROM INFOR I, TASK T, BLOCK_MAN BM, FM_STAT_OVERVIEW_TASK FSOT"
						+ " WHERE I.TASK_ID = T.TASK_ID"
						+ " AND T.TASK_ID = BM.TASK_ID"
						+ " AND T.TASK_TYPE = 4"
						+ " AND T.LATEST = 1"
						+ " AND BM.LATEST = 1"
						+ " AND T.TASK_ID = FSOT.TASK_ID(+)"
						+ " UNION ALL"
						+ " SELECT DISTINCT I.INFOR_ID AS CITY_ID,"
						+ " I.PLAN_STATUS,"
						+ " T.TASK_ID,"
						+ " T.STATUS,"
						+ " FSOT.PERCENT,"
						+ " FSOT.DIFF_DATE,"
						+ " FSOT.PROGRESS,"
						+ " FSOT.COLLECT_PROGRESS,"
						+ " FSOT.COLLECT_PERCENT,"
						+ " FSOT.DAILY_PROGRESS,"
						+ " FSOT.DAILY_PERCENT,"
						+ " FSOT.MONTHLY_PROGRESS,"
						+ " FSOT.MONTHLY_PERCENT,"
						+ " '',"
						+ " ''"
						+ " FROM INFOR I, TASK T, FM_STAT_OVERVIEW_TASK FSOT"
						+ " WHERE I.TASK_ID = T.TASK_ID"
						+ " AND T.TASK_TYPE = 4"
						+ " AND T.LATEST = 1"
						+ " AND T.TASK_ID = FSOT.TASK_ID(+)"
						+ " AND NOT EXISTS (SELECT 1 FROM BLOCK_MAN BM WHERE BM.TASK_ID = T.TASK_ID AND BM.LATEST = 1)"
						+ " ORDER BY CITY_ID";
			}
			Map<String,Object> result = StaticsOperation.queryTaskOverView(conn,selectSql);
			return result;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 各城市生产情况概览
	 * @return
	 * @throws ServiceException 
	 */
	/*public Map<String, Object> queryCityOverview() throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
		//查询数据
		String sql = "SELECT * FROM FM_STAT_OVERVIEW";
		Object[] params = {};
		//处理结果集
		ResultSetHandler<Map<String, Object>> rsh = new ResultSetHandler<Map<String,Object>>() {
			
			@Override
			public Map<String, Object> handle(ResultSet rs) throws SQLException {
				Map<String, Object> map = new HashMap<String, Object>();
				//处理数据
				while(rs.next()){
					map.put("collectPercent", rs.getLong("COLLECT_PERCENT"));
					map.put("collectPlanStartDate", rs.getTimestamp("COLLECT_PLAN_START_DATE"));
					map.put("collectPlanEndDate", rs.getTimestamp("COLLECT_PLAN_END_DATE"));
					map.put("collectPlanDate", rs.getLong("COLLECT_PLAN_DATE"));
					map.put("collectActualStartDate", rs.getTimestamp("COLLECT_ACTUAL_START_DATE"));
					map.put("collectActualEndDate", rs.getTimestamp("COLLECT_ACTUAL_END_DATE"));
					map.put("collectDiffDate", rs.getLong("COLLECT_DIFF_DATE"));
					map.put("dailyPercent", rs.getLong("DAILY_PERCENT"));
					map.put("dailyPlanStartDate", rs.getTimestamp("DAILY_PLAN_START_DATE"));
					map.put("dailyPlanEndDate", rs.getTimestamp("DAILY_PLAN_END_DATE"));
					map.put("dailyPlanDate", rs.getLong("DAILY_PLAN_DATE"));
					map.put("dailyActualStartDate", rs.getTimestamp("DAILY_ACTUAL_START_DATE"));
					map.put("dailyActualEndDate", rs.getTimestamp("DAILY_ACTUAL_END_DATE"));
					map.put("dailyDiffDate", rs.getLong("DAILY_DIFF_DATE"));
					map.put("poiPlanTotal", rs.getLong("POI_PLAN_TOTAL"));
					map.put("roadPlanTotal", rs.getLong("ROAD_PLAN_TOTAL"));
					map.put("statDate", rs.getTimestamp("STAT_DATE"));
					map.put("statTime", rs.getTimestamp("STAT_TIME"));
				}
				return map;
			}
		};
		Map<String, Object> result = queryRunner.query(conn, sql, rsh, params);
		//返回数据
		return result;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/
	
	/**
	 * 查询任务统计表
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> queryTaskStatByTaskId(long taskId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
		//查询数据
		String sql = "SELECT * FROM FM_STAT_OVERVIEW_TASK WHERE TASK_ID = ?";
		Object[] params = {taskId};
		//处理结果集
		ResultSetHandler<Map<String, Object>> rsh = new ResultSetHandler<Map<String,Object>>() {
			
			@Override
			public Map<String, Object> handle(ResultSet rs) throws SQLException {
				Map<String, Object> map = new HashMap<String, Object>();
				//处理数据
				while(rs.next()){
					map.put("taskId", rs.getLong("TASK_ID"));
					map.put("programId", rs.getLong("PROGRAM_ID"));
					map.put("progress", rs.getLong("PROGRESS"));
					map.put("percent", rs.getLong("PERCENT"));
					map.put("status", rs.getLong("STATUS"));
					map.put("planStartDate", rs.getTimestamp("PLAN_START_DATE"));
					map.put("planEndDate", rs.getTimestamp("PLAN_END_DATE"));
					map.put("diffDate", rs.getLong("DIFF_DATE"));
					map.put("poiPlanTotal", rs.getLong("POI_PLAN_TOTAL"));
					map.put("roadPlanTotal", rs.getLong("ROAD_PLAN_TOTAL"));
					map.put("statDate", rs.getTimestamp("STAT_DATE"));
					map.put("statTime", rs.getTimestamp("STAT_TIME"));
					map.put("planDate", rs.getLong("PLAN_DATE"));
					map.put("actualStartDate", rs.getTimestamp("ACTUAL_START_DATE"));
					map.put("actualEndDate", rs.getTimestamp("ACTUAL_END_DATE"));
					map.put("groupId", rs.getLong("GROUP_ID"));
					map.put("type", rs.getLong("TYPE"));
					
				}
				return map;
			}
		};
		Map<String, Object> result = queryRunner.query(conn, sql, rsh, params);
		//返回数据
		return result;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/*public List<Map<String, Object>> getPoiStatusMap(String wkt, int stage) throws Exception {
		//通过geo计算所跨图幅
		Geometry geo = GeometryUtils.getPolygonByWKT(wkt);
		Coordinate[] coords = geo.getCoordinates();
		if(coords.length<4){throw new Exception("wkt参数错误，wkt应为一个长方形geo");}
		// 最小外包矩形
		Geometry mbr = geo.getEnvelope();
		Coordinate[] mbrCoords = mbr.getCoordinates();
		String[] meshs=MeshUtils.rect2Meshes(mbrCoords[0].x, mbrCoords[0].y, mbrCoords[2].x, mbrCoords[2].y);
		//通过图幅，获取wkt所跨大区的dbid
		Connection conn=null;
		List<Integer> dbList=new ArrayList<Integer>();
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
			Clob clobMeshs=ConnectionUtil.createClob(conn);
			clobMeshs.setString(1,StringUtils.join(meshs, ","));
			String getDbSql="SELECT DISTINCT R.DAILY_DB_ID"
					+ "  FROM GRID G, REGION R, TABLE(CLOB_TO_TABLE(?)) B"
					+ " WHERE G.REGION_ID = R.REGION_ID"
					+ "   AND SUBSTR(G.GRID_ID, 0, LENGTH(G.GRID_ID) - 2) = B.COLUMN_VALUE";
			dbList=queryRunner.query(conn, getDbSql, clobMeshs, new ResultSetHandler<List<Integer>>() {

				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> dbIds=new ArrayList<Integer>();
					while (rs.next()) {
						dbIds.add(rs.getInt("DAILY_DB_ID"));						
					}
					return dbIds;
				}
			});			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		//循环进dbId查询poi状态。合并后返回
		List<Map<String,Object>> pois=new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> poiTmp=new ArrayList<Map<String,Object>>();
		for(int dbId:dbList){
			poiTmp=getPoiStatusMapByDbId(dbId,meshs,stage);
			if(poiTmp!=null&& !poiTmp.isEmpty()){
				pois.addAll(poiTmp);
			}
		}
		return pois;
	}*/
/*
	private List<Map<String, Object>> getPoiStatusMapByDbId(int dbId, String[] meshs, int stage) throws Exception {
		Connection conn=null;
		List<Map<String,Object>> pois=new ArrayList<Map<String,Object>>();
		try{
			conn = DBConnector.getInstance().getConnectionById(dbId);
			QueryRunner queryRunner = new QueryRunner();
			Clob clobMeshs=ConnectionUtil.createClob(conn);
			clobMeshs.setString(1,StringUtils.join(meshs, ","));
			//采集  1已采集  0未采集
			String collectSql="SELECT P.GEOMETRY,IS_UPLOAD STATUS"
					+ "  FROM POI_EDIT_STATUS S,"
					+ "       IX_POI P,"
					+ "       TABLE(CLOB_TO_TABLE(?)) B"
					+ " WHERE S.PID = P.PID"
					+ "   AND P.U_RECORD != 2"
					+ "   AND P.MESH_ID = B.COLUMN_VALUE";
			//日编  1已日编 0未日编
			String daySql="SELECT P.GEOMETRY,"
					+ "       CASE S.STATUS"
					+ "         WHEN 3 THEN 1 ELSE 0 END STATUS"
					+ "  FROM POI_EDIT_STATUS S,"
					+ "       IX_POI P,"
					+ "       TABLE(CLOB_TO_TABLE(?)) B"
					+ " WHERE S.PID = P.PID"
					+ "   AND P.U_RECORD != 2"
					+ "   AND P.MESH_ID = B.COLUMN_VALUE";
			String selectSql="";
			if(stage==0){selectSql=collectSql;}
			else{selectSql=daySql;}
			pois=queryRunner.query(conn, selectSql, clobMeshs, new ResultSetHandler<List<Map<String,Object>>>() {

				@Override
				public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String,Object>> pois=new ArrayList<Map<String,Object>>();
					while (rs.next()) {
						Map<String,Object> poi=new HashMap<String, Object>();
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						JSONObject geoJson;
						try {
							geoJson = GeoTranslator.jts2Geojson(GeoTranslator.struct2Jts(struct));
							poi.put("geometry", geoJson);
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						poi.put("status", rs.getInt("STATUS"));
						pois.add(poi);						
					}
					return pois;
				}
			});		
			return pois;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/

	/**
	 * @return 
	 * fm_stat_overview
	 * @throws ServiceException 
	 */
	/*public Map<String, Object> overview() throws ServiceException {
		Connection conn = null;
		Map<String,Object> overView = new HashMap<String,Object>();
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
			StringBuilder sb = new StringBuilder();
 
			sb.append(" SELECT O.COLLECT_PERCENT,");
			sb.append("   O.COLLECT_PLAN_START_DATE,");
			sb.append("   O.COLLECT_PLAN_END_DATE,");
			sb.append("   O.COLLECT_PLAN_DATE,");
			sb.append("   O.COLLECT_ACTUAL_START_DATE,");
			sb.append("   O.COLLECT_ACTUAL_END_DATE,");
			sb.append("   O.COLLECT_DIFF_DATE,");
			sb.append("   O.DAILY_PERCENT,");
			sb.append("   O.DAILY_PLAN_START_DATE,");
			sb.append("   O.DAILY_PLAN_END_DATE,");
			sb.append("   O.DAILY_PLAN_DATE,");
			sb.append("   O.DAILY_ACTUAL_START_DATE,");
			sb.append("   O.DAILY_ACTUAL_END_DATE,");
			sb.append("   O.DAILY_DIFF_DATE,");
			sb.append("   O.MONTHLY_PERCENT,");
			sb.append("   O.MONTHLY_PLAN_START_DATE,");
			sb.append("   O.MONTHLY_PLAN_END_DATE,");
			sb.append("   O.MONTHLY_PLAN_DATE,");
			sb.append("   O.MONTHLY_ACTUAL_START_DATE,");
			sb.append("   O.MONTHLY_ACTUAL_END_DATE,");
			sb.append("   O.MONTHLY_DIFF_DATE,");
			sb.append("   O.POI_PLAN_TOTAL,");
			sb.append("   O.ROAD_PLAN_TOTAL");
			sb.append(" FROM FM_STAT_OVERVIEW O");
			sb.append(" ORDER BY O.STAT_TIME DESC");

			String sql = sb.toString();

			overView = queryRunner.query(conn, sql, new ResultSetHandler<Map<String,Object>>() {

				@Override
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> overView = new HashMap<String,Object>();
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					if (rs.next()) {
						overView.put("collectPercent", rs.getInt("COLLECT_PERCENT"));
						overView.put("collectPlanDate", rs.getInt("COLLECT_PLAN_DATE"));
						overView.put("collectDiffDate", rs.getInt("COLLECT_DIFF_DATE"));
						overView.put("collectActualDate", rs.getInt("COLLECT_PLAN_DATE") - rs.getInt("COLLECT_DIFF_DATE"));
						Timestamp collectPlanStartDate = rs.getTimestamp("COLLECT_PLAN_START_DATE");
						Timestamp collectPlanEndDate = rs.getTimestamp("COLLECT_PLAN_END_DATE");
						Timestamp collectActualStartDate = rs.getTimestamp("COLLECT_ACTUAL_START_DATE");
						Timestamp collectActualEndDate = rs.getTimestamp("COLLECT_ACTUAL_END_DATE");
						if(collectPlanStartDate != null){
							overView.put("collectPlanStartDate", df.format(collectPlanStartDate));
						}else{
							overView.put("collectPlanStartDate", null);
						}
						if(collectPlanEndDate != null){
							overView.put("collectPlanEndDate", df.format(collectPlanEndDate));
						}else{
							overView.put("collectPlanEndDate", null);
						}
						if(collectActualStartDate != null){
							overView.put("collectActualStartDate", df.format(collectActualStartDate));
						}else{
							overView.put("collectActualStartDate", collectPlanStartDate);
						}
						if(collectActualEndDate != null){
							overView.put("collectActualEndDate", df.format(collectActualEndDate));
						}else{
							overView.put("collectActualEndDate", collectPlanEndDate);
						}

						overView.put("dailyPercent", rs.getInt("DAILY_PERCENT"));
						overView.put("dailyPlanDate", rs.getInt("DAILY_PLAN_DATE"));
						overView.put("dailyDiffDate", rs.getInt("DAILY_DIFF_DATE")); 
						overView.put("dailyActualDate", rs.getInt("DAILY_PLAN_DATE") - rs.getInt("DAILY_DIFF_DATE"));
						Timestamp dailyPlanStartDate = rs.getTimestamp("DAILY_PLAN_START_DATE");
						Timestamp dailyPlanEndDate = rs.getTimestamp("DAILY_PLAN_END_DATE");
						Timestamp dailyActualStartDate = rs.getTimestamp("DAILY_ACTUAL_START_DATE");
						Timestamp dailytActualEndDate = rs.getTimestamp("DAILY_ACTUAL_END_DATE");
						if(dailyPlanStartDate != null){
							overView.put("dailyPlanStartDate", df.format(dailyPlanStartDate));
						}else{
							overView.put("dailyPlanStartDate", null);
						}
						if(dailyPlanEndDate != null){
							overView.put("dailyPlanEndDate", df.format(dailyPlanEndDate));
						}else{
							overView.put("dailyPlanEndDate", null);
						}
						if(dailyActualStartDate != null){
							overView.put("dailyActualStartDate", df.format(dailyActualStartDate));
						}else{
							overView.put("dailyActualStartDate", dailyPlanStartDate);
						}
						if(dailytActualEndDate != null){
							overView.put("dailytActualEndDate", df.format(dailytActualEndDate));
						}else{
							overView.put("dailytActualEndDate", dailyPlanEndDate);
						}
						
						overView.put("monthlyPercent", rs.getInt("MONTHLY_PERCENT")); 
						overView.put("monthlyPlanDate", rs.getInt("MONTHLY_PLAN_DATE"));
						overView.put("monthlyDiffDate", rs.getInt("MONTHLY_DIFF_DATE")); 
						overView.put("monthlyActualDate", rs.getInt("MONTHLY_PLAN_DATE") - rs.getInt("MONTHLY_DIFF_DATE"));
						Timestamp monthlyPlanStartDate = rs.getTimestamp("MONTHLY_PLAN_START_DATE");
						Timestamp monthlyPlanEndDate = rs.getTimestamp("MONTHLY_PLAN_END_DATE");
						Timestamp monthlyActualStartDate = rs.getTimestamp("MONTHLY_ACTUAL_START_DATE");
						Timestamp monthlyActualEndDate = rs.getTimestamp("MONTHLY_ACTUAL_END_DATE");
						if(monthlyPlanStartDate != null){
							overView.put("monthlyPlanStartDate", df.format(monthlyPlanStartDate));
						}else{
							overView.put("monthlyPlanStartDate", null);
						}
						if(monthlyPlanEndDate != null){
							overView.put("monthlyPlanEndDate", df.format(monthlyPlanEndDate));
						}else{
							overView.put("monthlyPlanEndDate", null);
						}
						if(monthlyActualStartDate != null){
							overView.put("monthlyActualStartDate", df.format(monthlyActualStartDate));
						}else{
							overView.put("monthlyActualStartDate", monthlyPlanStartDate);
						}
						if(monthlyActualEndDate != null){
							overView.put("monthlyActualEndDate", df.format(monthlyActualEndDate));
						}else{
							overView.put("monthlyActualEndDate", monthlyPlanEndDate);
						}

						overView.put("poiPlanTotal", rs.getInt("POI_PLAN_TOTAL"));
						overView.put("roadPlanTotal", rs.getInt("ROAD_PLAN_TOTAL"));
					}
					else{
						overView.put("collectPercent", 0);
						overView.put("collectPlanDate", 0);
						overView.put("collectDiffDate", 0);
						overView.put("collectActualDate", 0);
						overView.put("collectPlanStartDate", null);
						overView.put("collectPlanEndDate", null);
						overView.put("collectActualStartDate", null);							
						overView.put("collectActualEndDate", null);

						overView.put("dailyPercent", 0);
						overView.put("dailyPlanDate", 0);
						overView.put("dailyDiffDate", 0); 
						overView.put("dailyActualDate", 0);
						overView.put("dailyPlanStartDate", null);
						overView.put("dailyPlanEndDate", null);
						overView.put("dailyActualStartDate", null);							
						overView.put("dailyActualEndDate", null);
						
						overView.put("monthlyPercent", 0);
						overView.put("monthlyPlanDate", 0);
						overView.put("monthlyDiffDate", 0); 
						overView.put("monthlyActualDate", 0);
						overView.put("monthlyPlanStartDate", null);
						overView.put("monthlyPlanEndDate", null);
						overView.put("monthlyActualStartDate", null);							
						overView.put("monthlyActualEndDate", null);
						
						overView.put("poiPlanTotal", 0);
						overView.put("roadPlanTotal", 0);

					}
					return overView;
				}
				
			});		
			return overView;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询概览失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/

	/**
	 * @param groupId
	 * @return
	 * @throws ServiceException 
	 */
	/*public Map<String, Object> groupOverview(int groupId) throws ServiceException {
		Connection conn = null;
		Map<String,Object> overView = new HashMap<String,Object>();
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT G.GROUP_ID,");
			sb.append("       G.PERCENT,");
			sb.append("       G.PLAN_START_DATE,");
			sb.append("       G.PLAN_END_DATE,");
			sb.append("       G.PLAN_DATE,");
			sb.append("       G.DIFF_DATE,");
			sb.append("       G.ACTUAL_START_DATE,");
			sb.append("       G.ACTUAL_END_DATE,");
			sb.append("       G.POI_PLAN_TOTAL,");
			sb.append("       G.ROAD_PLAN_TOTAL");
			sb.append("  FROM FM_STAT_OVERVIEW_GROUP G");
			sb.append(" WHERE G.GROUP_ID = " + groupId);
			sb.append(" ORDER BY G.STAT_TIME DESC");
			
			String sql = sb.toString();

			overView = queryRunner.query(conn, sql, new ResultSetHandler<Map<String,Object>>() {

				@Override
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> overView = new HashMap<String,Object>();
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					if (rs.next()) {
						overView.put("percent", rs.getInt("PERCENT"));
						overView.put("planDate", rs.getInt("PLAN_DATE"));
						overView.put("diffDate", rs.getInt("DIFF_DATE"));
						overView.put("actualDate", rs.getInt("PLAN_DATE") - rs.getInt("DIFF_DATE"));
						Timestamp planStartDate = rs.getTimestamp("PLAN_START_DATE");
						Timestamp planEndDate = rs.getTimestamp("PLAN_END_DATE");
						Timestamp actualStartDate = rs.getTimestamp("ACTUAL_START_DATE");
						Timestamp actualEndDate = rs.getTimestamp("ACTUAL_END_DATE");
						if(planStartDate != null){
							overView.put("planStartDate", df.format(planStartDate));
						}else{
							overView.put("planStartDate", null);
						}
						if(planEndDate != null){
							overView.put("planEndDate", df.format(planEndDate));
						}else{
							overView.put("planEndDate", null);
						}
						if(actualStartDate != null){
							overView.put("actualStartDate", df.format(actualStartDate));
						}else{
							overView.put("actualStartDate", null);
						}
						if(actualEndDate != null){
							overView.put("actualEndDate", df.format(actualEndDate));
						}else{
							overView.put("actualEndDate", null);
						}

						overView.put("poiPlanTotal", rs.getInt("POI_PLAN_TOTAL"));
						overView.put("roadPlanTotal", rs.getInt("ROAD_PLAN_TOTAL"));
					}
					else{
						overView.put("percent", 0);
						overView.put("planDate", 0);
						overView.put("diffDate", 0);
						overView.put("actualDate", 0);
						overView.put("planStartDate", null);
						overView.put("planEndDate", null);
						overView.put("actualStartDate", null);
						overView.put("actualEndDate", null);
						overView.put("poiPlanTotal", 0);
						overView.put("roadPlanTotal", 0);

					}
					return overView;
				}
			});		
			return overView;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询作业组概览失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/

	/**
	 * @param programId
	 * @return
	 * 根据programId获取统计概览
	 * @throws ServiceException 
	 */
	/*public Map<String, Object> programOverViewDetail(int programId) throws ServiceException {
		Connection conn = null;
		Map<String,Object> overView = new HashMap<String,Object>();
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append(" SELECT O.COLLECT_PERCENT,");
			sb.append("   O.COLLECT_PLAN_START_DATE,");
			sb.append("   O.COLLECT_PLAN_END_DATE,");
			sb.append("   O.COLLECT_PLAN_DATE,");
			sb.append("   O.COLLECT_ACTUAL_START_DATE,");
			sb.append("   O.COLLECT_ACTUAL_END_DATE,");
			sb.append("   O.COLLECT_DIFF_DATE,");
			sb.append("   O.DAILY_PERCENT,");
			sb.append("   O.DAILY_PLAN_START_DATE,");
			sb.append("   O.DAILY_PLAN_END_DATE,");
			sb.append("   O.DAILY_PLAN_DATE,");
			sb.append("   O.DAILY_ACTUAL_START_DATE,");
			sb.append("   O.DAILY_ACTUAL_END_DATE,");
			sb.append("   O.DAILY_DIFF_DATE,");
			sb.append("   O.MONTHLY_PERCENT,");
			sb.append("   O.MONTHLY_PLAN_START_DATE,");
			sb.append("   O.MONTHLY_PLAN_END_DATE,");
			sb.append("   O.MONTHLY_PLAN_DATE,");
			sb.append("   O.MONTHLY_ACTUAL_START_DATE,");
			sb.append("   O.MONTHLY_ACTUAL_END_DATE,");
			sb.append("   O.MONTHLY_DIFF_DATE,");
			sb.append("   O.POI_PLAN_TOTAL,");
			sb.append("   O.ROAD_PLAN_TOTAL,");
			sb.append("   O.PERCENT,");
			sb.append("   O.PLAN_START_DATE,");
			sb.append("   O.PLAN_END_DATE,");
			sb.append("   O.PLAN_DATE,");
			sb.append("   O.DIFF_DATE,");
			sb.append("   O.ACTUAL_START_DATE,");
			sb.append("   O.ACTUAL_END_DATE");
			sb.append(" FROM FM_STAT_OVERVIEW O");
			sb.append(" WHERE O.PROGRAM_ID = " + programId);
			sb.append(" ORDER BY O.STAT_TIME DESC");

			String sql = sb.toString();

			overView = queryRunner.query(conn, sql, new ResultSetHandler<Map<String,Object>>() {

				@Override
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> overView = new HashMap<String,Object>();
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					if (rs.next()) {
						overView.put("percent", rs.getInt("PERCENT")); 
						overView.put("planDate", rs.getInt("PLAN_DATE"));
						overView.put("diffDate", rs.getInt("DIFF_DATE")); 
						overView.put("actualDate", rs.getInt("PLAN_DATE") - rs.getInt("DIFF_DATE"));
						Timestamp planStartDate = rs.getTimestamp("PLAN_START_DATE");
						Timestamp planEndDate = rs.getTimestamp("PLAN_END_DATE");
						Timestamp pctualStartDate = rs.getTimestamp("ACTUAL_START_DATE");
						Timestamp pctualEndDate = rs.getTimestamp("ACTUAL_END_DATE");
						if(planStartDate != null){
							overView.put("planStartDate", df.format(planStartDate));
						}else{
							overView.put("planStartDate", null);
						}
						if(planEndDate != null){
							overView.put("planEndDate", df.format(planEndDate));
						}else{
							overView.put("planEndDate", null);
						}
						if(pctualStartDate != null){
							overView.put("pctualStartDate", df.format(pctualStartDate));
						}else{
							overView.put("pctualStartDate", null);
						}
						if(pctualEndDate != null){
							overView.put("pctualEndDate", df.format(pctualEndDate));
						}else{
							overView.put("pctualEndDate", null);
						}
						
						overView.put("collectPercent", rs.getInt("COLLECT_PERCENT"));
						overView.put("collectPlanDate", rs.getInt("COLLECT_PLAN_DATE"));
						overView.put("collectDiffDate", rs.getInt("COLLECT_DIFF_DATE"));
						overView.put("collectActualDate", rs.getInt("COLLECT_PLAN_DATE") - rs.getInt("COLLECT_DIFF_DATE"));
						Timestamp collectPlanStartDate = rs.getTimestamp("COLLECT_PLAN_START_DATE");
						Timestamp collectPlanEndDate = rs.getTimestamp("COLLECT_PLAN_END_DATE");
						Timestamp collectActualStartDate = rs.getTimestamp("COLLECT_ACTUAL_START_DATE");
						Timestamp collectActualEndDate = rs.getTimestamp("COLLECT_ACTUAL_END_DATE");
						if(collectPlanStartDate != null){
							overView.put("collectPlanStartDate", df.format(collectPlanStartDate));
						}else{
							overView.put("collectPlanStartDate", null);
						}
						if(collectPlanEndDate != null){
							overView.put("collectPlanEndDate", df.format(collectPlanEndDate));
						}else{
							overView.put("collectPlanEndDate", null);
						}
						if(collectActualStartDate != null){
							overView.put("collectActualStartDate", df.format(collectActualStartDate));
						}else{
							overView.put("collectActualStartDate", null);
						}
						if(collectActualEndDate != null){
							overView.put("collectActualEndDate", df.format(collectActualEndDate));
						}else{
							overView.put("collectActualEndDate", null);
						}
						
						overView.put("dailyPercent", rs.getInt("DAILY_PERCENT"));
						overView.put("dailyPlanDate", rs.getInt("DAILY_PLAN_DATE"));
						overView.put("dailyDiffDate", rs.getInt("DAILY_DIFF_DATE")); 
						overView.put("dailyActualDate", rs.getInt("DAILY_PLAN_DATE") - rs.getInt("DAILY_DIFF_DATE"));
						Timestamp dailyPlanStartDate = rs.getTimestamp("DAILY_PLAN_START_DATE");
						Timestamp dailyPlanEndDate = rs.getTimestamp("DAILY_PLAN_END_DATE");
						Timestamp dailyActualStartDate = rs.getTimestamp("DAILY_ACTUAL_START_DATE");
						Timestamp dailytActualEndDate = rs.getTimestamp("DAILY_ACTUAL_END_DATE");
						if(dailyPlanStartDate != null){
							overView.put("dailyPlanStartDate", df.format(dailyPlanStartDate));
						}else{
							overView.put("dailyPlanStartDate", null);
						}
						if(dailyPlanEndDate != null){
							overView.put("dailyPlanEndDate", df.format(dailyPlanEndDate));
						}else{
							overView.put("dailyPlanEndDate", null);
						}
						if(dailyActualStartDate != null){
							overView.put("dailyActualStartDate", df.format(dailyActualStartDate));
						}else{
							overView.put("dailyActualStartDate", null);
						}
						if(dailytActualEndDate != null){
							overView.put("dailytActualEndDate", df.format(dailytActualEndDate));
						}else{
							overView.put("dailytActualEndDate", null);
						}
						
						overView.put("monthlyPercent", rs.getInt("MONTHLY_PERCENT")); 
						overView.put("monthlyPlanDate", rs.getInt("MONTHLY_PLAN_DATE"));
						overView.put("monthlyDiffDate", rs.getInt("MONTHLY_DIFF_DATE")); 
						overView.put("monthlyActualDate", rs.getInt("MONTHLY_PLAN_DATE") - rs.getInt("MONTHLY_DIFF_DATE"));
						Timestamp monthlyPlanStartDate = rs.getTimestamp("MONTHLY_PLAN_START_DATE");
						Timestamp monthlyPlanEndDate = rs.getTimestamp("MONTHLY_PLAN_END_DATE");
						Timestamp monthlyActualStartDate = rs.getTimestamp("MONTHLY_ACTUAL_START_DATE");
						Timestamp monthlyActualEndDate = rs.getTimestamp("MONTHLY_ACTUAL_END_DATE");
						if(monthlyPlanStartDate != null){
							overView.put("monthlyPlanStartDate", df.format(monthlyPlanStartDate));
						}else{
							overView.put("monthlyPlanStartDate", null);
						}
						if(monthlyPlanEndDate != null){
							overView.put("monthlyPlanEndDate", df.format(monthlyPlanEndDate));
						}else{
							overView.put("monthlyPlanEndDate", null);
						}
						if(monthlyActualStartDate != null){
							overView.put("monthlyActualStartDate", df.format(monthlyActualStartDate));
						}else{
							overView.put("monthlyActualStartDate", null);
						}
						if(monthlyActualEndDate != null){
							overView.put("monthlyActualEndDate", df.format(monthlyActualEndDate));
						}else{
							overView.put("monthlyActualEndDate", null);
						}

						overView.put("poiPlanTotal", rs.getInt("POI_PLAN_TOTAL"));
						overView.put("roadPlanTotal", rs.getInt("ROAD_PLAN_TOTAL"));
					}
					return overView;
				}
			});		
			return overView;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询项目进展详情失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}*/

	/**
	 * @param taskId
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> taskOverviewDetail(int taskId) throws ServiceException {
		Connection conn = null;
		Map<String,Object> overView = new HashMap<String,Object>();
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT T.TASK_ID,");
			sb.append("       T.TYPE,");
			sb.append("       T.STATUS,");
			sb.append("       T.PLAN_START_DATE,");
			sb.append("       T.PLAN_END_DATE,");
			sb.append("       FT.ACTUAL_START_DATE,");
			sb.append("       FT.ACTUAL_END_DATE,");
			sb.append("       FT.PERCENT,");
			sb.append("       FT.DIFF_DATE,");
			sb.append("       FT.PLAN_DATE,");
			sb.append("       FT.POI_PLAN_TOTAL,");
			sb.append("       FT.ROAD_PLAN_TOTAL");
			sb.append("  FROM FM_STAT_OVERVIEW_TASK FT, TASK T");
			sb.append(" WHERE FT.TASK_ID(+) = T.TASK_ID");
			sb.append("   AND T.TASK_ID = " + taskId);

			String sql = sb.toString();

			overView = queryRunner.query(conn, sql, new ResultSetHandler<Map<String,Object>>() {

				@Override
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> overView = new HashMap<String,Object>();
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					if (rs.next()) {
						overView.put("taskId", rs.getInt("TASK_ID")); 
						overView.put("type", rs.getInt("TYPE")); 
						overView.put("percent", rs.getInt("PERCENT")); 
						int status=rs.getInt("STATUS");
						
						Timestamp planStartDate = rs.getTimestamp("PLAN_START_DATE");
						Timestamp planEndDate = rs.getTimestamp("PLAN_END_DATE");
						Timestamp actualStartDate = rs.getTimestamp("ACTUAL_START_DATE");
						Timestamp actualEndDate = rs.getTimestamp("ACTUAL_END_DATE");
						if(planStartDate != null){
							overView.put("planStartDate", df.format(planStartDate));
						}else{
							overView.put("planStartDate", null);
						}
						if(planEndDate != null){
							overView.put("planEndDate", df.format(planEndDate));
						}else{
							overView.put("planEndDate", null);
						}
						if(actualStartDate != null){
							overView.put("actualStartDate", df.format(actualStartDate));
						}else{
							overView.put("actualStartDate", overView.get("planStartDate"));
						}
						if(actualEndDate != null){
							overView.put("actualEndDate", df.format(actualEndDate));
						}else{
							if(status==0){
								overView.put("actualEndDate",  overView.get("planEndDate"));
							}else{
								overView.put("actualEndDate", null);
							}	
						}
						
						int planDate = rs.getInt("PLAN_DATE");
						int diffDate = rs.getInt("DIFF_DATE");
						//如果计划时间为0，则计算计划时间与距离计划结束时间天数，此种情况一般是还没有任务统计信息
						if((planDate==0)&&(planStartDate != null)&&(planEndDate != null)){
							planDate = daysOfTwo(planEndDate,planStartDate);
							diffDate = daysOfTwo(planEndDate,new Date());;
						}
						overView.put("planDate", planDate);
						overView.put("diffDate", diffDate); 
						overView.put("actualDate", planDate - diffDate);
						
						overView.put("poiPlanTotal", rs.getInt("POI_PLAN_TOTAL"));
						overView.put("roadPlanTotal", rs.getInt("ROAD_PLAN_TOTAL"));

					}
					else{
						overView.put("taskId", 0); 
						overView.put("type", 0); 
						overView.put("percent", 0); 
						overView.put("planStartDate", null);
						overView.put("planEndDate", null);
						overView.put("actualStartDate", null);
						overView.put("actualEndDate", null);
						overView.put("planDate", 0);
						overView.put("diffDate", 0); 
						overView.put("actualDate", 0);
						
						overView.put("poiPlanTotal", 0);
						overView.put("roadPlanTotal", 0);

					}
					return overView;
				}
			});		
			return overView;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询任务进展详情失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @param subtaskId
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> subtaskOverviewDetail(int subtaskId) throws ServiceException {
		Connection conn = null;
		Map<String,Object> overView = new HashMap<String,Object>();
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT ST.SUBTASK_ID,");
			sb.append("       ST.STAGE,");
			sb.append("       ST.TYPE,");
			sb.append("       S.PERCENT,S.TOTAL_POI, S.FINISHED_POI, S.TOTAL_ROAD, S.FINISHED_ROAD,");
			sb.append("       S.DIFF_DATE,");
			sb.append("       ST.STATUS,");
			sb.append("       S.PLAN_DATE,");
			sb.append("       ST.PLAN_START_DATE,");
			sb.append("       ST.PLAN_END_DATE,");
			sb.append("       S.ACTUAL_START_DATE,");
			sb.append("       S.ACTUAL_END_DATE");
			sb.append("  FROM FM_STAT_OVERVIEW_SUBTASK S, SUBTASK ST");
			sb.append(" WHERE S.SUBTASK_ID(+) = ST.SUBTASK_ID");
			sb.append("   AND ST.SUBTASK_ID = " + subtaskId);

			String sql = sb.toString();

			overView = queryRunner.query(conn, sql, new ResultSetHandler<Map<String,Object>>() {

				@Override
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> overView = new HashMap<String,Object>();
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					if (rs.next()) {
						int status=rs.getInt("STATUS");
						overView.put("subtaskId", rs.getInt("SUBTASK_ID")); 
						overView.put("stage", rs.getInt("STAGE")); 
						overView.put("type", rs.getInt("TYPE")); 
						overView.put("percent", rs.getInt("PERCENT")); 
						
						overView.put("totalPoi", rs.getInt("TOTAL_POI")); 
						overView.put("finishedPoi", rs.getInt("FINISHED_POI")); 
						overView.put("totalRoad", rs.getInt("TOTAL_ROAD")); 
						overView.put("finishedRoad", rs.getInt("FINISHED_ROAD"));
						
						Timestamp planStartDate = rs.getTimestamp("PLAN_START_DATE");
						Timestamp planEndDate = rs.getTimestamp("PLAN_END_DATE");
						Timestamp actualStartDate = rs.getTimestamp("ACTUAL_START_DATE");
						Timestamp actualEndDate = rs.getTimestamp("ACTUAL_END_DATE");
						if(planStartDate != null){
							overView.put("planStartDate", df.format(planStartDate));
						}else{
							overView.put("planStartDate", null);
						}
						if(planEndDate != null){
							overView.put("planEndDate", df.format(planEndDate));
						}else{
							overView.put("planEndDate", null);
						}
						if(actualStartDate != null){
							overView.put("actualStartDate", df.format(actualStartDate));
						}else{
							overView.put("actualStartDate", overView.get("planStartDate"));
						}
						if(actualEndDate != null){
							overView.put("actualEndDate", df.format(actualEndDate));
						}else{
							if(status==0){
								overView.put("actualEndDate", overView.get("planEndDate"));
							}else{
								overView.put("actualEndDate", null);
							}							
						}
						
						int planDate = rs.getInt("PLAN_DATE");
						int diffDate = rs.getInt("DIFF_DATE");
						//如果计划时间为0，则计算计划时间与距离计划结束时间天数，此种情况一般是还没有子任务统计信息
						if((planDate==0)&&(planStartDate != null)&&(planEndDate != null)){
							planDate = daysOfTwo(planEndDate,planStartDate);
							diffDate = daysOfTwo(planEndDate,new Date());
						}
						overView.put("planDate", planDate);
						overView.put("diffDate", diffDate); 
						overView.put("actualDate", planDate - diffDate);
						

					}
					else{
						overView.put("subtaskId", 0); 
						overView.put("stage", 0); 
						overView.put("type", 0); 
						overView.put("percent", 0); 
						overView.put("planStartDate", null);
						overView.put("planEndDate", null);
						overView.put("actualStartDate", null);
						overView.put("actualEndDate", null);

						overView.put("planDate", 0);
						overView.put("diffDate", 0); 
						overView.put("actualDate", 0);
					}
					return overView;
				}
			});		
			return overView;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询子任务进展详情失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public static int daysOfTwo(Date fDate, Date oDate) {

	       Calendar aCalendar = Calendar.getInstance();

	       aCalendar.setTime(fDate);
	       int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);

	       aCalendar.setTime(oDate);
	       int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);

	       return day2 - day1;

	    }
	/**
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public List<Map> getDayTaskTipsStatics(int taskId) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			return getDayTaskTipsStatics(conn,taskId);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("getCollectTaskIdsByTaskId失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public List<Map> getDayTaskTipsStatics(Connection conn,int taskId) throws Exception {
		List<Map> result = new ArrayList<Map>();
		Set<Integer> collectTaskIdSet = TaskService.getInstance().getCollectTaskIdsByTaskId(conn,taskId);
		//调用fccApi
		FccApi fccApi = (FccApi) ApplicationContextUtil
				.getBean("fccApi");
		result = fccApi.getCollectTaskTipsStats(collectTaskIdSet);
		
		return result;
	}
	
	/**
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> getTaskProgress(int taskId) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			//从orical中查询task部分统计项
			Map<String, Integer> oricalTaskData = getTaskProgress(conn, taskId);

			Map<String, Object> mongoTaskData = getTaskProgressFromMongo(taskId);
			
			return mergeTaskStaticData(conn, oricalTaskData, mongoTaskData, taskId);
		}catch(Exception e){
			log.error("getTaskProgress异常:" +e.getMessage(), e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("getTaskProgress失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 处理对应任务采集任务的时间
	 * @param Connection
	 * @param Set<Integer> collectTaskIdSet
	 * @throws Exception 
	 * 
	 * */
	public Map<String, Object> convertCollectData(Connection conn, Set<Integer> collectTaskIdSet) throws Exception{
		try{
			if(collectTaskIdSet.size() == 0){
				return new HashMap<>();
			}
			QueryRunner queryRunner = new QueryRunner();
			String sql = "select t.status, t.latest, t.plan_start_date, t.plan_end_date "
					+ "from TASK t where t.task_id in " + collectTaskIdSet.toString().replace("[", "(").replace("]", ")");
			
			log.info("CollectDataSql: " + sql);
			return queryRunner.query(conn, sql, new ResultSetHandler<Map<String, Object>>() {
				@Override
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String, Object> taskData = new HashMap<>();
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					String planStartDate = "";
					String planEndDate = "";
					long collectDiffDate = 0L;
					int status = 0;
					while(rs.next()) {
						planStartDate = (StringUtils.isNotBlank(planStartDate) && planStartDate.compareTo(df.format(rs.getTimestamp("plan_start_date"))) < 0) ? planStartDate : df.format(rs.getTimestamp("plan_start_date"));
						planEndDate = (StringUtils.isNotBlank(planEndDate) && planEndDate.compareTo(df.format(rs.getTimestamp("plan_end_date"))) > 0) ? planEndDate : df.format(rs.getTimestamp("plan_end_date"));
						status = rs.getInt("status");
						if(status != 0){
							//任务为非关闭状态：计划结束时间-当前时间
							collectDiffDate += rs.getTimestamp("plan_end_date").getTime() - System.currentTimeMillis();
						}
						//获取有效的采集任务的状态
						if(rs.getInt("latest") == 1){
							status = rs.getInt("status");
						}
					}

					long days = collectDiffDate / (1000 * 60 * 60 * 24);
					taskData.put("collectPlanStartDate", planStartDate);
					taskData.put("collectPlanEndDate", planEndDate);
					taskData.put("collectDiffDate", days);
					taskData.put("collectTaskStatus", status);
					return taskData;
				}
			});
		}catch(Exception e){
			log.info("处理采集任务对应的采集时间异常:" + e.getMessage(), e);
			throw e;
		}
	}
	
	/**
	 * 从orical中查询对应任务的统计项
	 * @param taskId
	 * @param Connection
	 * @return Map<String, Integer>
	 * @throws Exception 
	 */
	public Map<String, Integer> getTaskProgress(Connection conn, int taskId) throws Exception {
		try{
			QueryRunner queryRunner = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("select t.status, t.type, t.road_plan_total,  ");
			sb.append("       t.poi_plan_total, t.task_id from      ");
			sb.append("       FM_STAT_OVERVIEW_TASK t               ");
			sb.append("       where t.task_id = "+ taskId            );

			String sql = sb.toString();
			log.info("getTaskProgress sql:" + sb.toString());

			return queryRunner.query(conn, sql, new ResultSetHandler<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> handle(ResultSet rs) throws SQLException {
					Map<String, Integer> taskData = new HashMap<>();
					if(rs.next()) {
						taskData.put("status", rs.getInt("status"));
						taskData.put("type", rs.getInt("type"));
						taskData.put("roadPlanTotal", rs.getInt("road_plan_total"));
						taskData.put("poiPlanTotal", rs.getInt("poi_plan_total"));
					}
					return taskData;
				}
			});
		}catch(Exception e){
			log.error("taskId: "+taskId+"查询任务统计失败，原因为:" + e.getMessage(), e);
			throw e;
		}
	}
	
	
	/**
	 * 查询当前小时的mongo中task相应的统计数据
	 * @param int taskId
	 * @return Map<Integer, Map<String,Object>>
	 * @throws ServiceException 
	 */
	public Map<String, Object> getTaskProgressFromMongo(int taskId) throws Exception{
		try {
			StaticsApi api = (StaticsApi) ApplicationContextUtil.getBean("staticsApi");
			return api.getTaskProgressFromMongo(taskId);
		} catch (Exception e) {
			log.error("查询mongo中task相应的统计数据报错" + e.getMessage(), e);
			throw e;
		}
	}
	
	/**
	 * 处理任务统计项的返回值
	 * @throws Exception 
	 * 
	 * */
	public Map<String, Object> mergeTaskStaticData(Connection conn, Map<String, Integer> oricalTaskData, Map<String, Object> mongoTaskData, int taskId) throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		int type = oricalTaskData.get("type");
		resultMap.put("taskId", taskId);
		resultMap.put("status", oricalTaskData.get("status"));
		resultMap.put("type", type);
		resultMap.put("roadPlanTotal", oricalTaskData.get("roadPlanTotal") == null ? 0 : oricalTaskData.get("roadPlanTotal"));
		resultMap.put("poiPlanTotal", oricalTaskData.get("poiPlanTotal") == null ? 0 : oricalTaskData.get("poiPlanTotal"));
		if(type == 0){
			resultMap.put("poiUnfinishNum", mongoTaskData.get("poiUnfinishNum") == null ? 0 : mongoTaskData.get("poiUnfinishNum"));
			resultMap.put("crowdTipsTotal", mongoTaskData.get("crowdTipsTotal") == null ? 0 : mongoTaskData.get("crowdTipsTotal"));
			resultMap.put("inforTipsTotal", mongoTaskData.get("inforTipsTotal") == null ? 0 : mongoTaskData.get("inforTipsTotal"));
			resultMap.put("multisourcePoiTotal", mongoTaskData.get("multisourcePoiTotal") == null ? 0 : mongoTaskData.get("multisourcePoiTotal"));
			resultMap.put("collectTipsUploadNum", mongoTaskData.get("collectTipsUploadNum") == null ? 0 : mongoTaskData.get("collectTipsUploadNum"));
			resultMap.put("collectPoiUploadNum", mongoTaskData.get("collectPoiUploadNum") == null ? 0 : mongoTaskData.get("collectPoiUploadNum"));
		}else{
			Set<Integer> collectTaskIdSet = TaskService.getInstance().getCollectTaskIdsByTaskId(conn, taskId);
			Map<String, Object> collectData = convertCollectData(conn, collectTaskIdSet);
			resultMap.put("collectPlanStartDate", collectData.containsKey("collectPlanStartDate") ? collectData.get("collectPlanStartDate") : "");
			resultMap.put("collectPlanEndDate", collectData.containsKey("collectPlanEndDate") ? collectData.get("collectPlanEndDate") : "");
			resultMap.put("collectTaskStatus", collectData.containsKey("collectTaskStatus") ? collectData.get("collectTaskStatus") : "");
			resultMap.put("collectDiffDate", collectData.containsKey("collectDiffDate") ? collectData.get("collectDiffDate") : 0);
			resultMap.put("collectTipsUploadNum", mongoTaskData.get("collectTipsUploadNum") == null ? mongoTaskData.get("collectTipsUploadNum") : 0);
			resultMap.put("poiUploadNum", mongoTaskData.get("poiUploadNum") == null ? 0 : mongoTaskData.get("poiUploadNum"));
			resultMap.put("poiUnfinishNum", mongoTaskData.get("poiUnfinishNum") == null ? 0 : mongoTaskData.get("poiUnfinishNum"));
			resultMap.put("tipsCreateByEditNum", mongoTaskData.get("tipsCreateByEditNum") == null ? 0 : mongoTaskData.get("tipsCreateByEditNum"));
			if(type == 1){
				resultMap.put("dayEditTipsUnFinishNum", mongoTaskData.get("dayEditTipsUnFinishNum") == null ? 0 : mongoTaskData.get("dayEditTipsUnFinishNum"));
				resultMap.put("dayEditTipsFinishNum", mongoTaskData.get("dayEditTipsFinishNum") == null ? 0 : mongoTaskData.get("dayEditTipsFinishNum"));
			}
			if(type == 2){
				resultMap.put("day2MonthNum", mongoTaskData.get("day2MonthNum") == null ? 0 : mongoTaskData.get("day2MonthNum"));
				resultMap.put("monthPoiLogUnFinishNum", mongoTaskData.get("monthPoiLogUnFinishNum") == null ? 0 : mongoTaskData.get("monthPoiLogUnFinishNum"));
				resultMap.put("monthPoiLogFinishNum", mongoTaskData.get("monthPoiLogFinishNum") == null ? 0 : mongoTaskData.get("monthPoiLogFinishNum"));
			}
		}
		return resultMap;
	}
	
}
