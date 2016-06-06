package com.navinfo.dataservice.engine.man.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.engine.man.task.Task;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
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

	
	public void create(JSONObject json) throws Exception{
		Connection conn = null;
		try{
			if(!json.containsKey("tasks")){
				return;
			}
			
			JSONArray taskArray=json.getJSONArray("tasks");
			Object[][] taskBatchParam=new Object[taskArray.size()][];
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();
			for (int i = 0; i < taskArray.size(); i++) {
				JSONObject taskJson = taskArray.getJSONObject(i);
				Task  bean = (Task)JSONObject.toBean(taskJson, Task.class);	
				//Object[] taskList={bean.getTaskId() , bean.getCityId(), bean.getCreateUserId(), bean.getCreateDate(), bean.getStatus(), bean.getDescp(), bean.getCollectPlanStartDate(), bean.getCollectPlanEndDate(), bean.getDayEditPlanStartDate(), bean.getDayEditPlanEndDate(), bean.getBMonthEditPlanStartDate(), bean.getBMonthEditPlanEndDate(), bean.getCMonthEditPlanStartDate(), bean.getCMonthEditPlanEndDate(), bean.getDayProducePlanStartDate(), bean.getDayProducePlanEndDate(), bean.getMonthProducePlanStartDate(), bean.getMonthProducePlanEndDate(), bean.getLatest()};
				String createSql = "insert into task (TASK_ID,CITY_ID, CREATE_USER_ID, CREATE_DATE, STATUS, DESCP, "
						+ "COLLECT_PLAN_START_DATE, COLLECT_PLAN_END_DATE, DAY_EDIT_PLAN_START_DATE, "
						+ "DAY_EDIT_PLAN_END_DATE, B_MONTH_EDIT_PLAN_START_DATE, B_MONTH_EDIT_PLAN_END_DATE, "
						+ "C_MONTH_EDIT_PLAN_START_DATE, C_MONTH_EDIT_PLAN_END_DATE, DAY_PRODUCE_PLAN_START_DATE, "
						+ "DAY_PRODUCE_PLAN_END_DATE, MONTH_PRODUCE_PLAN_START_DATE, MONTH_PRODUCE_PLAN_END_DATE, "
						+ "LATEST) "
						+ "values(TASK_SEQ.NEXTVAL,"+bean.getCityId()+","+bean.getCreateUserId()+","+ bean.getCreateDate()
						+",1,'"+  bean.getDescp()+"',"+ bean.getCollectPlanStartDate()
						+","+ bean.getCollectPlanEndDate()+","+ bean.getDayEditPlanStartDate()
						+","+ bean.getDayEditPlanEndDate()+","+  bean.getBMonthEditPlanStartDate()
						+","+ bean.getBMonthEditPlanEndDate()+","+  bean.getCMonthEditPlanStartDate()
						+","+ bean.getCMonthEditPlanEndDate()+","+  bean.getDayProducePlanStartDate()
						+","+  bean.getDayProducePlanEndDate()+","+ bean.getMonthProducePlanStartDate()
						+","+ bean.getMonthProducePlanEndDate()+",1)";			
				run.update(conn,createSql);
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
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			Task  bean = (Task)JSONObject.toBean(obj, Task.class);	
			
			String updateSql = "update task set TASK_ID=?, CITY_ID=?, CREATE_USER_ID=?, CREATE_DATE=?, STATUS=?, DESCP=?, COLLECT_PLAN_START_DATE=?, COLLECT_PLAN_END_DATE=?, DAY_EDIT_PLAN_START_DATE=?, DAY_EDIT_PLAN_END_DATE=?, B_MONTH_EDIT_PLAN_START_DATE=?, B_MONTH_EDIT_PLAN_END_DATE=?, C_MONTH_EDIT_PLAN_START_DATE=?, C_MONTH_EDIT_PLAN_END_DATE=?, DAY_PRODUCE_PLAN_START_DATE=?, DAY_PRODUCE_PLAN_END_DATE=?, MONTH_PRODUCE_PLAN_START_DATE=?, MONTH_PRODUCE_PLAN_END_DATE=?, LATEST=? where 1=1 TASK_ID=? and CITY_ID=? and CREATE_USER_ID=? and CREATE_DATE=? and STATUS=? and DESCP=? and COLLECT_PLAN_START_DATE=? and COLLECT_PLAN_END_DATE=? and DAY_EDIT_PLAN_START_DATE=? and DAY_EDIT_PLAN_END_DATE=? and B_MONTH_EDIT_PLAN_START_DATE=? and B_MONTH_EDIT_PLAN_END_DATE=? and C_MONTH_EDIT_PLAN_START_DATE=? and C_MONTH_EDIT_PLAN_END_DATE=? and DAY_PRODUCE_PLAN_START_DATE=? and DAY_PRODUCE_PLAN_END_DATE=? and MONTH_PRODUCE_PLAN_START_DATE=? and MONTH_PRODUCE_PLAN_END_DATE=? and LATEST=?";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getTaskId()!=null && StringUtils.isNotEmpty(bean.getTaskId().toString())){
				updateSql+=" and TASK_ID=? ";
				values.add(bean.getTaskId());
			};
			if (bean!=null&&bean.getCityId()!=null && StringUtils.isNotEmpty(bean.getCityId().toString())){
				updateSql+=" and CITY_ID=? ";
				values.add(bean.getCityId());
			};
			if (bean!=null&&bean.getCreateUserId()!=null && StringUtils.isNotEmpty(bean.getCreateUserId().toString())){
				updateSql+=" and CREATE_USER_ID=? ";
				values.add(bean.getCreateUserId());
			};
			if (bean!=null&&bean.getCreateDate()!=null && StringUtils.isNotEmpty(bean.getCreateDate().toString())){
				updateSql+=" and CREATE_DATE=? ";
				values.add(bean.getCreateDate());
			};
			if (bean!=null&&bean.getStatus()!=null && StringUtils.isNotEmpty(bean.getStatus().toString())){
				updateSql+=" and STATUS=? ";
				values.add(bean.getStatus());
			};
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				updateSql+=" and DESCP=? ";
				values.add(bean.getDescp());
			};
			if (bean!=null&&bean.getCollectPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanStartDate().toString())){
				updateSql+=" and COLLECT_PLAN_START_DATE=? ";
				values.add(bean.getCollectPlanStartDate());
			};
			if (bean!=null&&bean.getCollectPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanEndDate().toString())){
				updateSql+=" and COLLECT_PLAN_END_DATE=? ";
				values.add(bean.getCollectPlanEndDate());
			};
			if (bean!=null&&bean.getDayEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanStartDate().toString())){
				updateSql+=" and DAY_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getDayEditPlanStartDate());
			};
			if (bean!=null&&bean.getDayEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanEndDate().toString())){
				updateSql+=" and DAY_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getDayEditPlanEndDate());
			};
			if (bean!=null&&bean.getBMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getBMonthEditPlanStartDate().toString())){
				updateSql+=" and B_MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getBMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getBMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getBMonthEditPlanEndDate().toString())){
				updateSql+=" and B_MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getBMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getCMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCMonthEditPlanStartDate().toString())){
				updateSql+=" and C_MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getCMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getCMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCMonthEditPlanEndDate().toString())){
				updateSql+=" and C_MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getCMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getDayProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanStartDate().toString())){
				updateSql+=" and DAY_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getDayProducePlanStartDate());
			};
			if (bean!=null&&bean.getDayProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanEndDate().toString())){
				updateSql+=" and DAY_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getDayProducePlanEndDate());
			};
			if (bean!=null&&bean.getMonthProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanStartDate().toString())){
				updateSql+=" and MONTH_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getMonthProducePlanStartDate());
			};
			if (bean!=null&&bean.getMonthProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanEndDate().toString())){
				updateSql+=" and MONTH_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getMonthProducePlanEndDate());
			};
			if (bean!=null&&bean.getLatest()!=null && StringUtils.isNotEmpty(bean.getLatest().toString())){
				updateSql+=" and LATEST=? ";
				values.add(bean.getLatest());
			};
			run.update(conn, 
					   updateSql, 
					   bean.getTaskId() ,bean.getCityId(),bean.getCreateUserId(),bean.getCreateDate(),bean.getStatus(),bean.getDescp(),bean.getCollectPlanStartDate(),bean.getCollectPlanEndDate(),bean.getDayEditPlanStartDate(),bean.getDayEditPlanEndDate(),bean.getBMonthEditPlanStartDate(),bean.getBMonthEditPlanEndDate(),bean.getCMonthEditPlanStartDate(),bean.getCMonthEditPlanEndDate(),bean.getDayProducePlanStartDate(),bean.getDayProducePlanEndDate(),bean.getMonthProducePlanStartDate(),bean.getMonthProducePlanEndDate(),bean.getLatest(),
					   values.toArray()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
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
	public Page list(JSONObject json ,final int currentPageNum)throws Exception{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			Task  bean = (Task)JSONObject.toBean(obj, Task.class);
			
			String selectSql = "select * from task where 1=1 ";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getTaskId()!=null && StringUtils.isNotEmpty(bean.getTaskId().toString())){
				selectSql+=" and TASK_ID=? ";
				values.add(bean.getTaskId());
			};
			if (bean!=null&&bean.getCityId()!=null && StringUtils.isNotEmpty(bean.getCityId().toString())){
				selectSql+=" and CITY_ID=? ";
				values.add(bean.getCityId());
			};
			if (bean!=null&&bean.getCreateUserId()!=null && StringUtils.isNotEmpty(bean.getCreateUserId().toString())){
				selectSql+=" and CREATE_USER_ID=? ";
				values.add(bean.getCreateUserId());
			};
			if (bean!=null&&bean.getCreateDate()!=null && StringUtils.isNotEmpty(bean.getCreateDate().toString())){
				selectSql+=" and CREATE_DATE=? ";
				values.add(bean.getCreateDate());
			};
			if (bean!=null&&bean.getStatus()!=null && StringUtils.isNotEmpty(bean.getStatus().toString())){
				selectSql+=" and STATUS=? ";
				values.add(bean.getStatus());
			};
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				selectSql+=" and DESCP=? ";
				values.add(bean.getDescp());
			};
			if (bean!=null&&bean.getCollectPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanStartDate().toString())){
				selectSql+=" and COLLECT_PLAN_START_DATE=? ";
				values.add(bean.getCollectPlanStartDate());
			};
			if (bean!=null&&bean.getCollectPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanEndDate().toString())){
				selectSql+=" and COLLECT_PLAN_END_DATE=? ";
				values.add(bean.getCollectPlanEndDate());
			};
			if (bean!=null&&bean.getDayEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanStartDate().toString())){
				selectSql+=" and DAY_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getDayEditPlanStartDate());
			};
			if (bean!=null&&bean.getDayEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanEndDate().toString())){
				selectSql+=" and DAY_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getDayEditPlanEndDate());
			};
			if (bean!=null&&bean.getBMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getBMonthEditPlanStartDate().toString())){
				selectSql+=" and B_MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getBMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getBMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getBMonthEditPlanEndDate().toString())){
				selectSql+=" and B_MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getBMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getCMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCMonthEditPlanStartDate().toString())){
				selectSql+=" and C_MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getCMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getCMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCMonthEditPlanEndDate().toString())){
				selectSql+=" and C_MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getCMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getDayProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanStartDate().toString())){
				selectSql+=" and DAY_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getDayProducePlanStartDate());
			};
			if (bean!=null&&bean.getDayProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanEndDate().toString())){
				selectSql+=" and DAY_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getDayProducePlanEndDate());
			};
			if (bean!=null&&bean.getMonthProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanStartDate().toString())){
				selectSql+=" and MONTH_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getMonthProducePlanStartDate());
			};
			if (bean!=null&&bean.getMonthProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanEndDate().toString())){
				selectSql+=" and MONTH_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getMonthProducePlanEndDate());
			};
			if (bean!=null&&bean.getLatest()!=null && StringUtils.isNotEmpty(bean.getLatest().toString())){
				selectSql+=" and LATEST=? ";
				values.add(bean.getLatest());
			};
			ResultSetHandler rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List list = new ArrayList();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						HashMap map = new HashMap();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						map.put("taskId", rs.getInt("TASK_ID"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createDate", rs.getObject("CREATE_DATE"));
						map.put("status", rs.getInt("STATUS"));
						map.put("descp", rs.getString("DESCP"));
						map.put("collectPlanStartDate", rs.getObject("COLLECT_PLAN_START_DATE"));
						map.put("collectPlanEndDate", rs.getObject("COLLECT_PLAN_END_DATE"));
						map.put("dayEditPlanStartDate", rs.getObject("DAY_EDIT_PLAN_START_DATE"));
						map.put("dayEditPlanEndDate", rs.getObject("DAY_EDIT_PLAN_END_DATE"));
						map.put("bMonthEditPlanStartDate", rs.getObject("B_MONTH_EDIT_PLAN_START_DATE"));
						map.put("bMonthEditPlanEndDate", rs.getObject("B_MONTH_EDIT_PLAN_END_DATE"));
						map.put("cMonthEditPlanStartDate", rs.getObject("C_MONTH_EDIT_PLAN_START_DATE"));
						map.put("cMonthEditPlanEndDate", rs.getObject("C_MONTH_EDIT_PLAN_END_DATE"));
						map.put("dayProducePlanStartDate", rs.getObject("DAY_PRODUCE_PLAN_START_DATE"));
						map.put("dayProducePlanEndDate", rs.getObject("DAY_PRODUCE_PLAN_END_DATE"));
						map.put("monthProducePlanStartDate", rs.getObject("MONTH_PRODUCE_PLAN_START_DATE"));
						map.put("monthProducePlanEndDate", rs.getObject("MONTH_PRODUCE_PLAN_END_DATE"));
						map.put("latest", rs.getInt("LATEST"));
						list.add(map);
					}
					page.setResult(list);
					return page;
				}
	    		
	    	}	;
			if (values.size()==0){
	    		return run.query(currentPageNum, 20, conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(currentPageNum, 20, conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	public List<HashMap> list(JSONObject json)throws Exception{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
					
			JSONObject obj = JSONObject.fromObject(json);	
			Task  bean = (Task)JSONObject.toBean(obj, Task.class);	
			String selectSql = "select * from task where 1=1 ";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getTaskId()!=null && StringUtils.isNotEmpty(bean.getTaskId().toString())){
				selectSql+=" and TASK_ID=? ";
				values.add(bean.getTaskId());
			};
			if (bean!=null&&bean.getCityId()!=null && StringUtils.isNotEmpty(bean.getCityId().toString())){
				selectSql+=" and CITY_ID=? ";
				values.add(bean.getCityId());
			};
			if (bean!=null&&bean.getCreateUserId()!=null && StringUtils.isNotEmpty(bean.getCreateUserId().toString())){
				selectSql+=" and CREATE_USER_ID=? ";
				values.add(bean.getCreateUserId());
			};
			if (bean!=null&&bean.getCreateDate()!=null && StringUtils.isNotEmpty(bean.getCreateDate().toString())){
				selectSql+=" and CREATE_DATE=? ";
				values.add(bean.getCreateDate());
			};
			if (bean!=null&&bean.getStatus()!=null && StringUtils.isNotEmpty(bean.getStatus().toString())){
				selectSql+=" and STATUS=? ";
				values.add(bean.getStatus());
			};
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				selectSql+=" and DESCP=? ";
				values.add(bean.getDescp());
			};
			if (bean!=null&&bean.getCollectPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanStartDate().toString())){
				selectSql+=" and COLLECT_PLAN_START_DATE=? ";
				values.add(bean.getCollectPlanStartDate());
			};
			if (bean!=null&&bean.getCollectPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanEndDate().toString())){
				selectSql+=" and COLLECT_PLAN_END_DATE=? ";
				values.add(bean.getCollectPlanEndDate());
			};
			if (bean!=null&&bean.getDayEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanStartDate().toString())){
				selectSql+=" and DAY_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getDayEditPlanStartDate());
			};
			if (bean!=null&&bean.getDayEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanEndDate().toString())){
				selectSql+=" and DAY_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getDayEditPlanEndDate());
			};
			if (bean!=null&&bean.getBMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getBMonthEditPlanStartDate().toString())){
				selectSql+=" and B_MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getBMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getBMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getBMonthEditPlanEndDate().toString())){
				selectSql+=" and B_MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getBMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getCMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCMonthEditPlanStartDate().toString())){
				selectSql+=" and C_MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getCMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getCMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCMonthEditPlanEndDate().toString())){
				selectSql+=" and C_MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getCMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getDayProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanStartDate().toString())){
				selectSql+=" and DAY_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getDayProducePlanStartDate());
			};
			if (bean!=null&&bean.getDayProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanEndDate().toString())){
				selectSql+=" and DAY_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getDayProducePlanEndDate());
			};
			if (bean!=null&&bean.getMonthProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanStartDate().toString())){
				selectSql+=" and MONTH_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getMonthProducePlanStartDate());
			};
			if (bean!=null&&bean.getMonthProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanEndDate().toString())){
				selectSql+=" and MONTH_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getMonthProducePlanEndDate());
			};
			if (bean!=null&&bean.getLatest()!=null && StringUtils.isNotEmpty(bean.getLatest().toString())){
				selectSql+=" and LATEST=? ";
				values.add(bean.getLatest());
			};
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						HashMap map = new HashMap();
						map.put("taskId", rs.getInt("TASK_ID"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createDate", rs.getObject("CREATE_DATE"));
						map.put("status", rs.getInt("STATUS"));
						map.put("descp", rs.getString("DESCP"));
						map.put("collectPlanStartDate", rs.getObject("COLLECT_PLAN_START_DATE"));
						map.put("collectPlanEndDate", rs.getObject("COLLECT_PLAN_END_DATE"));
						map.put("dayEditPlanStartDate", rs.getObject("DAY_EDIT_PLAN_START_DATE"));
						map.put("dayEditPlanEndDate", rs.getObject("DAY_EDIT_PLAN_END_DATE"));
						map.put("bMonthEditPlanStartDate", rs.getObject("B_MONTH_EDIT_PLAN_START_DATE"));
						map.put("bMonthEditPlanEndDate", rs.getObject("B_MONTH_EDIT_PLAN_END_DATE"));
						map.put("cMonthEditPlanStartDate", rs.getObject("C_MONTH_EDIT_PLAN_START_DATE"));
						map.put("cMonthEditPlanEndDate", rs.getObject("C_MONTH_EDIT_PLAN_END_DATE"));
						map.put("dayProducePlanStartDate", rs.getObject("DAY_PRODUCE_PLAN_START_DATE"));
						map.put("dayProducePlanEndDate", rs.getObject("DAY_PRODUCE_PLAN_END_DATE"));
						map.put("monthProducePlanStartDate", rs.getObject("MONTH_PRODUCE_PLAN_START_DATE"));
						map.put("monthProducePlanEndDate", rs.getObject("MONTH_PRODUCE_PLAN_END_DATE"));
						map.put("latest", rs.getInt("LATEST"));
						list.add(map);
					}
					return list;
				}
	    		
	    	}		;
	    	if (values.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public HashMap query(JSONObject json)throws Exception{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			Task  bean = (Task)JSONObject.toBean(obj, Task.class);	
			
			String selectSql = "select * from task where TASK_ID=? and CITY_ID=? and CREATE_USER_ID=? and CREATE_DATE=? and STATUS=? and DESCP=? and COLLECT_PLAN_START_DATE=? and COLLECT_PLAN_END_DATE=? and DAY_EDIT_PLAN_START_DATE=? and DAY_EDIT_PLAN_END_DATE=? and B_MONTH_EDIT_PLAN_START_DATE=? and B_MONTH_EDIT_PLAN_END_DATE=? and C_MONTH_EDIT_PLAN_START_DATE=? and C_MONTH_EDIT_PLAN_END_DATE=? and DAY_PRODUCE_PLAN_START_DATE=? and DAY_PRODUCE_PLAN_END_DATE=? and MONTH_PRODUCE_PLAN_START_DATE=? and MONTH_PRODUCE_PLAN_END_DATE=? and LATEST=?";
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>(){
				public HashMap handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						HashMap map = new HashMap();
						map.put("taskId", rs.getInt("TASK_ID"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createDate", rs.getObject("CREATE_DATE"));
						map.put("status", rs.getInt("STATUS"));
						map.put("descp", rs.getString("DESCP"));
						map.put("collectPlanStartDate", rs.getObject("COLLECT_PLAN_START_DATE"));
						map.put("collectPlanEndDate", rs.getObject("COLLECT_PLAN_END_DATE"));
						map.put("dayEditPlanStartDate", rs.getObject("DAY_EDIT_PLAN_START_DATE"));
						map.put("dayEditPlanEndDate", rs.getObject("DAY_EDIT_PLAN_END_DATE"));
						map.put("bMonthEditPlanStartDate", rs.getObject("B_MONTH_EDIT_PLAN_START_DATE"));
						map.put("bMonthEditPlanEndDate", rs.getObject("B_MONTH_EDIT_PLAN_END_DATE"));
						map.put("cMonthEditPlanStartDate", rs.getObject("C_MONTH_EDIT_PLAN_START_DATE"));
						map.put("cMonthEditPlanEndDate", rs.getObject("C_MONTH_EDIT_PLAN_END_DATE"));
						map.put("dayProducePlanStartDate", rs.getObject("DAY_PRODUCE_PLAN_START_DATE"));
						map.put("dayProducePlanEndDate", rs.getObject("DAY_PRODUCE_PLAN_END_DATE"));
						map.put("monthProducePlanStartDate", rs.getObject("MONTH_PRODUCE_PLAN_START_DATE"));
						map.put("monthProducePlanEndDate", rs.getObject("MONTH_PRODUCE_PLAN_END_DATE"));
						map.put("latest", rs.getInt("LATEST"));
						return map;
					}
					return null;
				}
	    		
	    	}		;				
			return run.query(conn, 
					   selectSql,
					   rsHandler, 
					   bean.getTaskId(), bean.getCityId(), bean.getCreateUserId(), bean.getCreateDate(), bean.getStatus(), bean.getDescp(), bean.getCollectPlanStartDate(), bean.getCollectPlanEndDate(), bean.getDayEditPlanStartDate(), bean.getDayEditPlanEndDate(), bean.getBMonthEditPlanStartDate(), bean.getBMonthEditPlanEndDate(), bean.getCMonthEditPlanStartDate(), bean.getCMonthEditPlanEndDate(), bean.getDayProducePlanStartDate(), bean.getDayProducePlanEndDate(), bean.getMonthProducePlanStartDate(), bean.getMonthProducePlanEndDate(), bean.getLatest());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
