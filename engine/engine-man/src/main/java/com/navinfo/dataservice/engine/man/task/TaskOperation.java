package com.navinfo.dataservice.engine.man.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.json.DateJsonValueProcessor;
import com.navinfo.dataservice.commons.json.TimestampMorpher;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.city.CityOperation;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JSONUtils;

public class TaskOperation {
	private static Logger log = LoggerRepos.getLogger(TaskOperation.class);
	
	public TaskOperation() {
		// TODO Auto-generated constructor stub
	}
	
	public static Task jsonToBean(JSONObject json){
		String[] formats={"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd"};  
		JSONUtils.getMorpherRegistry().registerMorpher(new TimestampMorpher(formats));  
		JSONObject taskJson=JSONObject.fromObject(json); 
		
		Task  bean = (Task)JSONObject.toBean(taskJson, Task.class);
		return bean;
		}
	
	public static String beanToJson(Task s){  
	    JsonConfig config=new JsonConfig();  
	    config.registerJsonValueProcessor(Timestamp.class, new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));  
	    JSONObject json=JSONObject.fromObject(s,config);  
	    return json.toString();
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
	public static List<HashMap> selectTaskBySql(Connection conn,String selectSql,List<Object> values) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
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
	    	if (null==values || values.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray()
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
	public static Page selectTaskBySql(Connection conn,String selectSql,List<Object> values,final int currentPageNum) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
				    Page page = new Page(currentPageNum);
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
					page.setResult(list);
					return page;
				}
	    		
	    	}		;
	    	if (null==values || values.size()==0){
	    		return run.query(currentPageNum, 20, conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(currentPageNum, 20, conn, selectSql, rsHandler,values.toArray()
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
			String createSql = "insert into task (TASK_ID,CITY_ID, CREATE_USER_ID, CREATE_DATE, STATUS, DESCP, "
					+ "COLLECT_PLAN_START_DATE, COLLECT_PLAN_END_DATE, DAY_EDIT_PLAN_START_DATE, "
					+ "DAY_EDIT_PLAN_END_DATE, B_MONTH_EDIT_PLAN_START_DATE, B_MONTH_EDIT_PLAN_END_DATE, "
					+ "C_MONTH_EDIT_PLAN_START_DATE, C_MONTH_EDIT_PLAN_END_DATE, DAY_PRODUCE_PLAN_START_DATE, "
					+ "DAY_PRODUCE_PLAN_END_DATE, MONTH_PRODUCE_PLAN_START_DATE, MONTH_PRODUCE_PLAN_END_DATE, "
					+ "LATEST) "
					+ "values(TASK_SEQ.NEXTVAL,"+bean.getCityId()+","+bean.getCreateUserId()+",sysdate,1,'"
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
