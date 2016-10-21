package com.navinfo.dataservice.engine.man.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

public class TaskOperation {
	private static Logger log = LoggerRepos.getLogger(TaskOperation.class);
	
	public TaskOperation() {
		// TODO Auto-generated constructor stub
	}
	
	public static int getNewTaskId(Connection conn) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			String querySql = "select TASK_SEQ.NEXTVAL as taskId from dual";

			int taskId = Integer.valueOf(run
					.query(conn, querySql, new MapHandler()).get("taskId")
					.toString());
			return taskId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
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
	 * task的status字段，修改成开启
	 */
	public static void updateStatus(Connection conn,JSONArray taskIds) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String updateSql="UPDATE TASK SET STATUS=1 WHERE TASK_ID IN ("+taskIds.join(",")+")";
			run.update(conn,updateSql);			
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
				    int total=0;
					while(rs.next()){
						Task map = new Task();
						map.setTaskId(rs.getInt("TASK_ID"));
						map.setCityName(rs.getString("CITY_NAME"));
						map.setTaskName(rs.getString("NAME"));
						map.setCityId(rs.getInt("CITY_ID"));
						map.setCreateUserId(rs.getInt("CREATE_USER_ID"));
						map.setCreateUserName(rs.getString("USER_REAL_NAME"));
						map.setCreateDate(rs.getTimestamp("CREATE_DATE"));
						map.setTaskStatus(rs.getInt("STATUS"));
						map.setTaskDescp(rs.getString("DESCP"));
						map.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						map.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						map.setMonthEditPlanStartDate(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE"));
						map.setMonthEditPlanEndDate(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE"));
						map.setMonthEditGroupId(rs.getInt("MONTH_EDIT_GROUP_ID"));
						map.setMonthEditGroupName(rs.getString("GROUP_NAME"));
						map.setLatest(rs.getInt("LATEST"));
						if(total==0){total=rs.getInt("TOTAL_RECORD_NUM_");}
						list.add(map);
					}
					page.setTotalCount(total);
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
	public static List<Task> selectTaskBySql2(Connection conn,String selectSql,List<Object> values) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<Task>> rsHandler = new ResultSetHandler<List<Task>>(){
				public List<Task> handle(ResultSet rs) throws SQLException {
					List<Task> list = new ArrayList<Task>();
					while(rs.next()){
						Task map = new Task();
						map.setTaskId(rs.getInt("TASK_ID"));
						map.setCityId(rs.getInt("CITY_ID"));
						map.setCreateUserId(rs.getInt("CREATE_USER_ID"));
						map.setCreateDate(rs.getTimestamp("CREATE_DATE"));
						map.setTaskStatus(rs.getInt("STATUS"));
						map.setTaskName(rs.getString("NAME"));
						map.setTaskDescp(rs.getString("DESCP"));
						map.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						map.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						map.setMonthEditPlanStartDate(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE"));
						map.setMonthEditPlanEndDate(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE"));
						map.setLatest(rs.getInt("LATEST"));
						map.setMonthProducePlanStartDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_START_DATE"));
						map.setMonthProducePlanEndDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_END_DATE"));
						map.setTaskType(rs.getInt("TASK_TYPE"));
						//map.setMonthEditGroupId(rs.getInt("MONTH_EDIT_GROUP_ID"));
						//map.setCityName(rs.getString("CITY_NAME"));
						//map.setCreateUserName(rs.getString("USER_REAL_NAME"));
						//map.setMonthEditGroupName(rs.getString("GROUP_NAME"));
						list.add(map);
					}
					return list;
				}
	    	}		;
	    	if (null==values || values.size()==0){
	    		return run.query( conn, selectSql, rsHandler);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
		
	public static void insertTask(Connection conn,Task bean) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String taskIdStr="TASK_SEQ.NEXTVAL";
			if(bean.getTaskId()!=null && bean.getTaskId()!=0){
				taskIdStr=bean.getTaskId().toString();
			}
			String createSql = "insert into task (TASK_ID,NAME,CITY_ID, CREATE_USER_ID, CREATE_DATE, STATUS, DESCP, "
					+ "PLAN_START_DATE, PLAN_END_DATE, MONTH_EDIT_PLAN_START_DATE, MONTH_EDIT_PLAN_END_DATE, "
					+ "MONTH_EDIT_GROUP_ID,TASK_TYPE,LATEST) "
					+ "values("+taskIdStr+",'"+bean.getTaskName()+"',"+bean.getCityId()+","+bean.getCreateUserId()+",sysdate,2,'"
					+  bean.getTaskDescp()+"',to_timestamp('"+ bean.getPlanStartDate()
					+"','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"+ bean.getPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"+  bean.getMonthEditPlanStartDate()
					+"','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"+ bean.getMonthEditPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),"+  bean.getMonthEditGroupId()
					+","+bean.getTaskType()+",1)";
			
			run.update(conn,createSql);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static Page getCommonUnPushListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			/* 记录默认排序原则：根据城市名称排序
			 * 点击搜索，根据城市名称\任务名称模糊查询
			 * • 点击筛选，根据任务状态筛选，可多选
			 * • 点击更多，跳转到<全国任务详情列表>页面，可进行批量操作
			 */
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (TASK_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("taskStatus".equals(key)) {
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" TASK_LIST.TASK_STATUS="+conditionJson.getInt(key);}
					if ("cityPlanStatus".equals(key)) {
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" TASK_LIST.CITY_PLAN_STATUS="+conditionJson.getInt(key);}}
			}	
			if(!statusSql.isEmpty()){//有非status
				conditionSql+=" and ("+statusSql+")";}
			
			//分页显示列表，不带条件查询
			String selectSql = "WITH TASK_LIST AS"
					+ " (SELECT T.TASK_ID,"
					+ "         T.NAME TASK_NAME,"
					+ "         C.CITY_ID,"
					+ "         C.CITY_NAME,"
					+ "         C.PLAN_STATUS CITY_PLAN_STATUS,"
					+ "         T.STATUS TASK_STATUS, "
					+ "         T.DESCP TASK_DESCP, "
					+ "         1 TASK_TYPE"
					+ "    FROM TASK T, CITY C"
					+ "   WHERE T.CITY_ID=C.CITY_ID"
					+ "   AND C.CITY_ID NOT IN (100000,100001,100002)"
					+ "   AND T.STATUS=2"
					+ "   AND T.LATEST=1"
					+ "  UNION"
					+ "  SELECT 0,"
					+ "         '---',"
					+ "         C.CITY_ID,"
					+ "         C.CITY_NAME,"
					+ "         C.PLAN_STATUS,"
					+ "         0,"
					+ "         '---' TASK_DESCP, "
					+ "         1 TASK_TYPE"
					+ "    FROM CITY C"
					+ "   WHERE C.CITY_ID NOT IN (100000,100001,100002)"
					+ "   AND C.PLAN_STATUS=0),"
					+ " FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM TASK_LIST"
					+ "    WHERE 1=1"
					+  conditionSql
					+ "   ORDER BY TASK_LIST.CITY_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			//System.out.println(selectSql);
			return run.query(conn, selectSql, getUnPushSnapshotQuery(currentPageNum,pageSize));		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static Page getCommonPushListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			/* 记录默认排序原则：
			 * ①根据剩余工期排序，逾期>剩余
			 * ②相同剩余工期，根据完成度排序，完成度高>完成度低
			 * • 点击搜索，根据城市名称\任务名称模糊查询
			 * • 点击筛选，根据分类筛选，可多选 采集/日编 正常/异常/完成
			 */
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (TASK_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("collectProgress".equals(key)) {
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" TASK_LIST.collect_Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}
					if ("dailyProgress".equals(key)) {
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" TASK_LIST.daily_Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}}
			}	
			if(!statusSql.isEmpty()){//有非status
				conditionSql+=" and ("+statusSql+")";}
			
			//分页显示列表，不带条件查询
			String selectSql = "WITH TASK_LIST AS"
					+ " (SELECT T.TASK_ID,"
					+ "       T.NAME        TASK_NAME,"
					+ "       C.CITY_ID,"
					+ "       C.CITY_NAME,"
					+ "       C.PLAN_STATUS,"
					+ "       T.STATUS      TASK_STATUS, "
					+ "       T.DESCP TASK_DESCP, "
					+ "       1 TASK_TYPE,"
					+ "       NVL(S.PERCENT,0) PERCENT,"
					+ "       NVL(S.DIFF_DATE,0) DIFF_DATE,"
					+ "       NVL(S.PROGRESS,0) PROGRESS,"
					+ "       NVL(S.COLLECT_PROGRESS,0) COLLECT_PROGRESS,"
					+ "       NVL(S.COLLECT_PERCENT,0) COLLECT_PERCENT,"
					+ "       NVL(S.DAILY_PROGRESS,0) DAILY_PROGRESS,"
					+ "       NVL(S.DAILY_PERCENT,0) DAILY_PERCENT"
					+ "  FROM TASK T, CITY C, FM_STAT_OVERVIEW_TASK S"
					+ " WHERE T.CITY_ID = C.CITY_ID"
					+ "   AND C.CITY_ID NOT IN (100000, 100001, 100002)"
					+ "   AND T.TASK_ID = S.TASK_ID(+)"
					+ "   AND T.STATUS = 1"
					+ "   AND T.LATEST = 1"
					+ "   AND (EXISTS(SELECT 1"
					+ "                    FROM BLOCK_MAN M"
					+ "                   WHERE M.TASK_ID = T.TASK_ID"
					+ "                     AND M.STATUS <> 0)"
					+ "      OR NOT EXISTS(SELECT 1 FROM BLOCK_MAN M"
					+ "                   WHERE M.TASK_ID = T.TASK_ID))),"
					+ " FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM TASK_LIST"
					+ "    WHERE 1=1"
					+  conditionSql
					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.PERCENT DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			//System.out.println(selectSql);
			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static Page getCommonOverListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			/* 
			 * 记录默认排序原则：
			 * ①根据状态排序，100%>已关闭
			 * ②根据剩余工期排序，逾期>按时>提前
			 * ③根据任务名称排序
			 * 
			 * • 点击搜索，根据城市名称\任务名称模糊查询
			 * • 点击筛选，根据分类筛选，可多选
			 */
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (TASK_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("diffDate".equals(key)) {
						JSONArray diffDateArray=conditionJson.getJSONArray(key);
						for(Object diffDate:diffDateArray){
							if((int) diffDate==1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date>0";
							}
							if((int) diffDate==0){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date=0";
							}
							if((int) diffDate==-1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date<0";
							}
							}
						}
				}	
				if(!statusSql.isEmpty()){//有非status
					conditionSql+=" and ("+statusSql+")";}	
			}			
			
			//分页显示列表，不带条件查询
			String selectSql = "WITH TASK_LIST AS"
					+ " (SELECT T.TASK_ID,"
					+ "       T.NAME        TASK_NAME,"
					+ "       C.CITY_ID,"
					+ "       C.CITY_NAME,"
					+ "       C.PLAN_STATUS,"
					+ "       T.STATUS      TASK_STATUS, "
					+ "       T.DESCP TASK_DESCP, "
					+ "       1 TASK_TYPE,"
					+ "       S.PERCENT,"
					+ "       S.DIFF_DATE,"
					+ "       S.PROGRESS,"
					+ "       S.COLLECT_PROGRESS,"
					+ "       S.COLLECT_PERCENT,"
					+ "       S.DAILY_PROGRESS,"
					+ "       S.DAILY_PERCENT"
					+ "  FROM TASK T, CITY C, FM_STAT_OVERVIEW_TASK S,BLOCK_MAN BM"
					+ " WHERE T.CITY_ID = C.CITY_ID"
					+ "   AND C.CITY_ID NOT IN (100000, 100001, 100002)"
					+ "   AND T.TASK_ID = S.TASK_ID(+)"
					+ "   AND T.TASK_ID = BM.TASK_ID"
					//+ "   AND S.PERCENT = 100"
					+ "   AND T.STATUS = 1"
					+ "   AND T.LATEST = 1"
					+ "   AND NOT EXISTS(SELECT 1"
					+ "                    FROM BLOCK_MAN M"
					+ "                   WHERE M.TASK_ID = T.TASK_ID"
					+ "                     AND M.LATEST = 1"
					+ "                     AND M.STATUS <> 0)),"
					+ " FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM TASK_LIST"
					+ "    WHERE 1=1"
					+  conditionSql
					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.TASK_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			//System.out.println(selectSql);
			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static Page getCommonCloseListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			/* 
			 * 记录默认排序原则：
			 * ①根据状态排序，100%>已关闭
			 * ②根据剩余工期排序，逾期>按时>提前
			 * ③根据任务名称排序
			 * 
			 * • 点击搜索，根据城市名称\任务名称模糊查询
			 * • 点击筛选，根据分类筛选，可多选
			 */
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (TASK_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("diffDate".equals(key)) {
						JSONArray diffDateArray=conditionJson.getJSONArray(key);
						for(Object diffDate:diffDateArray){
							if((int) diffDate==1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date>0";
							}
							if((int) diffDate==0){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date=0";
							}
							if((int) diffDate==-1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date<0";
							}
							}
						}
				}	
				if(!statusSql.isEmpty()){//有非status
					conditionSql+=" and ("+statusSql+")";}	
			}			
			
			//分页显示列表，不带条件查询
			String selectSql = "WITH TASK_LIST AS"
					+ " (SELECT T.TASK_ID,"
					+ "       T.NAME        TASK_NAME,"
					+ "       C.CITY_ID,"
					+ "       C.CITY_NAME,"
					+ "       C.PLAN_STATUS,"
					+ "       T.STATUS      TASK_STATUS, "
					+ "       T.DESCP TASK_DESCP, "
					+ "       1 TASK_TYPE,"
					+ "       S.PERCENT,"
					+ "       S.DIFF_DATE,"
					+ "       S.PROGRESS,"
					+ "       S.COLLECT_PROGRESS,"
					+ "       S.COLLECT_PERCENT,"
					+ "       S.DAILY_PROGRESS,"
					+ "       S.DAILY_PERCENT"
					+ "  FROM TASK T, CITY C, FM_STAT_OVERVIEW_TASK S"
					+ " WHERE T.CITY_ID = C.CITY_ID"
					+ "   AND C.CITY_ID NOT IN (100000, 100001, 100002)"
					+ "   AND T.TASK_ID = S.TASK_ID(+)"
					+ "   AND T.STATUS = 0"
					+ "   AND T.LATEST = 1),"
					+ " FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM TASK_LIST"
					+ "    WHERE 1=1"
					+  conditionSql
					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.TASK_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			//System.out.println(selectSql);
			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static Page getInforUnPushListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			/* 记录默认排序原则：根据城市名称排序
			 * 点击搜索，根据城市名称\任务名称模糊查询
			 * • 点击筛选，根据任务状态筛选，可多选
			 * • 点击更多，跳转到<全国任务详情列表>页面，可进行批量操作
			 */
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (TASK_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("taskStatus".equals(key)) {
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" TASK_LIST.TASK_STATUS="+conditionJson.getInt(key);}
					if ("blockPlanStatus".equals(key)) {
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" TASK_LIST.INFOR_PLAN_STATUS="+conditionJson.getInt(key);}}
			}	
			if(!statusSql.isEmpty()){//有非status
				conditionSql+=" and ("+statusSql+")";}
			
			//分页显示列表，不带条件查询
			String selectSql = "WITH TASK_LIST AS"
					+ " (SELECT T.TASK_ID,"
					+ "         T.NAME TASK_NAME,"
					+ "         C.INFOR_ID,"
					+ "         C.INFOR_NAME,"
					+ "         C.PLAN_STATUS INFOR_PLAN_STATUS,"
					+ "         T.STATUS TASK_STATUS, "
					+ "         T.DESCP TASK_DESCP, "
					+ "         4 TASK_TYPE"
					+ "    FROM TASK T, INFOR C"
					+ "   WHERE T.TASK_ID=C.TASK_ID"
					+ "   AND T.STATUS=2"
					+ "   AND T.LATEST=1"
					+ "  UNION"
					+ "  SELECT 0,"
					+ "         '---',"
					+ "         C.INFOR_ID,"
					+ "         C.INFOR_NAME,"
					+ "         C.PLAN_STATUS,"
					+ "         0,"
					+ "         '---' TASK_DESCP, "
					+ "         4 TASK_TYPE"
					+ "    FROM INFOR C"
					+ "   WHERE C.PLAN_STATUS=0),"
					+ " FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM TASK_LIST"
					+ "    WHERE 1=1"
					+  conditionSql
					+ "   ORDER BY TASK_LIST.INFOR_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			//System.out.println(selectSql);
			return run.query(conn, selectSql, getUnPushSnapshotQuery(currentPageNum,pageSize));		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static Page getInforPushListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			/* 记录默认排序原则：
			 * ①根据剩余工期排序，逾期>剩余
			 * ②相同剩余工期，根据完成度排序，完成度高>完成度低
			 * • 点击搜索，根据城市名称\任务名称模糊查询
			 * • 点击筛选，根据分类筛选，可多选 采集/日编 正常/异常/完成
			 */
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (TASK_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("collectProgress".equals(key)) {
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" TASK_LIST.collect_Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}
					if ("dailyProgress".equals(key)) {
						if(!statusSql.isEmpty()){statusSql+=" or ";}
						statusSql+=" TASK_LIST.daily_Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}}
			}	
			if(!statusSql.isEmpty()){//有非status
				conditionSql+=" and ("+statusSql+")";}
			
			//分页显示列表，不带条件查询
			String selectSql = "WITH TASK_LIST AS"
					+ " (SELECT T.TASK_ID,"
					+ "       T.NAME        TASK_NAME,"
					+ "       C.INFOR_ID,"
					+ "       C.INFOR_NAME,"
					+ "       C.PLAN_STATUS,"
					+ "       T.STATUS      TASK_STATUS, "
					+ "       T.DESCP TASK_DESCP, "
					+ "       4 TASK_TYPE,"
					+ "       NVL(S.PERCENT,0) PERCENT,"
					+ "       NVL(S.DIFF_DATE,0) DIFF_DATE,"
					+ "       NVL(S.PROGRESS,0) PROGRESS,"
					+ "       NVL(S.COLLECT_PROGRESS,0) COLLECT_PROGRESS,"
					+ "       NVL(S.COLLECT_PERCENT,0) COLLECT_PERCENT,"
					+ "       NVL(S.DAILY_PROGRESS,0) DAILY_PROGRESS,"
					+ "       NVL(S.DAILY_PERCENT,0) DAILY_PERCENT"
					+ "  FROM TASK T, INFOR C, FM_STAT_OVERVIEW_TASK S"
					+ " WHERE T.TASK_ID = C.TASK_ID"
					+ "   AND T.TASK_ID = S.TASK_ID(+)"
					+ "   AND T.STATUS = 1"
					+ "   AND T.LATEST = 1"
					+ "   AND (EXISTS(SELECT 1"
					+ "                    FROM BLOCK_MAN M"
					+ "                   WHERE M.TASK_ID = T.TASK_ID"
					+ "                     AND M.STATUS <> 0)"
					+ "      OR NOT EXISTS(SELECT 1 FROM BLOCK_MAN M"
					+ "                   WHERE M.TASK_ID = T.TASK_ID))),"
					+ " FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM TASK_LIST"
					+ "    WHERE 1=1"
					+  conditionSql
					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.PERCENT DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			//System.out.println(selectSql);
			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static Page getInforOverListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			/* 
			 * 记录默认排序原则：
			 * ①根据状态排序，100%>已关闭
			 * ②根据剩余工期排序，逾期>按时>提前
			 * ③根据任务名称排序
			 * 
			 * • 点击搜索，根据城市名称\任务名称模糊查询
			 * • 点击筛选，根据分类筛选，可多选
			 */
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (TASK_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("diffDate".equals(key)) {
						JSONArray diffDateArray=conditionJson.getJSONArray(key);
						for(Object diffDate:diffDateArray){
							if((int) diffDate==1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date>0";
							}
							if((int) diffDate==0){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date=0";
							}
							if((int) diffDate==-1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date<0";
							}
							}
						}
				}	
				if(!statusSql.isEmpty()){//有非status
					conditionSql+=" and ("+statusSql+")";}	
			}			
			
			//分页显示列表，不带条件查询
			String selectSql = "WITH TASK_LIST AS"
					+ " (SELECT T.TASK_ID,"
					+ "       T.NAME        TASK_NAME,"
					+ "       C.INFOR_ID,"
					+ "       C.INFOR_NAME,"
					+ "       C.PLAN_STATUS,"
					+ "       T.STATUS      TASK_STATUS, "
					+ "       T.DESCP TASK_DESCP, "
					+ "       4 TASK_TYPE,"
					+ "       S.PERCENT,"
					+ "       S.DIFF_DATE,"
					+ "       S.PROGRESS,"
					+ "       S.COLLECT_PROGRESS,"
					+ "       S.COLLECT_PERCENT,"
					+ "       S.DAILY_PROGRESS,"
					+ "       S.DAILY_PERCENT"
					+ "  FROM TASK T, INFOR C, FM_STAT_OVERVIEW_TASK S,BLOCK_MAN BM"
					+ " WHERE T.task_ID = C.TASK_ID"
					+ "   AND T.TASK_ID = S.TASK_ID(+)"
					+ "   AND T.TASK_ID=BM.TASK_ID"
					//+ "   AND S.PERCENT = 100"
					+ "   AND T.STATUS = 1"
					+ "   AND T.LATEST = 1"
					+ "   AND NOT EXISTS(SELECT 1"
					+ "                    FROM BLOCK_MAN M"
					+ "                   WHERE M.TASK_ID = T.TASK_ID"
					+ "                     AND M.LATEST = 1"
					+ "                     AND M.STATUS <> 0)),"
					+ " FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM TASK_LIST"
					+ "    WHERE 1=1"
					+  conditionSql
					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.TASK_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			//System.out.println(selectSql);
			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static Page getInforCloseListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			/* 
			 * 记录默认排序原则：
			 * ①根据状态排序，100%>已关闭
			 * ②根据剩余工期排序，逾期>按时>提前
			 * ③根据任务名称排序
			 * 
			 * • 点击搜索，根据城市名称\任务名称模糊查询
			 * • 点击筛选，根据分类筛选，可多选
			 */
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("name".equals(key)){
						conditionSql=conditionSql+" AND (TASK_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
					if ("diffDate".equals(key)) {
						JSONArray diffDateArray=conditionJson.getJSONArray(key);
						for(Object diffDate:diffDateArray){
							if((int) diffDate==1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date>0";
							}
							if((int) diffDate==0){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date=0";
							}
							if((int) diffDate==-1){
								if(!statusSql.isEmpty()){statusSql+=" or ";}
								statusSql+=" TASK_LIST.diff_date<0";
							}
							}
						}
				}	
				if(!statusSql.isEmpty()){//有非status
					conditionSql+=" and ("+statusSql+")";}	
			}			
			
			//分页显示列表，不带条件查询
			String selectSql = "WITH TASK_LIST AS"
					+ " (SELECT T.TASK_ID,"
					+ "       T.NAME        TASK_NAME,"
					+ "       C.INFOR_ID,"
					+ "       C.INFOR_NAME,"
					+ "       C.PLAN_STATUS,"
					+ "       T.STATUS      TASK_STATUS, "
					+ "       T.DESCP TASK_DESCP, "
					+ "       4 TASK_TYPE,"
					+ "       S.PERCENT,"
					+ "       S.DIFF_DATE,"
					+ "       S.PROGRESS,"
					+ "       S.COLLECT_PROGRESS,"
					+ "       S.COLLECT_PERCENT,"
					+ "       S.DAILY_PROGRESS,"
					+ "       S.DAILY_PERCENT"
					+ "  FROM TASK T, INFOR C, FM_STAT_OVERVIEW_TASK S"
					+ " WHERE T.task_ID = C.TASK_ID"
					+ "   AND T.TASK_ID = S.TASK_ID(+)"
					+ "   AND T.STATUS = 0"
					+ "   AND T.LATEST = 1),"
					+ " FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM TASK_LIST"
					+ "    WHERE 1=1"
					+  conditionSql
					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.TASK_NAME DESC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			//System.out.println(selectSql);
			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * TASK_STATUS:1常规，2多源，3代理店，4情报
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 */
	private static ResultSetHandler<Page> getUnPushSnapshotQuery(final int currentPageNum,final int pageSize){
		final String version=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
			public Page handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Page page = new Page(currentPageNum);
			    page.setPageSize(pageSize);
			    int total=0;
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("taskId", rs.getInt("TASK_ID"));
					map.put("taskName", rs.getString("TASK_NAME"));
					map.put("taskDescp", rs.getString("TASK_DESCP"));
					
					if(rs.getInt("TASK_TYPE")==1){
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("cityName", rs.getString("CITY_NAME"));
						map.put("cityPlanStatus", rs.getInt("CITY_PLAN_STATUS"));}
					else if(rs.getInt("TASK_TYPE")==4){
						map.put("inforId", rs.getString("INFOR_ID"));
						map.put("inforName", rs.getString("INFOR_NAME"));
						map.put("inforPlanStatus", rs.getInt("INFOR_PLAN_STATUS"));}
					map.put("taskType", rs.getInt("TASK_TYPE"));
					map.put("taskStatus", rs.getInt("TASK_STATUS"));
					map.put("version", version);
					total=rs.getInt("TOTAL_RECORD_NUM");
					list.add(map);
				}
				page.setTotalCount(total);
				page.setResult(list);
				return page;
			}
    	};
    	return rsHandler;
	}
	
	/**
	 * TASK_STATUS:1常规，2多源，3代理店，4情报
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 */
	private static ResultSetHandler<Page> getOtherSnapshotQuery(final int currentPageNum,final int pageSize){
		final String version=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
			public Page handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Page page = new Page(currentPageNum);
			    page.setPageSize(pageSize);
			    int total=0;
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("taskId", rs.getInt("TASK_ID"));
					map.put("taskName", rs.getString("TASK_NAME"));
					if(rs.getInt("TASK_TYPE")==1){
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("cityName", rs.getString("CITY_NAME"));
						map.put("cityPlanStatus", rs.getInt("PLAN_STATUS"));}
					else if(rs.getInt("TASK_TYPE")==4){
						map.put("inforId", rs.getString("INFOR_ID"));
						map.put("inforName", rs.getString("INFOR_NAME"));
						map.put("inforPlanStatus", rs.getInt("PLAN_STATUS"));}
					map.put("taskType", rs.getInt("TASK_TYPE"));
					map.put("taskStatus", rs.getInt("TASK_STATUS"));					
					map.put("percent", rs.getInt("PERCENT"));
					map.put("diffDate", rs.getInt("DIFF_DATE"));
					map.put("progress", rs.getInt("PROGRESS"));
					map.put("collectProgress", rs.getInt("COLLECT_PROGRESS"));
					map.put("collectPercent", rs.getInt("COLLECT_PERCENT"));
					map.put("dailyProgress", rs.getInt("DAILY_PROGRESS"));
					map.put("dailyPercent", rs.getInt("DAILY_PERCENT"));
					map.put("version", version);
					total=rs.getInt("TOTAL_RECORD_NUM");
					list.add(map);
				}
				page.setTotalCount(total);
				page.setResult(list);
				return page;
			}
    	};
    	return rsHandler;
	}
	
	public static Page getListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			//根据任务名称或城市名称模糊查询，显示搜索结果
			//用户在输入名称的时候，我们并不知道他输入的是情报名称，城市名称，任务名称，所以都要查一下
			//界面输入：全国。。这个时候是返回 全国多源，全国代理店,即查非情报所有任务的cityName
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				String name=conditionJson.getString("name");
				conditionSql="   where TASK_LIST.UPPER_LEVEL_NAME LIKE '%"+name+"%' OR TASK_LIST.TASK_NAME LIKE '%"+name+"%'";
			}			
			
			//分页显示列表，不带条件查询
			String selectSql = "WITH T AS"
					+ " (SELECT T.TASK_ID,"
					+ "         T.NAME,"
					+ "         T.CITY_ID,"
					+ "         T.TASK_TYPE,"
					+ "         T.STATUS,"
					+ "         SUM(NVL(F.FINISH_PERCENT, 0)) / COUNT(DISTINCT S.SUBTASK_ID) FINISH_PERCENT"
					+ "    FROM SUBTASK S, TASK T, SUBTASK_FINISH F"
					+ "   WHERE S.TASK_ID = T.TASK_ID"
					+ "     AND T.LATEST = 1"
					+ "     AND S.SUBTASK_ID = F.SUBTASK_ID(+)"
					+ "   GROUP BY T.TASK_ID, T.NAME, T.CITY_ID, T.TASK_TYPE, T.STATUS"
					+ "  UNION"
					+ "  SELECT T.TASK_ID,"
					+ "         T.NAME,"
					+ "         T.CITY_ID,"
					+ "         T.TASK_TYPE,"
					+ "         T.STATUS,"
					+ "         SUM(NVL(F.FINISH_PERCENT, 0)) / COUNT(DISTINCT S.SUBTASK_ID) FINISH_PERCENT"
					+ "    FROM SUBTASK S, BLOCK B, CITY C, TASK T, SUBTASK_FINISH F"
					+ "   WHERE S.BLOCK_ID = B.BLOCK_ID"
					+ "     AND B.CITY_ID = C.CITY_ID"
					+ "     AND C.CITY_ID = T.CITY_ID"
					+ "     AND T.LATEST = 1"
					+ "     AND S.SUBTASK_ID = F.SUBTASK_ID(+)"
					+ "   GROUP BY T.TASK_ID, T.NAME, T.CITY_ID, T.TASK_TYPE, T.STATUS"
					+ "   UNION"
					+ "  SELECT T.TASK_ID,"
					+ "         T.NAME,"
					+ "         T.CITY_ID,"
					+ "         T.TASK_TYPE,"
					+ "         T.STATUS,"
					+ "         0 FINISH_PERCENT"
					+ "    FROM TASK T"
					+ "   WHERE T.LATEST = 1"
					+ "     AND NOT EXISTS"
					+ "   (SELECT 1"
					+ "            FROM SUBTASK S, BLOCK B, CITY C"
					+ "           WHERE S.BLOCK_ID = B.BLOCK_ID"
					+ "             AND B.CITY_ID = C.CITY_ID"
					+ "             AND C.CITY_ID = T.CITY_ID"
					+ "          UNION"
					+ "          SELECT 1 FROM SUBTASK S WHERE S.TASK_ID = T.TASK_ID)),"
					+ "TASK_LIST AS(SELECT NVL(T.TASK_ID, 0) TASK_ID,"
					+ "       NVL(T.NAME, '---') TASK_NAME,"
					+ "       TO_CHAR(C.CITY_ID) UPPER_LEVEL_ID,"
					+ "       C.CITY_NAME UPPER_LEVEL_NAME,"
					+ "       1 TASK_TYPE,"
					+ "       NVL(T.STATUS, 0) TASK_STATUS,"
					+ "       C.PLAN_STATUS,"
					+ "       NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT"
					+ "  FROM T, CITY C"
					+ " WHERE T.CITY_ID(+) = C.CITY_ID"
					+ "   AND C.REGION_ID <> 0   "
					+ " UNION ALL"
					+ " SELECT NVL(T.TASK_ID, 0) TASK_ID,"
					+ "       NVL(T.NAME, '---') NAME,"
					+ "       TO_CHAR(C.CITY_ID) UPPER_LEVEL_ID,"
					+ "       C.CITY_NAME UPPER_LEVEL_NAME,"
					+ "       2 TASK_TYPE,"
					+ "       NVL(T.STATUS, 0) TASK_STATUS,"
					+ "       C.PLAN_STATUS,"
					+ "       NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT"
					+ "  FROM T, CITY C"
					+ " WHERE T.CITY_ID(+) = C.CITY_ID"
					+ "   AND C.CITY_ID = 100000"
					+ " UNION ALL"
					+ " SELECT NVL(T.TASK_ID, 0) TASK_ID,"
					+ "       NVL(T.NAME, '---') NAME,"
					+ "       TO_CHAR(C.CITY_ID) UPPER_LEVEL_ID,"
					+ "       C.CITY_NAME UPPER_LEVEL_NAME,"
					+ "       3 TASK_TYPE,"
					+ "       NVL(T.STATUS, 0) TASK_STATUS,"
					+ "       C.PLAN_STATUS,"
					+ "       NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT"
					+ "  FROM T, CITY C"
					+ " WHERE T.CITY_ID(+) = C.CITY_ID"
					+ "   AND C.CITY_ID = 100001"
					+ " UNION ALL"
					+ " SELECT NVL(T.TASK_ID, 0) TASK_ID,"
					+ "       NVL(T.NAME, '---') NAME,"
					+ "       I.INFOR_ID,"
					+ "       I.INFOR_NAME,"
					+ "       4 TASK_TYPE,"
					+ "       NVL(T.STATUS, 0) TASK_STATUS,"
					+ "       I.PLAN_STATUS,"
					+ "       NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT"
					+ "  FROM T, INFOR I"
					+ " WHERE T.TASK_ID(+) = I.TASK_ID),"
					+ " FINAL_TABLE AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM TASK_LIST"
					+ conditionSql
					+ "   ORDER BY TASK_LIST.TASK_STATUS DESC, TASK_LIST.FINISH_PERCENT ASC)"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
			//System.out.println(selectSql);
			return run.query(conn, selectSql, getSnapshotQuery(currentPageNum,pageSize));		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * TASK_STATUS:1常规，2多源，3代理店，4情报
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 */
	private static ResultSetHandler<Page> getSnapshotQuery(final int currentPageNum,final int pageSize){
		//NVL(T.TASK_ID, 0) TASK_ID,NVL(T.NAME, '---') TASK_NAME, TO_CHAR(C.CITY_ID) UPPER_LEVEL_ID,
		//C.CITY_NAME UPPER_LEVEL_NAME,1 TASK_TYPE,NVL(T.STATUS, 0) TASK_STATUS,C.PLAN_STATUS,
		//NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT,ROWNUM_,TOTAL_RECORD_NUM
		final String version=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
			public Page handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Page page = new Page(currentPageNum);
			    page.setPageSize(pageSize);
			    int total=0;
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("taskId", rs.getInt("TASK_ID"));
					map.put("taskName", rs.getString("TASK_NAME"));
					map.put("upperLevelId", rs.getString("UPPER_LEVEL_ID"));
					map.put("upperLevelName", rs.getString("UPPER_LEVEL_NAME"));
					map.put("taskType", rs.getInt("TASK_TYPE"));
					map.put("taskStatus", rs.getInt("TASK_STATUS"));
					map.put("planStatus", rs.getInt("PLAN_STATUS"));
					map.put("finishPercent", rs.getInt("FINISH_PERCENT"));
					map.put("version", version);
					//map.put("ROWNUM_", rs.getInt("ROWNUM_"));
					//map.put("TOTAL_RECORD_NUM", rs.getInt("TOTAL_RECORD_NUM"));
					total=rs.getInt("TOTAL_RECORD_NUM");
					list.add(map);
				}
				page.setTotalCount(total);
				page.setResult(list);
				return page;
			}
    	};
    	return rsHandler;
	}
	
	public static Page getListIntegrate(Connection conn,JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			
			/*默认任务ID排序显示
			 *搜索功能，搜索项包括：任务 ID，任务名称，创建人，城市/情报名称；
			 *筛选功能，筛选项包括：任务状态(未规划/草稿/开启/关闭)
			 *排序功能，排序项包括：任务 ID，任务总体计划开始时间，任务总体计划结束时间,城市/情报名称,任务状态,任务类型
			 *搜索、筛选、排序功能可同时使用*/
			String conditionSql="";
			String statusSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if("taskStatus".equals(key) || "planStatus".equals(key)){
						if(!statusSql.isEmpty()){statusSql+=" or ";}
					}
					else{
						if(conditionSql.isEmpty()){conditionSql+=" where ";}
						else{conditionSql+=" and ";}}
					
					//城市/情报名称
					if ("upperLevelName".equals(key)) {conditionSql+=" TASK_LIST.UPPER_LEVEL_NAME like '%"+conditionJson.getString(key)+"%'";}
					if ("taskId".equals(key)) {conditionSql+=" TASK_LIST.TASK_ID="+conditionJson.getInt(key);}
					if ("taskName".equals(key)) {conditionSql+=" TASK_LIST.TASK_NAME like '%"+conditionJson.getString(key)+"%'";}
					if ("createUserName".equals(key)) {conditionSql+=" TASK_LIST.create_User_Name like '%"+conditionJson.getString(key)+"%'";}
					
					if ("taskStatus".equals(key)) {statusSql+=" (TASK_LIST.TASK_STATUS IN ("+conditionJson.getJSONArray(key).join(",")+")"
							+ " AND TASK_LIST.PLAN_STATUS!=0)";}
					if ("planStatus".equals(key)) {statusSql+=" TASK_LIST.PLAN_STATUS="+conditionJson.getInt(key);}
					}
				if(conditionSql.isEmpty()){
					//只有status
					conditionSql+=" where "+statusSql;}
				else if(!statusSql.isEmpty()){//有非status
					conditionSql+=" and ("+statusSql+")";}
				}
			
			String orderSql="";
			if(null!=orderJson && !orderJson.isEmpty()){
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					//城市/情报名称,任务状态,任务类型
					if ("taskId".equals(key)) {orderSql+=" order by TASK_LIST.TASK_ID "+orderJson.getString(key);break;}
					if ("planStartDate".equals(key)) {orderSql+=" order by TASK_LIST.PLAN_START_DATE "+orderJson.getString(key);break;}
					if ("planEndDate".equals(key)) {orderSql+=" order by TASK_LIST.PLAN_END_DATE "+orderJson.getString(key);break;}
					
					if ("upperLevelName".equals(key)) {orderSql+=" order by TASK_LIST.UPPER_LEVEL_NAME "+orderJson.getString(key);break;}
					if ("taskStatus".equals(key)) {orderSql+=" order by TASK_LIST.TASK_STATUS "+orderJson.getString(key);break;}
					if ("taskType".equals(key)) {orderSql+=" order by TASK_LIST.TASK_TYPE "+orderJson.getString(key);break;}
					}
			}else{
				orderSql+=" order by TASK_LIST.TASK_ID";
			}
			
			//分页显示列表，不带条件查询
			String selectSql = "WITH T AS"
					+ " (SELECT T.TASK_ID,"
					+ "         T.NAME,"
					+ "         T.DESCP,"
					+ "         T.CITY_ID,"
					+ "         T.TASK_TYPE,"
					+ "         T.PLAN_START_DATE,"
					+ "         T.PLAN_END_DATE,"
					+ "         T.MONTH_EDIT_PLAN_START_DATE,"
					+ "         T.MONTH_EDIT_PLAN_END_DATE,"
					+ "         T.MONTH_EDIT_GROUP_ID,"
					+ "         T.MONTH_PRODUCE_PLAN_START_DATE,"
					+ "         T.MONTH_PRODUCE_PLAN_END_DATE,"
					+ "         T.STATUS,"
					+ "         NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,"
					+ "         NVL(U.USER_REAL_NAME, '---') CREATE_USER_NAME,"
					+ "         NVL(G.GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
					+ "         S.PERCENT"
					+ "    FROM TASK T, FM_STAT_OVERVIEW_TASK S, USER_INFO U, USER_GROUP G"
					+ "   WHERE S.TASK_ID(+) = T.TASK_ID"
					+ "     AND T.LATEST = 1"
					+ "     AND T.CREATE_USER_ID = U.USER_ID(+)"
					+ "     AND T.MONTH_EDIT_GROUP_ID = G.GROUP_ID(+)),"
					+ " TASK_LIST AS"
					+ " (SELECT NVL(T.TASK_ID, 0) TASK_ID,"
					+ "         NVL(T.NAME, '---') TASK_NAME,"
					+ "         NVL(T.DESCP, '---') TASK_DESCP,"
					+ "         TO_CHAR(C.CITY_ID) UPPER_LEVEL_ID,"
					+ "         C.CITY_NAME UPPER_LEVEL_NAME,"
					+ "         CASE C.CITY_ID"
					+ "           WHEN 100000 THEN 2"
					+ "           WHEN 100001 THEN 3"
					+ "           ELSE 1 END TASK_TYPE,"
					+ "         T.PLAN_START_DATE,"
					+ "         T.PLAN_END_DATE,"
					+ "         T.MONTH_EDIT_PLAN_START_DATE,"
					+ "         T.MONTH_EDIT_PLAN_END_DATE,"
					+ "         NVL(T.MONTH_EDIT_GROUP_ID, 0) MONTH_EDIT_GROUP_ID,"
					+ "         T.MONTH_PRODUCE_PLAN_START_DATE,"
					+ "         T.MONTH_PRODUCE_PLAN_END_DATE,"
					+ "         NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,"
					+ "         NVL(T.CREATE_USER_NAME, '---') CREATE_USER_NAME,"
					+ "         NVL(T.MONTH_EDIT_GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
					+ "         NVL(T.STATUS, 0) TASK_STATUS,"
					+ "         C.PLAN_STATUS,"
					+ "         NVL(T.PERCENT, 0) PERCENT"
					+ "    FROM T, CITY C"
					+ "   WHERE T.CITY_ID(+) = C.CITY_ID"
					+ "     AND C.CITY_ID <> 100002"
					+ "  UNION ALL"
					+ "  SELECT NVL(T.TASK_ID, 0) TASK_ID,"
					+ "         NVL(T.NAME, '---') NAME,"
					+ "         NVL(T.DESCP, '---') TASK_DESCP,"
					+ "         I.INFOR_ID,"
					+ "         I.INFOR_NAME,"
					+ "         4 TASK_TYPE,"
					+ "         T.PLAN_START_DATE,"
					+ "         T.PLAN_END_DATE,"
					+ "         T.MONTH_EDIT_PLAN_START_DATE,"
					+ "         T.MONTH_EDIT_PLAN_END_DATE,"
					+ "         NVL(T.MONTH_EDIT_GROUP_ID, 0) MONTH_EDIT_GROUP_ID,"
					+ "         T.MONTH_PRODUCE_PLAN_START_DATE,"
					+ "         T.MONTH_PRODUCE_PLAN_END_DATE,"
					+ "         NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,"
					+ "         NVL(T.CREATE_USER_NAME, '---') CREATE_USER_NAME,"
					+ "         NVL(T.MONTH_EDIT_GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
					+ "         NVL(T.STATUS, 0) TASK_STATUS,"
					+ "         I.PLAN_STATUS,"
					+ "         NVL(T.PERCENT, 0) PERCENT"
					+ "    FROM T, INFOR I"
					+ "   WHERE T.TASK_ID(+) = I.TASK_ID),"
					+ " QUERY AS"
					+ " (SELECT DISTINCT *"
					+ "    FROM TASK_LIST"
					+conditionSql
					+orderSql
					+ ")"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " T.*, (SELECT COUNT(1) FROM QUERY) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT T.*, ROWNUM AS ROWNUM_ FROM QUERY T WHERE ROWNUM <= "+pageEndNum+") T"
					+ " WHERE T.ROWNUM_ >= "+pageStartNum;
			return run.query(conn, selectSql, getIntegrateQuery(currentPageNum,pageSize));		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * TASK_STATUS:1常规，2多源，3代理店，4情报
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 */
	private static ResultSetHandler<Page> getIntegrateQuery(final int currentPageNum,final int pageSize){
		/*NVL(T.TASK_ID, 0) TASK_ID,NVL(T.NAME, '---') TASK_NAME,TO_CHAR(C.CITY_ID) UPPER_LEVEL_ID,
          C.CITY_NAME UPPER_LEVEL_NAME,1 TASK_TYPE,T.PLAN_START_DATE,T.PLAN_END_DATE,
          T.MONTH_EDIT_PLAN_START_DATE,T.MONTH_EDIT_PLAN_END_DATE,
          NVL(T.MONTH_EDIT_GROUP_ID, 0) MONTH_EDIT_GROUP_ID,T.MONTH_PRODUCE_PLAN_START_DATE,
          T.MONTH_PRODUCE_PLAN_END_DATE,NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,
          NVL(T.CREATE_USER_NAME, '---') CREATE_USER_NAME,NVL(T.MONTH_EDIT_GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,
          NVL(T.STATUS, 0) TASK_STATUS,C.PLAN_STATUS,NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT,
          ROWNUM_,TOTAL_RECORD_NUM*/
		final String version=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
			public Page handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Page page = new Page(currentPageNum);
			    page.setPageSize(pageSize);
			    int total=0;
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("taskId", rs.getInt("TASK_ID"));
					map.put("taskName", rs.getString("TASK_NAME"));
					map.put("taskDescp", rs.getString("TASK_DESCP"));
					map.put("upperLevelId", rs.getString("UPPER_LEVEL_ID"));
					map.put("upperLevelName", rs.getString("UPPER_LEVEL_NAME"));
					map.put("taskType", rs.getInt("TASK_TYPE"));
					map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
					map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
					map.put("monthEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
					map.put("monthEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
					map.put("monthEditGroupId", rs.getInt("MONTH_EDIT_GROUP_ID"));
					map.put("monthProducePlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_PRODUCE_PLAN_START_DATE")));
					map.put("monthProducePlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_PRODUCE_PLAN_END_DATE")));
					map.put("createUserId", rs.getInt("CREATE_USER_ID"));
					map.put("createUserName", rs.getString("CREATE_USER_NAME"));
					map.put("monthEditGroupName", rs.getString("MONTH_EDIT_GROUP_NAME"));
					map.put("taskStatus", rs.getInt("TASK_STATUS"));
					map.put("upperPlanStatus", rs.getInt("PLAN_STATUS"));
					map.put("percent", rs.getInt("PERCENT"));
					map.put("version", version);
					//map.put("ROWNUM_", rs.getInt("ROWNUM_"));
					//map.put("TOTAL_RECORD_NUM", rs.getInt("TOTAL_RECORD_NUM"));
					total=rs.getInt("TOTAL_RECORD_NUM");
					list.add(map);
				}
				page.setTotalCount(total);
				page.setResult(list);
				return page;
			}
    	};
    	return rsHandler;
	}
		
	public static void updateTask(Connection conn,Task bean) throws Exception{
		try{
			String baseSql = "update task set ";
			QueryRunner run = new QueryRunner();
			String updateSql="";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getTaskDescp()!=null && StringUtils.isNotEmpty(bean.getTaskDescp().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" DESCP=? ";
				values.add(bean.getTaskDescp());
			};
			if (bean!=null&&bean.getTaskName()!=null && StringUtils.isNotEmpty(bean.getTaskName().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" NAME=? ";
				values.add(bean.getTaskName());
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
			if (bean!=null&&bean.getMonthEditGroupId()!=null && bean.getMonthEditGroupId()!=0 && StringUtils.isNotEmpty(bean.getMonthEditGroupId().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" MONTH_EDIT_GROUP_ID=? ";
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
	
	/**
	 * 
	 * @param conn
	 * @param condition 搜索条件{"taskIds":[1,2,3],"taskStatus":[1,2]}
	 * @return [{"taskId":12,"taskStatus":1,"taskName":"123"}]
	 */
	public static List<Map<String, Object>> queryTaskTable(Connection conn,JSONObject condition) throws Exception{
		
		String conditionSql="";
		if(null!=condition && !condition.isEmpty()){
			Iterator keys = condition.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				
				if ("taskIds".equals(key)) {conditionSql+=" AND t.task_id IN ("+condition.getJSONArray(key).join(",")+")";}
				if ("taskStatus".equals(key)) {conditionSql+=" AND T.STATUS IN ("+condition.getJSONArray(key).join(",")+")";}
			}
		}
		
		String selectSql="select t.task_id,t.status task_status,t.NAME task_name from task t where 1=1 "+conditionSql;
		
		ResultSetHandler<List<Map<String, Object>>> rsHandler = new ResultSetHandler<List<Map<String, Object>>>(){
			public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("taskId", rs.getInt("TASK_ID"));
					map.put("taskStatus", rs.getInt("TASK_STATUS"));
					map.put("taskName", rs.getString("TASK_NAME"));
					list.add(map);
				}
				return list;
			}
    	};
		
		QueryRunner run=new QueryRunner();
		return run.query(conn, selectSql, rsHandler);
	}

	public static List<Map<String, Object>> queryTask(Connection conn,int taskId) throws Exception {
		String selectSql="WITH T AS"
				+ " (SELECT T.TASK_ID,"
				+ "         T.NAME,"
				+ "         T.DESCP,"
				+ "         T.CITY_ID,"
				+ "         T.TASK_TYPE,"
				+ "         T.PLAN_START_DATE,"
				+ "         T.PLAN_END_DATE,"
				+ "         T.MONTH_EDIT_PLAN_START_DATE,"
				+ "         T.MONTH_EDIT_PLAN_END_DATE,"
				+ "         T.MONTH_EDIT_GROUP_ID,"
				+ "         T.MONTH_PRODUCE_PLAN_START_DATE,"
				+ "         T.MONTH_PRODUCE_PLAN_END_DATE,"
				+ "         T.STATUS,"
				+ "         NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,"
				+ "         NVL(U.USER_REAL_NAME, '---') CREATE_USER_NAME,"
				+ "         NVL(G.GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
				+ "         SUM(NVL(F.FINISH_PERCENT, 0)) /COUNT(DISTINCT NVL(S.SUBTASK_ID, 1)) FINISH_PERCENT"
				+ "    FROM SUBTASK S, TASK T, SUBTASK_FINISH F, USER_INFO U, USER_GROUP G"
				+ "   WHERE S.TASK_ID = T.TASK_ID"
				+ "     AND T.LATEST = 1"
				+ "     AND T.CREATE_USER_ID = U.USER_ID(+)"
				+ "     AND T.MONTH_EDIT_GROUP_ID = G.GROUP_ID(+)"
				+ "     AND S.SUBTASK_ID = F.SUBTASK_ID(+)"
				+ "   GROUP BY T.TASK_ID,"
				+ "            T.NAME,"
				+ "            T.DESCP,"
				+ "            T.CITY_ID,"
				+ "            T.TASK_TYPE,"
				+ "            T.STATUS,"
				+ "            T.CREATE_USER_ID,"
				+ "            U.USER_REAL_NAME,"
				+ "            G.GROUP_NAME,"
				+ "            T.PLAN_START_DATE,"
				+ "            T.PLAN_END_DATE,"
				+ "            T.MONTH_EDIT_PLAN_START_DATE,"
				+ "            T.MONTH_EDIT_PLAN_END_DATE,"
				+ "            T.MONTH_EDIT_GROUP_ID,"
				+ "            T.MONTH_PRODUCE_PLAN_START_DATE,"
				+ "            T.MONTH_PRODUCE_PLAN_END_DATE"
				+ "  UNION"
				+ "  SELECT T.TASK_ID,"
				+ "         T.NAME,"
				+ "         T.DESCP,"
				+ "         T.CITY_ID,"
				+ "         T.TASK_TYPE,"
				+ "         T.PLAN_START_DATE,"
				+ "         T.PLAN_END_DATE,"
				+ "         T.MONTH_EDIT_PLAN_START_DATE,"
				+ "         T.MONTH_EDIT_PLAN_END_DATE,"
				+ "         T.MONTH_EDIT_GROUP_ID,"
				+ "         T.MONTH_PRODUCE_PLAN_START_DATE,"
				+ "         T.MONTH_PRODUCE_PLAN_END_DATE,"
				+ "         T.STATUS,"
				+ "         NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,"
				+ "         NVL(U.USER_REAL_NAME, '---') CREATE_USER_NAME,"
				+ "         NVL(G.GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
				+ "         SUM(NVL(F.FINISH_PERCENT, 0)) /COUNT(DISTINCT NVL(S.SUBTASK_ID, 1)) FINISH_PERCENT"
				+ "    FROM SUBTASK        S,"
				+ "         BLOCK          B,"
				+ "         CITY           C,"
				+ "         TASK           T,"
				+ "         SUBTASK_FINISH F,"
				+ "         USER_INFO      U,"
				+ "         USER_GROUP     G"
				+ "   WHERE S.BLOCK_ID = B.BLOCK_ID"
				+ "     AND B.CITY_ID = C.CITY_ID"
				+ "     AND C.CITY_ID = T.CITY_ID"
				+ "     AND T.LATEST = 1"
				+ "     AND T.CREATE_USER_ID = U.USER_ID(+)"
				+ "     AND T.MONTH_EDIT_GROUP_ID = G.GROUP_ID(+)"
				+ "     AND S.SUBTASK_ID = F.SUBTASK_ID(+)"
				+ "   GROUP BY T.TASK_ID,"
				+ "            T.NAME,"
				+ "            T.DESCP,"
				+ "            T.CITY_ID,"
				+ "            T.TASK_TYPE,"
				+ "            T.STATUS,"
				+ "            T.CREATE_USER_ID,"
				+ "            U.USER_REAL_NAME,"
				+ "            G.GROUP_NAME,"
				+ "            T.PLAN_START_DATE,"
				+ "            T.PLAN_END_DATE,"
				+ "            T.MONTH_EDIT_PLAN_START_DATE,"
				+ "            T.MONTH_EDIT_PLAN_END_DATE,"
				+ "            T.MONTH_EDIT_GROUP_ID,"
				+ "            T.MONTH_PRODUCE_PLAN_START_DATE,"
				+ "            T.MONTH_PRODUCE_PLAN_END_DATE"
				+ "  UNION"
				+ "  SELECT T.TASK_ID,"
				+ "         T.NAME,"
				+ "         T.DESCP,"
				+ "         T.CITY_ID,"
				+ "         T.TASK_TYPE,"
				+ "         T.PLAN_START_DATE,"
				+ "         T.PLAN_END_DATE,"
				+ "         T.MONTH_EDIT_PLAN_START_DATE,"
				+ "         T.MONTH_EDIT_PLAN_END_DATE,"
				+ "         T.MONTH_EDIT_GROUP_ID,"
				+ "         T.MONTH_PRODUCE_PLAN_START_DATE,"
				+ "         T.MONTH_PRODUCE_PLAN_END_DATE,"
				+ "         T.STATUS,"
				+ "         NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,"
				+ "         NVL(U.USER_REAL_NAME, '---') CREATE_USER_NAME,"
				+ "         NVL(G.GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
				+ "         0 FINISH_PERCENT"
				+ "    FROM TASK T, USER_INFO U, USER_GROUP G"
				+ "   WHERE T.LATEST = 1"
				+ "     AND T.CREATE_USER_ID = U.USER_ID(+)"
				+ "     AND T.MONTH_EDIT_GROUP_ID = G.GROUP_ID(+)"
				+ "     AND NOT EXISTS"
				+ "   (SELECT 1"
				+ "            FROM SUBTASK S, BLOCK B, CITY C"
				+ "           WHERE S.BLOCK_ID = B.BLOCK_ID"
				+ "             AND B.CITY_ID = C.CITY_ID"
				+ "             AND C.CITY_ID = T.CITY_ID"
				+ "          UNION"
				+ "          SELECT 1 FROM SUBTASK S WHERE S.TASK_ID = T.TASK_ID)),"
				+ " TASK_LIST AS"
				+ " (SELECT NVL(T.TASK_ID, 0) TASK_ID,"
				+ "         NVL(T.NAME, '---') TASK_NAME,"
				+ "         NVL(T.DESCP,'---') TASK_DESCP,"
				+ "         TO_CHAR(C.CITY_ID) UPPER_LEVEL_ID,"
				+ "         C.CITY_NAME UPPER_LEVEL_NAME,"
				+ "         T.TASK_TYPE,"
				+ "         T.PLAN_START_DATE,"
				+ "         T.PLAN_END_DATE,"
				+ "         T.MONTH_EDIT_PLAN_START_DATE,"
				+ "         T.MONTH_EDIT_PLAN_END_DATE,"
				+ "         NVL(T.MONTH_EDIT_GROUP_ID, 0) MONTH_EDIT_GROUP_ID,"
				+ "         T.MONTH_PRODUCE_PLAN_START_DATE,"
				+ "         T.MONTH_PRODUCE_PLAN_END_DATE,"
				+ "         NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,"
				+ "         NVL(T.CREATE_USER_NAME, '---') CREATE_USER_NAME,"
				+ "         NVL(T.MONTH_EDIT_GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
				+ "         NVL(T.STATUS, 0) TASK_STATUS,"
				+ "         C.PLAN_STATUS,"
				+ "         NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT"
				+ "    FROM T, CITY C"
				+ "   WHERE T.CITY_ID = C.CITY_ID"
				+ "     AND C.CITY_ID <> 100002"
				+ "  UNION ALL"
				+ "  SELECT NVL(T.TASK_ID, 0) TASK_ID,"
				+ "         NVL(T.NAME, '---') NAME,"
				+ "         NVL(T.DESCP,'---') DESCP,"
				+ "         I.INFOR_ID,"
				+ "         I.INFOR_NAME,"
				+ "         4 TASK_TYPE,"
				+ "         T.PLAN_START_DATE,"
				+ "         T.PLAN_END_DATE,"
				+ "         T.MONTH_EDIT_PLAN_START_DATE,"
				+ "         T.MONTH_EDIT_PLAN_END_DATE,"
				+ "         NVL(T.MONTH_EDIT_GROUP_ID, 0) MONTH_EDIT_GROUP_ID,"
				+ "         T.MONTH_PRODUCE_PLAN_START_DATE,"
				+ "         T.MONTH_PRODUCE_PLAN_END_DATE,"
				+ "         NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,"
				+ "         NVL(T.CREATE_USER_NAME, '---') CREATE_USER_NAME,"
				+ "         NVL(T.MONTH_EDIT_GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
				+ "         NVL(T.STATUS, 0) TASK_STATUS,"
				+ "         I.PLAN_STATUS,"
				+ "         NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT"
				+ "    FROM T, INFOR I"
				+ "   WHERE T.TASK_ID = I.TASK_ID)"
				+ " SELECT * FROM TASK_LIST WHERE TASK_ID = "+taskId;
		QueryRunner run=new QueryRunner();
		return run.query(conn, selectSql, getAllIntegrateQuery());	
	}
	
	/**
	 * TASK_STATUS:1常规，2多源，3代理店，4情报
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 */
	private static ResultSetHandler<List<Map<String, Object>>> getAllIntegrateQuery(){
		/*NVL(T.TASK_ID, 0) TASK_ID,NVL(T.NAME, '---') TASK_NAME,TO_CHAR(C.CITY_ID) UPPER_LEVEL_ID,
          C.CITY_NAME UPPER_LEVEL_NAME,1 TASK_TYPE,T.PLAN_START_DATE,T.PLAN_END_DATE,
          T.MONTH_EDIT_PLAN_START_DATE,T.MONTH_EDIT_PLAN_END_DATE,
          NVL(T.MONTH_EDIT_GROUP_ID, 0) MONTH_EDIT_GROUP_ID,T.MONTH_PRODUCE_PLAN_START_DATE,
          T.MONTH_PRODUCE_PLAN_END_DATE,NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,
          NVL(T.CREATE_USER_NAME, '---') CREATE_USER_NAME,NVL(T.MONTH_EDIT_GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,
          NVL(T.STATUS, 0) TASK_STATUS,C.PLAN_STATUS,NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT,
          ROWNUM_,TOTAL_RECORD_NUM*/
		final String version=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		ResultSetHandler<List<Map<String, Object>>> rsHandler = new ResultSetHandler<List<Map<String, Object>>>(){
			public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("taskId", rs.getInt("TASK_ID"));
					map.put("taskName", rs.getString("TASK_NAME"));
					map.put("taskDescp", rs.getString("TASK_DESCP"));
					map.put("upperLevelId", rs.getString("UPPER_LEVEL_ID"));
					map.put("upperLevelName", rs.getString("UPPER_LEVEL_NAME"));
					map.put("taskType", rs.getInt("TASK_TYPE"));
					map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
					map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
					map.put("monthEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
					map.put("monthEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
					map.put("monthEditGroupId", rs.getInt("MONTH_EDIT_GROUP_ID"));
					map.put("monthProducePlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_PRODUCE_PLAN_START_DATE")));
					map.put("monthProducePlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_PRODUCE_PLAN_END_DATE")));
					map.put("createUserId", rs.getInt("CREATE_USER_ID"));
					map.put("createUserName", rs.getString("CREATE_USER_NAME"));
					map.put("monthEditGroupName", rs.getString("MONTH_EDIT_GROUP_NAME"));
					map.put("taskStatus", rs.getInt("TASK_STATUS"));
					map.put("planStatus", rs.getInt("PLAN_STATUS"));
					map.put("finishPercent", rs.getInt("FINISH_PERCENT"));
					map.put("version", version);
					//map.put("ROWNUM_", rs.getInt("ROWNUM_"));
					//map.put("TOTAL_RECORD_NUM", rs.getInt("TOTAL_RECORD_NUM"));
					list.add(map);
				}
				return list;
			}
    	};
    	return rsHandler;
	}

	public static Page queryMonthTask(Connection conn,
			int monthEditGroupId, JSONObject condition, int currentPageNum,int pageSize) throws Exception {
		long pageStartNum = (currentPageNum - 1) * pageSize + 1;
		long pageEndNum = currentPageNum * pageSize;
		String conditionSql="";
		if(null!=condition && !condition.isEmpty()){
			String taskName=condition.getString("taskName");
			conditionSql="   AND TASK_LIST.TASK_NAME LIKE '%"+taskName+"%'";
		}
		String selectSql="WITH T AS"
				+ " (SELECT T.TASK_ID,"
				+ "         T.NAME,"
				+ "         T.CITY_ID,"
				+ "         T.TASK_TYPE,"
				+ "         T.MONTH_EDIT_PLAN_START_DATE,"
				+ "         T.MONTH_EDIT_PLAN_END_DATE,"
				+ "         T.MONTH_EDIT_GROUP_ID,"
				+ "         T.STATUS,"
				+ "         NVL(G.GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
				+ "         SUM(NVL(F.FINISH_PERCENT, 0)) /COUNT(DISTINCT NVL(S.SUBTASK_ID, 1)) FINISH_PERCENT"
				+ "    FROM SUBTASK S, TASK T, SUBTASK_FINISH F, USER_GROUP G"
				+ "   WHERE S.TASK_ID = T.TASK_ID"
				+ "     AND T.LATEST = 1"
				+ "     AND T.MONTH_EDIT_GROUP_ID = G.GROUP_ID"
				+ "     AND S.SUBTASK_ID = F.SUBTASK_ID(+)"
				+ "   GROUP BY T.TASK_ID,"
				+ "            T.NAME,"
				+ "            T.CITY_ID,"
				+ "            T.TASK_TYPE,"
				+ "            T.STATUS,"
				+ "            G.GROUP_NAME,"
				+ "            T.MONTH_EDIT_PLAN_START_DATE,"
				+ "            T.MONTH_EDIT_PLAN_END_DATE,"
				+ "            T.MONTH_EDIT_GROUP_ID),"
				+ "TASK_LIST AS"
				+ " (SELECT NVL(T.TASK_ID, 0) TASK_ID,"
				+ "         NVL(T.NAME, '---') TASK_NAME,"
				+ "         TO_CHAR(C.CITY_ID) UPPER_LEVEL_ID,"
				+ "         C.CITY_NAME UPPER_LEVEL_NAME,"
				+ "         T.TASK_TYPE,"
				+ "         T.MONTH_EDIT_PLAN_START_DATE,"
				+ "         T.MONTH_EDIT_PLAN_END_DATE,"
				+ "         NVL(T.MONTH_EDIT_GROUP_ID, 0) MONTH_EDIT_GROUP_ID,"
				+ "         NVL(T.MONTH_EDIT_GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
				+ "         NVL(T.STATUS, 0) TASK_STATUS,"
				+ "         C.PLAN_STATUS,"
				+ "         NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT"
				+ "    FROM T, CITY C"
				+ "   WHERE T.CITY_ID = C.CITY_ID"
				+ "     AND C.CITY_ID <> 100002"
				+ "  UNION ALL"
				+ "  SELECT NVL(T.TASK_ID, 0) TASK_ID,"
				+ "         NVL(T.NAME, '---') NAME,"
				+ "         I.INFOR_ID,"
				+ "         I.INFOR_NAME,"
				+ "         4 TASK_TYPE,"
				+ "         T.MONTH_EDIT_PLAN_START_DATE,"
				+ "         T.MONTH_EDIT_PLAN_END_DATE,"
				+ "         NVL(T.MONTH_EDIT_GROUP_ID, 0) MONTH_EDIT_GROUP_ID,"
				+ "         NVL(T.MONTH_EDIT_GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
				+ "         NVL(T.STATUS, 0) TASK_STATUS,"
				+ "         I.PLAN_STATUS,"
				+ "         NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT"
				+ "    FROM T, INFOR I"
				+ "   WHERE T.TASK_ID = I.TASK_ID),"
				+ " QUERY AS"
				+ " (SELECT * FROM TASK_LIST WHERE MONTH_EDIT_GROUP_ID = "+monthEditGroupId+conditionSql+""
				+ "          ORDER BY TASK_LIST.TASK_STATUS DESC, TASK_LIST.FINISH_PERCENT ASC)"
				+ " SELECT /*+FIRST_ROWS ORDERED*/"
				+ " T.*, (SELECT COUNT(1) FROM QUERY) AS TOTAL_RECORD_NUM"
				+ "  FROM (SELECT T.*, ROWNUM AS ROWNUM_ FROM QUERY T WHERE ROWNUM <= "+pageEndNum+") T"
				+ " WHERE T.ROWNUM_ >= "+pageStartNum;
		QueryRunner run=new QueryRunner();
		return run.query(conn, selectSql, getMonthTaskSnapShotQuery(currentPageNum,pageSize));	
	}
	
	/**
	 * TASK_STATUS:1常规，2多源，3代理店，4情报
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 */
	private static ResultSetHandler<Page> getMonthTaskSnapShotQuery(final int currentPageNum,final int pageSize){
		
		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
			public Page handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Page page = new Page(currentPageNum);
			    page.setPageSize(pageSize);
			    int total=0;
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("taskId", rs.getInt("TASK_ID"));
					map.put("taskName", rs.getString("TASK_NAME"));
					map.put("upperLevelId", rs.getString("UPPER_LEVEL_ID"));
					map.put("upperLevelName", rs.getString("UPPER_LEVEL_NAME"));
					map.put("taskType", rs.getInt("TASK_TYPE"));
					map.put("monthEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
					map.put("monthEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
					map.put("monthEditGroupId", rs.getInt("MONTH_EDIT_GROUP_ID"));
					map.put("monthEditGroupName", rs.getString("MONTH_EDIT_GROUP_NAME"));
					map.put("taskStatus", rs.getInt("TASK_STATUS"));
					map.put("planStatus", rs.getInt("PLAN_STATUS"));
					map.put("finishPercent", rs.getInt("FINISH_PERCENT"));
					total=rs.getInt("TOTAL_RECORD_NUM");
					list.add(map);
				}
				page.setTotalCount(total);
				page.setResult(list);
				return page;
			}
    	};
    	return rsHandler;
	}

}
