package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.QueryRunner;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * Created by wangshishuai3966 on 2017/7/10.
 */
public class Tips2MarkUtils {
    private static Logger log = LoggerRepos.getLogger(Tips2MarkUtils.class);

    private static Map<String, Object> getTaskInfo(Connection conn, long taskId) throws Exception {
        try {
            QueryRunner run = new QueryRunner();
            String selectSql = "SELECT T.TASK_ID           COLLECT_ID," +
                    "       T.NAME              COLLECT_NAME," +
                    "       T.STATUS," +
                    "       T.TYPE," +
                    "       R.MONTHLY_DB_ID," +
                    "       C.PROVINCE_NAME," +
                    "       C.CITY_NAME,t.lot,t.UPLOAD_METHOD," +
                    "       B.BLOCK_NAME" +
                    "  FROM TASK              T," +
                    "       BLOCK             B," +
                    "       CITY              C," +
                    "       REGION            R" +
                    " WHERE T.REGION_ID = R.REGION_ID" +
                    "   AND T.BLOCK_ID = B.BLOCK_ID" +
                    "   AND B.CITY_ID = C.CITY_ID" +
                    "   AND T.TASK_ID = ?";
            ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
                public Map<String, Object> handle(ResultSet rs) throws SQLException {
                    Map<String, Object> result = new HashMap<String, Object>();
                    if (rs.next()) {
                        result.put("collectId", rs.getInt("COLLECT_ID"));
                        result.put("collectName", rs.getString("COLLECT_NAME"));
                        result.put("dbId", rs.getInt("MONTHLY_DB_ID"));
                        result.put("lot", rs.getInt("LOT"));
                        result.put("uploadMethod", rs.getString("UPLOAD_METHOD"));
                        result.put("provinceName", rs.getString("PROVINCE_NAME"));
                        result.put("cityName", rs.getString("CITY_NAME"));
                        result.put("blockName", rs.getString("BLOCK_NAME"));
                        result.put("status", rs.getInt("status"));
                        result.put("type", rs.getInt("type"));
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, taskId);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询任务信息失败，原因为:" + e.getMessage(), e);
        }
    }

    private static Map<String, Object> getProgramInfo(Connection conn, final long projectId) throws Exception {
        try {
            QueryRunner run = new QueryRunner();
            String selectSql = "SELECT DISTINCT P.PROGRAM_ID     COLLECT_ID," +
                    "       P.NAME              COLLECT_NAME," +
                    "       P.TYPE," +
                    "       P.STATUS," +
                    "       R.MONTHLY_DB_ID,t.lot,t.UPLOAD_METHOD," +
                    "       I.ADMIN_NAME,i.INFOR_NAME," +
                    "       (select listagg(task_id,',') within GROUP (order by task_id) " +
                    "        from TASK T1 where T1.status=0 and T1.type=0 and T1.program_id=P.program_id) TASKS" +
                    "  FROM PROGRAM           P," +
                    "       TASK              T," +
                    "       REGION            R," +
                    "       INFOR             I" +
                    " WHERE P.PROGRAM_ID = T.PROGRAM_ID" +
                    "   AND T.REGION_ID = R.REGION_ID" +
                    "   AND P.INFOR_ID  = I.INFOR_ID" +
                    "   AND P.PROGRAM_ID = ?";
            ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
                public Map<String, Object> handle(ResultSet rs) throws SQLException {
                    Map<String, Object> result = new HashMap<String, Object>();
                    if (rs.next()) {
                        result.put("collectId", rs.getInt("COLLECT_ID"));
                        result.put("collectName", rs.getString("COLLECT_NAME"));
                        result.put("inforName", rs.getString("INFOR_NAME"));
                        result.put("dbId", rs.getInt("MONTHLY_DB_ID"));
                        result.put("lot", rs.getInt("LOT"));
                        result.put("uploadMethod", rs.getString("UPLOAD_METHOD"));
                        result.put("status", rs.getInt("STATUS"));
                        result.put("type", rs.getInt("TYPE"));
                        result.put("tasks", rs.getString("TASKS"));
                        String adminName = rs.getString("ADMIN_NAME");
                        String provinceName = "测试";
                        String cityName = "测试";
                        String blockName = "测试";
                        if (adminName != null) {
                            String[] names = adminName.split("\\|");
                            if (names.length > 0) {
                                provinceName = names[0];
                            }
                            if (names.length > 1) {
                                cityName = names[1];
                            }
                            if (names.length > 2) {
                                blockName = names[2];
                            }
                        }
                        result.put("provinceName", provinceName);
                        result.put("cityName", cityName);
                        result.put("blockName", blockName);
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, projectId);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询项目信息失败，原因为:" + e.getMessage(), e);
        }
    }

    private static Map<String, Object> getSubTaskInfo(Connection conn, long subtaskId) throws Exception {
        try {
            QueryRunner run = new QueryRunner();
            String selectSql = "SELECT ST.SUBTASK_ID           COLLECT_ID," +
                    "       ST.NAME              COLLECT_NAME," +
                    "       ST.STAGE," +
                    "       ST.STATUS," +
                    "       R.MONTHLY_DB_ID,t.lot,t.UPLOAD_METHOD,t.task_id," +
                    "       C.PROVINCE_NAME," +
                    "       C.CITY_NAME," +
                    "       B.BLOCK_NAME" +
                    "  FROM SUBTASK           ST," +
                    "       TASK              T," +
                    "       BLOCK             B," +
                    "       CITY              C," +
                    "       REGION            R" +
                    " WHERE ST.TASK_ID = T.TASK_ID" +
                    "   AND T.REGION_ID = R.REGION_ID" +
                    "   AND T.BLOCK_ID = B.BLOCK_ID" +
                    "   AND B.CITY_ID = C.CITY_ID" +
                    "   AND ST.SUBTASK_ID = ?";
            ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
                public Map<String, Object> handle(ResultSet rs) throws SQLException {
                    Map<String, Object> result = new HashMap<String, Object>();
                    if (rs.next()) {
                        result.put("collectId", rs.getInt("COLLECT_ID"));
                        result.put("collectName", rs.getString("COLLECT_NAME"));
                        result.put("dbId", rs.getInt("MONTHLY_DB_ID"));
                        result.put("provinceName", rs.getString("PROVINCE_NAME"));
                        result.put("lot", rs.getInt("LOT"));
                        result.put("uploadMethod", rs.getString("UPLOAD_METHOD"));
                        result.put("cityName", rs.getString("CITY_NAME"));
                        result.put("blockName", rs.getString("BLOCK_NAME"));
                        result.put("status", rs.getInt("STATUS"));
                        result.put("taskId", rs.getInt("TASK_ID"));
                        result.put("type", rs.getInt("STAGE"));
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, subtaskId);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询子任务信息失败，原因为:" + e.getMessage(), e);
        }
    }

    public static Map<String, Object> getItemInfo(Connection conn, long itemId, ItemType itemType) throws Exception {
    	Map<String, Object> cmsInfo=new HashMap<>();
    	List<Task> tasks = new ArrayList<>();
    	switch (itemType) {
            case PROJECT:
            	cmsInfo= Tips2MarkUtils.getProgramInfo(conn, itemId);
            	List<Task> taskPojos = TaskService.getInstance().getTaskByProgramId(conn, Integer.valueOf(String.valueOf(itemId)));
				for(Task task : taskPojos){
					//采集任务
					if(0 == task.getType()){
						tasks.add(task);
					}
				}
            	break;
            case TASK:
            	cmsInfo=Tips2MarkUtils.getTaskInfo(conn, itemId);
            	Task task = TaskService.getInstance().queryNoGeoByTaskId(conn, Integer.valueOf(String.valueOf(itemId)));
        		tasks.add(task);
            	break;
            case SUBTASK:
            	cmsInfo= Tips2MarkUtils.getSubTaskInfo(conn, itemId);
            	break;
        }
    	//子任务不传poiMeshes和poiPlanLoad
    	if(itemType != ItemType.SUBTASK){
    		Map<Integer, Integer> poiPlanLoad = queryMeshesByTasks(conn, tasks, itemType);
    		cmsInfo.put("poiMeshes", poiPlanLoad.keySet());
    		cmsInfo.put("poiPlanLoad", poiPlanLoad);
    	}    	
    	return cmsInfo;
    }
    
    /**
     * 根据taskIds查询增加参数的数据
     * @throws Exception 
     * 
     * */
    public static Map<Integer, Integer> queryMeshesByTasks(Connection conn, List<Task> tasks,ItemType itemType) throws Exception{
    	
    	Map<Integer, Integer> result = new HashMap<>();
    	
		for(Task task : tasks){
			Connection dailyConn = null;
			try{
				Region region = RegionService.getInstance().query(conn, task.getRegionId());				
				dailyConn = DBConnector.getInstance().getConnectionById(region.getDailyDbId());
				String taskType = "medium_task_id";
		    	if(itemType == ItemType.PROJECT){
		    		taskType = "quick_task_id";
		    	}
		    	Map<Integer, Integer> oneResult = getPoiMeshDetail(dailyConn,taskType,task.getTaskId());
		    	appendResult(result,oneResult);
		    	//仅中线任务制作点门牌
		    	if(itemType == ItemType.TASK){
		    		oneResult = getPointMeshDetail(dailyConn,taskType,task.getTaskId());
		    		appendResult(result,oneResult);}
			}catch(Exception e){
	    		log.error("queryMeshesByTasks error" + e.getMessage(), e);
	    		throw e;
	    	}finally{
	    		DbUtils.closeQuietly(dailyConn);
	    	}
		}    	
    	return result;
    }
    
    public static void appendResult(Map<Integer, Integer> result,Map<Integer, Integer> oneResult){
    	for (int meshId:oneResult.keySet()){
    		if(!result.containsKey(meshId)){
    			result.put(meshId, 0);
    		}
    		result.put(meshId, result.get(meshId)+oneResult.get(meshId));
    	}
    }
    
    public static Map<Integer, Integer> getPoiMeshDetail(Connection dailyConn,String taskType,int taskId) throws Exception{
    	QueryRunner run = new QueryRunner();    	
		String poiSql = "select count(1), t.mesh_id from IX_POI t, POI_EDIT_STATUS ts "
				+ "where ts." + taskType+" = " + taskId + 
				" and ts.pid = t.pid group by t.mesh_id";
    	
    	log.info("querypoiSql :" + poiSql);
    	
    	ResultSetHandler<Map<Integer, Integer>> handler = new ResultSetHandler<Map<Integer, Integer>>() {
    		public Map<Integer, Integer> handle(ResultSet rs) throws SQLException {
    			Map<Integer, Integer> meshesCount = new HashMap<>();
    			while(rs.next()) {
    				meshesCount.put(rs.getInt("mesh_id"), rs.getInt("count(1)"));
    			}
    			return meshesCount;
    		}
    	};
    	Map<Integer, Integer> oneResult = run.query(dailyConn, poiSql, handler);
    	return oneResult;
    }
    
    public static Map<Integer, Integer> getPointMeshDetail(Connection dailyConn,String taskType,int taskId) throws Exception{
    	QueryRunner run = new QueryRunner();    	
		String poiSql = "select count(1), t.mesh_id from IX_POINTADDRESS t, pointaddress_edit_status ts "
				+ "where ts." + taskType+" = " + taskId + 
				" and ts.pid = t.pid group by t.mesh_id";
    	
    	log.info("querypoiSql :" + poiSql);
    	
    	ResultSetHandler<Map<Integer, Integer>> handler = new ResultSetHandler<Map<Integer, Integer>>() {
    		public Map<Integer, Integer> handle(ResultSet rs) throws SQLException {
    			Map<Integer, Integer> meshesCount = new HashMap<>();
    			while(rs.next()) {
    				meshesCount.put(rs.getInt("mesh_id"), rs.getInt("count(1)"));
    			}
    			return meshesCount;
    		}
    	};
    	Map<Integer, Integer> oneResult = run.query(dailyConn, poiSql, handler);
    	return oneResult;
    }
}
