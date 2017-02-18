package com.navinfo.dataservice.engine.man.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import com.navinfo.dataservice.engine.man.block.BlockOperation;
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
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/*
	 * task的latest字段，修改成无效，0
	 */
	public static void updateLatest(Connection conn, Integer programId, Integer regionId,int blockId, Integer type) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String updateBlock="UPDATE TASK SET LATEST=0 WHERE LATEST=1 AND TYPE=" + type + " AND BLOCK_ID="+blockId + " AND PROGRAM_ID="+programId + " AND REGION_ID="+regionId;
			run.update(conn,updateBlock);			
		}catch(Exception e){
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
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
//	/*
//	 * 根据sql语句查询task
//	 */
//	public static Page selectTaskBySql(Connection conn,String selectSql,List<Object> values,final int currentPageNum,final int pageSize) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
//				public Page handle(ResultSet rs) throws SQLException {
//					List<HashMap> list = new ArrayList<HashMap>();
//				    Page page = new Page(currentPageNum);
//				    page.setPageSize(pageSize);
//					while(rs.next()){
//						HashMap map = new HashMap();
//						map.put("taskId", rs.getInt("TASK_ID"));
//						map.put("name", rs.getString("NAME"));
//						map.put("cityId", rs.getInt("CITY_ID"));
//						map.put("createUserId", rs.getInt("CREATE_USER_ID"));
//						map.put("createDate", DateUtils.dateToString(rs.getTimestamp("CREATE_DATE")));
//						map.put("status", rs.getInt("STATUS"));
//						map.put("descp", rs.getString("DESCP"));
//						map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
//						map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
//						map.put("monthEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
//						map.put("monthEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
//						map.put("monthEditGroupId", rs.getInt("MONTH_EDIT_GROUP_ID"));
//						map.put("latest", rs.getInt("LATEST"));
//						list.add(map);
//					}
//					//page.setTotalCount(list.size());
//					page.setResult(list);
//					return page;
//				}
//	    		
//	    	}		;
//	    	if (null==values || values.size()==0){
//	    		return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler
//						);
//	    	}
//	    	return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler,values.toArray()
//					);
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
//	/*
//	 * 根据sql语句查询task
//	 */
//	public static Page selectTaskBySql2(Connection conn,String selectSql,List<Object> values,final int currentPageNum,final int pageSize) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
//				public Page handle(ResultSet rs) throws SQLException {
//					List<Task> list = new ArrayList<Task>();
//				    Page page = new Page(currentPageNum);
//				    page.setPageSize(pageSize);
//				    int total=0;
//					while(rs.next()){
//						Task map = new Task();
//						map.setTaskId(rs.getInt("TASK_ID"));
////						map.setCityName(rs.getString("CITY_NAME"));
////						map.setTaskName(rs.getString("NAME"));
////						map.setCityId(rs.getInt("CITY_ID"));
//						map.setCreateUserId(rs.getInt("CREATE_USER_ID"));
//						map.setCreateUserName(rs.getString("USER_REAL_NAME"));
//						map.setCreateDate(rs.getTimestamp("CREATE_DATE"));
////						map.setTaskStatus(rs.getInt("STATUS"));
////						map.setTaskDescp(rs.getString("DESCP"));
//						map.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
//						map.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
////						map.setMonthEditPlanStartDate(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE"));
////						map.setMonthEditPlanEndDate(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE"));
////						map.setMonthEditGroupId(rs.getInt("MONTH_EDIT_GROUP_ID"));
////						map.setMonthEditGroupName(rs.getString("GROUP_NAME"));
//						map.setLatest(rs.getInt("LATEST"));
//						if(total==0){total=rs.getInt("TOTAL_RECORD_NUM_");}
//						list.add(map);
//					}
//					page.setTotalCount(total);
//					page.setResult(list);
//					return page;
//				}
//	    		
//	    	}		;
//	    	if (null==values || values.size()==0){
//	    		return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler
//						);
//	    	}
//	    	return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler,values.toArray()
//					);
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
//		}
//	}
	
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
//						map.setCityId(rs.getInt("CITY_ID"));
						map.setCreateUserId(rs.getInt("CREATE_USER_ID"));
						map.setCreateDate(rs.getTimestamp("CREATE_DATE"));
//						map.setTaskStatus(rs.getInt("STATUS"));
//						map.setTaskName(rs.getString("NAME"));
//						map.setTaskDescp(rs.getString("DESCP"));
						map.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						map.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
//						map.setMonthEditPlanStartDate(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE"));
//						map.setMonthEditPlanEndDate(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE"));
						map.setLatest(rs.getInt("LATEST"));
//						map.setMonthProducePlanStartDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_START_DATE"));
//						map.setMonthProducePlanEndDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_END_DATE"));
//						map.setTaskType(rs.getInt("TASK_TYPE"));
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
			
			String insertPart="";
			String valuePart="";
			if (bean!=null&&bean.getTaskId()!=null && bean.getTaskId()!=0 && StringUtils.isNotEmpty(bean.getTaskId().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" TASK_ID ";
				valuePart+=bean.getTaskId();
			};
			if (bean!=null&&bean.getProgramId()!=null && bean.getProgramId()!=0 && StringUtils.isNotEmpty(bean.getProgramId().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" PROGRAM_ID ";
				valuePart+=bean.getProgramId();
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" NAME ";
				valuePart+= "'" + bean.getName() + "'";
			};
			if (bean!=null&&bean.getBlockId()!=null && bean.getBlockId()!=0 && StringUtils.isNotEmpty(bean.getBlockId().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" BLOCK_ID ";
				valuePart+=bean.getBlockId();
			};
			if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
			insertPart+=" CREATE_USER_ID,CREATE_DATE,STATUS,LATEST ";
			valuePart+=bean.getCreateUserId()+",sysdate,2,1";
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" DESCP ";
				valuePart+="'"+bean.getDescp()+"'";
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" PLAN_START_DATE ";
				valuePart+="to_timestamp('"+ bean.getPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" PLAN_END_DATE ";
				valuePart+="to_timestamp('"+ bean.getPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getType()!=null && StringUtils.isNotEmpty(bean.getType().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" TYPE ";
				valuePart+=bean.getType();
			};
			if (bean!=null&&bean.getLot()!=null && bean.getLot()!=0 && StringUtils.isNotEmpty(bean.getLot().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" LOT";
				valuePart+= bean.getLot();
			};
			if (bean!=null&&bean.getGroupId()!=null && bean.getGroupId()!=0 && StringUtils.isNotEmpty(bean.getGroupId().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" GROUP_ID ";
				valuePart+=bean.getGroupId();
			};
			if (bean!=null&&bean.getRoadPlanTotal()!=null && bean.getRoadPlanTotal()!=0 && StringUtils.isNotEmpty(bean.getRoadPlanTotal().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" ROAD_PLAN_TOTAL ";
				valuePart+=bean.getRoadPlanTotal();
			};
			if (bean!=null&&bean.getPoiPlanTotal()!=null && bean.getPoiPlanTotal()!=0 && StringUtils.isNotEmpty(bean.getPoiPlanTotal().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" POI_PLAN_TOTAL ";
				valuePart+=bean.getPoiPlanTotal();
			};
			if (bean!=null&&bean.getProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getProducePlanStartDate().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" PRODUCE_PLAN_START_DATE ";
				valuePart+="to_timestamp('"+ bean.getProducePlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getProducePlanEndDate().toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" PRODUCE_PLAN_END_DATE";
				valuePart+="to_timestamp('"+ bean.getProducePlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			String createSql = "insert into task ("+insertPart+") values("+valuePart+")";
			
			run.update(conn,createSql);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
//	public static Page getCommonUnPushListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			/* 记录默认排序原则：根据城市名称排序
//			 * 点击搜索，根据城市名称\任务名称模糊查询
//			 * • 点击筛选，根据任务状态筛选，可多选
//			 * • 点击更多，跳转到<全国任务详情列表>页面，可进行批量操作
//			 */
//			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
//			long pageEndNum = currentPageNum * pageSize;
//			String conditionSql="";
//			String statusSql="";
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if("name".equals(key)){
//						conditionSql=conditionSql+" AND (TASK_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
//					if ("cityId".equals(key)) {
//						conditionSql+=" AND TASK_LIST.CITY_ID="+conditionJson.getInt(key);}
//					if ("taskStatus".equals(key)) {
//						if(!statusSql.isEmpty()){statusSql+=" or ";}
//						statusSql+=" TASK_LIST.TASK_STATUS="+conditionJson.getInt(key);}
//					if ("cityPlanStatus".equals(key)) {
//						if(!statusSql.isEmpty()){statusSql+=" or ";}
//						statusSql+=" TASK_LIST.CITY_PLAN_STATUS="+conditionJson.getInt(key);}
//					//1-11未规划,草稿,采集正常,采集异常,采集完成,日编正常,日编异常,日编完成,按时完成,提前完成,逾期完成
//					if("selectParam1".equals(key)){
//						JSONArray selectParam1=conditionJson.getJSONArray(key);
//						for(Object i:selectParam1){
//							int tmp=(int) i;
//							if(tmp==1){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.CITY_PLAN_STATUS =0";}
//							if(tmp==2){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.TASK_STATUS =2";}
//						}
//					}
//				}
//			}	
//			if(!statusSql.isEmpty()){//有非status
//				conditionSql+=" and ("+statusSql+")";}
//			
//			//分页显示列表，不带条件查询
//			String selectSql = "WITH TASK_LIST AS"
//					+ " (SELECT T.TASK_ID,"
//					+ "         T.NAME TASK_NAME,"
//					+ "         C.CITY_ID,"
//					+ "         C.CITY_NAME,"
//					+ "         C.PLAN_STATUS CITY_PLAN_STATUS,"
//					+ "         T.STATUS TASK_STATUS, "
//					+ "         T.DESCP TASK_DESCP, "
//					+ "         1 TASK_TYPE"
//					+ "    FROM TASK T, CITY C"
//					+ "   WHERE T.CITY_ID=C.CITY_ID"
//					+ "   AND C.CITY_ID NOT IN (100000,100001,100002)"
//					+ "   AND T.STATUS=2"
//					+ "   AND T.LATEST=1"
//					+ "  UNION"
//					+ "  SELECT 0,"
//					+ "         '---',"
//					+ "         C.CITY_ID,"
//					+ "         C.CITY_NAME,"
//					+ "         C.PLAN_STATUS,"
//					+ "         0,"
//					+ "         '---' TASK_DESCP, "
//					+ "         1 TASK_TYPE"
//					+ "    FROM CITY C"
//					+ "   WHERE C.CITY_ID NOT IN (100000,100001,100002)"
//					+ "   AND C.PLAN_STATUS=0),"
//					+ " FINAL_TABLE AS"
//					+ " (SELECT DISTINCT *"
//					+ "    FROM TASK_LIST"
//					+ "    WHERE 1=1"
//					+  conditionSql
//					+ "   ORDER BY TASK_LIST.CITY_NAME DESC)"
//					+ " SELECT /*+FIRST_ROWS ORDERED*/"
//					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
//					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
//					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
//			//System.out.println(selectSql);
//			return run.query(conn, selectSql, getUnPushSnapshotQuery(currentPageNum,pageSize));		
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
//	public static Page getCommonPushListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			/* 记录默认排序原则：
//			 * ①根据剩余工期排序，逾期>剩余
//			 * ②相同剩余工期，根据完成度排序，完成度高>完成度低
//			 * • 点击搜索，根据城市名称\任务名称模糊查询
//			 * • 点击筛选，根据分类筛选，可多选 采集/日编 正常/异常/完成
//			 */
//			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
//			long pageEndNum = currentPageNum * pageSize;
//			String conditionSql="";
//			String statusSql="";
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if("name".equals(key)){
//						conditionSql=conditionSql+" AND (TASK_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
//					if ("cityId".equals(key)) {
//						conditionSql+=" AND TASK_LIST.CITY_ID="+conditionJson.getInt(key);}
//					if ("collectProgress".equals(key)) {
//						if(!statusSql.isEmpty()){statusSql+=" or ";}
//						statusSql+=" TASK_LIST.collect_Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}
//					if ("dailyProgress".equals(key)) {
//						if(!statusSql.isEmpty()){statusSql+=" or ";}
//						statusSql+=" TASK_LIST.daily_Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}
//					//1-11未规划,草稿,采集正常,采集异常,采集完成,日编正常,日编异常,日编完成,按时完成,提前完成,逾期完成,12月编正常,13月编异常,14月编完成
//					if("selectParam1".equals(key)){
//						JSONArray selectParam1=conditionJson.getJSONArray(key);
//						JSONArray collectProgress=new JSONArray();
//						JSONArray dailyProgress=new JSONArray();
//						JSONArray monthlyProgress=new JSONArray();
//						for(Object i:selectParam1){
//							int tmp=(int) i;
//							if(tmp==3||tmp==4||tmp==5){collectProgress.add(tmp-2);}
//							if(tmp==6||tmp==7||tmp==8){dailyProgress.add(tmp-5);}
//							if(tmp==12||tmp==13||tmp==14){monthlyProgress.add(tmp-11);}
//						}
//						if(!collectProgress.isEmpty()){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" TASK_LIST.collect_Progress IN ("+collectProgress.join(",")+")";}
//						if(!dailyProgress.isEmpty()){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" TASK_LIST.daily_Progress IN ("+dailyProgress.join(",")+")";}
//						if(!monthlyProgress.isEmpty()){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" TASK_LIST.monthly_Progress IN ("+monthlyProgress.join(",")+")";}
//					}
//				}
//			}	
//			if(!statusSql.isEmpty()){//有非status
//				conditionSql+=" and ("+statusSql+")";}
//			
//			//分页显示列表，不带条件查询
//			String selectSql = "WITH TASK_LIST AS"
//					+ " (SELECT T.TASK_ID,"
//					+ "       T.NAME        TASK_NAME,"
//					+ "       C.CITY_ID,"
//					+ "       C.CITY_NAME,"
//					+ "       C.PLAN_STATUS,"
//					+ "       T.STATUS      TASK_STATUS, "
//					+ "       T.DESCP TASK_DESCP, "
//					+ "       1 TASK_TYPE,"
//					+ "       NVL(S.PERCENT,0) PERCENT,"
//					+ "       NVL(S.DIFF_DATE,0) DIFF_DATE,"
//					+ "       NVL(S.PROGRESS,0) PROGRESS,"
//					+ "       NVL(S.COLLECT_PROGRESS,0) COLLECT_PROGRESS,"
//					+ "       NVL(S.COLLECT_PERCENT,0) COLLECT_PERCENT,"
//					+ "       NVL(S.DAILY_PROGRESS,0) DAILY_PROGRESS,"
//					+ "       NVL(S.DAILY_PERCENT,0) DAILY_PERCENT,"
//					+ "       NVL(S.MONTHLY_PROGRESS,0) MONTHLY_PROGRESS,"
//					+ "       NVL(S.MONTHLY_PERCENT,0) MONTHLY_PERCENT"
//					+ "  FROM TASK T, CITY C, FM_STAT_OVERVIEW_TASK S"
//					+ " WHERE T.CITY_ID = C.CITY_ID"
//					+ "   AND C.CITY_ID NOT IN (100000, 100001, 100002)"
//					+ "   AND T.TASK_ID = S.TASK_ID(+)"
//					+ "   AND T.STATUS = 1"
//					+ "   AND T.LATEST = 1"
//					+ "   AND (EXISTS(SELECT 1"
//					+ "                    FROM BLOCK_MAN M"
//					+ "                   WHERE M.TASK_ID = T.TASK_ID"
//					+ "                     AND M.STATUS <> 0)"
//					+ "      OR NOT EXISTS(SELECT 1 FROM BLOCK_MAN M"
//					+ "                   WHERE M.TASK_ID = T.TASK_ID))),"
//					+ " FINAL_TABLE AS"
//					+ " (SELECT DISTINCT *"
//					+ "    FROM TASK_LIST"
//					+ "    WHERE 1=1"
//					+  conditionSql
//					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.PERCENT DESC)"
//					+ " SELECT /*+FIRST_ROWS ORDERED*/"
//					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
//					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
//					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
//			//System.out.println(selectSql);
//			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
//	public static Page getCommonOverListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			/* 
//			 * 记录默认排序原则：
//			 * ①根据状态排序，100%>已关闭
//			 * ②根据剩余工期排序，逾期>按时>提前
//			 * ③根据任务名称排序
//			 * 
//			 * • 点击搜索，根据城市名称\任务名称模糊查询
//			 * • 点击筛选，根据分类筛选，可多选
//			 */
//			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
//			long pageEndNum = currentPageNum * pageSize;
//			String conditionSql="";
//			String statusSql="";
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if("name".equals(key)){
//						conditionSql=conditionSql+" AND (TASK_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
//					if ("cityId".equals(key)) {
//						conditionSql+=" AND TASK_LIST.CITY_ID="+conditionJson.getInt(key);}
//					if ("diffDate".equals(key)) {
//						JSONArray diffDateArray=conditionJson.getJSONArray(key);
//						for(Object diffDate:diffDateArray){
//							if((int) diffDate==1){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date>0";
//							}
//							if((int) diffDate==0){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date=0";
//							}
//							if((int) diffDate==-1){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date<0";
//							}
//							}
//						}
//					//1-11未规划,草稿,采集正常,采集异常,采集完成,日编正常,日编异常,日编完成,按时完成,提前完成,逾期完成
//					if("selectParam1".equals(key)){
//						JSONArray selectParam1=conditionJson.getJSONArray(key);
//						for(Object i:selectParam1){
//							int tmp=(int) i;												
//							if(tmp==9){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date=0";
//							}
//							if(tmp==10){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date>0";
//							}
//							if(tmp==11){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date<0";
//							}
//						}
//					}
//				}	
//				if(!statusSql.isEmpty()){//有非status
//					conditionSql+=" and ("+statusSql+")";}	
//			}			
//			
//			//分页显示列表，不带条件查询
//			String selectSql = "WITH TASK_LIST AS"
//					+ " (SELECT T.TASK_ID,"
//					+ "       T.NAME        TASK_NAME,"
//					+ "       C.CITY_ID,"
//					+ "       C.CITY_NAME,"
//					+ "       C.PLAN_STATUS,"
//					+ "       T.STATUS      TASK_STATUS, "
//					+ "       T.DESCP TASK_DESCP, "
//					+ "       1 TASK_TYPE,"
//					+ "       S.PERCENT,"
//					+ "       S.DIFF_DATE,"
//					+ "       S.PROGRESS,"
//					+ "       S.COLLECT_PROGRESS,"
//					+ "       S.COLLECT_PERCENT,"
//					+ "       S.DAILY_PROGRESS,"
//					+ "       S.DAILY_PERCENT,"
//					+ "       S.MONTHLY_PROGRESS,"
//					+ "       S.MONTHLY_PERCENT"
//					+ "  FROM TASK T, CITY C, FM_STAT_OVERVIEW_TASK S,BLOCK_MAN BM"
//					+ " WHERE T.CITY_ID = C.CITY_ID"
//					+ "   AND C.CITY_ID NOT IN (100000, 100001, 100002)"
//					+ "   AND T.TASK_ID = S.TASK_ID(+)"
//					+ "   AND T.TASK_ID = BM.TASK_ID"
//					//+ "   AND S.PERCENT = 100"
//					+ "   AND T.STATUS = 1"
//					+ "   AND T.LATEST = 1"
//					+ "   AND NOT EXISTS(SELECT 1"
//					+ "                    FROM BLOCK_MAN M"
//					+ "                   WHERE M.TASK_ID = T.TASK_ID"
//					+ "                     AND M.LATEST = 1"
//					+ "                     AND M.STATUS <> 0)),"
//					+ " FINAL_TABLE AS"
//					+ " (SELECT DISTINCT *"
//					+ "    FROM TASK_LIST"
//					+ "    WHERE 1=1"
//					+  conditionSql
//					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.TASK_NAME DESC)"
//					+ " SELECT /*+FIRST_ROWS ORDERED*/"
//					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
//					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
//					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
//			//System.out.println(selectSql);
//			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
//	public static Page getCommonCloseListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			/* 
//			 * 记录默认排序原则：
//			 * ①根据状态排序，100%>已关闭
//			 * ②根据剩余工期排序，逾期>按时>提前
//			 * ③根据任务名称排序
//			 * 
//			 * • 点击搜索，根据城市名称\任务名称模糊查询
//			 * • 点击筛选，根据分类筛选，可多选
//			 */
//			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
//			long pageEndNum = currentPageNum * pageSize;
//			String conditionSql="";
//			String statusSql="";
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if("name".equals(key)){
//						conditionSql=conditionSql+" AND (TASK_LIST.CITY_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
//					if ("cityId".equals(key)) {
//						conditionSql+=" AND TASK_LIST.CITY_ID="+conditionJson.getInt(key);}
//					if ("diffDate".equals(key)) {
//						JSONArray diffDateArray=conditionJson.getJSONArray(key);
//						for(Object diffDate:diffDateArray){
//							if((int) diffDate==1){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date>0";
//							}
//							if((int) diffDate==0){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date=0";
//							}
//							if((int) diffDate==-1){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date<0";
//							}
//							}
//						}
//					//1-11未规划,草稿,采集正常,采集异常,采集完成,日编正常,日编异常,日编完成,按时完成,提前完成,逾期完成
//					if("selectParam1".equals(key)){
//						JSONArray selectParam1=conditionJson.getJSONArray(key);
//						for(Object i:selectParam1){
//							int tmp=(int) i;												
//							if(tmp==9){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date=0";
//							}
//							if(tmp==10){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date>0";
//							}
//							if(tmp==11){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date<0";
//							}
//						}
//					}
//				}	
//				if(!statusSql.isEmpty()){//有非status
//					conditionSql+=" and ("+statusSql+")";}	
//			}			
//			
//			//分页显示列表，不带条件查询
//			String selectSql = "WITH TASK_LIST AS"
//					+ " (SELECT T.TASK_ID,"
//					+ "       T.NAME        TASK_NAME,"
//					+ "       C.CITY_ID,"
//					+ "       C.CITY_NAME,"
//					+ "       C.PLAN_STATUS,"
//					+ "       T.STATUS      TASK_STATUS, "
//					+ "       T.DESCP TASK_DESCP, "
//					+ "       1 TASK_TYPE,"
//					+ "       S.PERCENT,"
//					+ "       S.DIFF_DATE,"
//					+ "       S.PROGRESS,"
//					+ "       S.COLLECT_PROGRESS,"
//					+ "       S.COLLECT_PERCENT,"
//					+ "       S.DAILY_PROGRESS,"
//					+ "       S.DAILY_PERCENT,"
//					+ "       S.MONTHLY_PROGRESS,"
//					+ "       S.MONTHLY_PERCENT"
//					+ "  FROM TASK T, CITY C, FM_STAT_OVERVIEW_TASK S"
//					+ " WHERE T.CITY_ID = C.CITY_ID"
//					+ "   AND C.CITY_ID NOT IN (100000, 100001, 100002)"
//					+ "   AND T.TASK_ID = S.TASK_ID(+)"
//					+ "   AND T.STATUS = 0"
//					+ "   AND T.LATEST = 1),"
//					+ " FINAL_TABLE AS"
//					+ " (SELECT DISTINCT *"
//					+ "    FROM TASK_LIST"
//					+ "    WHERE 1=1"
//					+  conditionSql
//					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.TASK_NAME DESC)"
//					+ " SELECT /*+FIRST_ROWS ORDERED*/"
//					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
//					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
//					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
//			//System.out.println(selectSql);
//			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
//	public static Page getInforUnPushListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			/* 记录默认排序原则：根据城市名称排序
//			 * 点击搜索，根据城市名称\任务名称模糊查询
//			 * • 点击筛选，根据任务状态筛选，可多选
//			 * • 点击更多，跳转到<全国任务详情列表>页面，可进行批量操作
//			 */
//			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
//			long pageEndNum = currentPageNum * pageSize;
//			String conditionSql="";
//			String statusSql="";
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if("name".equals(key)){
//						conditionSql=conditionSql+" AND (TASK_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
//					if ("taskStatus".equals(key)) {
//						if(!statusSql.isEmpty()){statusSql+=" or ";}
//						statusSql+=" TASK_LIST.TASK_STATUS="+conditionJson.getInt(key);}
//					if ("inforId".equals(key)) {
//						conditionSql+=" AND TASK_LIST.INFOR_ID='"+conditionJson.getString(key)+"'";}
//					if ("blockPlanStatus".equals(key)) {
//						if(!statusSql.isEmpty()){statusSql+=" or ";}
//						statusSql+=" TASK_LIST.INFOR_PLAN_STATUS="+conditionJson.getInt(key);}
//					//1-11未规划,草稿,采集正常,采集异常,采集完成,日编正常,日编异常,日编完成,按时完成,提前完成,逾期完成
//					if("selectParam1".equals(key)){
//						JSONArray selectParam1=conditionJson.getJSONArray(key);
//						for(Object i:selectParam1){
//							int tmp=(int) i;
//							if(tmp==1){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.INFOR_PLAN_STATUS =0";}
//							if(tmp==2){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.TASK_STATUS =2";}	
//						}
//					}
//					
//				}
//				
//			}	
//			if(!statusSql.isEmpty()){//有非status
//				conditionSql+=" and ("+statusSql+")";}
//			
//			//分页显示列表，不带条件查询
//			String selectSql = "WITH TASK_LIST AS"
//					+ " (SELECT T.TASK_ID,"
//					+ "         T.NAME TASK_NAME,"
//					+ "         C.INFOR_ID,"
//					+ "         C.INFOR_NAME,"
//					+ "         C.PLAN_STATUS INFOR_PLAN_STATUS,"
//					+ "         T.STATUS TASK_STATUS, "
//					+ "         T.DESCP TASK_DESCP, "
//					+ "         4 TASK_TYPE"
//					+ "    FROM TASK T, INFOR C"
//					+ "   WHERE T.TASK_ID=C.TASK_ID"
//					+ "   AND T.STATUS=2"
//					+ "   AND T.LATEST=1"
//					+ "  UNION"
//					+ "  SELECT 0,"
//					+ "         '---',"
//					+ "         C.INFOR_ID,"
//					+ "         C.INFOR_NAME,"
//					+ "         C.PLAN_STATUS,"
//					+ "         0,"
//					+ "         '---' TASK_DESCP, "
//					+ "         4 TASK_TYPE"
//					+ "    FROM INFOR C"
//					+ "   WHERE C.PLAN_STATUS=0),"
//					+ " FINAL_TABLE AS"
//					+ " (SELECT DISTINCT *"
//					+ "    FROM TASK_LIST"
//					+ "    WHERE 1=1"
//					+  conditionSql
//					+ "   ORDER BY TASK_LIST.INFOR_NAME DESC)"
//					+ " SELECT /*+FIRST_ROWS ORDERED*/"
//					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
//					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
//					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
//			//System.out.println(selectSql);
//			return run.query(conn, selectSql, getUnPushSnapshotQuery(currentPageNum,pageSize));		
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
//	public static Page getInforPushListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			/* 记录默认排序原则：
//			 * ①根据剩余工期排序，逾期>剩余
//			 * ②相同剩余工期，根据完成度排序，完成度高>完成度低
//			 * • 点击搜索，根据城市名称\任务名称模糊查询
//			 * • 点击筛选，根据分类筛选，可多选 采集/日编 正常/异常/完成
//			 */
//			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
//			long pageEndNum = currentPageNum * pageSize;
//			String conditionSql="";
//			String statusSql="";
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if("name".equals(key)){
//						conditionSql=conditionSql+" AND (TASK_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
//					if ("inforId".equals(key)) {
//						conditionSql+=" AND TASK_LIST.INFOR_ID='"+conditionJson.getString(key)+"'";}
//					if ("collectProgress".equals(key)) {
//						if(!statusSql.isEmpty()){statusSql+=" or ";}
//						statusSql+=" TASK_LIST.collect_Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}
//					if ("dailyProgress".equals(key)) {
//						if(!statusSql.isEmpty()){statusSql+=" or ";}
//						statusSql+=" TASK_LIST.daily_Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}
//					//1-11未规划,草稿,采集正常,采集异常,采集完成,日编正常,日编异常,日编完成,按时完成,提前完成,逾期完成
//					if("selectParam1".equals(key)){
//						JSONArray selectParam1=conditionJson.getJSONArray(key);
//						JSONArray collectProgress=new JSONArray();
//						JSONArray dailyProgress=new JSONArray();
//						for(Object i:selectParam1){
//							int tmp=(int) i;
//							if(tmp==3||tmp==4||tmp==5){collectProgress.add(tmp-2);}
//							if(tmp==6||tmp==7||tmp==8){dailyProgress.add(tmp-5);}												
//						}
//						if(!collectProgress.isEmpty()){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" TASK_LIST.collect_Progress IN ("+collectProgress.join(",")+")";}
//						if(!dailyProgress.isEmpty()){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" TASK_LIST.daily_Progress IN ("+dailyProgress.join(",")+")";}
//					}
//				}
//			}	
//			if(!statusSql.isEmpty()){//有非status
//				conditionSql+=" and ("+statusSql+")";}
//			
//			//分页显示列表，不带条件查询
//			String selectSql = "WITH TASK_LIST AS"
//					+ " (SELECT T.TASK_ID,"
//					+ "       T.NAME        TASK_NAME,"
//					+ "       C.INFOR_ID,"
//					+ "       C.INFOR_NAME,"
//					+ "       C.PLAN_STATUS,"
//					+ "       T.STATUS      TASK_STATUS, "
//					+ "       T.DESCP TASK_DESCP, "
//					+ "       4 TASK_TYPE,"
//					+ "       NVL(S.PERCENT,0) PERCENT,"
//					+ "       NVL(S.DIFF_DATE,0) DIFF_DATE,"
//					+ "       NVL(S.PROGRESS,0) PROGRESS,"
//					+ "       NVL(S.COLLECT_PROGRESS,0) COLLECT_PROGRESS,"
//					+ "       NVL(S.COLLECT_PERCENT,0) COLLECT_PERCENT,"
//					+ "       NVL(S.DAILY_PROGRESS,0) DAILY_PROGRESS,"
//					+ "       NVL(S.DAILY_PERCENT,0) DAILY_PERCENT,"
//					+ "       NVL(S.MONTHLY_PROGRESS,0) MONTHLY_PROGRESS,"
//					+ "       NVL(S.MONTHLY_PERCENT,0) MONTHLY_PERCENT"
//					+ "  FROM TASK T, INFOR C, FM_STAT_OVERVIEW_TASK S"
//					+ " WHERE T.TASK_ID = C.TASK_ID"
//					+ "   AND T.TASK_ID = S.TASK_ID(+)"
//					+ "   AND T.STATUS = 1"
//					+ "   AND T.LATEST = 1"
//					+ "   AND (EXISTS(SELECT 1"
//					+ "                    FROM BLOCK_MAN M"
//					+ "                   WHERE M.TASK_ID = T.TASK_ID"
//					+ "                     AND M.STATUS <> 0)"
//					+ "      OR NOT EXISTS(SELECT 1 FROM BLOCK_MAN M"
//					+ "                   WHERE M.TASK_ID = T.TASK_ID))"
//					+ "   AND (EXISTS(SELECT 1"
//					+ "                    FROM SUBTASK ST"
//					+ "                   WHERE ST.TASK_ID = T.TASK_ID"
//					+ "                     AND ST.STATUS <> 0)"
//					+ "      OR NOT EXISTS(SELECT 1 FROM SUBTASK ST"
//					+ "                   WHERE ST.TASK_ID = T.TASK_ID))),"
//					+ " FINAL_TABLE AS"
//					+ " (SELECT DISTINCT *"
//					+ "    FROM TASK_LIST"
//					+ "    WHERE 1=1"
//					+  conditionSql
//					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.PERCENT DESC)"
//					+ " SELECT /*+FIRST_ROWS ORDERED*/"
//					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
//					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
//					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
//			//System.out.println(selectSql);
//			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
//		}
//		catch(Exception e)
//		{
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
//	public static Page getInforOverListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			/* 
//			 * 记录默认排序原则：
//			 * ①根据状态排序，100%>已关闭
//			 * ②根据剩余工期排序，逾期>按时>提前
//			 * ③根据任务名称排序
//			 * 
//			 * • 点击搜索，根据城市名称\任务名称模糊查询
//			 * • 点击筛选，根据分类筛选，可多选
//			 */
//			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
//			long pageEndNum = currentPageNum * pageSize;
//			String conditionSql="";
//			String statusSql="";
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if("name".equals(key)){
//						conditionSql=conditionSql+" AND (TASK_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
//					if ("inforId".equals(key)) {
//						conditionSql+=" AND TASK_LIST.INFOR_ID='"+conditionJson.getString(key)+"'";}
//					//1-11未规划,草稿,采集正常,采集异常,采集完成,日编正常,日编异常,日编完成,按时完成,提前完成,逾期完成
//					if("selectParam1".equals(key)){
//						JSONArray selectParam1=conditionJson.getJSONArray(key);
//						for(Object i:selectParam1){
//							int tmp=(int) i;													
//							if(tmp==9){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date=0";
//							}
//							if(tmp==10){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date>0";
//							}
//							if(tmp==11){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date<0";
//							}
//						}
//					}
//					
//					if ("diffDate".equals(key)) {
//						JSONArray diffDateArray=conditionJson.getJSONArray(key);
//						for(Object diffDate:diffDateArray){
//							if((int) diffDate==1){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date>0";
//							}
//							if((int) diffDate==0){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date=0";
//							}
//							if((int) diffDate==-1){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date<0";
//							}
//							}
//						}
//				}	
//				if(!statusSql.isEmpty()){//有非status
//					conditionSql+=" and ("+statusSql+")";}	
//			}			
//			
//			//分页显示列表，不带条件查询
//			String selectSql = "WITH TASK_LIST AS"
//					+ " (SELECT T.TASK_ID,"
//					+ "       T.NAME        TASK_NAME,"
//					+ "       C.INFOR_ID,"
//					+ "       C.INFOR_NAME,"
//					+ "       C.PLAN_STATUS,"
//					+ "       T.STATUS      TASK_STATUS, "
//					+ "       T.DESCP TASK_DESCP, "
//					+ "       4 TASK_TYPE,"
//					+ "       S.PERCENT,"
//					+ "       S.DIFF_DATE,"
//					+ "       S.PROGRESS,"
//					+ "       S.COLLECT_PROGRESS,"
//					+ "       S.COLLECT_PERCENT,"
//					+ "       S.DAILY_PROGRESS,"
//					+ "       S.DAILY_PERCENT,"
//					+ "       NVL(S.MONTHLY_PROGRESS,0) MONTHLY_PROGRESS,"
//					+ "       NVL(S.MONTHLY_PERCENT,0) MONTHLY_PERCENT"
//					+ "  FROM TASK T, INFOR C, FM_STAT_OVERVIEW_TASK S,BLOCK_MAN BM"
//					+ " WHERE T.task_ID = C.TASK_ID"
//					+ "   AND T.TASK_ID = S.TASK_ID(+)"
//					+ "   AND T.TASK_ID=BM.TASK_ID"
//					//+ "   AND S.PERCENT = 100"
//					+ "   AND T.STATUS = 1"
//					+ "   AND T.LATEST = 1"
//					+ "   AND NOT EXISTS(SELECT 1"
//					+ "                    FROM BLOCK_MAN M"
//					+ "                   WHERE M.TASK_ID = T.TASK_ID"
//					+ "                     AND M.LATEST = 1"
//					+ "                     AND M.STATUS <> 0)"
//					+ "   AND NOT EXISTS(SELECT 1"
//					+ "                    FROM SUBTASK M"
//					+ "                   WHERE M.TASK_ID = T.TASK_ID"
//					+ "                     AND M.STATUS <> 0)),"
//					+ " FINAL_TABLE AS"
//					+ " (SELECT DISTINCT *"
//					+ "    FROM TASK_LIST"
//					+ "    WHERE 1=1"
//					+  conditionSql
//					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.TASK_NAME DESC)"
//					+ " SELECT /*+FIRST_ROWS ORDERED*/"
//					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
//					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
//					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
//			//System.out.println(selectSql);
//			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
//	public static Page getInforCloseListSnapshot(Connection conn,JSONObject conditionJson,int currentPageNum,int pageSize) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			/* 
//			 * 记录默认排序原则：
//			 * ①根据状态排序，100%>已关闭
//			 * ②根据剩余工期排序，逾期>按时>提前
//			 * ③根据任务名称排序
//			 * 
//			 * • 点击搜索，根据城市名称\任务名称模糊查询
//			 * • 点击筛选，根据分类筛选，可多选
//			 */
//			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
//			long pageEndNum = currentPageNum * pageSize;
//			String conditionSql="";
//			String statusSql="";
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if("name".equals(key)){
//						conditionSql=conditionSql+" AND (TASK_LIST.INFOR_NAME LIKE '%"+conditionJson.getString(key)+"%' OR TASK_LIST.TASK_NAME LIKE '%"+conditionJson.getString(key)+"%')";}
//					if ("inforId".equals(key)) {
//						conditionSql+=" AND TASK_LIST.INFOR_ID='"+conditionJson.getString(key)+"'";}
//					if ("diffDate".equals(key)) {
//						JSONArray diffDateArray=conditionJson.getJSONArray(key);
//						for(Object diffDate:diffDateArray){
//							if((int) diffDate==1){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date>0";
//							}
//							if((int) diffDate==0){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date=0";
//							}
//							if((int) diffDate==-1){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date<0";
//							}
//							}
//						}
//					//1-11未规划,草稿,采集正常,采集异常,采集完成,日编正常,日编异常,日编完成,按时完成,提前完成,逾期完成
//					if("selectParam1".equals(key)){
//						JSONArray selectParam1=conditionJson.getJSONArray(key);
//						for(Object i:selectParam1){
//							int tmp=(int) i;													
//							if(tmp==9){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date=0";
//							}
//							if(tmp==10){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date>0";
//							}
//							if(tmp==11){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date<0";
//							}
//						}
//					}
//				}	
//				if(!statusSql.isEmpty()){//有非status
//					conditionSql+=" and ("+statusSql+")";}	
//			}			
//			
//			//分页显示列表，不带条件查询
//			String selectSql = "WITH TASK_LIST AS"
//					+ " (SELECT T.TASK_ID,"
//					+ "       T.NAME        TASK_NAME,"
//					+ "       C.INFOR_ID,"
//					+ "       C.INFOR_NAME,"
//					+ "       C.PLAN_STATUS,"
//					+ "       T.STATUS      TASK_STATUS, "
//					+ "       T.DESCP TASK_DESCP, "
//					+ "       4 TASK_TYPE,"
//					+ "       S.PERCENT,"
//					+ "       S.DIFF_DATE,"
//					+ "       S.PROGRESS,"
//					+ "       S.COLLECT_PROGRESS,"
//					+ "       S.COLLECT_PERCENT,"
//					+ "       S.DAILY_PROGRESS,"
//					+ "       S.DAILY_PERCENT,"
//					+ "       NVL(S.MONTHLY_PROGRESS,0) MONTHLY_PROGRESS,"
//					+ "       NVL(S.MONTHLY_PERCENT,0) MONTHLY_PERCENT"
//					+ "  FROM TASK T, INFOR C, FM_STAT_OVERVIEW_TASK S"
//					+ " WHERE T.task_ID = C.TASK_ID"
//					+ "   AND T.TASK_ID = S.TASK_ID(+)"
//					+ "   AND T.STATUS = 0"
//					+ "   AND T.LATEST = 1),"
//					+ " FINAL_TABLE AS"
//					+ " (SELECT DISTINCT *"
//					+ "    FROM TASK_LIST"
//					+ "    WHERE 1=1"
//					+  conditionSql
//					+ "   ORDER BY TASK_LIST.DIFF_DATE ASC,TASK_LIST.TASK_NAME DESC)"
//					+ " SELECT /*+FIRST_ROWS ORDERED*/"
//					+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
//					+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
//					+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
//			//System.out.println(selectSql);
//			return run.query(conn, selectSql, getOtherSnapshotQuery(currentPageNum,pageSize));		
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
//	/**
//	 * TASK_STATUS:1常规，2多源，3代理店，4情报
//	 * @param currentPageNum
//	 * @param pageSize
//	 * @return
//	 */
//	private static ResultSetHandler<Page> getUnPushSnapshotQuery(final int currentPageNum,final int pageSize){
//		final String version=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
//		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
//			public Page handle(ResultSet rs) throws SQLException {
//				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//				Page page = new Page(currentPageNum);
//			    page.setPageSize(pageSize);
//			    int total=0;
//				while(rs.next()){
//					Map<String, Object> map = new HashMap<String, Object>();
//					map.put("taskId", rs.getInt("TASK_ID"));
//					map.put("taskName", rs.getString("TASK_NAME"));
//					map.put("taskDescp", rs.getString("TASK_DESCP"));
//					
//					if(rs.getInt("TASK_TYPE")==1){
//						map.put("cityId", rs.getInt("CITY_ID"));
//						map.put("cityName", rs.getString("CITY_NAME"));
//						map.put("cityPlanStatus", rs.getInt("CITY_PLAN_STATUS"));}
//					else if(rs.getInt("TASK_TYPE")==4){
//						map.put("inforId", rs.getString("INFOR_ID"));
//						map.put("inforName", rs.getString("INFOR_NAME"));
//						map.put("inforPlanStatus", rs.getInt("INFOR_PLAN_STATUS"));}
//					map.put("taskType", rs.getInt("TASK_TYPE"));
//					map.put("taskStatus", rs.getInt("TASK_STATUS"));
//					map.put("version", version);
//					total=rs.getInt("TOTAL_RECORD_NUM");
//					list.add(map);
//				}
//				page.setTotalCount(total);
//				page.setResult(list);
//				return page;
//			}
//    	};
//    	return rsHandler;
//	}
//	
//	/**
//	 * TASK_STATUS:1常规，2多源，3代理店，4情报
//	 * @param currentPageNum
//	 * @param pageSize
//	 * @return
//	 */
//	private static ResultSetHandler<Page> getOtherSnapshotQuery(final int currentPageNum,final int pageSize){
//		final String version=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
//		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
//			public Page handle(ResultSet rs) throws SQLException {
//				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//				Page page = new Page(currentPageNum);
//			    page.setPageSize(pageSize);
//			    int total=0;
//				while(rs.next()){
//					Map<String, Object> map = new HashMap<String, Object>();
//					map.put("taskId", rs.getInt("TASK_ID"));
//					map.put("taskName", rs.getString("TASK_NAME"));
//					if(rs.getInt("TASK_TYPE")==1){
//						map.put("cityId", rs.getInt("CITY_ID"));
//						map.put("cityName", rs.getString("CITY_NAME"));
//						map.put("cityPlanStatus", rs.getInt("PLAN_STATUS"));}
//					else if(rs.getInt("TASK_TYPE")==4){
//						map.put("inforId", rs.getString("INFOR_ID"));
//						map.put("inforName", rs.getString("INFOR_NAME"));
//						map.put("inforPlanStatus", rs.getInt("PLAN_STATUS"));}
//					map.put("taskType", rs.getInt("TASK_TYPE"));
//					map.put("taskStatus", rs.getInt("TASK_STATUS"));					
//					map.put("percent", rs.getInt("PERCENT"));
//					map.put("diffDate", rs.getInt("DIFF_DATE"));
//					map.put("progress", rs.getInt("PROGRESS"));
//					map.put("collectProgress", rs.getInt("COLLECT_PROGRESS"));
//					map.put("collectPercent", rs.getInt("COLLECT_PERCENT"));
//					map.put("dailyProgress", rs.getInt("DAILY_PROGRESS"));
//					map.put("dailyPercent", rs.getInt("DAILY_PERCENT"));
//					map.put("monthlyProgress", rs.getInt("MONTHLY_PROGRESS"));
//					map.put("monthlyPercent", rs.getInt("MONTHLY_PERCENT"));
//					map.put("version", version);
//					total=rs.getInt("TOTAL_RECORD_NUM");
//					list.add(map);
//				}
//				page.setTotalCount(total);
//				page.setResult(list);
//				return page;
//			}
//    	};
//    	return rsHandler;
//	}
//	
//	public static Page getListIntegrate(Connection conn,JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
//			long pageEndNum = currentPageNum * pageSize;
//			
//			/*默认任务ID排序显示
//			 *搜索功能，搜索项包括：任务 ID，任务名称，创建人，城市/情报名称；
//			 *筛选功能，筛选项包括：任务状态(未规划/草稿/开启/关闭)
//			 *排序功能，排序项包括：任务 ID，任务总体计划开始时间，任务总体计划结束时间,城市/情报名称,任务状态,任务类型
//			 *搜索、筛选、排序功能可同时使用*/
//			String conditionSql="";
//			String statusSql="";
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if("taskStatus".equals(key) || "planStatus".equals(key)){
//						if(!statusSql.isEmpty()){statusSql+=" or ";}
//					}
//					else{
//						if(conditionSql.isEmpty()){conditionSql+=" where ";}
//						else{conditionSql+=" and ";}}
//					
//					if ("upperLevelId".equals(key)) {
//						conditionSql+=" AND TASK_LIST.UPPER_LEVEL_ID='"+conditionJson.getString(key)+"'";}
//					
//					//城市/情报名称
//					if ("upperLevelName".equals(key)) {conditionSql+=" TASK_LIST.UPPER_LEVEL_NAME like '%"+conditionJson.getString(key)+"%'";}
//					if ("taskId".equals(key)) {conditionSql+=" TASK_LIST.TASK_ID="+conditionJson.getInt(key);}
//					if ("taskName".equals(key)) {conditionSql+=" TASK_LIST.TASK_NAME like '%"+conditionJson.getString(key)+"%'";}
//					if ("createUserName".equals(key)) {conditionSql+=" TASK_LIST.create_User_Name like '%"+conditionJson.getString(key)+"%'";}
//					
//					//1-11未规划,草稿,采集正常,采集异常,采集完成,日编正常,日编异常,日编完成,按时完成,提前完成,逾期完成
//					if("selectParam1".equals(key)){
//						JSONArray selectParam1=conditionJson.getJSONArray(key);
//						JSONArray collectProgress=new JSONArray();
//						JSONArray dailyProgress=new JSONArray();
//						for(Object i:selectParam1){
//							int tmp=(int) i;
//							if(tmp==3||tmp==4||tmp==5){collectProgress.add(tmp-2);}
//							if(tmp==6||tmp==7||tmp==8){dailyProgress.add(tmp-5);}
//							if(tmp==1){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.PLAN_STATUS =0";}
//							if(tmp==2){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.TASK_STATUS =2";}													
//							if(tmp==9){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date=0";
//							}
//							if(tmp==10){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date>0";
//							}
//							if(tmp==11){
//								if(!statusSql.isEmpty()){statusSql+=" or ";}
//								statusSql+=" TASK_LIST.diff_date<0";
//							}
//						}
//						if(!collectProgress.isEmpty()){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" TASK_LIST.collect_Progress IN ("+collectProgress.join(",")+")";}
//						if(!dailyProgress.isEmpty()){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" TASK_LIST.daily_Progress IN ("+dailyProgress.join(",")+")";}
//					}
//					
//					if ("taskStatus".equals(key)) {statusSql+=" (TASK_LIST.TASK_STATUS IN ("+conditionJson.getJSONArray(key).join(",")+")"
//							+ " AND TASK_LIST.PLAN_STATUS!=0)";}
//					if ("planStatus".equals(key)) {statusSql+=" TASK_LIST.PLAN_STATUS="+conditionJson.getInt(key);}
//					}
//				if(conditionSql.isEmpty()){
//					//只有status
//					conditionSql+=" where "+statusSql;}
//				else if(!statusSql.isEmpty()){//有非status
//					conditionSql+=" and ("+statusSql+")";}
//				}
//			
//			String orderSql="";
//			if(null!=orderJson && !orderJson.isEmpty()){
//				Iterator keys = orderJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					//城市/情报名称,任务状态,任务类型
//					if ("taskId".equals(key)) {orderSql+=" order by TASK_LIST.TASK_ID "+orderJson.getString(key);break;}
//					if ("planStartDate".equals(key)) {orderSql+=" order by TASK_LIST.PLAN_START_DATE "+orderJson.getString(key);break;}
//					if ("planEndDate".equals(key)) {orderSql+=" order by TASK_LIST.PLAN_END_DATE "+orderJson.getString(key);break;}
//					
//					if ("upperLevelName".equals(key)) {orderSql+=" order by TASK_LIST.UPPER_LEVEL_NAME "+orderJson.getString(key);break;}
//					if ("taskStatus".equals(key)) {orderSql+=" order by TASK_LIST.TASK_STATUS "+orderJson.getString(key);break;}
//					if ("taskType".equals(key)) {orderSql+=" order by TASK_LIST.TASK_TYPE "+orderJson.getString(key);break;}
//					}
//			}else{
//				orderSql+=" order by TASK_LIST.TASK_ID";
//			}
//			
//			//分页显示列表，不带条件查询
//			String selectSql = "WITH T AS"
//					+ " (SELECT T.TASK_ID,"
//					+ "         T.NAME,"
//					+ "         T.DESCP,"
//					+ "         T.CITY_ID,"
//					+ "         T.TASK_TYPE,"
//					+ "         T.PLAN_START_DATE,"
//					+ "         T.PLAN_END_DATE,"
//					+ "         T.MONTH_EDIT_PLAN_START_DATE,"
//					+ "         T.MONTH_EDIT_PLAN_END_DATE,"
//					+ "         T.MONTH_EDIT_GROUP_ID,"
//					+ "         T.MONTH_PRODUCE_PLAN_START_DATE,"
//					+ "         T.MONTH_PRODUCE_PLAN_END_DATE,"
//					+ "         T.STATUS,"
//					+ "         NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,"
//					+ "         NVL(U.USER_REAL_NAME, '---') CREATE_USER_NAME,"
//					+ "         G.GROUP_NAME MONTH_EDIT_GROUP_NAME,"
//					+ "         S.PERCENT,S.collect_Progress,S.DAILY_Progress,S.DIFF_DATE,"
//					+ "         S.MONTHLY_Progress"
//					+ "    FROM TASK T, FM_STAT_OVERVIEW_TASK S, USER_INFO U, USER_GROUP G"
//					+ "   WHERE S.TASK_ID(+) = T.TASK_ID"
//					+ "     AND T.LATEST = 1"
//					+ "     AND T.CREATE_USER_ID = U.USER_ID(+)"
//					+ "     AND T.MONTH_EDIT_GROUP_ID = G.GROUP_ID(+)),"
//					+ " TASK_LIST AS"
//					+ " (SELECT NVL(T.TASK_ID, 0) TASK_ID,"
//					+ "         NVL(T.NAME, '---') TASK_NAME,"
//					+ "         NVL(T.DESCP, '---') TASK_DESCP,"
//					+ "         TO_CHAR(C.CITY_ID) UPPER_LEVEL_ID,"
//					+ "         C.CITY_NAME UPPER_LEVEL_NAME,"
//					+ "         CASE C.CITY_ID"
//					+ "           WHEN 100000 THEN 2"
//					+ "           WHEN 100001 THEN 3"
//					+ "           ELSE 1 END TASK_TYPE,"
//					+ "         T.PLAN_START_DATE,"
//					+ "         T.PLAN_END_DATE,"
//					+ "         T.MONTH_EDIT_PLAN_START_DATE,"
//					+ "         T.MONTH_EDIT_PLAN_END_DATE,"
//					+ "         NVL(T.MONTH_EDIT_GROUP_ID, 0) MONTH_EDIT_GROUP_ID,"
//					+ "         T.MONTH_PRODUCE_PLAN_START_DATE,"
//					+ "         T.MONTH_PRODUCE_PLAN_END_DATE,"
//					+ "         NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,"
//					+ "         NVL(T.CREATE_USER_NAME, '---') CREATE_USER_NAME,"
//					+ "         T.MONTH_EDIT_GROUP_NAME, "
//					+ "         NVL(T.STATUS, 0) TASK_STATUS,"
//					+ "         C.PLAN_STATUS,"
//					+ "         NVL(T.PERCENT, 0) PERCENT,"
//					+ " NVL(T.collect_Progress, 0) collect_Progress,NVL(T.DAILY_Progress, 0) DAILY_Progress,"
//					+ "NVL(T.DIFF_DATE, 0) DIFF_DATE,NVL(T.MONTHLY_Progress, 0) monthly_Progress "
//					+ "    FROM T, CITY C"
//					+ "   WHERE T.CITY_ID(+) = C.CITY_ID"
//					+ "     AND C.CITY_ID <> 100002"
//					+ "  UNION ALL"
//					+ "  SELECT NVL(T.TASK_ID, 0) TASK_ID,"
//					+ "         NVL(T.NAME, '---') NAME,"
//					+ "         NVL(T.DESCP, '---') TASK_DESCP,"
//					+ "         I.INFOR_ID,"
//					+ "         I.INFOR_NAME,"
//					+ "         4 TASK_TYPE,"
//					+ "         T.PLAN_START_DATE,"
//					+ "         T.PLAN_END_DATE,"
//					+ "         T.MONTH_EDIT_PLAN_START_DATE,"
//					+ "         T.MONTH_EDIT_PLAN_END_DATE,"
//					+ "         NVL(T.MONTH_EDIT_GROUP_ID, 0) MONTH_EDIT_GROUP_ID,"
//					+ "         T.MONTH_PRODUCE_PLAN_START_DATE,"
//					+ "         T.MONTH_PRODUCE_PLAN_END_DATE,"
//					+ "         NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,"
//					+ "         NVL(T.CREATE_USER_NAME, '---') CREATE_USER_NAME,"
//					+ "         NVL(T.MONTH_EDIT_GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,"
//					+ "         NVL(T.STATUS, 0) TASK_STATUS,"
//					+ "         I.PLAN_STATUS,"
//					+ "         NVL(T.PERCENT, 0) PERCENT,"
//					+ " NVL(T.collect_Progress, 0) collect_Progress,NVL(T.DAILY_Progress, 0) DAILY_Progress,"
//					+ "NVL(T.DIFF_DATE, 0) DIFF_DATE,NVL(T.MONTHLY_Progress, 0) monthly_Progress "
//					+ "    FROM T, INFOR I"
//					+ "   WHERE T.TASK_ID(+) = I.TASK_ID),"
//					+ " QUERY AS"
//					+ " (SELECT DISTINCT *"
//					+ "    FROM TASK_LIST"
//					+conditionSql
//					+orderSql
//					+ ")"
//					+ " SELECT /*+FIRST_ROWS ORDERED*/"
//					+ " T.*, (SELECT COUNT(1) FROM QUERY) AS TOTAL_RECORD_NUM"
//					+ "  FROM (SELECT T.*, ROWNUM AS ROWNUM_ FROM QUERY T WHERE ROWNUM <= "+pageEndNum+") T"
//					+ " WHERE T.ROWNUM_ >= "+pageStartNum;
//			return run.query(conn, selectSql, getIntegrateQuery(currentPageNum,pageSize));		
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
//	/**
//	 * TASK_STATUS:1常规，2多源，3代理店，4情报
//	 * @param currentPageNum
//	 * @param pageSize
//	 * @return
//	 */
//	private static ResultSetHandler<Page> getIntegrateQuery(final int currentPageNum,final int pageSize){
//		/*NVL(T.TASK_ID, 0) TASK_ID,NVL(T.NAME, '---') TASK_NAME,TO_CHAR(C.CITY_ID) UPPER_LEVEL_ID,
//          C.CITY_NAME UPPER_LEVEL_NAME,1 TASK_TYPE,T.PLAN_START_DATE,T.PLAN_END_DATE,
//          T.MONTH_EDIT_PLAN_START_DATE,T.MONTH_EDIT_PLAN_END_DATE,
//          NVL(T.MONTH_EDIT_GROUP_ID, 0) MONTH_EDIT_GROUP_ID,T.MONTH_PRODUCE_PLAN_START_DATE,
//          T.MONTH_PRODUCE_PLAN_END_DATE,NVL(T.CREATE_USER_ID, 0) CREATE_USER_ID,
//          NVL(T.CREATE_USER_NAME, '---') CREATE_USER_NAME,NVL(T.MONTH_EDIT_GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME,
//          NVL(T.STATUS, 0) TASK_STATUS,C.PLAN_STATUS,NVL(T.FINISH_PERCENT, 0) FINISH_PERCENT,
//          ROWNUM_,TOTAL_RECORD_NUM*/
//		final String version=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
//		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
//			public Page handle(ResultSet rs) throws SQLException {
//				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//				Page page = new Page(currentPageNum);
//			    page.setPageSize(pageSize);
//			    int total=0;
//				while(rs.next()){
//					Map<String, Object> map = new HashMap<String, Object>();
//					map.put("taskId", rs.getInt("TASK_ID"));
//					map.put("taskName", rs.getString("TASK_NAME"));
//					map.put("taskDescp", rs.getString("TASK_DESCP"));
//					map.put("upperLevelId", rs.getString("UPPER_LEVEL_ID"));
//					map.put("upperLevelName", rs.getString("UPPER_LEVEL_NAME"));
//					map.put("taskType", rs.getInt("TASK_TYPE"));
//					map.put("planStartDate", DateUtils.dateToString(rs.getTimestamp("PLAN_START_DATE")));
//					map.put("planEndDate", DateUtils.dateToString(rs.getTimestamp("PLAN_END_DATE")));
//					map.put("monthEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
//					map.put("monthEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
//					map.put("monthEditGroupId", rs.getInt("MONTH_EDIT_GROUP_ID"));
//					map.put("monthProducePlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_PRODUCE_PLAN_START_DATE")));
//					map.put("monthProducePlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_PRODUCE_PLAN_END_DATE")));
//					map.put("createUserId", rs.getInt("CREATE_USER_ID"));
//					map.put("createUserName", rs.getString("CREATE_USER_NAME"));
//					map.put("monthEditGroupName", rs.getString("MONTH_EDIT_GROUP_NAME"));
//					map.put("taskStatus", rs.getInt("TASK_STATUS"));
//					map.put("upperPlanStatus", rs.getInt("PLAN_STATUS"));
//					map.put("percent", rs.getInt("PERCENT"));
//					map.put("version", version);
//					//map.put("ROWNUM_", rs.getInt("ROWNUM_"));
//					//map.put("TOTAL_RECORD_NUM", rs.getInt("TOTAL_RECORD_NUM"));
//					total=rs.getInt("TOTAL_RECORD_NUM");
//					list.add(map);
//				}
//				page.setTotalCount(total);
//				page.setResult(list);
//				return page;
//			}
//    	};
//    	return rsHandler;
//	}
//		
	public static void updateTask(Connection conn,Task bean) throws Exception{
		try{
			String baseSql = "update task set ";
			QueryRunner run = new QueryRunner();
			String updateSql="";
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" DESCP= '" + bean.getDescp() + "'";
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" NAME='" + bean.getName() + "'";
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" PLAN_START_DATE=to_timestamp('"+ bean.getPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" PLAN_END_DATE=to_timestamp('"+ bean.getPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getProducePlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" PRODUCE_PLAN_START_DATE=to_timestamp('"+ bean.getProducePlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getProducePlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" PRODUCE_PLAN_END_DATE=to_timestamp('"+ bean.getProducePlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getLot()!=null && bean.getLot()!=0 && StringUtils.isNotEmpty(bean.getLot().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" LOT= " + bean.getLot();
			};
			if (bean!=null&&bean.getGroupId()!=null && bean.getGroupId()!=0 && StringUtils.isNotEmpty(bean.getGroupId().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" GROUP_ID= "+bean.getGroupId();
			};
			if (bean!=null&&bean.getTaskId()!=null && StringUtils.isNotEmpty(bean.getTaskId().toString())){
				updateSql+=" where TASK_ID=" + bean.getTaskId();
			};
			run.update(conn,baseSql+updateSql);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 
	 * @param conn
	 * @param condition 搜索条件{"taskIds":[1,2,3],"taskStatus":[1,2]}
	 * @return [{"taskId":12,"taskStatus":1,"taskName":"123","monthEditGroupId":2}]
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
		
		String selectSql="select t.task_id,t.status task_status,t.NAME task_name ,t.MONTH_EDIT_GROUP_ID MONTH_EDIT_GROUP_ID from task t where 1=1 "+conditionSql;
		
		ResultSetHandler<List<Map<String, Object>>> rsHandler = new ResultSetHandler<List<Map<String, Object>>>(){
			public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("taskId", rs.getLong("TASK_ID"));
					map.put("taskStatus", rs.getLong("TASK_STATUS"));
					map.put("taskName", rs.getString("TASK_NAME"));
					map.put("monthEditGroupId", rs.getLong("MONTH_EDIT_GROUP_ID"));
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
				+ "         NVL(G.GROUP_NAME, '---') MONTH_EDIT_GROUP_NAME"
				+ "    FROM TASK T, USER_INFO U, USER_GROUP G"
				+ "   WHERE T.LATEST = 1"
				+ "     AND T.CREATE_USER_ID = U.USER_ID(+)"
				+ "     AND T.MONTH_EDIT_GROUP_ID = G.GROUP_ID(+)),"
				+ " TASK_LIST AS"
				+ " (SELECT NVL(T.TASK_ID, 0) TASK_ID,"
				+ "         NVL(T.NAME, '---') TASK_NAME,"
				+ "         NVL(T.DESCP,'---') TASK_DESCP,"
				+ "         C.CITY_ID,"
				+ "         C.CITY_NAME,"
				+ "         '' infor_id,"
				+ "         '' infor_name,"
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
				+ "         C.PLAN_STATUS city_plan_status,"
				+ "         0 infor_plan_status"
				+ "    FROM T, CITY C"
				+ "   WHERE T.CITY_ID = C.CITY_ID"
				+ "     AND C.CITY_ID <> 100002"
				+ "  UNION ALL"
				+ "  SELECT NVL(T.TASK_ID, 0) TASK_ID,"
				+ "         NVL(T.NAME, '---') NAME,"
				+ "         NVL(T.DESCP,'---') DESCP,"
				+ "         0 city_ID,"
				+ "         '' city_NAME,"
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
				+ "         0,"
				+ "         I.PLAN_STATUS"
				+ "    FROM T, INFOR I"
				+ "   WHERE T.TASK_ID = I.TASK_ID)"
				+ " SELECT * FROM TASK_LIST WHERE TASK_ID = "+taskId;
		QueryRunner run=new QueryRunner();
		return run.query(conn, selectSql, getIntegrateQuery());	
	}
	
	/**
	 * TASK_STATUS:1常规，2多源，3代理店，4情报
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 */
	private static ResultSetHandler<List<Map<String, Object>>> getIntegrateQuery(){
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
					map.put("cityId", rs.getInt("CITY_ID"));
					map.put("cityName", rs.getString("CITY_NAME"));
					map.put("inforId", rs.getString("INFOR_ID"));
					map.put("inforName", rs.getString("INFOR_NAME"));
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
					map.put("cityPlanStatus", rs.getInt("CITY_PLAN_STATUS"));
					map.put("inforPlanStatus", rs.getInt("INFOR_PLAN_STATUS"));
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
//
//	public static Page queryMonthTask(Connection conn,JSONObject conditionJson, int currentPageNum,int pageSize) throws Exception {
//		String conditionSql="";
//		String statusSql="";
//		if(null!=conditionJson && !conditionJson.isEmpty()){
//			Iterator keys = conditionJson.keys();
//			while (keys.hasNext()) {
//				String key = (String) keys.next();
//				if("groupId".equals(key)){
//					conditionSql=conditionSql+" AND MAN_LIST.GROUP_ID ="+conditionJson.getInt(key);}
//				if("planStatus".equals(key)){
//					conditionSql=conditionSql+" AND MAN_LIST.TASK_STATUS =1 AND MAN_LIST.PLAN_STATUS="+conditionJson.getInt(key);}
//				if("taskName".equals(key)){
//					conditionSql=conditionSql+" AND MAN_LIST.TASK_NAME LIKE '%" +conditionJson.getString(key) + "%'";}
//				//1-6月编正常,月编异常,待分配,正常完成,逾期完成,提前完成
//				if("selectParam1".equals(key)){
//					JSONArray selectParam1=conditionJson.getJSONArray(key);
//					JSONArray progress=new JSONArray();
//					for(Object i:selectParam1){
//						int tmp=(int) i;
//						if(tmp==1||tmp==2){progress.add(tmp);}		
//						if(tmp==3){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" MAN_LIST.ASSIGN_STATUS=0";
//						}
//						if(tmp==4){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" MAN_LIST.diff_date=0";
//						}
//						if(tmp==6){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" MAN_LIST.diff_date>0";
//						}
//						if(tmp==5){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" MAN_LIST.diff_date<0";
//						}
//					}
//					//进展正常/异常 必须是已分配的（ASSIGN_STATUS=1）
//					if(!progress.isEmpty()){
//						if(!statusSql.isEmpty()){statusSql+=" or ";}
//						statusSql+=" (MAN_LIST.Progress IN ("+progress.join(",")+") AND MAN_LIST.ASSIGN_STATUS=1)";}
//				}
//				
//				if ("assignStatus".equals(key)) {
//					if(!statusSql.isEmpty()){statusSql+=" or ";}
//					statusSql+=" MAN_LIST.ASSIGN_STATUS="+conditionJson.getInt(key);}
//				if ("progress".equals(key)) {
//					if(!statusSql.isEmpty()){statusSql+=" or ";}
//					statusSql+=" MAN_LIST.Progress IN ("+conditionJson.getJSONArray(key).join(",")+")";}
//				if ("diffDate".equals(key)) {
//					JSONArray diffDateArray=conditionJson.getJSONArray(key);
//					for(Object diffDate:diffDateArray){
//						if((int) diffDate==1){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" MAN_LIST.diff_date>0";
//						}
//						if((int) diffDate==0){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" MAN_LIST.diff_date=0";
//						}
//						if((int) diffDate==-1){
//							if(!statusSql.isEmpty()){statusSql+=" or ";}
//							statusSql+=" MAN_LIST.diff_date<0";
//						}
//						}
//					}
//			}
//		}	
//		if(!statusSql.isEmpty()){//有非status
//			conditionSql+=" and ("+statusSql+")";}
//		
//		long pageStartNum = (currentPageNum - 1) * pageSize + 1;
//		long pageEndNum = currentPageNum * pageSize;
//		String selectSql = "";
//		selectSql="WITH TASK_LIST AS"
//				//未分配子任务
//				+ " (SELECT TT.CITY_ID,"
//				+ "         TT.TASK_ID,"
//				+ "         TT.NAME TASK_NAME,"
//				+ "         TT.STATUS TASK_STATUS,"
//				+ "         0 ASSIGN_STATUS,"
//				+ "         2 PLAN_STATUS,"
//				+ "         TO_CHAR(TT.MONTH_EDIT_PLAN_START_DATE, 'YYYYMMDD') PLAN_START_DATE,"
//				+ "         TO_CHAR(TT.MONTH_EDIT_PLAN_END_DATE, 'YYYYMMDD') PLAN_END_DATE,"
//				+ "         TT.MONTH_EDIT_GROUP_ID GROUP_ID,"
//				+ "         NVL(S.MONTHLY_PERCENT, 0) PERCENT,"
//				+ "         NVL(S.MONTHLY_DIFF_DATE, 0) DIFF_DATE,"
//				+ "         NVL(S.MONTHLY_PROGRESS, 1) progress,"
//				+ "         G.GROUP_NAME,"
//				+ "         TT.TASK_TYPE"
//				+ "    FROM TASK TT, USER_GROUP G, FM_STAT_OVERVIEW_TASK S"
//				+ "   WHERE TT.LATEST = 1"
//				+ "     AND (EXISTS (SELECT 1"
//				+ "                    FROM SUBTASK STT"
//				+ "                   WHERE STT.TASK_ID = TT.TASK_ID"
//				+ "                     AND STT.STAGE = 2"
//				+ "                   GROUP BY STT.TASK_ID"
//				+ "                  HAVING SUM(DISTINCT STT.STATUS) = 2) OR NOT EXISTS"
//				+ "          (SELECT 1"
//				+ "             FROM SUBTASK STT"
//				+ "            WHERE STT.TASK_ID = TT.TASK_ID"
//				+ "              AND STT.STAGE = 2))"
////				+ "     AND TT.MONTH_EDIT_GROUP_ID = G.GROUP_ID(+)"
//				+ "     AND TT.MONTH_EDIT_GROUP_ID = G.GROUP_ID"
//				+ "     AND TT.TASK_ID = S.TASK_ID(+)"
//				+ "  UNION"
//				//分配子任务，且子任务都是关闭状态==〉已完成
//				+ "  SELECT TT.CITY_ID,"
//				+ "         TT.TASK_ID,"
//				+ "         TT.NAME TASK_NAME,"
//				+ "         TT.STATUS TASK_STATUS,"
//				+ "         1 ASSIGN_STATUS,"
//				+ "         3 PLAN_STATUS,"
//				+ "         TO_CHAR(TT.MONTH_EDIT_PLAN_START_DATE, 'YYYYMMDD') PLAN_START_DATE,"
//				+ "         TO_CHAR(TT.MONTH_EDIT_PLAN_END_DATE, 'YYYYMMDD') PLAN_END_DATE,"
//				+ "         TT.MONTH_EDIT_GROUP_ID GROUP_ID,"
//				+ "         NVL(S.MONTHLY_PERCENT, 0) PERCENT,"
//				+ "         NVL(S.MONTHLY_DIFF_DATE, 0) DIFF_DATE,"
//				+ "         NVL(S.MONTHLY_PROGRESS, 1) progress,"
//				+ "         G.GROUP_NAME,"
//				+ "         TT.TASK_TYPE"
//				+ "    FROM TASK TT, USER_GROUP G, FM_STAT_OVERVIEW_TASK S, SUBTASK ST"
//				+ "   WHERE TT.LATEST = 1"
//				+ "     AND TT.STATUS = 1"
//				+ "     AND ST.STATUS IN (0, 1)"
//				+ "     AND ST.STAGE = 2"
////				+ "     AND TT.MONTH_EDIT_GROUP_ID = G.GROUP_ID(+)"
//				+ "     AND TT.MONTH_EDIT_GROUP_ID = G.GROUP_ID"
//				+ "     AND NOT EXISTS (SELECT 1"
//				+ "            FROM SUBTASK STT"
//				+ "           WHERE STT.TASK_ID = TT.TASK_ID"
//				+ "             AND STT.STATUS in (1,2)"
//				+ "             AND STT.STAGE = 2)"
//				+ "     AND TT.TASK_ID = S.TASK_ID(+)"
//				+ "     AND TT.TASK_ID = ST.TASK_ID"
//				+ "  UNION"
//				//分配子任务，且存在非关子任务==〉作业中
//				+ "  SELECT TT.CITY_ID,"
//				+ "         TT.TASK_ID,"
//				+ "         TT.NAME TASK_NAME,"
//				+ "         TT.STATUS TASK_STATUS,"
//				+ "         1 ASSIGN_STATUS,"
//				+ "         2 PLAN_STATUS,"
//				+ "         TO_CHAR(TT.MONTH_EDIT_PLAN_START_DATE, 'YYYYMMDD') PLAN_START_DATE,"
//				+ "         TO_CHAR(TT.MONTH_EDIT_PLAN_END_DATE, 'YYYYMMDD') PLAN_END_DATE,"
//				+ "         TT.MONTH_EDIT_GROUP_ID GROUP_ID,"
//				+ "         NVL(S.MONTHLY_PERCENT, 0) PERCENT,"
//				+ "         NVL(S.MONTHLY_DIFF_DATE, 0) DIFF_DATE,"
//				+ "         NVL(S.MONTHLY_PROGRESS, 1) progress,"
//				+ "         G.GROUP_NAME,"
//				+ "         TT.TASK_TYPE"
//				+ "    FROM TASK TT, USER_GROUP G, FM_STAT_OVERVIEW_TASK S, SUBTASK ST"
//				+ "   WHERE TT.LATEST = 1"
//				+ "     AND TT.STATUS = 1"
//				+ "     AND ST.STATUS IN (0, 1)"
//				+ "     AND ST.STAGE = 2"
////				+ "     AND TT.MONTH_EDIT_GROUP_ID = G.GROUP_ID(+)"
//				+ "     AND TT.MONTH_EDIT_GROUP_ID = G.GROUP_ID"
//				+ "     AND EXISTS (SELECT 1"
//				+ "            FROM SUBTASK STT"
//				+ "           WHERE STT.TASK_ID = TT.TASK_ID"
//				+ "             AND STT.STATUS <> 0"
//				+ "             AND STT.STAGE = 2)"
//				+ "     AND TT.TASK_ID = S.TASK_ID(+)"
//				+ "     AND TT.TASK_ID = ST.TASK_ID(+)),"
//				+ "MAN_LIST AS"
//				+ " (SELECT T.*,"
//				+ "         C.PLAN_STATUS CITY_PLAN_STATUS,"
//				+ "         C.CITY_NAME,"
//				+ "         0 INFOR_PLAN_STATUS,"
//				+ "         '' INFOR_NAME,"
//				+ "         '' INFOR_ID"
//				+ "    FROM CITY C, TASK_LIST T"
//				+ "   WHERE C.CITY_ID = T.CITY_ID"
//				+ "     AND T.TASK_TYPE = 1"
//				+ "  UNION ALL"
//				+ "  SELECT T.*,"
//				+ "         0 CITY_PLAN_STATUS,"
//				+ "         '' CITY_NAME,"
//				+ "         C.PLAN_STATUS INFOR_PLAN_STATUS,"
//				+ "         C.INFOR_NAME,"
//				+ "         C.INFOR_ID"
//				+ "    FROM INFOR C, TASK_LIST T"
//				+ "   WHERE C.TASK_ID = T.TASK_ID"
//				+ "     AND T.TASK_TYPE = 4),"				
//				+ " FINAL_TABLE AS"
//				+ " (SELECT *"
//				+ "    FROM MAN_LIST"
//				+ "    WHERE 1=1"
//				+ conditionSql+")"
//				+ " SELECT /*+FIRST_ROWS ORDERED*/"
//				+ " TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
//				+ "  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_ FROM FINAL_TABLE  WHERE ROWNUM <= "+pageEndNum+") TT"
//				+ " WHERE TT.ROWNUM_ >= "+pageStartNum;
//		return TaskOperation.getSnapshotQuery(conn, selectSql,currentPageNum,pageSize);	
//	}
//	
//	public static Page getSnapshotQuery(Connection conn, String selectSql,final int currentPageNum,final int pageSize) throws Exception {
//		try {
//			QueryRunner run = new QueryRunner();
//			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
//				public Page handle(ResultSet rs) throws SQLException {
//					List<HashMap> list = new ArrayList<HashMap>();
//					Page page = new Page(currentPageNum);
//					page.setPageSize(pageSize);
//					int totalCount = 0;
//					while (rs.next()) {
//						HashMap map = new HashMap();
//						map.put("taskId", rs.getInt("TASK_ID"));
//						map.put("taskName", rs.getString("TASK_NAME"));
//						map.put("taskStatus", rs.getInt("TASK_STATUS"));
//						map.put("cityId", rs.getInt("CITY_ID"));
//						map.put("cityName", rs.getString("CITY_NAME"));
//						map.put("cityPlanStatus", rs.getInt("CITY_PLAN_STATUS"));
//						map.put("inforId", rs.getString("INFOR_ID"));
//						map.put("inforName", rs.getString("INFOR_NAME"));
//						map.put("inforPlanStatus", rs.getInt("INFOR_PLAN_STATUS"));
//						map.put("planStatus", rs.getInt("PLAN_STATUS"));
//						map.put("planStartDate", rs.getString("PLAN_START_DATE"));
//						map.put("planEndDate", rs.getString("PLAN_END_DATE"));
//						map.put("assignStatus", rs.getInt("ASSIGN_STATUS"));
//						map.put("groupId", rs.getInt("GROUP_ID"));
//						map.put("groupName", rs.getString("GROUP_NAME"));
//						map.put("diffDate", rs.getInt("DIFF_DATE"));
//						map.put("progress", rs.getInt("PROGRESS"));						
//						map.put("taskType", rs.getInt("TASK_TYPE"));
//						map.put("percent",rs.getInt("PERCENT"));
//						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
//						totalCount=rs.getInt("TOTAL_RECORD_NUM");
//						list.add(map);
//					}
//					page.setResult(list);
//					page.setTotalCount(totalCount);
//					return page;
//				}
//
//			};
//			return  run.query(conn, selectSql, rsHandler);
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
//		}
//	}
//	
//	/**
//	 * TASK_STATUS:1常规，2多源，3代理店，4情报
//	 * @param currentPageNum
//	 * @param pageSize
//	 * @return
//	 */
//	private static ResultSetHandler<Page> getMonthTaskSnapShotQuery(final int currentPageNum,final int pageSize){
//		
//		ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
//			public Page handle(ResultSet rs) throws SQLException {
//				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//				Page page = new Page(currentPageNum);
//			    page.setPageSize(pageSize);
//			    int total=0;
//				while(rs.next()){
//					Map<String, Object> map = new HashMap<String, Object>();
//					map.put("taskId", rs.getInt("TASK_ID"));
//					map.put("taskName", rs.getString("TASK_NAME"));
//					map.put("cityId", rs.getInt("CITY_ID"));
//					map.put("cityName", rs.getString("CITY_NAME"));
//					map.put("inforId", rs.getString("INFOR_ID"));
//					map.put("inforName", rs.getString("INFOR_NAME"));
//					map.put("taskType", rs.getInt("TASK_TYPE"));
//					map.put("monthEditPlanStartDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
//					map.put("monthEditPlanEndDate", DateUtils.dateToString(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
//					map.put("monthEditGroupId", rs.getInt("MONTH_EDIT_GROUP_ID"));
//					map.put("monthEditGroupName", rs.getString("MONTH_EDIT_GROUP_NAME"));
//					map.put("taskStatus", rs.getInt("TASK_STATUS"));
//					map.put("planStatus", rs.getInt("PLAN_STATUS"));
//					map.put("monthlyPercent", rs.getInt("MONTHLY_PERCENT"));
//					total=rs.getInt("TOTAL_RECORD_NUM");
//					list.add(map);
//				}
//				page.setTotalCount(total);
//				page.setResult(list);
//				return page;
//			}
//    	};
//    	return rsHandler;
//	}
//	
	/**
	 * 通过taskId查询blockMan数据
	 * @author Han Shaoming
	 * @param conn
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> getBlockManByTaskId(Connection conn,long taskId,long blockManStatus) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String querySql="SELECT B.BLOCK_MAN_ID BLOCK_MAN_ID,B.TASK_ID TASK_ID,B.COLLECT_GROUP_ID COLLECT_GROUP_ID,B.DAY_EDIT_GROUP_ID DAY_EDIT_GROUP_ID "
					+ "FROM BLOCK_MAN B WHERE B.STATUS= ? AND B.TASK_ID = ? and b.LATEST=1";
			Object[] params = {blockManStatus,taskId};		
			ResultSetHandler<Map<String,Object>> rsh = new ResultSetHandler<Map<String,Object>>() {
				@Override
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					Map<String,Object> map = new HashMap<String, Object>();
					while(rs.next()){
						map.put("blockManId", rs.getLong("BLOCK_MAN_ID"));
						map.put("taskId", rs.getLong("TASK_ID"));
						map.put("collectGroupId", rs.getLong("COLLECT_GROUP_ID"));
						map.put("dayEditGroupId", rs.getLong("DAY_EDIT_GROUP_ID"));
					}
					return map;
				}
			};
			Map<String, Object> userInfo = run.query(conn, querySql, params, rsh);
			return userInfo;			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * 通过cityId查询task
	 * @author Han Shaoming
	 * @param conn
	 * @param cityId
	 * @param taskStatus
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, Object>> getTaskByCityId(Connection conn,long cityId,long taskStatus) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String querySql="SELECT TASK_ID,CITY_ID,MONTH_EDIT_GROUP_ID FROM TASK WHERE CITY_ID=? AND TASK_TYPE=? and latest=1";
			Object[] params = {cityId,taskStatus};		
			ResultSetHandler<List<Map<String, Object>>> rsh = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					while(rs.next()){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("taskId", rs.getLong("TASK_ID"));
						map.put("cityId", rs.getLong("CITY_ID"));
						map.put("monthEditGroupId", rs.getLong("MONTH_EDIT_GROUP_ID"));
						list.add(map);
					}
					return list;
				}
			};
			List<Map<String, Object>> taskList = run.query(conn, querySql, params, rsh);
			return taskList;			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn
	 * @param bean
	 * 插入TASK_GRID_MAPPING
	 * @throws Exception 
	 */
	public static void insertTaskGridMapping(Connection conn, Task bean) throws Exception {
		try{
			Map<Integer,Integer> gridIds = bean.getGridIds();
			
			insertTaskGridMapping(conn,bean.getTaskId(),gridIds);

		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
		
	}

	/**
	 * @param conn
	 * @param taskId
	 * @param gridIds
	 * @throws Exception 
	 */
	public static void insertTaskGridMapping(Connection conn, Integer taskId, Map<Integer, Integer> gridIds) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			String createMappingSql = "insert into TASK_GRID_MAPPING (TASK_ID, GRID_ID,TYPE) VALUES (?,?,?)";
			Object[][] inParam = new Object[gridIds.size()][];
			int i = 0;
			for(Map.Entry<Integer, Integer> entry:gridIds.entrySet()){
				Object[] temp = new Object[3];
				temp[0] = taskId;
				temp[1] = entry.getKey();
				temp[2] = entry.getValue();
				inParam[i] = temp;
				i++;
			}
			run.batch(conn, createMappingSql, inParam);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
		
	}

	/**
	 * @param conn
	 * @param blockId
	 * @throws Exception 
	 */
	public static void closeBlock(Connection conn, Integer blockId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			String updateSql = "UPDATE BLOCK B SET B.PLAN_STATUS = 2"
					+ " WHERE (SELECT COUNT(1) FROM TASK T WHERE T.BLOCK_ID = B.BLOCK_ID AND T.STATUS <> 0) <> 0";

			run.update(conn, updateSql);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("更新block状态失败，原因为:"+e.getMessage(),e);
		}
		
	}

	/**
	 * @param conn
	 * @param taskId
	 * @throws Exception 
	 */
	public static Map<Integer, Integer> getAddedGridMap(Connection conn, int taskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			StringBuilder sb = new StringBuilder();
			sb.append("SELECT SM.GRID_ID FROM SUBTASK_GRID_MAPPING SM ,SUBTASK S"); 
			sb.append("WHERE S.SUBTASK_ID = SM.SUBTASK_ID");
			sb.append("AND SM.TYPE = 2");
			sb.append("AND S.TASK_ID = " + taskId);

			ResultSetHandler<Map<Integer, Integer>> rsh = new ResultSetHandler<Map<Integer, Integer>>() {
				@Override
				public Map<Integer, Integer> handle(ResultSet rs) throws SQLException {
					Map<Integer, Integer> list = new HashMap<Integer, Integer>();
					while(rs.next()){
						list.put(rs.getInt("GRID_ID"), 2);
					}
					return list;
				}
			};
			Map<Integer, Integer> gridList = run.query(conn, sb.toString(), rsh);
			return gridList;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
		
	}

	/**
	 * @param conn
	 * @param gridIdMap 
	 * @param taskId：参照任务Id
	 * @param type:待调整的任务类型
	 * @throws Exception 
	 */
	public static void updateTaskRegion(Connection conn, int taskId, int type, Map<Integer, Integer> gridIdMap) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			StringBuilder sb = new StringBuilder();

			sb.append("SELECT TT.TASK_ID FROM TASK T, TASK TT"); 
			sb.append(" WHERE T.PROGRAM_ID = TT.PROGRAM_ID");
			sb.append(" AND T.BLOCK_ID = TT.BLOCK_ID");
			sb.append(" AND T.REGION_ID = TT.REGION_ID");
			sb.append(" AND TT.LATEST = 1");
			sb.append(" AND TT.TYPE = " + type);
			sb.append(" AND T.TASK_ID = " + taskId);

			ResultSetHandler<List<Integer>> rsh = new ResultSetHandler<List<Integer>>() {
				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> list = new ArrayList<Integer>();
					while(rs.next()){
						list.add(rs.getInt("TASK_ID"));
					}
					return list;
				}
			};
			List<Integer> list = run.query(conn, sb.toString(), rsh);
			
			for(Integer taskIdToAdjust:list){
				insertTaskGridMapping(conn, taskIdToAdjust, gridIdMap);
			}

		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
		
	}

	/**
	 * 返回该任务下子任务列表：
	 * 子任务类型满足条件
	 * 子任务状态为非关闭的
	 * 子任务信息只包含subtaskId
	 * @param conn
	 * @param taskId
	 * @param type
	 * @throws Exception 
	 * 
	 */
	public static List<Subtask> getSubTaskListByType(Connection conn, int taskId, int type) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			StringBuilder sb = new StringBuilder();
			sb.append("SELECT S.SUBTASK_ID FROM SUBTASK S "); 
			sb.append(" WHERE S.TASK_ID = " + taskId);
			sb.append(" AND S.TYPE = " + type);
			sb.append(" AND S.STATUS <> 0 ");
			log.info("getSubTaskListByType SQL："+sb.toString());
			ResultSetHandler<List<Subtask>> rsh = new ResultSetHandler<List<Subtask>>() {
				@Override
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while(rs.next()){
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						list.add(subtask);
					}
					return list;
				}
			};
			List<Subtask> list = run.query(conn, sb.toString(), rsh);
			return list;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
}
