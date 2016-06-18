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

import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

public class TaskOperation {
	private static Logger log = LoggerRepos.getLogger(TaskOperation.class);
	
	public TaskOperation() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * task的latest字段，修改成无效，0
	 */
	public static void updateLatest(Connection conn,int cityId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String updateCity="UPDATE TASK SET LATEST=0 WHERE LATEST=1 AND CITY_ID="+cityId;
			run.update(conn,updateCity);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/*
	 * 根据sql语句查询task
	 */
	public static Page selectTaskBySql(Connection conn,String selectSql,List<Object> values,final int currentPageNum,final int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
				    Page page = new Page(currentPageNum);
				    page.setPageSize(pageSize);
					while(rs.next()){
						HashMap map = new HashMap();
						map.put("taskId", rs.getInt("TASK_ID"));
						map.put("name", rs.getString("NAME"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
						map.put("createDate", DateUtils.dateToString(rs.getTimestamp("CREATE_DATE")));
						map.put("status", rs.getInt("STATUS"));
						map.put("descp", rs.getString("DESCP"));
						map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
						map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
						map.put("monthEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
						map.put("monthEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
						map.put("monthEditGroupId", rs.getInt("MONTH_EDIT_GROUP_ID"));
						map.put("latest", rs.getInt("LATEST"));
						list.add(map);
					}
					//page.setTotalCount(list.size());
					page.setResult(list);
					return page;
				}
	    		
	    	}		;
	    	if (null==values || values.size()==0){
	    		return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/*
	 * 根据sql语句查询task
	 */
	public static Page selectTaskBySql2(Connection conn,String selectSql,List<Object> values,final int currentPageNum,final int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<Task> list = new ArrayList<Task>();
				    Page page = new Page(currentPageNum);
				    page.setPageSize(pageSize);
					while(rs.next()){
						Task map = new Task();
						map.setTaskId(rs.getInt("TASK_ID"));
						map.setCityName(rs.getString("CITY_NAME"));
						map.setName(rs.getString("NAME"));
						map.setCityId(rs.getInt("CITY_ID"));
						map.setCreateUserId(rs.getInt("CREATE_USER_ID"));
						map.setCreateUserName(rs.getString("USER_REAL_NAME"));
						map.setCreateDate(rs.getTimestamp("CREATE_DATE"));
						map.setStatus(rs.getInt("STATUS"));
						map.setDescp(rs.getString("DESCP"));
						map.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						map.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						map.setMonthEditPlanStartDate(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE"));
						map.setMonthEditPlanEndDate(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE"));
						map.setMonthEditGroupId(rs.getInt("MONTH_EDIT_GROUP_ID"));
						map.setLatest(rs.getInt("LATEST"));
						list.add(map);
					}
					//page.setTotalCount(list.size());
					page.setResult(list);
					return page;
				}
	    		
	    	}		;
	    	if (null==values || values.size()==0){
	    		return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
		
	public static void insertTask(Connection conn,Task bean) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String createSql = "insert into task (TASK_ID,NAME,CITY_ID, CREATE_USER_ID, CREATE_DATE, STATUS, DESCP, "
					+ "PLAN_START_DATE, PLAN_END_DATE, MONTH_EDIT_PLAN_START_DATE, MONTH_EDIT_PLAN_END_DATE, "
					+ "MONTH_EDIT_GROUP_ID,LATEST) "
					+ "values(TASK_SEQ.NEXTVAL,'"+bean.getName()+"',"+bean.getCityId()+","+bean.getCreateUserId()+",sysdate,1,'"
					+  bean.getDescp()+"',to_timestamp('"+ bean.getPlanStartDate()
					+"','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"+ bean.getPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"+  bean.getMonthEditPlanStartDate()
					+"','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"+ bean.getMonthEditPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),"+  bean.getMonthEditGroupId()+",1)";
			
			run.update(conn,createSql);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
		
	public static void updateTask(Connection conn,Task bean) throws Exception{
		try{
			String baseSql = "update task set ";
			QueryRunner run = new QueryRunner();
			String updateSql="";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" DESCP=? ";
				values.add(bean.getDescp());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" PLAN_START_DATE=? ";
				values.add(bean.getPlanStartDate());
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" PLAN_END_DATE=? ";
				values.add(bean.getPlanEndDate());
			};
			if (bean!=null&&bean.getMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getMonthEditPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getMonthEditPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getMonthEditGroupId()!=null && StringUtils.isNotEmpty(bean.getMonthEditGroupId().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getMonthEditGroupId());
			};
			if (bean!=null&&bean.getTaskId()!=null && StringUtils.isNotEmpty(bean.getTaskId().toString())){
				updateSql+=" where TASK_ID=?";
				values.add(bean.getTaskId());
			};
			run.update(conn,baseSql+updateSql,values.toArray());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}

}
