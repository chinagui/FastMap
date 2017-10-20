package com.navinfo.dataservice.engine.man.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringConverter;
import com.navinfo.navicommons.database.QueryRunner;

public class TaskOperation {
	private static Logger log = LoggerRepos.getLogger(TaskOperation.class);
	
	public TaskOperation() {
	}
	
	public static int getNewTaskId(Connection conn) throws Exception {
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
	 * task的latest字段，修改成无效，0
	 */
	public static void updateLatest(Connection conn, Integer taskId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String updateBlock="UPDATE TASK SET LATEST=0 WHERE LATEST=1 AND task_id=" + taskId;
			run.update(conn,updateBlock);			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/*
	 * task的status字段，修改成开启
	 */
	public static void updateStatus(Connection conn,List<Integer> taskIds,int status) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String updateSql="UPDATE TASK SET STATUS=" + status +" WHERE TASK_ID IN ("+StringUtils.join(taskIds,",")+")";
			log.info("updateStatus sql:" + updateSql);
			run.update(conn,updateSql);			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/*
	 * task的status字段，修改成开启
	 */
	public static void updateStatus(Connection conn,Integer taskId,int status) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String updateSql="UPDATE TASK SET STATUS=" + status +" WHERE TASK_ID =" + taskId ;
			log.info("updateStatus sql:" + updateSql);
			run.update(conn,updateSql);			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:"+e.getMessage(),e);
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
						map.setCreateDate(rs.getTimestamp("CREATE_DATE"));
						map.setBlockId(rs.getInt("BLOCK_ID"));
						map.setTaskId(rs.getInt("TASK_ID"));
						map.setProgramId(rs.getInt("PROGRAM_ID"));
						map.setGroupId(rs.getInt("GROUP_ID"));
						map.setPoiPlanTotal((rs.getInt("POI_PLAN_TOTAL")));
						map.setRoadPlanTotal((rs.getInt("ROAD_PLAN_TOTAL")));
//						map.setCityId(rs.getInt("CITY_ID"));
						map.setWorkKind(rs.getString("WORK_KIND"));
						map.setCreateUserId(rs.getInt("CREATE_USER_ID"));
						map.setCreateDate(rs.getTimestamp("CREATE_DATE"));
						map.setStatus(rs.getInt("STATUS"));
//						map.setTaskName(rs.getString("NAME"));
//						map.setTaskDescp(rs.getString("DESCP"));
						map.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						map.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
//						map.setMonthEditPlanStartDate(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE"));
//						map.setMonthEditPlanEndDate(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE"));
						map.setLatest(rs.getInt("LATEST"));
//						map.setMonthProducePlanStartDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_START_DATE"));
//						map.setMonthProducePlanEndDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_END_DATE"));
						map.setType(rs.getInt("TYPE"));
						map.setLot(rs.getInt("LOT"));
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
			Map<String, Object> changeFields = bean.getChangeFields();
			StringBuffer insert = new StringBuffer();
			StringBuffer value = new StringBuffer();
			if (changeFields.containsKey("UPLOAD_METHOD")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" UPLOAD_METHOD ");
				value.append("'" + bean.getUploadMethod() + "'");
			};
			if (changeFields.containsKey("WORK_KIND")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" WORK_KIND ");
				value.append("'" + bean.getWorkKind() + "'");
			};
			if (changeFields.containsKey("TASK_ID")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" TASK_ID ");
				value.append(bean.getTaskId());
			};
			if (changeFields.containsKey("PROGRAM_ID")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" PROGRAM_ID ");
				value.append(bean.getProgramId());
			};
			if (changeFields.containsKey("NAME")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" NAME ");
				value.append("'" + bean.getName() + "'");
			};
			if (changeFields.containsKey("BLOCK_ID")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" BLOCK_ID ");
				value.append(bean.getBlockId());
			};
			
			StringConverter.manCreatDataUtils(insert, value);
			insert.append(" CREATE_USER_ID,CREATE_DATE,STATUS,LATEST ");
			value.append(bean.getCreateUserId()+", sysdate, 2, 1");
			if (changeFields.containsKey("DESCP")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" DESCP ");
				value.append("'"+bean.getDescp()+"'");
			};
			if (changeFields.containsKey("PLAN_START_DATE") && StringUtils.isNotBlank(changeFields.get("PLAN_START_DATE").toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" PLAN_START_DATE ");
				value.append("to_timestamp('"+ bean.getPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')");
			};
			if (changeFields.containsKey("PLAN_END_DATE") && StringUtils.isNotBlank(changeFields.get("PLAN_END_DATE").toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" PLAN_END_DATE ");
				value.append("to_timestamp('"+ bean.getPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')");
			};
			if (changeFields.containsKey("TYPE")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" TYPE ");
				value.append(bean.getType());
			};
			if (changeFields.containsKey("LOT")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" LOT");
				value.append(bean.getLot());
			};
			if (changeFields.containsKey("GROUP_ID")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" GROUP_ID ");
				value.append(bean.getGroupId());
			};
			if (changeFields.containsKey("REGION_ID")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" REGION_ID ");
				value.append(bean.getRegionId());
			};
			if (changeFields.containsKey("ROAD_PLAN_TOTAL")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" ROAD_PLAN_TOTAL ");
				value.append(bean.getRoadPlanTotal());
			};
			if (changeFields.containsKey("POI_PLAN_TOTAL")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" POI_PLAN_TOTAL ");
				value.append(bean.getPoiPlanTotal());
			};
			//modify by songhe 添加road/poi_plan_in/out
			if (changeFields.containsKey("ROAD_PLAN_IN")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" ROAD_PLAN_IN ");
				value.append(bean.getRoadPlanIn());
			};
			if (changeFields.containsKey("ROAD_PLAN_OUT")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" ROAD_PLAN_OUT ");
				value.append(bean.getRoadPlanOut());
			};
			if (changeFields.containsKey("POI_PLAN_IN")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" POI_PLAN_IN ");
				value.append(bean.getPoiPlanIn());
			};
			if (changeFields.containsKey("POI_PLAN_OUT")){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" POI_PLAN_OUT ");
				value.append(bean.getPoiPlanOut());
			};
			if (changeFields.containsKey("PRODUCE_PLAN_START_DATE") && StringUtils.isNotBlank(changeFields.get("PRODUCE_PLAN_START_DATE").toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" PRODUCE_PLAN_START_DATE ");
				value.append("to_timestamp('"+ bean.getProducePlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')");
			};
			if (changeFields.containsKey("PRODUCE_PLAN_END_DATE") && StringUtils.isNotBlank(changeFields.get("PRODUCE_PLAN_END_DATE").toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" PRODUCE_PLAN_END_DATE");
				value.append("to_timestamp('"+ bean.getProducePlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')");
			};
			String createSql = "insert into task ("+insert.toString()+") values("+value.toString()+")";
			
			log.info("creatTaskSql:" + createSql);
			run.update(conn,createSql);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(), e);
		}
	}
		
	public static void updateTask(Connection conn,Task bean) throws Exception{
		try{
			Map<String, Object> changeFields = bean.getChangeFields();
			String baseSql = "update task set ";
			QueryRunner run = new QueryRunner();
			String updateSql="";
			if (changeFields.containsKey("DESCP")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" DESCP= '" + bean.getDescp() + "'";
			};
			if (changeFields.containsKey("NAME")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" NAME='" + bean.getName() + "'";
			};
			if (changeFields.containsKey("PLAN_START_DATE")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" PLAN_START_DATE=to_timestamp('"+ bean.getPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (changeFields.containsKey("PLAN_END_DATE")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" PLAN_END_DATE=to_timestamp('"+ bean.getPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (changeFields.containsKey("PRODUCE_PLAN_START_DATE")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" PRODUCE_PLAN_START_DATE=to_timestamp('"+ bean.getProducePlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (changeFields.containsKey("PRODUCE_PLAN_END_DATE")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" PRODUCE_PLAN_END_DATE=to_timestamp('"+ bean.getProducePlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (changeFields.containsKey("LOT")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" LOT= " + bean.getLot();
			};
			if (changeFields.containsKey("POI_PLAN_TOTAL")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" poi_plan_total= " + bean.getPoiPlanTotal();
			};
			if (changeFields.containsKey("ROAD_PLAN_TOTAL")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" road_plan_total= " + bean.getRoadPlanTotal();
			};
			if (changeFields.containsKey("GROUP_ID")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" GROUP_ID= "+bean.getGroupId();
			};
			if (changeFields.containsKey("WORK_KIND")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" work_kind='" + bean.getWorkKind() + "'";
			};
			if (changeFields.containsKey("OVERDUE_REASON")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" overdue_reason='" + bean.getOverdueReason() + "'";
			};
			if (changeFields.containsKey("OVERDUE_OTHER_REASON")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" overdue_other_reason='" + bean.getOverdueOtherReason() + "'";
			};
			//modify by songhe 添加road/poi_plan_in/out
			if (changeFields.containsKey("ROAD_PLAN_IN")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" road_plan_in = " + bean.getRoadPlanIn();
			};
			if (changeFields.containsKey("ROAD_PLAN_OUT")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" road_plan_out = " + bean.getRoadPlanOut();
			};
			if (changeFields.containsKey("POI_PLAN_IN")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" poi_plan_in = " + bean.getPoiPlanIn();
			};
			if (changeFields.containsKey("POI_PLAN_OUT")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql+=" poi_plan_out = " + bean.getPoiPlanOut();
			};
			updateSql+=" where TASK_ID=" + bean.getTaskId();
			log.info("taskUpdateSql:"+updateSql);
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
	 * 判断cms任务是否能发布
	 * @author Han Shaoming
	 * @param conn
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public static List<Integer> pushCmsTasks(Connection conn,List<Integer> cmsTaskList) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String querySql="SELECT T.TASK_ID"
					+ "  FROM TASK T"
					+ " WHERE T.TASK_ID in "+cmsTaskList.toString().replace("[", "(").replace("]", ")")
					+ "   AND NOT EXISTS (SELECT 1"
					+ "          FROM TASK T2"
					+ "         WHERE T.BLOCK_ID = T2.BLOCK_ID"
					+ "           AND T2.LATEST = 1"
					+ "           AND T2.STATUS != 0"
					+ "           AND T2.TYPE in (0))";	
			ResultSetHandler<List<Integer>> rsh = new ResultSetHandler<List<Integer>>() {
				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> map = new ArrayList<Integer>();
					while(rs.next()){
						map.add(rs.getInt("TASK_ID"));
					}
					return map;
				}
			};
			List<Integer> taskId = run.query(conn, querySql, rsh);
			return taskId;			
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
	public static int changeTaskGridBySubtask(Connection conn, int subtaskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			String createMappingSql = "INSERT INTO TASK_GRID_MAPPING"
					+ "  (TASK_ID, GRID_ID, TYPE)"
					+ "  SELECT S.TASK_ID, GRID_ID, 2"
					+ "    FROM SUBTASK_GRID_MAPPING M, SUBTASK S"
					+ "   WHERE M.SUBTASK_ID = "+subtaskId
					+ "     AND S.SUBTASK_ID = M.SUBTASK_ID"
					+ "  MINUS"
					+ "  SELECT S.TASK_ID, T.GRID_ID, 2"
					+ "    FROM TASK_GRID_MAPPING T, SUBTASK S"
					+ "   WHERE S.SUBTASK_ID = "+subtaskId
					+ "     AND S.TASK_ID = T.TASK_ID";
			return run.update(conn, createMappingSql);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
		
	}
	
	/**
	 * 中线采集任务范围调整
	 * @param Connection
	 * @param List<Integer>
	 * @param Subtask
	 * @throws Exception 
	 * 
	 * */
	public static void changeTaskGridByGrids(Connection conn, List<Integer> grids, Subtask subtask) throws Exception{
		try{
			QueryRunner run = new QueryRunner();

			String sql = "insert into TASK_GRID_MAPPING (TASK_ID, GRID_ID, TYPE) VALUES (?,?,?)";
			Object[][] param = new Object[grids.size()][];
			int i = 0;
			for(int grid : grids){
				Object[] temp = new Object[3];
				temp[0] = subtask.getTaskId();
				temp[1] = grid;
				temp[2] = 2;
				param[i] = temp;
				i++;
			}
			run.batch(conn, sql, param);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("中线采集任务范围更新失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 快线：采集/日编子任务关闭进行动态调整，增加动态调整快线月编任务，月编子任务范围
	 * 根据项目修改对应月编任务范围，快线的月编任务范围和任务对应的项目范围一致
	 * @param conn
	 * @param taskId
	 * @throws Exception 
	 * 
	 */
	public static int changeMonthTaskGridByProgram(Connection conn, int taskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			String sql = "INSERT INTO TASK_GRID_MAPPING"
					+ "  (TASK_ID, GRID_ID, TYPE)"
					+ "  SELECT UT.TASK_ID, M.GRID_ID, 2"
					+ "     FROM PROGRAM_GRID_MAPPING M, PROGRAM P, TASK T, TASK UT"
					+ "    WHERE T.TASK_ID = "+taskId
					+ "     AND UT.PROGRAM_ID = T.PROGRAM_ID"
					+ "     AND P.PROGRAM_ID = UT.PROGRAM_ID"
					+ "     AND M.PROGRAM_ID = P.PROGRAM_ID"
					+ "     AND UT.TYPE = 2"
					+ "     AND P.TYPE = 4"
					+ "  MINUS"
					+ "  SELECT UT.TASK_ID, M.GRID_ID, 2"
					+ "    FROM TASK_GRID_MAPPING M, PROGRAM P, TASK T, TASK UT"
					+ "   WHERE T.TASK_ID = "+taskId
					+ "     AND UT.PROGRAM_ID = T.PROGRAM_ID"
					+ "     AND P.PROGRAM_ID = UT.PROGRAM_ID"
					+ "     AND M.TASK_ID = UT.TASK_ID"
					+ "     AND UT.TYPE = 2"
					+ "     AND P.TYPE = 4";
			log.info("根据项目调整月编任务sql："+sql);
			return run.update(conn, sql);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
		
	}
	
	/**
	 * 快线项目下，根据某个任务id获取与他同项目下的月编任务的扩展grid
	 * @param conn
	 * @param taskId
	 * @throws Exception 
	 * 
	 */
	public static Task getMonthTaskGridByOtherTask(Connection conn, int otherTaskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			String sql = "  SELECT UT.TASK_ID, M.GRID_ID, M.TYPE"
					+ "    FROM TASK_GRID_MAPPING M, PROGRAM P, TASK T, TASK UT"
					+ "   WHERE T.TASK_ID = "+otherTaskId
					+ "     AND UT.PROGRAM_ID = T.PROGRAM_ID"
					+ "     AND P.PROGRAM_ID = UT.PROGRAM_ID"
					+ "     AND M.TASK_ID = UT.TASK_ID"
					//+ "     AND M.TYPE = 2"
					+ "     AND UT.TYPE = 2"
					+ "     AND P.TYPE = 4";
			log.info("getExtentMonthTaskGridByOtherTask:"+sql);
			ResultSetHandler<Task> rsHandler = new ResultSetHandler<Task>() {
				public Task handle(ResultSet rs) throws SQLException {
					Task task=new Task();
					Map<Integer,Integer> gridMap = new HashMap<Integer,Integer>();
					while (rs.next()) {
						task.setTaskId(rs.getInt("TASK_ID"));
						gridMap.put(rs.getInt("GRID_ID"), rs.getInt("TYPE"));
					}
					task.setGridIds(gridMap);
					return task;
				}
			};
			return run.query(conn, sql, rsHandler);
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
					+ " WHERE B.BLOCK_ID = " + blockId
					+ " AND (SELECT COUNT(1) FROM TASK T WHERE T.BLOCK_ID = B.BLOCK_ID AND T.STATUS <> 0) = 0";
			log.info("closeBlock sql:" + updateSql);
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
			sb.append(" WHERE S.SUBTASK_ID = SM.SUBTASK_ID");
			sb.append(" AND SM.TYPE = 2");
			sb.append(" AND S.TASK_ID = " + taskId);

			log.info("getAddedGridMap sql:" + sb.toString());
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

			log.info("updateTaskRegion sql:" + sb.toString());
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
			List<Subtask> list = new ArrayList<Subtask>();
			list = run.query(conn, sb.toString(), rsh);
			return list;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * 获取block对应的regionid
	 * @param blockId
	 * @return
	 * @throws Exception 
	 */
	public static int getRegionIdByBlockId(Integer blockId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();

			StringBuilder sb = new StringBuilder();
			sb.append("SELECT DISTINCT G.REGION_ID FROM GRID G WHERE G.BLOCK_ID = " + blockId); 
			log.info("getRegionIdByBlockId SQL："+sb.toString());
			ResultSetHandler<Integer> rsh = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					int regionId = 0;
					if(rs.next()){
						regionId = 	rs.getInt("REGION_ID");
					}
					return regionId;
				}
			};
			Integer list = run.query(conn, sb.toString(), rsh);
			return list;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * taskId对应的task表记录以及task_grid_mapping记录进行复制，同时将newTaskId的状态修改为草稿
	 * @param conn
	 * @param userId
	 * @param newTaskId
	 * @param taskId
	 * @throws Exception
	 */
	public static void copyTask(Connection conn,Long userId, int newTaskId, int taskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			
			String insertTask="INSERT INTO TASK (TASK_ID,CREATE_USER_ID,CREATE_DATE,STATUS,NAME,"
					+ "   DESCP,PLAN_START_DATE,PLAN_END_DATE,LATEST,PROGRAM_ID,BLOCK_ID,REGION_ID,"
					+ "   PRODUCE_PLAN_START_DATE,PRODUCE_PLAN_END_DATE,TYPE,LOT,GROUP_ID,ROAD_PLAN_TOTAL,"
					+ "   POI_PLAN_TOTAL,WORK_KIND,geometry)"
					+ "  SELECT "+newTaskId+","+userId+",SYSDATE,2,b.block_name||'_'||TO_CHAR(SYSDATE,'YYYYMMDD'),t.DESCP,t.PLAN_START_DATE,"
					+ "         t.PLAN_END_DATE,1,t.PROGRAM_ID,t.BLOCK_ID,t.REGION_ID,t.PRODUCE_PLAN_START_DATE,"
					+ "         t.PRODUCE_PLAN_END_DATE,t.TYPE,t.LOT,t.GROUP_ID,t.ROAD_PLAN_TOTAL,t.POI_PLAN_TOTAL,"
					+ "t.WORK_KIND,t.geometry"
					+ "    FROM TASK t,block b"
					+ "   WHERE t.block_id=b.block_id AND t.TASK_ID = "+taskId;
			String insertTaskGrid="INSERT INTO TASK_GRID_MAPPING"
					+ "  (TASK_ID, GRID_ID, TYPE)"
					+ "  SELECT "+newTaskId+", GRID_ID, TYPE"
					+ "    FROM TASK_GRID_MAPPING"
					+ "   WHERE TYPE = 1"
					+ "     AND TASK_ID = "+taskId;
			run.update(conn, insertTask);
			run.update(conn, insertTaskGrid);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}

	public static int changeDayCmsTaskGridByCollectTask(Connection conn,int taskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			//modify by songhe
			//删除task.type对应=3的二代编辑任务
			String createMappingSql = "INSERT INTO TASK_GRID_MAPPING"
					+ "  (TASK_ID, GRID_ID, TYPE)"
					+ "  SELECT UT.TASK_ID, GRID_ID, 2"
					+ "    FROM TASK_GRID_MAPPING M, TASK S, TASK UT"
					+ "   WHERE M.TASK_ID = "+taskId
					+ "     AND S.TASK_ID = M.TASK_ID"
					+ "     AND ut.region_id = s.region_id"
					+ "     AND UT.BLOCK_ID = S.BLOCK_ID"
					+ "     AND UT.PROGRAM_ID = S.PROGRAM_ID"
					+ "     AND UT.TYPE = 1"
					+ "  MINUS"
					+ "  SELECT UT.TASK_ID, T.GRID_ID, 2"
					+ "    FROM TASK_GRID_MAPPING T, TASK S, TASK UT"
					+ "   WHERE S.TASK_ID = "+taskId
					+ "     AND UT.BLOCK_ID = S.BLOCK_ID"
					+ "     AND UT.PROGRAM_ID = S.PROGRAM_ID"
					+ "     AND ut.region_id = s.region_id"
					+ "     AND UT.TASK_ID = T.TASK_ID"
					+ "     AND UT.TYPE = 1";
			return run.update(conn, createMappingSql);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}		
	}
	/**
	 *获取采集任务对应的日编任务id
	 * @param conn
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public static int getDayTaskGridByCollectTask(Connection conn,int taskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			//modify by songhe
			//删除task.type对应=3的二代编辑任务
			String createMappingSql = "SELECT UT.TASK_ID"
					+ "    FROM TASK S, TASK UT"
					+ "   WHERE S.TASK_ID = "+taskId
					+ "     AND UT.BLOCK_ID = S.BLOCK_ID"
					+ "     AND UT.PROGRAM_ID = S.PROGRAM_ID"
					+ "     AND ut.region_id = s.region_id"
					+ "     AND UT.latest = 1"
					+ "     AND UT.TYPE = 1";
			return run.query(conn, createMappingSql, new ResultSetHandler<Integer>(){

				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						return rs.getInt("TASK_ID");
					}
					return 0;
				}
				
			});
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}		
	}
	
	/**
	 * 任务对应的block若为关闭，则同步更新为已规划，否则不动
	 * @param conn
	 * @throws Exception
	 */
	public static void reOpenBlockByTask(Connection conn,int taskId) throws Exception {		
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "UPDATE BLOCK"
					+ "   SET PLAN_STATUS = 1"
					+ " WHERE PLAN_STATUS = 2"
					+ "   AND BLOCK_ID = (SELECT BLOCK_ID FROM TASK WHERE TASK_ID = "+taskId+")" ;
			run.update(conn,selectSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * task的workKind字段，特定值域改成1,例如：原work_Kind='1|0|0|0'，参数subtaskWorkKind=3，则修改后work_Kind='1|0|1|0'
	 * @param conn
	 * @param taskId
	 * @param subtaskWorkKind:1外业采集，2众包，3情报矢量，4多源,将任务的对应修改为1
	 * @throws Exception
	 */
	public static void updateWorkKind(Connection conn, int taskId, int subtaskWorkKind) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String updateSql="UPDATE TASK SET work_kind=substr(work_kind,1,"+(subtaskWorkKind-1)*2+")||1||substr(work_kind,"+subtaskWorkKind*2+",length(work_kind)) WHERE task_id="+taskId;
			run.update(conn,updateSql);			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}	
	
	/**
	 * @Title: updateSubtask
	 * @Description: 修改子任务(修)(第七迭代)
	 * @param conn
	 * @param bean
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月7日 下午2:21:21 
	 */
	public static void updateTaskGeo(Connection conn,String geoStr,int taskId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String baseSql = "update TASK set GEOMETRY=? where TASK_ID="+taskId;			
			log.info("updatetask sql:" + baseSql);
			run.update(conn,baseSql,GeoTranslator.wkt2Struct(conn,geoStr));			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:"+e.getMessage(),e);
		}
	}
}
