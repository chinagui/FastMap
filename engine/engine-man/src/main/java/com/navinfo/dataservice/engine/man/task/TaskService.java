package com.navinfo.dataservice.engine.man.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.engine.dao.DBConnector;
import com.navinfo.dataservice.engine.man.city.CityOperation;
import com.navinfo.dataservice.engine.man.task.Task;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.geo.computation.GeometryUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

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
			conn = DBConnector.getInstance().getConnection();
			for (int i = 0; i < taskArray.size(); i++) {
				JSONObject taskJson = taskArray.getJSONObject(i);
				Task bean = TaskOperation.jsonToBean(taskJson);	
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
			
			conn = DBConnector.getInstance().getConnection();
			
			for (int i = 0; i < taskArray.size(); i++) {
				JSONObject taskJson = taskArray.getJSONObject(i);
				Task bean=TaskOperation.jsonToBean(taskJson);
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
			conn = DBConnector.getInstance().getConnection();	
					
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
	
	public Page list(JSONObject conditionJson,JSONObject orderJson,int currentPageNum)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getConnection();	
					
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
			return TaskOperation.selectTaskBySql(conn, selectSql, null,currentPageNum);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	public void delete(JSONObject json)throws Exception{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			Task  bean = (Task)JSONObject.toBean(obj, Task.class);	
			
			String deleteSql = "delete from  task where 1=1 ";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getTaskId()!=null && StringUtils.isNotEmpty(bean.getTaskId().toString())){
				deleteSql+=" and TASK_ID=? ";
				values.add(bean.getTaskId());
			};
			if (bean!=null&&bean.getCityId()!=null && StringUtils.isNotEmpty(bean.getCityId().toString())){
				deleteSql+=" and CITY_ID=? ";
				values.add(bean.getCityId());
			};
			if (bean!=null&&bean.getCreateUserId()!=null && StringUtils.isNotEmpty(bean.getCreateUserId().toString())){
				deleteSql+=" and CREATE_USER_ID=? ";
				values.add(bean.getCreateUserId());
			};
			if (bean!=null&&bean.getCreateDate()!=null && StringUtils.isNotEmpty(bean.getCreateDate().toString())){
				deleteSql+=" and CREATE_DATE=? ";
				values.add(bean.getCreateDate());
			};
			if (bean!=null&&bean.getStatus()!=null && StringUtils.isNotEmpty(bean.getStatus().toString())){
				deleteSql+=" and STATUS=? ";
				values.add(bean.getStatus());
			};
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				deleteSql+=" and DESCP=? ";
				values.add(bean.getDescp());
			};
			if (bean!=null&&bean.getCollectPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanStartDate().toString())){
				deleteSql+=" and COLLECT_PLAN_START_DATE=? ";
				values.add(bean.getCollectPlanStartDate());
			};
			if (bean!=null&&bean.getCollectPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanEndDate().toString())){
				deleteSql+=" and COLLECT_PLAN_END_DATE=? ";
				values.add(bean.getCollectPlanEndDate());
			};
			if (bean!=null&&bean.getDayEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanStartDate().toString())){
				deleteSql+=" and DAY_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getDayEditPlanStartDate());
			};
			if (bean!=null&&bean.getDayEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanEndDate().toString())){
				deleteSql+=" and DAY_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getDayEditPlanEndDate());
			};
			if (bean!=null&&bean.getBMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getBMonthEditPlanStartDate().toString())){
				deleteSql+=" and B_MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getBMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getBMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getBMonthEditPlanEndDate().toString())){
				deleteSql+=" and B_MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getBMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getCMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCMonthEditPlanStartDate().toString())){
				deleteSql+=" and C_MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getCMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getCMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCMonthEditPlanEndDate().toString())){
				deleteSql+=" and C_MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getCMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getDayProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanStartDate().toString())){
				deleteSql+=" and DAY_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getDayProducePlanStartDate());
			};
			if (bean!=null&&bean.getDayProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanEndDate().toString())){
				deleteSql+=" and DAY_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getDayProducePlanEndDate());
			};
			if (bean!=null&&bean.getMonthProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanStartDate().toString())){
				deleteSql+=" and MONTH_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getMonthProducePlanStartDate());
			};
			if (bean!=null&&bean.getMonthProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanEndDate().toString())){
				deleteSql+=" and MONTH_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getMonthProducePlanEndDate());
			};
			if (bean!=null&&bean.getLatest()!=null && StringUtils.isNotEmpty(bean.getLatest().toString())){
				deleteSql+=" and LATEST=? ";
				values.add(bean.getLatest());
			};
			if (values.size()==0){
	    		run.update(conn, deleteSql);
	    	}else{
	    		run.update(conn, deleteSql,values.toArray());
	    	}
	    	
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
			conn = DBConnector.getInstance().getConnection();	
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
