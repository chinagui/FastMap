package com.navinfo.dataservice.engine.man.subtask;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Message;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.api.statics.model.SubtaskStatInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.commons.util.ArrayUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.xinge.XingeUtil;
import com.navinfo.dataservice.engine.man.message.MessageService;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.userDevice.UserDeviceService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/** 
 * @ClassName: SubtaskOperation
 * @author songdongyan
 * @date 2016年6月13日
 * @Description: SubtaskOperation.java
 */
public class SubtaskOperation {
	private static Logger log = LoggerRepos.getLogger(TaskOperation.class);
	
	public SubtaskOperation() {
		// TODO Auto-generated constructor stub
	}
	
	
	public static void updateSubtask(Connection conn,Subtask bean) throws Exception{
		try{
			String baseSql = "update SUBTASK set ";
			QueryRunner run = new QueryRunner();
			String updateSql="";

			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " NAME= " + "'" + bean.getName() + "'";
			};
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " DESCP= " + "'" + bean.getDescp() + "'";
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " PLAN_START_DATE= " + "to_timestamp('" + bean.getPlanStartDate() + "','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getExeUserId()!=null && StringUtils.isNotEmpty(bean.getExeUserId().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " EXE_USER_ID= " + bean.getExeUserId();
			};
			if (bean!=null&&bean.getExeGroupId()!=null && StringUtils.isNotEmpty(bean.getExeGroupId().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " EXE_GROUP_ID= " + bean.getExeGroupId();
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " PLAN_END_DATE= " + "to_timestamp('" + bean.getPlanEndDate()+ "','yyyy-mm-dd hh24:mi:ss.ff')";
			};
			if (bean!=null&&bean.getStatus()!=null && StringUtils.isNotEmpty(bean.getStatus().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " STATUS= " + bean.getStatus();
			};

			if (bean!=null&&bean.getSubtaskId()!=null && StringUtils.isNotEmpty(bean.getSubtaskId().toString())){
				updateSql += " where SUBTASK_ID= " + bean.getSubtaskId();
			};
			
			run.update(conn,baseSql+updateSql);
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:"+e.getMessage(),e);
		}
	}
	
	//根据subtaskId列表获取包含subtask type,status,gridIds信息的List<Subtask>
	public static List<Subtask> getSubtaskListBySubtaskIdList(Connection conn,List<Integer> subtaskIdList) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String subtaskIds = "(" + StringUtils.join(subtaskIdList.toArray(),",") + ")";
			
			
			String selectSql = "SELECT S.SUBTASK_ID,S.NAME,S.STAGE,S.TYPE,S.EXE_USER_ID,S.EXE_GROUP_ID,S.STATUS,S.BLOCK_ID,S.TASK_ID"
					+ " FROM SUBTASK S"
					+ " WHERE S.SUBTASK_ID IN " + subtaskIds;
			
			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>(){
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while(rs.next()){
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setName(rs.getString("NAME"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setExeUserId(rs.getInt("EXE_USER_ID"));
						subtask.setExeGroupId(rs.getInt("EXE_GROUP_ID"));
						subtask.setStatus(rs.getInt("STATUS"));
						subtask.setBlockId(rs.getInt("BLOCK_ID"));
						subtask.setTaskId(rs.getInt("TASK_ID"));
						list.add(subtask);
					}
					return list;
				}
	    	};
	    	
	    	List<Subtask> subtaskList = run.query(conn, selectSql,rsHandler);
	    	return subtaskList;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
//	//根据subtaskId列表获取包含subtask type,status,gridIds信息的List<Subtask>
//	public static List<Subtask> getSubtaskListByIdList(Connection conn,List<Integer> subtaskIdList) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			String subtaskIds = "(";
//			
//			subtaskIds += StringUtils.join(subtaskIdList.toArray(),",") + ")";
//			
//			
//			String selectSql = "select m.SUBTASK_ID"
//					+ ",listagg(m.GRID_ID, ',') within group(order by m.SUBTASK_ID) as GRID_ID"
//					+ ",s.TYPE"
//					+ ",s.STAGE"
//					+ " from SUBTASK_GRID_MAPPING m"
//					+ ", SUBTASK s"
//					+ " where s.SUBTASK_ID = m.Subtask_Id"
//					+ " and s.SUBTASK_ID in " + subtaskIds
//					+ " group by m.SUBTASK_ID"
//					+ ", s.TYPE, s.STAGE";
//			
//			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>(){
//				public List<Subtask> handle(ResultSet rs) throws SQLException {
//					List<Subtask> list = new ArrayList<Subtask>();
//					while(rs.next()){
//						Subtask subtask = new Subtask();
//						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
//						subtask.setStage(rs.getInt("STAGE"));
//						subtask.setType(rs.getInt("TYPE"));
//						String gridIds = rs.getString("GRID_ID");
//
//						String[] gridIdList = gridIds.split(",");
//						subtask.setGridIds(ArrayUtil.convertList(Arrays.asList(gridIdList)));
//						list.add(subtask);
//					}
//					return list;
//				}
//	    		
//	    	};
//	    	
//	    	List<Subtask> subtaskList = run.query(conn, selectSql,rsHandler);
//	    	return subtaskList;
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
//		}
//	}
//	
	
	//判断采集任务是否可关闭
	public static Boolean isCollectReadyToClose(StaticsApi staticsApi,Subtask subtask)throws Exception{
		try{
			List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(subtask.getSubtaskId());
			List<String> gridIdList = ArrayUtil.reConvertList(gridIds);
			List<GridStatInfo> gridStatInfoColArr = staticsApi.getLatestCollectStatByGrids(gridIdList);
			//0POI
			if(0==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if(100 > (int)gridStatInfoColArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//1道路
			else if(1==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if(100 > (int)gridStatInfoColArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}
			//2一体化
			else if(2==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if((100 > (int)gridStatInfoColArr.get(j).getPercentPoi()) 
							|| 
							(100 > (int)gridStatInfoColArr.get(j).getPercentRoad())){
						return false;
					}
				}
				return true;
			}else{
				return true;
			}
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
	//判断日编任务是否可关闭
	public static Boolean isDailyEditReadyToClose(StaticsApi staticsApi,Subtask subtask)throws Exception{
		try{
			List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(subtask.getSubtaskId());
			List<String> gridIdList = ArrayUtil.reConvertList(gridIds);
			List<GridStatInfo> gridStatInfoDailyEditArr = staticsApi.getLatestDailyEditStatByGrids(gridIdList);
			//0POI,5多源POI
			if(0==subtask.getType()||5==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if(100 > (int)gridStatInfoDailyEditArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//3一体化_grid粗编，4一体化_区域粗编
			else if(3==subtask.getType()||4==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if((100 > (int)gridStatInfoDailyEditArr.get(j).getPercentPoi()) 
							|| 
							(100 > (int)gridStatInfoDailyEditArr.get(j).getPercentRoad())){
						return false;
					}
				}
				return true;
			}else{
				return true;
			}
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
	//判断月编任务是否可关闭
	public static Boolean isMonthlyEditReadyToClose(StaticsApi staticsApi,Subtask subtask)throws Exception{
		try{
			List<Integer> gridIds = SubtaskOperation.getGridIdsBySubtaskId(subtask.getSubtaskId());
			List<String> gridIdList = ArrayUtil.reConvertList(gridIds);
			List<GridStatInfo> gridStatInfoMonthlyEditArr = staticsApi.getLatestDailyEditStatByGrids(gridIdList);
			//6代理店， 7POI专项
			if(6==subtask.getType()||7==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if(100 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//8道路_grid精编，9道路_grid粗编，10道路区域专项
			else if(8==subtask.getType()||9==subtask.getType()||10==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if(100 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}else{
				return true;
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static void closeBySubtaskList(Connection conn,List<Integer> closedSubtaskList) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String closedSubtaskStr = "(";
			
			closedSubtaskStr += StringUtils.join(closedSubtaskList.toArray(),",") + ")";
						
			String updateSql = "update SUBTASK "
					+ "set STATUS=0 "
					+ "where SUBTASK_ID in "
					+ closedSubtaskStr;	
			

			run.update(conn,updateSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param conn
	 * @param bean
	 * @return
	 * @throws Exception 
	 */
	public static int getSubtaskId(Connection conn, Subtask bean) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			String querySql = "select SUBTASK_SEQ.NEXTVAL as subTaskId from dual";

			int subTaskId = Integer.valueOf(run
					.query(conn, querySql, new MapHandler()).get("subTaskId")
					.toString());
			return subTaskId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn
	 * @param bean
	 * @throws Exception 
	 */
	public static void insertSubtask(Connection conn, Subtask bean) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			List<Object> value = new ArrayList<Object>();
			
			value.add(bean.getSubtaskId());
			value.add(bean.getName());
			
			//geo
			Clob c = ConnectionUtil.createClob(conn);
			c.setString(1, bean.getGeometry());
			value.add(c);
//			SqlClause inClause = SqlClause.genGeoClauseWithGeoString(conn,bean.getGeometry());
//			if (inClause!=null)
//				value.add(inClause.getValues().get(0));
			
			value.add(bean.getStage());
			value.add(bean.getType());
			value.add(bean.getCreateUserId());
//			value.add(bean.getExeUserId());
			value.add(new Timestamp(System.currentTimeMillis()).toString().substring(0, 10));
			value.add(bean.getStatus());
			value.add(bean.getPlanStartDate().toString().substring(0, 10));
			value.add(bean.getPlanEndDate().toString().substring(0, 10));
			value.add(bean.getDescp());
			//GEOMETRY, ,sdo_geometry(?,8307)
			String createSql = "insert into SUBTASK " ;
			String column = "(SUBTASK_ID, NAME, GEOMETRY,STAGE, TYPE, CREATE_USER_ID, CREATE_DATE, STATUS, PLAN_START_DATE, PLAN_END_DATE, DESCP";
			String values = "values(?,?,sdo_geometry(?,8307),?,?,?,to_date(?,'yyyy-MM-dd HH24:MI:ss'),?,to_date(?,'yyyy-MM-dd HH24:MI:ss'),to_date(?,'yyyy-MM-dd HH24:MI:ss'),?";

			if(0!=bean.getBlockManId()){
				column += ", BLOCK_MAN_ID";
				value.add(bean.getBlockManId());
				values += ",?";

			}else{
				column += ", TASK_ID";
				value.add(bean.getTaskId());
				values += ",?";
			}
			
			if(0!=bean.getExeGroupId()){
				column += ", EXE_GROUP_ID)";
				value.add(bean.getExeGroupId());
				values += ",?)";

			}else{
				column += ", EXE_USER_ID)";
				value.add(bean.getExeUserId());
				values += ",?)";
			}
			
			createSql += column+values;

			run.update(conn, createSql,value.toArray());

		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn
	 * @param bean
	 * @throws Exception 
	 */
	public static void insertSubtaskGridMapping(Connection conn, Subtask bean) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			String createMappingSql = "insert into SUBTASK_GRID_MAPPING (SUBTASK_ID, GRID_ID) VALUES (?,?)";

			List<Integer> gridIds = bean.getGridIds();
			Object[][] inParam = new Object[gridIds.size()][];
			for (int i = 0; i < inParam.length; i++) {
				Object[] temp = new Object[2];
				temp[0] = bean.getSubtaskId();
				temp[1] = gridIds.get(i);
				inParam[i] = temp;

			}

			run.batch(conn, createMappingSql, inParam);

		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn
	 * @param bean
	 * @param curPageNum
	 * @param pageSize
	 * @param platForm 
	 * @return
	 * @throws Exception 
	 */
	public static Page getListByUserSnapshotPage(Connection conn, Subtask bean, final int curPageNum, final int pageSize, int platForm) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select st.SUBTASK_ID ,st.NAME"
					+ ",st.DESCP,st.PLAN_START_DATE,st.PLAN_END_DATE"
					+ ",st.STAGE,st.TYPE,st.STATUS"
					+ ",r.DAILY_DB_ID,r.MONTHLY_DB_ID";

			String fromSql_task = " from subtask st,task t,city c,region r";

			String fromSql_block = " from subtask st,block_man bm,block b,region r";

			String conditionSql_task = " where st.task_id = t.task_id "
					+ "and t.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and (st.EXE_USER_ID = " + bean.getExeUserId() + " or st.EXE_GROUP_ID = " + bean.getExeGroupId() + ")";
//					+ "and st.EXE_USER_ID = " + bean.getExeUserId() + " ";

			String conditionSql_block = " where st.block_man_id = bm.block_man_id "
					+ "and b.region_id = r.region_id "
					+ "and bm.block_id = b.block_id "
					+ "and (st.EXE_USER_ID = " + bean.getExeUserId() + " or st.EXE_GROUP_ID = " + bean.getExeGroupId() + ")";
//					+ "and st.EXE_USER_ID = " + bean.getExeUserId() + " ";

			if (bean.getStage() != null) {
				conditionSql_task = conditionSql_task + " and st.STAGE = "
						+ bean.getStage();
				conditionSql_block = conditionSql_block + " and st.STAGE = "
						+ bean.getStage();
			}else{
				if(0 == platForm){
					//采集端
					conditionSql_task = conditionSql_task + " and st.STAGE in (0) ";
					conditionSql_block = conditionSql_block + " and st.STAGE in (0) ";
				}else if(1 == platForm){
					//编辑端
					conditionSql_task = conditionSql_task + " and st.STAGE in (1,2) ";
					conditionSql_block = conditionSql_block + " and st.STAGE in (1,2) ";
				}
			}

			if (bean.getType() != null) {
				conditionSql_task = conditionSql_task + " and st.TYPE = "
						+ bean.getType();
				conditionSql_block = conditionSql_block + " and st.TYPE = "
						+ bean.getType();
			}

			if (bean.getStatus() != null) {
				conditionSql_task = conditionSql_task + " and st.STATUS = "
						+ bean.getStatus();
				conditionSql_block = conditionSql_block + " and st.STATUS = "
						+ bean.getStatus();
			}


			selectSql = selectSql + fromSql_task + conditionSql_task
						+ " union all " + selectSql
						+ fromSql_block + conditionSql_block;
			
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
					Page page = new Page(curPageNum);
				    page.setPageSize(pageSize);
				    int total = 0;
					while (rs.next()) {
						if(total==0){
							total=rs.getInt("TOTAL_RECORD_NUM_");
						}
						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
						
						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
						subtask.put("name", rs.getString("NAME"));
						subtask.put("descp", rs.getString("DESCP"));

						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("type", rs.getInt("TYPE"));
						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
						subtask.put("status", rs.getInt("STATUS"));
						
						if (1 == rs.getInt("STAGE")) {
							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
						} else if (2 == rs.getInt("STAGE")) {
							subtask.put("dbId",rs.getInt("MONTHLY_DB_ID"));
						} else {
							subtask.put("dbId", rs.getInt("MONTHLY_DB_ID"));
						}

						list.add(subtask);

					}
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}

			};

			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);

		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn
	 * @param bean
	 * @param curPageNum
	 * @param pageSize
	 * @param platForm 
	 * @return
	 * @throws Exception 
	 */
	public static Page getListByUserPage(Connection conn, Subtask bean, final int curPageNum, final int pageSize, int platForm) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select st.SUBTASK_ID "
					+ ",st.NAME"
					+ ",st.DESCP"
					+ ",st.PLAN_START_DATE"
					+ ",st.PLAN_END_DATE"
					+ ",st.STAGE"
					+ ",st.TYPE"
					+ ",st.STATUS"
					+ ",r.DAILY_DB_ID"
					+ ",r.MONTHLY_DB_ID"
					+ ",st.GEOMETRY";

			String fromSql_task = " from subtask st"
					+ ",task t"
					+ ",city c"
					+ ",region r";

			String fromSql_block = " from subtask st"
					+ ",block_man bm"
					+ ",block b"
					+ ",region r";

			String conditionSql_task = " where st.task_id = t.task_id "
					+ "and t.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and (st.EXE_USER_ID = " + bean.getExeUserId() + " or st.EXE_GROUP_ID = " + bean.getExeGroupId() + ")";
//					+ "and st.EXE_USER_ID = " + bean.getExeUserId();

			String conditionSql_block = " where st.block_man_id = bm.block_man_id "
					+ "and bm.block_id = b.block_id "
					+ "and b.region_id = r.region_id "
					+ "and (st.EXE_USER_ID = " + bean.getExeUserId() + " or st.EXE_GROUP_ID = " + bean.getExeGroupId() + ")";
//					+ "and st.EXE_USER_ID = " + bean.getExeUserId();

			if (bean.getStage() != null) {
				conditionSql_task = conditionSql_task + " and st.STAGE = "
						+ bean.getStage();
				conditionSql_block = conditionSql_block + " and st.STAGE = "
						+ bean.getStage();
			}else{
				if(0 == platForm){
					//采集端
					conditionSql_task = conditionSql_task + " and st.STAGE in (0) ";
					conditionSql_block = conditionSql_block + " and st.STAGE in (0) ";
				}else if(1 == platForm){
					//编辑端
					conditionSql_task = conditionSql_task + " and st.STAGE in (1,2) ";
					conditionSql_block = conditionSql_block + " and st.STAGE in (1,2) ";
				}
			}

			if (bean.getType() != null) {
				conditionSql_task = conditionSql_task + " and st.TYPE = "
						+ bean.getType();
				conditionSql_block = conditionSql_block + " and st.TYPE = "
						+ bean.getType();
			}

			if (bean.getStatus() != null) {
				conditionSql_task = conditionSql_task + " and st.STATUS = "
						+ bean.getStatus();
				conditionSql_block = conditionSql_block + " and st.STATUS = "
						+ bean.getStatus();
			}
			
			selectSql = selectSql + fromSql_task
			+ conditionSql_task
			+ " union all " + selectSql
			+ fromSql_block + conditionSql_block;

			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
					Page page = new Page(curPageNum);
				    page.setPageSize(pageSize);
				    int total = 0;
					while (rs.next()) {
						if(total==0){
							total=rs.getInt("TOTAL_RECORD_NUM_");
						}
						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
						
						Subtask subtaskk = new Subtask();
						subtaskk.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtaskk.setGeometry(rs.getString("GEOMETRY"));
						
						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
						subtask.put("name", rs.getString("NAME"));
						subtask.put("descp", rs.getString("DESCP"));

						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("type", rs.getInt("TYPE"));
						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
						subtask.put("status", rs.getInt("STATUS"));
						
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.put("geometry", GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
							List<Integer> gridIds = getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
							subtask.put("gridIds", gridIds);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
												
						
						if (1 == rs.getInt("STAGE")) {
							subtask.put("dbId", rs.getInt("DAILY_DB_ID"));
						} else if (2 == rs.getInt("STAGE")) {
							subtask.put("dbId",rs.getInt("MONTHLY_DB_ID"));
						} else {
							subtask.put("dbId", rs.getInt("MONTHLY_DB_ID"));
						}
						
						list.add(subtask);

					}
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}

			};

			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}

	}


	/**
	 * @param int1
	 * @return
	 * @throws Exception 
	 */
	public static List<Integer> getGridIdsBySubtaskId(int subtaskId) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			String selectSql = "select sgm.grid_id from subtask_grid_mapping sgm where sgm.subtask_id = " + subtaskId;
			
			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> gridIds= new ArrayList<Integer>(); 
					while (rs.next()) {
						gridIds.add(rs.getInt("grid_id"));
					}
					return gridIds;
				}
			};

			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}
	
//
//	/**
//	 * @param createUserId
//	 * @param exeUserId 
//	 * @param name
//	 * @return
//	 * @throws Exception 
//	 */
//	public static Object pushMessage(Integer createUserId, Integer exeUserId, String subtaskName) throws Exception {
//		// TODO Auto-generated method stub
//		try{
//			String msgTitle="子任务通知";
//			UserDeviceService userDeviceService=new UserDeviceService();
//			UserInfoService userService=UserInfoService.getInstance();
//			UserInfo userObj=userService.queryUserInfoByUserId((int)createUserId);
//			String msgContent="【Fastmap】通知："+userObj.getUserRealName()+"已分配“"+subtaskName+"”子任务；请下载数据，安排作业！";
//			userDeviceService.pushMessage(exeUserId, msgTitle, msgContent, 
//					XingeUtil.PUSH_MSG_TYPE_PROJECT, "");
//			
//			return null;
//		
//		}catch(Exception e){
//			log.error(e.getMessage(), e);
//			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
//		}
//		
//	}



	/**
	 * @param conn 
	 * @param userId 
	 * @param groupId
	 * @param stage 
	 * @param conditionJson
	 * @param orderJson
	 * @param pageSize
	 * @param curPageNum
	 * @return
	 * @throws ServiceException 
	 */
	public static Page getList(Connection conn, long userId, int groupId, int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
			final int curPageNum) throws ServiceException {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			String selectSql = "";
			String selectUserSql = "";
			String selectGroupSql = "";
			String extraConditionSql = "";
			
			// 0采集，1日编，2月编，
			if (0 == stage) {
				selectUserSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY, B.BLOCK_ID, B.BLOCK_NAME, U.USER_REAL_NAME AS EXECUTER"
						+ " FROM SUBTASK S, BLOCK B, USER_INFO U, BLOCK_MAN BM"
						+ " WHERE S.BLOCK_ID = B.BLOCK_ID"
						+ " AND U.USER_ID = S.EXE_USER_ID"
						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
						+ " AND BM.LATEST = 1"
						+ " AND BM.COLLECT_GROUP_ID = " + groupId
						+ " AND S.STAGE = " + stage;
			} else if (1 == stage) {
				selectUserSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY, B.BLOCK_ID, B.BLOCK_NAME, U.USER_REAL_NAME AS EXECUTER"
						+ " FROM SUBTASK S, BLOCK B, USER_INFO U, BLOCK_MAN BM"
						+ " WHERE S.BLOCK_ID = B.BLOCK_ID"
						+ " AND U.USER_ID = S.EXE_USER_ID"
						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
						+ " AND BM.LATEST = 1"
						+ " AND BM.DAY_EDIT_GROUP_ID = " + groupId
						+ " AND S.STAGE = " + stage;
				selectGroupSql = "SELECT S.SUBTASK_ID, S.NAME, S.STAGE, S.TYPE, S.DESCP, S.STATUS, S.PLAN_START_DATE, S.PLAN_END_DATE, S.GEOMETRY, B.BLOCK_ID, B.BLOCK_NAME, UG1.GROUP_NAME AS EXECUTER"
						+ " FROM SUBTASK S, BLOCK B, BLOCK_MAN BM, USER_GROUP UG1"
						+ " WHERE S.BLOCK_ID = B.BLOCK_ID"
						+ " AND UG1.GROUP_ID = S.EXE_GROUP_ID"
						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
						+ " AND BM.LATEST = 1"
						+ " AND BM.DAY_EDIT_GROUP_ID = " + groupId
						+ " AND S.STAGE = " + stage;
			} else if (2 == stage) {
				selectUserSql = "SELECT S.SUBTASK_ID,S.NAME,S.STAGE,S.TYPE,S.DESCP,S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,S.GEOMETRY,T.TASK_ID,T.NAME AS TASK_NAME,T.TASK_TYPE,U.USER_REAL_NAME AS EXECUTER"
						+ " FROM SUBTASK S, USER_INFO U, TASK T"
						+ " WHERE S.TASK_ID = T.TASK_ID"
						+ " AND U.USER_ID = S.EXE_USER_ID"
						+ " AND T.LATEST = 1"
						+ " AND T.MONTH_EDIT_GROUP_ID = " + groupId
						+ " AND S.STAGE = " + stage;
				
				selectGroupSql = "SELECT S.SUBTASK_ID,S.NAME,S.STAGE,S.TYPE,S.DESCP,S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,S.GEOMETRY,T.TASK_ID,T.NAME AS TASK_NAME,T.TASK_TYPE,UG1.GROUP_NAME AS EXECUTER"
						+ " FROM SUBTASK S, TASK T, USER_GROUP UG1"
						+ " WHERE S.TASK_ID = T.TASK_ID"
						+ " AND UG1.GROUP_ID = S.EXE_GROUP_ID"
						+ " AND T.LATEST = 1"
						+ " AND T.MONTH_EDIT_GROUP_ID = " + groupId
						+ " AND S.STAGE = " + stage;
			} 
		
			//查询条件
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator<?> keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("subtaskId".equals(key)) {extraConditionSql+=" AND S.SUBTASK_ID="+conditionJson.getInt(key);}
					if ("subtaskName".equals(key)) {	
						extraConditionSql+=" AND S.NAME LIKE '%" + conditionJson.getString(key) +"%'";
					}
					if ("ExeUserId".equals(key)) {extraConditionSql+=" AND S.EXE_USER_ID="+conditionJson.getInt(key);}
					if ("ExeUserName".equals(key)) {
						extraConditionSql+=" AND U.USER_REAL_NAME LIKE '%" + conditionJson.getString(key) +"%'";
					}
					if ("blockName".equals(key)) {
						extraConditionSql+=" AND S.BLOCK_ID = B.BLOCK_ID AND B.BLOCK_NAME LIKE '%" + conditionJson.getString(key) +"%'";
					}
					if ("blockId".equals(key)) {extraConditionSql+=" AND S.BLOCK_ID = "+conditionJson.getInt(key);}
					if ("taskId".equals(key)) {extraConditionSql+=" ADN S.TASK_ID = "+conditionJson.getInt(key);}
					if ("taskName".equals(key)) {
						extraConditionSql+=" AND T.NAME LIKE '%" + conditionJson.getInt(key) +"%'";
					}
					if ("status".equals(key)) {
						extraConditionSql+=" AND S.STATUS IN (" + StringUtils.join(conditionJson.getJSONArray(key).toArray(),",") +")";
					}
				}
			}
			
			String orderSql = "";
			
			// 排序
			if(null!=orderJson && !orderJson.isEmpty()){
				Iterator<?> keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("status".equals(key)) {orderSql+=" ORDER BY STATUS "+orderJson.getString(key);}
					if ("subtaskId".equals(key)) {orderSql+=" ORDER BY SUBTASK_ID "+orderJson.getString(key);}
					if ("blockId".equals(key)) {orderSql+=" ORDER BY block_id "+orderJson.getString(key);}
					if ("planStartDate".equals(key)) {orderSql+=" ORDER BY PLAN_START_DATE "+orderJson.getString(key);}
					if ("planEndDate".equals(key)) {orderSql+=" ORDER BY PLAN_END_DATE "+orderJson.getString(key);}
				}
			}else{orderSql += " ORDER BY SUBTASK_ID";}
	
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
					Page page = new Page(curPageNum);
				    page.setPageSize(pageSize);
				    int total = 0;
					while (rs.next()) {
						if(total==0){
							total=rs.getInt("TOTAL_RECORD_NUM_");
						}
						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
						subtask.put("subtaskName", rs.getString("NAME"));
						subtask.put("descp", rs.getString("DESCP"));
						
						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
						
						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("type", rs.getInt("TYPE"));
						subtask.put("status", rs.getInt("STATUS"));
						
						subtask.put("executer", rs.getString("EXECUTER"));
	
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.put("geometry", GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						//月编
						if(2 == rs.getInt("STAGE")){
							subtask.put("taskId", rs.getInt("TASK_ID"));
							subtask.put("taskName", rs.getString("TASK_NAME"));
							subtask.put("taskType", rs.getInt("TASK_TYPE"));
						}else{
							subtask.put("blockId", rs.getInt("BLOCK_ID"));
							subtask.put("blockName", rs.getString("BLOCK_NAME"));
						}
						
						if(1 == rs.getInt("STATUS")){
							SubtaskStatInfo stat = staticApi.getStatBySubtask(rs.getInt("SUBTASK_ID"));						
							subtask.put("percent", stat.getPercent());
						}
	
						list.add(subtask);
					}
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}
			};
			
			if(0==stage){
				selectSql = selectUserSql + extraConditionSql + orderSql;
			}else{
				selectSql = selectUserSql + extraConditionSql + " UNION ALL " + selectGroupSql + extraConditionSql + orderSql;
			}
			
			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		}
	}


	/**
	 * @param conn 
	 * @param userId 
	 * @param groupId
	 * @param stage 
	 * @param conditionJson
	 * @param orderJson
	 * @param pageSize
	 * @param curPageNum
	 * @return
	 * @throws ServiceException 
	 */
	public static Page getListSnapshot(Connection conn, long userId, int groupId, int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
			final int curPageNum) throws ServiceException {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			String selectSql = "";
			
			//0采集，1日编，2月编
			if(0 == stage){
				selectSql = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,B.BLOCK_ID,B.BLOCK_NAME"
						+ " FROM SUBTASK S ,BLOCK B,BLOCK_MAN BM"
						+ " WHERE S.STAGE = 0"
						+ " AND S.BLOCK_ID = B.BLOCK_ID"
						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
						+ " AND BM.LATEST = 1"
						+ " AND BM.COLLECT_GROUP_ID = " + groupId;
			}else if(1 == stage){
				selectSql = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,B.BLOCK_ID,B.BLOCK_NAME"
						+ " FROM SUBTASK S ,BLOCK B,BLOCK_MAN BM"
						+ " WHERE S.STAGE = 1"
						+ " AND S.BLOCK_ID = B.BLOCK_ID"
						+ " AND B.BLOCK_ID = BM.BLOCK_ID"
						+ " AND BM.LATEST = 1"
						+ " AND BM.DAY_EDIT_GROUP_ID = " + groupId;
			}
			else if(2 ==stage){
				selectSql = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,T.TASK_ID,T.NAME AS TASK_NAME"
						+ " FROM SUBTASK S ,TASK T"
						+ " WHERE S.STAGE = 2"
						+ " AND S.TASK_ID = T.TASK_ID"
						+ " AND T.LATEST = 1"
						+ " AND T.MONTH_EDIT_GROUP_ID = " + groupId;
			}

			//查询条件
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator<?> keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("subtaskName".equals(key)) {	
						selectSql+=" AND S.NAME like '%" + conditionJson.getString(key) +"%'";
					}
					if ("blockId".equals(key)) {selectSql+=" AND S.BLOCK_ID="+conditionJson.getInt(key);}
					if ("taskId".equals(key)) {selectSql+=" AND S.TASK_ID="+conditionJson.getInt(key);}
				}
			}
			
			// 排序
			if(null!=orderJson && !orderJson.isEmpty()){
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("status".equals(key)) {selectSql+=" ORDER BY S.STATUS "+orderJson.getString(key);}
					if ("subtaskId".equals(key)) {selectSql+=" ORDER BY S.SUBTASK_ID "+orderJson.getString(key);}
				}
			}else{
				selectSql+=" ORDER BY S.STATUS DESC";
			}
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
					List<HashMap<Object,Object>> openList = new ArrayList<HashMap<Object,Object>>();
					List<HashMap<Object,Object>> closeList = new ArrayList<HashMap<Object,Object>>();
					List<HashMap<Object,Object>> draftList = new ArrayList<HashMap<Object,Object>>();
					Map<Integer,Object> draftMap = new HashMap<Integer,Object>();
					Page page = new Page(curPageNum);
				    page.setPageSize(pageSize);
				    int total = 0;
				    StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
					while (rs.next()) {
						if(total==0){
							total=rs.getInt("TOTAL_RECORD_NUM_");
						}
						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
						subtask.put("subtaskName", rs.getString("NAME"));
						subtask.put("status", rs.getInt("STATUS"));
						subtask.put("type", rs.getInt("TYPE"));
						
						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
						
						if(rs.getInt("STAGE") == 2){
							subtask.put("taskId", rs.getInt("TASK_ID"));
							subtask.put("taskName", rs.getString("TASK_NAME"));
						}else{
							subtask.put("blockId", rs.getInt("BLOCK_ID"));
							subtask.put("blockName", rs.getString("BLOCK_NAME"));
						}
						
						if(2==rs.getInt("STATUS")){
							draftList.add(subtask);
							continue;
						}else if(0==rs.getInt("STATUS")){
							closeList.add(subtask);
							continue;
						}
						
						SubtaskStatInfo stat = staticApi.getStatBySubtask(rs.getInt("SUBTASK_ID"));
						int percent = stat.getPercent();
						
						subtask.put("percent", percent);
						
						draftMap.put(rs.getInt("SUBTASK_ID"), subtask);
	
//						list.add(subtask);
					}
	
					//开启子任务根据完成度排序
					Object[] key_arr = draftMap.keySet().toArray();     
					Arrays.sort(key_arr);     
					for  (Object key : key_arr) {     
						openList.add((HashMap<Object, Object>) draftMap.get(key));     
					}  
					
					list.addAll(draftList);
					list.addAll(openList);
					list.addAll(closeList);
					
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}
	
			};
			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		}
	}


	/**
	 * 根据taskId获取city几何
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public static String getWktByTaskId(int taskId) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT C.GEOMETRY FROM TASK T, CITY C WHERE T.CITY_ID = C.CITY_ID AND T.LATEST = 1 AND T.TASK_ID = " + taskId;
			
			ResultSetHandler<String> rsHandler = new ResultSetHandler<String>() {
				public String handle(ResultSet rs) throws SQLException {
					String wkt = null; 
					if(rs.next()) {
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							wkt = GeoTranslator.struct2Wkt(struct);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					return wkt;
				}
			};
			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询city几何失败，原因为:"+e.getMessage(),e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


	/**
	 * 根据blockId获取block几何
	 * @param blockId
	 * @return
	 * @throws Exception 
	 */
	public static String getWktByBlockManId(int blockManId) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT B.GEOMETRY FROM BLOCK B,BLOCK_MAN BM WHERE B.BLOCK_ID = BM.BLOCK_ID AND BM.BLOCK_MAN_ID = " + blockManId;
			
			ResultSetHandler<String> rsHandler = new ResultSetHandler<String>() {
				public String handle(ResultSet rs) throws SQLException {
					String wkt = null; 
					if(rs.next()) {
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							wkt = GeoTranslator.struct2Wkt(struct);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					return wkt;
				}
			};
			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询block几何失败，原因为:"+e.getMessage(),e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


//	/**
//	 * @param conn 
//	 * @param subTaskIds
//	 * @return
//	 * @throws SQLException 
//	 */
//	public static Map<Integer, Map<String, Object>> queryMessageAccessorByIds(Connection conn, JSONArray subTaskIds) throws SQLException {
//		// TODO Auto-generated method stub
//		String conditionSql="";
//		conditionSql+=" AND S.SUBTASK_ID IN  ("+subTaskIds.join(",")+")";
//
//		
//		String selectSql1="SELECT S.SUBTASK_ID,S.NAME,S.EXE_USER_ID,S.STAGE,S.STATUS"
//				+ " FROM SUBTASK S , USER_INFO U"
//				+ " WHERE S.EXE_USER_ID = U.USER_ID "+conditionSql;
//		String selectSql2="SELECT S.SUBTASK_ID, S.NAME, U.USER_ID AS EXE_USER_ID, S.STAGE, S.STATUS"
//				+ " FROM SUBTASK S, USER_INFO U, USER_GROUP UG, GROUP_USER_MAPPING GUM"
//				+ " WHERE S.EXE_GROUP_ID = UG.GROUP_ID"
//				+ " AND UG.GROUP_ID = GUM.GROUP_ID"
//				+ " AND GUM.USER_ID = U.USER_ID "+conditionSql;
//		
//		String selectSql = selectSql1 + " UNION ALL " + selectSql2;
//		
//		ResultSetHandler<Map<Integer, Map<String, Object>>> rsHandler = new ResultSetHandler<Map<Integer, Map<String, Object>>>(){
//			public Map<Integer, Map<String, Object>> handle(ResultSet rs) throws SQLException {
//				Map<Integer,Map<String, Object>> result = new HashMap<Integer,Map<String, Object>>();
//				while(rs.next()){
//					if(result.containsKey(rs.getInt("SUBTASK_ID"))){
//						Map<String, Object> map = result.get(rs.getInt("SUBTASK_ID"));
//						List<Integer> accessors = (List<Integer>) map.get("accessors");
//						accessors.add(rs.getInt("EXE_USER_ID"));
//						map.put("accessors", accessors);
//						result.put(rs.getInt("SUBTASK_ID"), map);
//					}else{
//						Map<String, Object> map = new HashMap<String, Object>();
//						map.put("subtaskId", rs.getInt("SUBTASK_ID"));
//						map.put("name", rs.getString("NAME"));
//						map.put("stage", rs.getInt("STAGE"));
//						map.put("status", rs.getInt("STATUS"));
//						List<Integer> accessors = new ArrayList<Integer>();
//						accessors.add(rs.getInt("EXE_USER_ID"));
//						map.put("accessors", accessors);
//						result.put(rs.getInt("SUBTASK_ID"), map);
//					}
//				}
//				return result;
//			}
//    	};
//		
//		QueryRunner run=new QueryRunner();
//		return run.query(conn, selectSql, rsHandler);
//	}


	/**
	 * @param conn
	 * @param subtaskId
	 * @throws Exception 
	 */
	public static void updateStatus(Connection conn, int subtaskId) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String updateSql="UPDATE SUBTASK SET STATUS=1 WHERE SUBTASK_ID =" + subtaskId;
			run.update(conn,updateSql);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn 
	 * @param bean
	 * @throws Exception 
	 */
	public static void pushMessage(Connection conn, Subtask subtask) throws Exception {
		// TODO Auto-generated method stub
		try{
			List<Integer> userIdList = new ArrayList<Integer>();
			//作业组
			if(subtask.getExeGroupId()!=0){
				userIdList = SubtaskOperation.getUserListByGroupId(conn,subtask.getExeGroupId());
			}else{
				userIdList.add(subtask.getExeUserId());
			}

			//构造消息
			String msgTitle = "子任务开启";
			String msgContent = "";
			int push = 0;
			if((int)subtask.getStage()== 0){
				msgContent = "采集子任务:" + subtask.getName() + "内容发生变更，请关注";
				push = 1;
			}else if((int)subtask.getStage()== 1){
				msgContent = "日编子任务:" + subtask.getName() + "内容发生变更，请关注";
			}else{
				msgContent = "月编子任务:" + subtask.getName() + "内容发生变更，请关注";
			}

			for(int i=0;i<userIdList.size();i++){
				Message message = new Message();
				message.setMsgTitle(msgTitle);
				message.setMsgContent(msgContent);
				message.setPushUserId((int)subtask.getExeUserId());
				message.setReceiverId(userIdList.get(i));

				MessageService.getInstance().push(message, push);
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("推送消息失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn 
	 * @param exeGroupId
	 * @return
	 * @throws Exception 
	 */
	public static List<Integer> getUserListByGroupId(Connection conn, Integer exeGroupId) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select gum.user_id from group_user_mapping gum where gum.group_id  = " + exeGroupId;
			
			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> userIds = new ArrayList<Integer>(); 
					while (rs.next()) {
						userIds.add(rs.getInt("user_id"));
					}
					return userIds;
				}
			};

			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
		
	}


//	/**
//	 * @param conn
//	 * @param condition
//	 * @param pageSize
//	 * @param curPageNum
//	 * @return
//	 */
//	public static List<Map<String,Object>> getListSnapshot(Connection conn, JSONObject condition, final int pageSize, final int curPageNum) {
//		// TODO Auto-generated method stub
//		try{
//			QueryRunner run = new QueryRunner();
//			
//			String sql = "";
//
//			
//			String selectSqlCollect = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,U.USER_REAL_NAME AS EXECUTER"
//					+ " FROM SUBTASK S ,BLOCK_MAN BM,USER_INFO U"
//					+ " WHERE S.STAGE = 0"
//					+ " AND S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
//					+ " AND U.USER_ID = S.EXE_USER_ID";
//
//			String selectSqlDailyUser = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,U.USER_REAL_NAME AS EXECUTER"
//					+ " FROM SUBTASK S ,BLOCK_MAN BM,USER_INFO U"
//					+ " WHERE S.STAGE = 1"
//					+ " AND S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
//					+ " AND U.USER_ID = S.EXE_USER_ID";
//			String selectSqlDailyGroup = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS, UG.GROUP_NAME AS EXECUTER"
//					+ " FROM SUBTASK S ,BLOCK_MAN BM, USER_GROUP UG"
//					+ " WHERE S.STAGE = 1"
//					+ " AND S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
//					+ " AND UG.GROUP_ID = S.EXE_GROUP_ID";
//
//			String selectSqlMonthlyUser = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS,U.USER_REAL_NAME AS EXECUTER"
//					+ " FROM SUBTASK S ,TASK T,USER_INFO U"
//					+ " WHERE S.STAGE = 2"
//					+ " AND S.TASK_ID = T.TASK_ID"
//					+ " AND U.USER_ID = S.EXE_USER_ID";
//			String selectSqlMonthlyGroup = "SELECT S.SUBTASK_ID, S.STAGE, S.NAME, S.TYPE, S.STATUS, UG.GROUP_NAME AS EXECUTER"
//					+ " FROM SUBTASK S ,TASK T, USER_GROUP UG"
//					+ " WHERE S.STAGE = 2"
//					+ " AND S.TASK_ID = T.TASK_ID"
//					+ " AND UG.GROUP_ID = S.EXE_GROUP_ID";
//
//
//			//查询条件
//			String conditionSql = "";
//			int stage = -1;
//			int taskFlg = 0;
//			int status = -1;
//			int progress = -1;
//			int completionStatus = -1;
//			int planStatus = -1;
//			Iterator<?> keys = condition.keys();
//			while (keys.hasNext()) {
//				String key = (String) keys.next();
//				//模糊查询
//				if ("subtaskName".equals(key)) {	
//					conditionSql+=" AND S.NAME like '%" + condition.getString(key) +"%'";
//				}
//				//查询条件
//				if ("blockManId".equals(key)) {conditionSql+=" AND S.BLOCK_MAN_ID="+condition.getInt(key);}
//				if ("taskId".equals(key)) {
//					conditionSql+=" AND S.TASK_ID="+condition.getInt(key);
//					taskFlg = 1;
//				}
//				if ("stage".equals(key)) {stage = condition.getInt(key);}
//				//筛选条件
//				if ("status".equals(key)) {
//					conditionSql+=" AND S.STATUS="+condition.getInt(key);
//					status = condition.getInt(key);
//				}
//				if ("progress".equals(key)) {progress = condition.getInt(key);}
//				if ("completionStatus".equals(key)) {completionStatus = condition.getInt(key);}
//				//排序方式
//				if ("planStatus".equals(key)) {planStatus = condition.getInt(key);}
//
//			}
//			
//			if(stage==0){
//				//采集
//				sql = selectSqlCollect + conditionSql;
//			}else if(stage==1){
//				//日编
//				sql = selectSqlDailyUser + conditionSql + " UNION ALL " + selectSqlDailyGroup + conditionSql;
//			}else if(stage==2){
//				//月编
//				sql = selectSqlMonthlyUser + conditionSql + " UNION ALL " + selectSqlMonthlyGroup + conditionSql;
//			}else{
//				if(0 == taskFlg){
//					//采集/日编
//					sql = selectSqlCollect + conditionSql + " UNION ALL " + selectSqlDailyUser + conditionSql + " UNION ALL " + selectSqlDailyGroup + conditionSql;
//				}else{
//					//月编
//					sql = selectSqlMonthlyUser + conditionSql + " UNION ALL " + selectSqlMonthlyGroup + conditionSql;
//				}
//			}
//			
//			
//			ResultSetHandler<List<Map<String,Object>>> rsHandler = new ResultSetHandler<List<Map<String,Object>>>() {
//				public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
//					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
//					
//					Map<Integer,HashMap<Object,Object>> openMap = new HashMap<Integer,HashMap<Object,Object>>();
//					Map<Integer,HashMap<Object,Object>> closeMap = new HashMap<Integer,HashMap<Object,Object>>();
//					Map<Integer,HashMap<Object,Object>> draftMap = new HashMap<Integer,HashMap<Object,Object>>();
//					
////					Map<Integer,Object> draftMap = new HashMap<Integer,Object>();
//				    int total = 0;
//				    StaticsApi staticApi=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
//					while (rs.next()) {
//						total += 1;
//						HashMap<Object,Object> subtask = new HashMap<Object,Object>();
//						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
//						subtask.put("subtaskName", rs.getString("NAME"));
//						subtask.put("status", rs.getInt("STATUS"));
//						subtask.put("type", rs.getInt("TYPE"));
//						
//						if(2==rs.getInt("STATUS")){
//							draftMap.put(rs.getInt("SUBTASK_ID"), subtask);
//						}else if(0==rs.getInt("STATUS")){
//							closeMap.put(rs.getInt("SUBTASK_ID"), subtask);
//						}
//						
//						SubtaskStatInfo stat = staticApi.getStatBySubtask(rs.getInt("SUBTASK_ID"));
//						int percent = stat.getPercent();
//						
//						subtask.put("percent", percent);
//						
//						draftMap.put(rs.getInt("SUBTASK_ID"), subtask);
//	
////						list.add(subtask);
//					}
//	
//					//开启子任务根据完成度排序
//					Object[] key_arr = draftMap.keySet().toArray();     
//					Arrays.sort(key_arr);     
//					for  (Object key : key_arr) {     
//						openList.add((HashMap<Object, Object>) draftMap.get(key));     
//					}  
//					
//					list.addAll(draftList);
//					list.addAll(openList);
//					list.addAll(closeList);
//					
//					page.setTotalCount(total);
//					page.setResult(list);
//					return page;
//				}
//	
//			};
//			return run.query(curPageNum, pageSize, conn, sql, rsHandler);
//			
//			//筛选条件
////			"status"//子任务状态。0关闭，1开启,2草稿,1完成
////			"progress" //进度。1正常(实际作业小于预期)，2异常(实际作业等于或大于预期)
////			"completionStatus"//完成状态。0逾期，1按时，2提前
//
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
//		}
//	}
		
}
