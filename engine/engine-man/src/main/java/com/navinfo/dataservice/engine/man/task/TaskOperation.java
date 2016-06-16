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
						map.put("collectPlanStartDate", DateUtils.dateToString(rs.getTimestamp("COLLECT_PLAN_START_DATE")));
						map.put("collectPlanEndDate", DateUtils.dateToString(rs.getTimestamp("COLLECT_PLAN_END_DATE")));
						map.put("dayEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE")));
						map.put("dayEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE")));
						map.put("bMonthEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("B_MONTH_EDIT_PLAN_START_DATE")));
						map.put("bMonthEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("B_MONTH_EDIT_PLAN_END_DATE")));
						map.put("cMonthEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("C_MONTH_EDIT_PLAN_START_DATE")));
						map.put("cMonthEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("C_MONTH_EDIT_PLAN_END_DATE")));
						map.put("dayProducePlanStartDate", DateUtils.dateToString(rs.getTimestamp("DAY_PRODUCE_PLAN_START_DATE")));
						map.put("dayProducePlanEndDate", DateUtils.dateToString(rs.getTimestamp("DAY_PRODUCE_PLAN_END_DATE")));
						map.put("monthProducePlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_PRODUCE_PLAN_START_DATE")));
						map.put("monthProducePlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_PRODUCE_PLAN_END_DATE")));
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
						map.setName(rs.getString("NAME"));
						map.setCityId(rs.getInt("CITY_ID"));
						map.setCreateUserId(rs.getInt("CREATE_USER_ID"));
						map.setCreateDate(rs.getTimestamp("CREATE_DATE"));
						map.setStatus(rs.getInt("STATUS"));
						map.setDescp(rs.getString("DESCP"));
						map.setCollectPlanStartDate(rs.getTimestamp("COLLECT_PLAN_START_DATE"));
						map.setCollectPlanEndDate(rs.getTimestamp("COLLECT_PLAN_END_DATE"));
						map.setDayEditPlanStartDate(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE"));
						map.setDayEditPlanEndDate(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE"));
						map.setBMonthEditPlanStartDate(rs.getTimestamp("B_MONTH_EDIT_PLAN_START_DATE"));
						map.setBMonthEditPlanEndDate(rs.getTimestamp("B_MONTH_EDIT_PLAN_END_DATE"));
						map.setCMonthEditPlanStartDate(rs.getTimestamp("C_MONTH_EDIT_PLAN_START_DATE"));
						map.setCMonthEditPlanEndDate(rs.getTimestamp("C_MONTH_EDIT_PLAN_END_DATE"));
						map.setDayEditPlanStartDate(rs.getTimestamp("DAY_PRODUCE_PLAN_START_DATE"));
						map.setDayEditPlanEndDate(rs.getTimestamp("DAY_PRODUCE_PLAN_END_DATE"));
						map.setMonthProducePlanStartDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_START_DATE"));
						map.setMonthProducePlanEndDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_END_DATE"));
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
					+ "COLLECT_PLAN_START_DATE, COLLECT_PLAN_END_DATE, DAY_EDIT_PLAN_START_DATE, "
					+ "DAY_EDIT_PLAN_END_DATE, B_MONTH_EDIT_PLAN_START_DATE, B_MONTH_EDIT_PLAN_END_DATE, "
					+ "C_MONTH_EDIT_PLAN_START_DATE, C_MONTH_EDIT_PLAN_END_DATE, DAY_PRODUCE_PLAN_START_DATE, "
					+ "DAY_PRODUCE_PLAN_END_DATE, MONTH_PRODUCE_PLAN_START_DATE, MONTH_PRODUCE_PLAN_END_DATE, "
					+ "LATEST) "
					+ "values(TASK_SEQ.NEXTVAL,'"+bean.getName()+"',"+bean.getCityId()+","+bean.getCreateUserId()+",sysdate,1,'"
					+  bean.getDescp()+"',"+ bean.getCollectPlanStartDate()
					+","+ bean.getCollectPlanEndDate()+","+ bean.getDayEditPlanStartDate()
					+","+ bean.getDayEditPlanEndDate()+","+  bean.getBMonthEditPlanStartDate()
					+","+ bean.getBMonthEditPlanEndDate()+","+  bean.getCMonthEditPlanStartDate()
					+","+ bean.getCMonthEditPlanEndDate()+","+  bean.getDayProducePlanStartDate()
					+","+  bean.getDayProducePlanEndDate()+","+ bean.getMonthProducePlanStartDate()
					+","+ bean.getMonthProducePlanEndDate()+",1)";
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
			if (bean!=null&&bean.getCollectPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" COLLECT_PLAN_START_DATE=? ";
				values.add(bean.getCollectPlanStartDate());
			};
			if (bean!=null&&bean.getCollectPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" COLLECT_PLAN_END_DATE=? ";
				values.add(bean.getCollectPlanEndDate());
			};
			if (bean!=null&&bean.getDayEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" DAY_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getDayEditPlanStartDate());
			};
			if (bean!=null&&bean.getDayEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" DAY_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getDayEditPlanEndDate());
			};
			if (bean!=null&&bean.getBMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getBMonthEditPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" B_MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getBMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getBMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getBMonthEditPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" B_MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getBMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getCMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCMonthEditPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" C_MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getCMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getCMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCMonthEditPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" C_MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getCMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getDayProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" DAY_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getDayProducePlanStartDate());
			};
			if (bean!=null&&bean.getDayProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" DAY_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getDayProducePlanEndDate());
			};
			if (bean!=null&&bean.getMonthProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" MONTH_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getMonthProducePlanStartDate());
			};
			if (bean!=null&&bean.getMonthProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" MONTH_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getMonthProducePlanEndDate());
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
