package com.navinfo.dataservice.engine.man.task;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.engine.man.city.CityOperation;
import com.navinfo.dataservice.engine.man.task.Task;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName:  TaskService 
* @author code generator
* @date 2016-06-06 06:12:30 
* @Description: TODO
*/
@Service
public class TaskService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(long userId,JSONObject json) throws Exception{
		Connection conn = null;
		try{
			if(!json.containsKey("tasks")){
				return;
			}			
			JSONArray taskArray=json.getJSONArray("tasks");
			conn = DBConnector.getInstance().getManConnection();
			for (int i = 0; i < taskArray.size(); i++) {
				JSONObject taskJson = taskArray.getJSONObject(i);
				Task bean = (Task) JsonOperation.jsonToBean(taskJson,Task.class);
				bean.setCreateUserId((int) userId);
				TaskOperation.insertTask(conn, bean);
				TaskOperation.updateLatest(conn,bean.getCityId());
				CityOperation.updatePlanStatus(conn,bean.getCityId(),1);
			}			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public void update(JSONObject json) throws Exception{
		Connection conn = null;
		try{
			if(!json.containsKey("tasks")){return;}
			
			JSONArray taskArray=json.getJSONArray("tasks");
			
			conn = DBConnector.getInstance().getManConnection();
			
			for (int i = 0; i < taskArray.size(); i++) {
				JSONObject taskJson = taskArray.getJSONObject(i);
				Task bean=(Task) JsonOperation.jsonToBean(taskJson,Task.class);
				TaskOperation.updateTask(conn, bean);				
			}
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public List<HashMap> list(JSONObject conditionJson,JSONObject orderJson)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
					
			JSONObject obj = JSONObject.fromObject(conditionJson);
			
			String selectSql = "select * from task where LATEST=1 ";
			if(null!=conditionJson){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("cityId".equals(key)) {selectSql+=" and city_id="+conditionJson.getInt(key);}
					if ("createUserId".equals(key)) {selectSql+=" and create_user_id="+conditionJson.getInt(key);}
					if ("descp".equals(key)) {selectSql+=" and descp="+conditionJson.getString(key);}
					if ("status".equals(key)) {selectSql+=" and status="+conditionJson.getInt(key);}					
					}
				}
			if(null!=orderJson){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("collectPlanStartDate".equals(key)) {selectSql+=" order by COLLECT_PLAN_START_DATE";break;}
					if ("collectPlanEndDate".equals(key)) {selectSql+=" order by COLLECT_PLAN_END_DATE";break;}
					if ("dayEditPlanStartDate".equals(key)) {selectSql+=" order by DAY_EDIT_PLAN_START_DATE";break;}
					if ("dayEditPlanEndDate".equals(key)) {selectSql+=" order by DAY_EDIT_PLAN_END_DATE";break;}
					if ("bMonthEditPlanStartDate".equals(key)) {selectSql+=" order by B_MONTH_EDIT_PLAN_START_DATE";break;}
					if ("bMonthEditPlanEndDate".equals(key)) {selectSql+=" order by B_MONTH_EDIT_PLAN_END_DATE";break;}
					if ("cMonthEditPlanStartDate".equals(key)) {selectSql+=" order by C_MONTH_EDIT_PLAN_START_DATE";break;}
					if ("cMonthEditPlanEndDate".equals(key)) {selectSql+=" order by C_MONTH_EDIT_PLAN_END_DATE";break;}	
					if ("dayProducePlanStartDate".equals(key)) {selectSql+=" order by DAY_PRODUCE_PLAN_START_DATE";break;}
					if ("dayProducePlanEndDate".equals(key)) {selectSql+=" order by DAY_PRODUCE_PLAN_END_DATE";break;}
					if ("monthProducePlanStartDate".equals(key)) {selectSql+=" order by MONTH_PRODUCE_PLAN_START_DATE";break;}
					if ("monthProducePlanStartDate".equals(key)) {selectSql+=" order by MONTH_PRODUCE_PLAN_END_DATE";break;}
					}
			}
			return TaskOperation.selectTaskBySql(conn, selectSql, null);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Page list(JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
					
			JSONObject obj = JSONObject.fromObject(conditionJson);
			
			String selectSql = "select * from task where LATEST=1 ";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("cityId".equals(key)) {selectSql+=" and city_id="+conditionJson.getInt(key);}
					if ("createUserId".equals(key)) {selectSql+=" and create_user_id="+conditionJson.getInt(key);}
					if ("descp".equals(key)) {selectSql+=" and descp="+conditionJson.getString(key);}
					if ("status".equals(key)) {selectSql+=" and status="+conditionJson.getInt(key);}					
					}
				}
			if(null!=orderJson && !orderJson.isEmpty()){
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("collectPlanStartDate".equals(key)) {selectSql+=" order by COLLECT_PLAN_START_DATE";break;}
					if ("collectPlanEndDate".equals(key)) {selectSql+=" order by COLLECT_PLAN_END_DATE";break;}
					if ("dayEditPlanStartDate".equals(key)) {selectSql+=" order by DAY_EDIT_PLAN_START_DATE";break;}
					if ("dayEditPlanEndDate".equals(key)) {selectSql+=" order by DAY_EDIT_PLAN_END_DATE";break;}
					if ("bMonthEditPlanStartDate".equals(key)) {selectSql+=" order by B_MONTH_EDIT_PLAN_START_DATE";break;}
					if ("bMonthEditPlanEndDate".equals(key)) {selectSql+=" order by B_MONTH_EDIT_PLAN_END_DATE";break;}
					if ("cMonthEditPlanStartDate".equals(key)) {selectSql+=" order by C_MONTH_EDIT_PLAN_START_DATE";break;}
					if ("cMonthEditPlanEndDate".equals(key)) {selectSql+=" order by C_MONTH_EDIT_PLAN_END_DATE";break;}	
					if ("dayProducePlanStartDate".equals(key)) {selectSql+=" order by DAY_PRODUCE_PLAN_START_DATE";break;}
					if ("dayProducePlanEndDate".equals(key)) {selectSql+=" order by DAY_PRODUCE_PLAN_END_DATE";break;}
					if ("monthProducePlanStartDate".equals(key)) {selectSql+=" order by MONTH_PRODUCE_PLAN_START_DATE";break;}
					if ("monthProducePlanStartDate".equals(key)) {selectSql+=" order by MONTH_PRODUCE_PLAN_END_DATE";break;}
					}
			}
//			return TaskOperation.selectTaskBySql(conn, selectSql, null,currentPageNum,pageSize);
			return null;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	public List<List<String>> close(JSONObject json)throws Exception{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			JSONArray taskIds=json.getJSONArray("taskIds");
			String taskIdStr=taskIds.toString().replace("[", "").replace("]", "").replace("\"", "");
			//判断任务是否可关闭
			String checkSql="SELECT T.TASK_ID, '有未关闭的BLOCK，任务无法关闭'"
					+ "  FROM TASK T, BLOCK B"
					+ " WHERE T.TASK_ID IN ("+taskIdStr+")"
					+ "   AND T.CITY_ID = B.CITY_ID"
					+ "   AND B.PLAN_STATUS <> 2"
					+ " UNION"
					+ " SELECT ST.TASK_ID, '有未关闭的月编子任务，任务无法关闭'"
					+ "  FROM SUBTASK ST"
					+ " WHERE ST.TASK_ID IN ("+taskIdStr+")"
					+ "   AND ST.STAGE = 2"
					+ "   AND ST.STATUS <> 0";
			List<List<String>> checkResult=DbOperation.exeSelectBySql(conn, checkSql, null);
			JSONArray closeTask=new JSONArray();
			List newTask=new ArrayList();
			newTask=JSONArray.toList(taskIds);
			if(checkResult.size()>0){
				List errorTask=new ArrayList();
				for(int i=0;i<checkResult.size();i++){
					errorTask.add(checkResult.get(i).get(0));
				}
				newTask.removeAll(errorTask);				
			}
			String updateSql="UPDATE TASK SET STATUS=0 "
					+ "WHERE TASK_ID IN ("+newTask.toString().replace("[", "").
					replace("]", "").replace("\"", "")+")";
			DbOperation.exeUpdateOrInsertBySql(conn, updateSql);
	    	return checkResult;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public HashMap query(int taskId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
			String selectSql = "select * from task where taskId= "+taskId;
			List<HashMap> taskList=TaskOperation.selectTaskBySql(conn, selectSql, null);
			return taskList.get(0);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
