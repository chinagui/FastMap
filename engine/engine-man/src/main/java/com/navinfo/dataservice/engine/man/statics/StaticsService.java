package com.navinfo.dataservice.engine.man.statics;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.BlockExpectStatInfo;
import com.navinfo.dataservice.api.statics.model.GridChangeStatInfo;
import com.navinfo.dataservice.api.statics.model.SubtaskStatInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.block.BlockService;
import com.navinfo.dataservice.engine.man.city.CityService;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
	public List<GridChangeStatInfo> gridChangeStaticQuery(String wkt, int stage, int type, String date)
			throws JSONException, Exception {
		//通过wkt获取gridIdList
		Geometry geo=GeoTranslator.geojson2Jts(Geojson.wkt2Geojson(wkt));
		Set<String> grids= CompGeometryUtil.geo2GridsWithoutBreak(geo);
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");

		return api.getChangeStatByGrids(grids, type, stage, date);
	}
	
	public List<HashMap> blockExpectStatQuery(String wkt) throws JSONException, Exception{
		BlockService service = BlockService.getInstance();
		
		JSONObject json = new JSONObject();
		
		json.put("wkt",wkt);
		
		JSONArray status = new JSONArray();
		
		status.add(0);
		
		status.add(1);
		
		json.put("snapshot", 0);
		
		json.put("planningStatus", status);
		
		List<HashMap> data = service.listByWkt(json);
		
		Set<Integer> blocks = new HashSet<Integer>();
		
		for(HashMap map : data){
			int blockId = (int) map.get("blockId");
			
			blocks.add(blockId);
		}
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
		
		Map<Integer,Integer> statusMap = api.getExpectStatusByBlocks(blocks);
		
		for(HashMap map : data){
			map.put("expectStatus", statusMap.get(map.get("blockId")));
		}
		
		return data;
	}
	
	public HashMap blockExpectStatQuery(int blockId, int stage) throws JSONException, Exception{
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
	
		HashMap data = new HashMap();
		
		List<BlockExpectStatInfo> poiStat = api.getExpectStatByBlock(blockId, stage, 0);
		
		List<BlockExpectStatInfo> roadStat = api.getExpectStatByBlock(blockId, stage, 1);
		
		data.put("poi", poiStat);
		
		data.put("road", roadStat);
		
		return data;
	}
	
	public List<HashMap> cityExpectStatQuery(String wkt) throws JSONException, Exception{
		CityService service = CityService.getInstance();
		
		JSONObject json = new JSONObject();
		
		json.put("wkt",wkt);
		
		JSONArray status = new JSONArray();
		
		status.add(0);
		
		status.add(1);
		
		json.put("planningStatus", status);
		
		List<HashMap> data = service.queryListByWkt(json);
		
		Set<Integer> citys = new HashSet<Integer>();
		
		for(HashMap map : data){
			int blockId = (int) map.get("cityId");
			
			citys.add(blockId);
		}
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
		
		Map<Integer,Integer> statusMap = api.getExpectStatusByCitys(citys);
		
		for(HashMap map : data){
			map.put("expectStatus", statusMap.get(map.get("cityId")));
		}
		
		return data;
	}
	
//	public SubtaskStatInfo subtaskStatQuery(int subtaskId) throws JSONException, Exception{
//		
//		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
//		
//		return api.getStatBySubtask(subtaskId);
//
//	}
	
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

	}
	
//	public JSONObject queryTaskOverView(int taskType) throws JSONException, Exception{		
//		Connection conn = null;
//		try {	
//			conn = DBConnector.getInstance().getManConnection();
//			JSONObject taskStaticsJson= new JSONObject();
//			//0关闭 1开启,<100 2未规划+草稿 3已完成=开启，=100
//			taskStaticsJson.put(0, 0);
//			taskStaticsJson.put(1, 0);
//			taskStaticsJson.put(2, 0);
//			taskStaticsJson.put(3, 0);
//			
//			JSONObject cityStaticsJson= new JSONObject();
//			cityStaticsJson.put(0, 0);
//			cityStaticsJson.put(1, 0);
//			cityStaticsJson.put(2, 0);
//			
//			String selectTaskSql="";
//			String selectCitySql="";
//			if(taskType==1){	
//				selectTaskSql = "SELECT 2 STATUS, COUNT(1) taskCount"
//						+ "  FROM CITY"
//						+ " WHERE CITY_ID NOT IN (100000, 100001, 100002)"
//						+ "   AND PLAN_STATUS = 0"
//						+ " UNION ALL"
//						+ " SELECT STATUS, COUNT(1)"
//						+ "  FROM TASK"
//						+ " WHERE LATEST = 1"
//						+ "   AND TASK_TYPE = 1"
//						+ " GROUP BY STATUS";
//				selectCitySql = "SELECT plan_status,COUNT(1) planCount FROM city where CITY_ID NOT IN (100000, 100001, 100002) GROUP BY plan_status";
//			}else if (taskType==4) {
//				selectTaskSql = "SELECT 2 STATUS, COUNT(1) taskCount"
//						+ "  FROM INFOR"
//						+ " WHERE PLAN_STATUS = 0"
//						+ " UNION ALL"
//						+ " SELECT STATUS, COUNT(1)"
//						+ "  FROM TASK"
//						+ " WHERE LATEST = 1"
//						+ "   AND TASK_TYPE = 4"
//						+ " GROUP BY STATUS"; 
//				selectCitySql = "SELECT plan_status,COUNT(1) planCount FROM INFOR GROUP BY plan_status";
//			}
//			PreparedStatement stmtTask = null;
//			PreparedStatement stmtCity = null;
//			try {
//				stmtTask = conn.prepareStatement(selectTaskSql);
//				stmtCity = conn.prepareStatement(selectCitySql);
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			ResultSet rsTask = stmtTask.executeQuery();
//			ResultSet rsCity = stmtCity.executeQuery();
//		
//			while (rsTask.next()) {
//				int status=rsTask.getInt("status");
//				int num=(int) taskStaticsJson.get(String.valueOf(status));
//				taskStaticsJson.put(status,num+rsTask.getInt("taskCount"));
//			}
//			while (rsCity.next()) {
//				cityStaticsJson.put(rsCity.getInt("plan_status"),rsCity.getInt("planCount"));
//			}
//			//获取任务开启，完成度100%的任务list
//			List<Integer> taskList=new ArrayList<Integer>();
//			StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
//			taskList=api.getOpen100TaskIdList();
//			int openTaskNum=0;
//			if(taskList!=null && taskList.size()>0){
//				String selectSql="SELECT COUNT(1) NUM"
//						+ "  FROM TASK"
//						+ " WHERE LATEST = 1"
//						+ "   AND TASK_TYPE = 1"
//						+ "   AND STATUS = 1"
//						+ "   AND TASK_ID IN ("+taskList.toString().replace("[", "").replace("]", "").replace("\"", "")+")";
//				PreparedStatement stmtOpenTask100 = null;
//				stmtOpenTask100 = conn.prepareStatement(selectSql);
//				ResultSet rsOpenTask100 = stmtOpenTask100.executeQuery();
//				while (rsOpenTask100.next()) {
//					openTaskNum=rsOpenTask100.getInt("NUM");
//					break;
//				}
//				if(openTaskNum>0){
//					int num50=(int) taskStaticsJson.get(1);
//					taskStaticsJson.put(1,num50-openTaskNum);
//					taskStaticsJson.put(3,openTaskNum);}
//			}
//			JSONObject staticsJson= new JSONObject();
//			staticsJson.put("task", taskStaticsJson);
//			staticsJson.put("city", cityStaticsJson);
//			return staticsJson;
//			
//			} catch (Exception e) {
//				DbUtils.rollbackAndCloseQuietly(conn);
//				log.error(e.getMessage(), e);
//			throw new ServiceException("查询失败:" + e.getMessage(), e);
//		} finally {
//				DbUtils.commitAndCloseQuietly(conn);
//			}
//	}

	public JSONObject querymonthTaskOverView()throws Exception{
		Connection conn = null;
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
			PreparedStatement stmtGrid = null;
			PreparedStatement stmtOther = null;
			try {
				stmtGrid = conn.prepareStatement(gridSql);
				stmtOther = conn.prepareStatement(otherSql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ResultSet rsGrid = stmtGrid.executeQuery();
			ResultSet rsOther = stmtOther.executeQuery();
		
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
				DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public JSONObject queryCollectOverView(int groupId) throws Exception {
		Connection conn = null;
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

	/**
	 * @param groupId
	 * @param stage 
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String,Object> queryBlockOverViewByGroup(int groupId, int stage) throws ServiceException {
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
	public Map<String,Object> queryGroupOverView(int groupId, int stage) throws ServiceException {
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
	}
	
	/**
	 * @param taskId
	 * @param type 
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> queryBlockOverViewByTask(int taskId, int type) throws ServiceException {
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
	}

//	/**
//	 * @param taskId
//	 * @param type 
//	 * @return
//	 * @throws ServiceException 
//	 */
//	public Map<String, Object> queryBlockOverViewByTask(int taskId, int type) throws ServiceException {
//		// TODO Auto-generated method stub
//		Connection conn = null;
//		try {
//			conn = DBConnector.getInstance().getManConnection();
//			QueryRunner run = new QueryRunner();
//			
//			//1常规，2多源，3代理店，4情报
//			
//			String selectSql = "";
//			//BLOCK未规划 unPlanned
//			String selectSql_unPlanned = "SELECT 'unPlanned' AS TYPE, COUNT(1) AS NUM FROM TASK T ,BLOCK B WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 0 AND T.TASK_ID = " + taskId;
//			//情报BLOCK未规划 unPlanned_info
//			String selectSql_unPlanned_info = "SELECT 'unPlanned' AS TYPE, COUNT(1) AS NUM FROM BLOCK_MAN BM WHERE BM.STATUS = 2 AND BM.TASK_ID = " + taskId;
//			//Block已规划 planned
//			String selectSql_planned = "SELECT 'planned' AS TYPE, COUNT(1) AS NUM FROM TASK T ,BLOCK B WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND T.TASK_ID = " + taskId;
//			//block已关闭 planClosed
//			String selectSql_planClosed = "SELECT 'planClosed' AS TYPE, COUNT(1) AS NUM FROM TASK T ,BLOCK B WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 2 AND T.TASK_ID = " + taskId;
//			//block_man草稿 draft
//			String selectSql_draft = "SELECT 'draft' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 2 AND T.TASK_ID = " + taskId;
//			//block_man已开启 ongoing
//			String selectSql_ongoing = "SELECT 'ongoing' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 1 AND T.TASK_ID = " + taskId;
//			//block_man已开启未完成 ongoingUnfinished
//			String selectSql_ongoingUnfinished = "SELECT 'ongoingUnfinished' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 1 AND ((EXISTS (SELECT 1 FROM SUBTASK S WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID AND S.STATUS IN (1, 2))) OR (NOT EXISTS (SELECT 1 FROM SUBTASK S WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID))) AND T.TASK_ID = " + taskId;
//			//block_man已关闭 closed
//			String selectSql_closed = "SELECT 'closed' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 0 AND T.TASK_ID = " + taskId;
//
//			//block_man采集作业中正常 ongoingRegularCollect
//			String selectSql_ongoingRegularCollect = "SELECT 'ongoingRegularCollect' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM, FM_STAT_OVERVIEW_BLOCKMAN FSOB WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 1 AND FSOB.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID AND (FSOB.COLLECT_PROGRESS = 1 OR FSOB.COLLECT_PROGRESS IS NULL) AND EXISTS (SELECT 1 FROM SUBTASK S WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID AND S.STAGE = 0 AND S.STATUS = 1) AND T.TASK_ID = " + taskId;
//			//block_man采集作业中异常 ongoingUnexpectedCollect
//			String selectSql_ongoingUnexpectedCollect ="SELECT 'ongoingUnexpectedCollect' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM, FM_STAT_OVERVIEW_BLOCKMAN FSOB WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 1 AND FSOB.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID AND FSOB.COLLECT_PROGRESS = 2 AND EXISTS (SELECT 1 FROM SUBTASK S WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID AND S.STAGE = 0 AND S.STATUS = 1) AND T.TASK_ID = " + taskId;
//			//block_man采集作业中 ongoingCollect
//			String selectSql_ongoingCollect = "SELECT 'ongoingCollect' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 1 AND EXISTS (SELECT 1 FROM SUBTASK S WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID AND S.STAGE = 0 AND S.STATUS = 1) AND T.TASK_ID = " + taskId;
//			
//			//block_man日编作业中正常 ongoingRegularDaily
//			String selectSql_ongoingRegularDaily = "SELECT 'ongoingRegularDaily' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM, FM_STAT_OVERVIEW_BLOCKMAN FSOB WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 1 AND FSOB.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID AND (FSOB.DAILY_PROGRESS = 1 OR FSOB.DAILY_PROGRESS IS NULL) AND EXISTS (SELECT 1 FROM SUBTASK S WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID AND S.STAGE = 1 AND S.STATUS = 1) AND T.TASK_ID = " + taskId;
//			//block_man日编作业中异常 ongoingUnexpectedDaily
//			String selectSql_ongoingUnexpectedDaily = "SELECT 'ongoingUnexpectedDaily' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM, FM_STAT_OVERVIEW_BLOCKMAN FSOB WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 1 AND FSOB.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID AND FSOB.DAILY_PROGRESS = 2 AND EXISTS (SELECT 1 FROM SUBTASK S WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID AND S.STAGE = 1 AND S.STATUS = 1) AND T.TASK_ID = " + taskId;
//			//block_man日编作业中 ongoingDaily
//			String selectSql_ongoingDaily = "SELECT 'ongoingDaily' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 1 AND EXISTS (SELECT 1 FROM SUBTASK S WHERE S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID AND S.STAGE = 1 AND S.STATUS = 1) AND T.TASK_ID = " + taskId;
//
//			//block_man关闭正常完成 finishedRegular
//			String selectSql_finishedRegular = "SELECT 'finishedRegular' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM,FM_STAT_OVERVIEW_BLOCKMAN FSOB WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 0 AND FSOB.DIFF_DATE = 0 AND T.TASK_ID = " + taskId;
//			//block_man关闭逾期完成 finishedOverdue
//			String selectSql_finishedOverdue = "SELECT 'finishedOverdue' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM,FM_STAT_OVERVIEW_BLOCKMAN FSOB WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 0 AND FSOB.DIFF_DATE < 0 AND T.TASK_ID = " + taskId;
//			//block_man关闭提前完成 finishedAdvanced
//			String selectSql_finishedAdvanced = "SELECT 'finishedAdvanced' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM,FM_STAT_OVERVIEW_BLOCKMAN FSOB WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 0 AND FSOB.DIFF_DATE > 0 AND T.TASK_ID = " + taskId;
//			//block_man关闭采集逾期 finishedOverdueCollect
//			String selectSql_finishedOverdueCollect = "SELECT 'finishedOverdueCollect' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM,FM_STAT_OVERVIEW_BLOCKMAN FSOB WHERE T.CITY_ID = B.CITY_ID AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 0 AND FSOB.COLLECT_DIFF_DATE < 0 AND T.TASK_ID = " + taskId;
//			//block_man关闭日编逾期 finishedOverdueDaily
//			String selectSql_finishedOverdueDaily = "SELECT 'finishedOverdueDaily' AS TYPE, COUNT(1) AS NUM FROM TASK T, BLOCK B, BLOCK_MAN BM,FM_STAT_OVERVIEW_BLOCKMAN FSOB WHERE T.CITY_ID = B.CITY_ID AND T.TASK_ID = 2 AND B.PLAN_STATUS = 1 AND BM.BLOCK_ID = B.BLOCK_ID AND BM.LATEST = 1 AND BM.STATUS = 0 AND FSOB.DAILY_DIFF_DATE < 0 AND T.TASK_ID = " + taskId;
//			
//			selectSql = selectSql_planned + " UNION ALL " + selectSql_planClosed
//					+ " UNION ALL " + selectSql_draft + " UNION ALL " + selectSql_ongoing
//					+ " UNION ALL " + selectSql_ongoingUnfinished + " UNION ALL " + selectSql_closed
//					+ " UNION ALL " + selectSql_ongoingRegularCollect + " UNION ALL " + selectSql_ongoingUnexpectedCollect
//					+ " UNION ALL " + selectSql_ongoingCollect + " UNION ALL " + selectSql_ongoingRegularDaily
//					+ " UNION ALL " + selectSql_ongoingUnexpectedDaily + " UNION ALL " + selectSql_ongoingDaily
//					+ " UNION ALL " + selectSql_finishedRegular + " UNION ALL " + selectSql_finishedOverdue
//					+ " UNION ALL " + selectSql_finishedAdvanced + " UNION ALL " + selectSql_finishedOverdueCollect
//					+ " UNION ALL " + selectSql_finishedOverdueDaily;
//			
//			if(2 == type){
//				selectSql += " UNION ALL " +  selectSql_unPlanned;
//			}else{
//				selectSql += " UNION ALL " +  selectSql_unPlanned_info;
//			}
//
//			ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
//				public Map<String,Object> handle(ResultSet rs) throws SQLException {
//					Map<String,Object> result = new HashMap<String,Object>();
//					int	unPlanned = 0;
//					int planned = 0;
//					int planClosed = 0;
//					int draft = 0;
//					int ongoing = 0;
//					int ongoingUnfinished = 0;
//					int closed = 0;
//					int ongoingRegularCollect = 0;
//					int ongoingUnexpectedCollect = 0;
//					int ongoingCollect = 0;
//					int ongoingRegularDaily = 0;
//					int ongoingUnexpectedDaily = 0;
//					int ongoingDaily = 0;
//					int finishedRegular = 0;
//					int finishedOverdue = 0;
//					int finishedAdvanced = 0;
//					int finishedOverdueCollect = 0;
//					int finishedOverdueDaily = 0;
//					while (rs.next()) {
//						if(rs.getString("TYPE").equals("unPlanned")){unPlanned = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("planned")){planned = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("planClosed")){planClosed = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("draft")){draft = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("ongoing")){ongoing = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("ongoingUnfinished")){ongoingUnfinished = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("closed")){closed = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("ongoingRegularCollect")){ongoingRegularCollect = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("ongoingUnexpectedCollect")){ongoingUnexpectedCollect = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("ongoingCollect")){ongoingCollect = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("ongoingRegularDaily")){ongoingRegularDaily = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("ongoingUnexpectedDaily")){ongoingUnexpectedDaily = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("ongoingDaily")){ongoingDaily = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("finishedRegular")){finishedRegular = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("finishedOverdue")){finishedOverdue = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("finishedAdvanced")){finishedAdvanced = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("finishedOverdueCollect")){finishedOverdueCollect = rs.getInt("NUM");}
//						else if(rs.getString("TYPE").equals("finishedOverdueDaily")){finishedOverdueDaily = rs.getInt("NUM");}
//					}
//					
//					//规划
//					Map<String,Integer> planningInfo = new HashMap<String,Integer>();
//					planningInfo.put("unPlanned", unPlanned);
//					planningInfo.put("planned", planned);
//					planningInfo.put("planClosed", planClosed);
//					result.put("planningInfo", planningInfo);
//					
//					result.put("totalPlanning", unPlanned + planClosed + planned);
//					
//					//作业
//					Map<String,Integer> workingInfo = new HashMap<String,Integer>();
//					workingInfo.put("unreleased", unPlanned + draft);
//					workingInfo.put("ongoing", ongoingUnfinished);
//					workingInfo.put("finished", ongoing - ongoingUnfinished);
//					workingInfo.put("closed", closed);
//					result.put("workingInfo", workingInfo);
//					
//					result.put("totalWorking", unPlanned + draft + ongoing + closed);
//									
//					Map<String,Integer> ongoingCollectInfo = new HashMap<String,Integer>();
//					ongoingCollectInfo.put("ongoingRegularCollect", ongoingRegularCollect);
//					ongoingCollectInfo.put("ongoingUnexpectedCollect", ongoingUnexpectedCollect);
//					ongoingCollectInfo.put("ongoingFinishedCollect", ongoingCollect -ongoingRegularCollect - ongoingUnexpectedCollect);
//					ongoingCollectInfo.put("ongoingCollect", ongoingCollect);
//					result.put("ongoingCollectInfo", ongoingCollectInfo);
//					
//					Map<String,Integer> ongoingDailyInfo = new HashMap<String,Integer>();
//					ongoingDailyInfo.put("ongoingRegularDaily", ongoingRegularDaily);
//					ongoingDailyInfo.put("ongoingUnexpectedDaily", ongoingUnexpectedDaily);
//					ongoingDailyInfo.put("ongoingFinishedDaily", ongoingDaily - ongoingRegularDaily - ongoingUnexpectedDaily);
//					ongoingDailyInfo.put("ongoingDaily", ongoingDaily);
//					result.put("ongoingDailyInfo", ongoingDailyInfo);
//					
//					Map<String,Integer> unreleasedInfo = new HashMap<String,Integer>();
//					unreleasedInfo.put("draft", draft);
//					unreleasedInfo.put("unPlanned", unPlanned);
//					result.put("unreleasedInfo", unreleasedInfo);
//					
//					Map<String,Integer> finishedInfo = new HashMap<String,Integer>();
//					finishedInfo.put("finishedRegular", finishedRegular);
//					finishedInfo.put("finishedOverdue", finishedOverdue);
//					finishedInfo.put("finishedAdvanced", finishedAdvanced);
//					result.put("finishedInfo", finishedInfo);
//					
//					Map<String,Integer> overdueInfo = new HashMap<String,Integer>();
//					overdueInfo.put("finishedOverdueCollect", finishedOverdueCollect);
//					overdueInfo.put("finishedOverdueDaily", finishedOverdueDaily);
//					result.put("overdueInfo", overdueInfo);
//					
//					return result;
//				}
//	
//			};
//
//			return run.query(conn, selectSql,rsHandler);
//			
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
//		} finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}

	/**
	 * @param blockManId
	 * @param taskId 
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> querySubtaskOverView(int blockManId, int taskId) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "";
			
			if(0 != taskId){
				selectSql = "SELECT S.SUBTASK_ID,S.STAGE,S.TYPE,S.STATUS,FSOS.PERCENT,FSOS.DIFF_DATE,FSOS.PROGRESS"
						+ " FROM SUBTASK S,FM_STAT_OVERVIEW_SUBTASK FSOS"
						+ " WHERE S.SUBTASK_ID = FSOS.SUBTASK_ID(+)"
						+ " AND S.TASK_ID = " + taskId;
			}else{
				selectSql = "SELECT S.SUBTASK_ID,S.STAGE,S.TYPE,S.STATUS,FSOS.PERCENT,FSOS.DIFF_DATE,FSOS.PROGRESS"
						+ " FROM SUBTASK S,FM_STAT_OVERVIEW_SUBTASK FSOS"
						+ " WHERE S.SUBTASK_ID = FSOS.SUBTASK_ID(+)"
						+ " AND S.BLOCK_MAN_ID = " + blockManId;
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
	public Map<String, Object> queryCityOverview() throws ServiceException {
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
	}
	
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
					map.put("progress", rs.getLong("PROGRESS"));
					map.put("percent", rs.getLong("PERCENT"));
					map.put("poiPlanTotal", rs.getLong("POI_PLAN_TOTAL"));
					map.put("roadPlanTotal", rs.getLong("ROAD_PLAN_TOTAL"));
					map.put("planStartDate", rs.getTimestamp("PLAN_START_DATE"));
					map.put("planEndDate", rs.getTimestamp("PLAN_END_DATE"));
					map.put("planDate", rs.getLong("PLAN_DATE"));
					map.put("actualStartDate", rs.getTimestamp("ACTUAL_START_DATE"));
					map.put("actualEndDate", rs.getTimestamp("ACTUAL_END_DATE"));
					map.put("diffDate", rs.getLong("DIFF_DATE"));
					map.put("collectProgress", rs.getLong("COLLECT_PROGRESS"));
					map.put("collectPercent", rs.getLong("COLLECT_PERCENT"));
					map.put("collectPlanStartDate", rs.getTimestamp("COLLECT_PLAN_START_DATE"));
					map.put("collectPlanEndDate", rs.getTimestamp("COLLECT_PLAN_END_DATE"));
					map.put("collectPlanDate", rs.getLong("COLLECT_PLAN_DATE"));
					map.put("collectActualStartDate", rs.getTimestamp("COLLECT_ACTUAL_START_DATE"));
					map.put("collectActualEndDate", rs.getTimestamp("COLLECT_ACTUAL_END_DATE"));
					map.put("collectDiffDate", rs.getLong("COLLECT_DIFF_DATE"));
					map.put("dailyProgress", rs.getLong("DAILY_PROGRESS"));
					map.put("dailyPercent", rs.getLong("DAILY_PERCENT"));
					map.put("dailyPlanStartDate", rs.getTimestamp("DAILY_PLAN_START_DATE"));
					map.put("dailyPlanEndDate", rs.getTimestamp("DAILY_PLAN_END_DATE"));
					map.put("dailyPlanDate", rs.getLong("DAILY_PLAN_DATE"));
					map.put("dailyActualStartDate", rs.getTimestamp("DAILY_ACTUAL_START_DATE"));
					map.put("dailyActualEndDate", rs.getTimestamp("DAILY_ACTUAL_END_DATE"));
					map.put("dailyDiffDate", rs.getLong("DAILY_DIFF_DATE"));
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
	}

	public List<Map<String, Object>> getPoiStatusMap(String wkt, int stage) throws Exception {
		//通过geo计算所跨图幅
		Geometry geo = GeometryUtils.getPolygonByWKT(wkt);
		Coordinate[] coords = geo.getCoordinates();
		if(coords.length<4){throw new Exception("wkt参数错误，wkt应为一个长方形geo");}
		String[] meshs=MeshUtils.rect2Meshes(coords[0].x, coords[0].y, coords[2].x, coords[2].y);
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
	}

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
	}

}
