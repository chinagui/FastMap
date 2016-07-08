package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.ArrayUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

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
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " PLAN_END_DATE= " + "to_timestamp('" + bean.getPlanEndDate()+ "','yyyy-mm-dd hh24:mi:ss.ff')";
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
	public static List<Subtask> getSubtaskListByIdList(Connection conn,List<Integer> subtaskIdList) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String subtaskIds = "(";
			
			subtaskIds += StringUtils.join(subtaskIdList.toArray(),",") + ")";
			
			
			String selectSql = "select m.SUBTASK_ID"
					+ ",listagg(m.GRID_ID, ',') within group(order by m.SUBTASK_ID) as GRID_ID"
					+ ",s.TYPE"
					+ ",s.STAGE"
					+ " from SUBTASK_GRID_MAPPING m"
					+ ", SUBTASK s"
					+ " where s.SUBTASK_ID = m.Subtask_Id"
					+ " and s.SUBTASK_ID in " + subtaskIds
					+ " group by m.SUBTASK_ID"
					+ ", s.TYPE, s.STAGE";
			
			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>(){
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while(rs.next()){
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						String gridIds = rs.getString("GRID_ID");

						String[] gridIdList = gridIds.split(",");
						subtask.setGridIds(ArrayUtil.convertList(Arrays.asList(gridIdList)));
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
	
	
	//判断采集任务是否可关闭
	public static Boolean isCollectReadyToClose(StaticsApi staticsApi,Subtask subtask)throws Exception{
		try{
			List<String> gridIds = ArrayUtil.reConvertList(subtask.getGridIds());
			List<GridStatInfo> gridStatInfoColArr = staticsApi.getLatestCollectStatByGrids(gridIds);
			//POI
			if(0==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if(1 > (int)gridStatInfoColArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//道路
			else if(1==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if(1 > (int)gridStatInfoColArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}
			//一体化
			else if(2==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if((1 > (int)gridStatInfoColArr.get(j).getPercentPoi()) 
							|| 
							(1 > (int)gridStatInfoColArr.get(j).getPercentRoad())){
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
			List<String> gridIds = ArrayUtil.reConvertList(subtask.getGridIds());
			List<GridStatInfo> gridStatInfoDailyEditArr = staticsApi.getLatestDailyEditStatByGrids(gridIds);
			//POI
			if(0==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if(1 > (int)gridStatInfoDailyEditArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//道路
			else if(1==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if(1 > (int)gridStatInfoDailyEditArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}
			//一体化
			else if(2==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if((1 > (int)gridStatInfoDailyEditArr.get(j).getPercentPoi()) 
							|| 
							(1 > (int)gridStatInfoDailyEditArr.get(j).getPercentRoad())){
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
			List<String> gridIds = ArrayUtil.reConvertList(subtask.getGridIds());
			List<GridStatInfo> gridStatInfoMonthlyEditArr = staticsApi.getLatestDailyEditStatByGrids(gridIds);
			//POI
			if(0==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if(1 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//道路
			else if(1==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if(1 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}
			//一体化
			else if(2==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if((1 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentPoi()) 
							|| 
							(1 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentRoad())){
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
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 * @throws Exception 
	 */
	public static List<Subtask> getListByUser(Connection conn, Subtask bean, int currentPageNum, int pageSize) throws Exception {
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
					+ ",TO_CHAR(st.GEOMETRY.get_wkt()) AS GEOMETRY"
					+ ",listagg(sgm.GRID_ID, ',') within group(order by st.SUBTASK_ID) as GRID_ID ";

			String fromSql_task = " from subtask st"
					+ ",task t"
					+ ",city c"
					+ ",region r"
					+ ",subtask_grid_mapping sgm ";

			String fromSql_block = " from subtask st"
					+ ",block b"
					+ ",city c"
					+ ",region r"
					+ ",subtask_grid_mapping sgm ";

			String conditionSql_task = " where st.task_id = t.task_id "
					+ "and t.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and st.EXE_USER_ID = " + bean.getExeUserId() 
					+ " and st.subtask_id = sgm.subtask_id ";

			String conditionSql_block = " where st.block_id = b.block_id "
					+ "and b.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and st.EXE_USER_ID = " + bean.getExeUserId() 
					+ " and st.subtask_id = sgm.subtask_id ";

			String groupBySql = " group by st.SUBTASK_ID"
					+ ",st.NAME"
					+ ",st.DESCP"
					+ ",st.PLAN_START_DATE"
					+ ",st.PLAN_END_DATE"
					+ ",st.STAGE"
					+ ",st.TYPE"
					+ ",st.STATUS"
					+ ",r.DAILY_DB_ID"
					+ ",r.MONTHLY_DB_ID"
					+ ",TO_CHAR(st.GEOMETRY.get_wkt())";

			if (bean.getStage() != 0) {
				conditionSql_task = conditionSql_task + " and st.STAGE = "
						+ bean.getStage();
				conditionSql_block = conditionSql_block + " and st.STAGE = "
						+ bean.getStage();
			}

			if (bean.getType() != 0) {
				conditionSql_task = conditionSql_task + " and st.TYPE = "
						+ bean.getType();
				conditionSql_block = conditionSql_block + " and st.TYPE = "
						+ bean.getType();
			}

			if (bean.getStatus() != 0) {
				conditionSql_task = conditionSql_task + " and st.STATUS = "
						+ bean.getStatus();
				conditionSql_block = conditionSql_block + " and st.STATUS = "
						+ bean.getStatus();
			}

			selectSql = selectSql + fromSql_task
					+ conditionSql_task + groupBySql
					+ " union all " + selectSql
					+ fromSql_block + conditionSql_block
					+ groupBySql;

			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>() {
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while (rs.next()) {
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setName(rs.getString("NAME"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(rs
								.getTimestamp("PLAN_START_DATE"));
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						subtask.setDescp(rs.getString("DESCP"));
						subtask.setStatus(rs.getInt("STATUS"));

						subtask.setGeometry(rs.getString("GEOMETRY"));

						String gridIds = rs.getString("GRID_ID");
						String[] gridIdList = gridIds.split(",");
						subtask.setGridIds(ArrayUtil.convertList(Arrays.asList(gridIdList)));

						if (1 == rs.getInt("STAGE")) {
							subtask.setDbId(rs.getInt("DAILY_DB_ID"));
						} else if (2 == rs.getInt("STAGE")) {
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
						} else {
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
						}
						list.add(subtask);

					}
					return list;
				}

			};

			return run.query(pageSize, currentPageNum, conn, selectSql,
					rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}

	}


	/**
	 * @param conn
	 * @param bean
	 * @param currentPageNum
	 * @param pageSize
	 * @return
	 * @throws Exception 
	 */
	public static List<Subtask> getListByUserSnapshot(Connection conn, Subtask bean, int currentPageNum, int pageSize) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select st.SUBTASK_ID " + ",st.NAME"
					+ ",st.DESCP" + ",st.PLAN_START_DATE" + ",st.PLAN_END_DATE"
					+ ",st.STAGE" + ",st.TYPE" + ",st.STATUS"
					+ ",r.DAILY_DB_ID" + ",r.MONTHLY_DB_ID";

			String fromSql_task = " from subtask st" + ",task t" + ",city c"
					+ ",region r";

			String fromSql_block = " from subtask st" + ",block b" + ",city c"
					+ ",region r";

			String conditionSql_task = " where st.task_id = t.task_id "
					+ "and t.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and st.EXE_USER_ID = " + bean.getExeUserId() + " ";

			String conditionSql_block = " where st.block_id = b.block_id "
					+ "and b.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and st.EXE_USER_ID = " + bean.getExeUserId() + " ";

			if (bean.getStage() != 0) {
				conditionSql_task = conditionSql_task + " and st.STAGE = "
						+ bean.getStage();
				conditionSql_block = conditionSql_block + " and st.STAGE = "
						+ bean.getStage();
			}

			if (bean.getType() != 0) {
				conditionSql_task = conditionSql_task + " and st.TYPE = "
						+ bean.getType();
				conditionSql_block = conditionSql_block + " and st.TYPE = "
						+ bean.getType();
			}

			if (bean.getStatus() != 0) {
				conditionSql_task = conditionSql_task + " and st.STATUS = "
						+ bean.getStatus();
				conditionSql_block = conditionSql_block + " and st.STATUS = "
						+ bean.getStatus();
			}


			selectSql = selectSql + fromSql_task + conditionSql_task
						+ " union all " + selectSql
						+ fromSql_block + conditionSql_block;

			ResultSetHandler<List<Subtask>> rsHandler = new ResultSetHandler<List<Subtask>>() {
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while (rs.next()) {
						Subtask subtask = new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setName(rs.getString("NAME"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setPlanStartDate(rs
								.getTimestamp("PLAN_START_DATE"));
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						subtask.setDescp(rs.getString("DESCP"));
						subtask.setStatus(rs.getInt("STATUS"));
						
						if (1 == rs.getInt("STAGE")) {
							subtask.setDbId(rs.getInt("DAILY_DB_ID"));
						} else if (2 == rs.getInt("STAGE")) {
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
						} else {
							subtask.setDbId(rs.getInt("MONTHLY_DB_ID"));
						}

						list.add(subtask);

					}
					return list;
				}

			};

			return run.query(pageSize, currentPageNum, conn, selectSql,
					rsHandler);

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
			String createSql = "insert into SUBTASK " ;
			String column = "(SUBTASK_ID, NAME, GEOMETRY, STAGE, TYPE, CREATE_USER_ID, EXE_USER_ID, CREATE_DATE, STATUS, PLAN_START_DATE, PLAN_END_DATE, DESCP";
			String values = " values("
					+ bean.getSubtaskId()
					+ ",'"
					+ bean.getName()
					+ "',"
					+ "sdo_geometry("
					+ "'"
					+ bean.getGeometry()
					+ "',8307)"
					+ ","
					+ bean.getStage()
					+ ","
					+ bean.getType()
					+ ","
					+ bean.getCreateUserId()
					+ ","
					+ bean.getExeUserId()
					+ ", sysdate"
					+ ","
					+ "1"
					+ ",to_date('"
					+ bean.getPlanStartDate().toString().substring(0, 10)
					+ "','yyyy-MM-dd HH24:MI:ss')"
					+ ",to_date('"
					+ bean.getPlanEndDate().toString().substring(0, 10)
					+ "','yyyy-MM-dd HH24:MI:ss')"
					+ ",'"
					+ bean.getDescp()
					+ "'";
			if(0!=bean.getBlockId()){
				column += ", BLOCK_ID)";
				values += ","
						+ bean.getBlockId()
						+ ")";
			}else{
				column += ", TASK_ID)";
				values += ","
						+ bean.getTaskId()
						+ ")";
			}
			createSql += column+values;
			run.update(conn, createSql);

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
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn
	 * @param bean
	 * @param curPageNum
	 * @param pageSize
	 * @return
	 * @throws Exception 
	 */
	public static Page getListByUserSnapshotPage(Connection conn, Subtask bean, final int curPageNum, final int pageSize) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select st.SUBTASK_ID " + ",st.NAME"
					+ ",st.DESCP" + ",st.PLAN_START_DATE" + ",st.PLAN_END_DATE"
					+ ",st.STAGE" + ",st.TYPE" + ",st.STATUS"
					+ ",r.DAILY_DB_ID" + ",r.MONTHLY_DB_ID";

			String fromSql_task = " from subtask st" + ",task t" + ",city c"
					+ ",region r";

			String fromSql_block = " from subtask st" + ",block b" + ",city c"
					+ ",region r";

			String conditionSql_task = " where st.task_id = t.task_id "
					+ "and t.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and st.EXE_USER_ID = " + bean.getExeUserId() + " ";

			String conditionSql_block = " where st.block_id = b.block_id "
					+ "and b.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and st.EXE_USER_ID = " + bean.getExeUserId() + " ";

			if (bean.getStage() != null) {
				conditionSql_task = conditionSql_task + " and st.STAGE = "
						+ bean.getStage();
				conditionSql_block = conditionSql_block + " and st.STAGE = "
						+ bean.getStage();
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
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}


	/**
	 * @param conn
	 * @param bean
	 * @param curPageNum
	 * @param pageSize
	 * @return
	 * @throws Exception 
	 */
	public static Page getListByUserPage(Connection conn, Subtask bean, final int curPageNum, final int pageSize) throws Exception {
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
					+ ",TO_CHAR(st.GEOMETRY.get_wkt()) AS GEOMETRY";

			String fromSql_task = " from subtask st"
					+ ",task t"
					+ ",city c"
					+ ",region r";

			String fromSql_block = " from subtask st"
					+ ",block b"
					+ ",city c"
					+ ",region r";

			String conditionSql_task = " where st.task_id = t.task_id "
					+ "and t.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and st.EXE_USER_ID = " + bean.getExeUserId();

			String conditionSql_block = " where st.block_id = b.block_id "
					+ "and b.city_id = c.city_id "
					+ "and c.region_id = r.region_id "
					+ "and st.EXE_USER_ID = " + bean.getExeUserId();

			if (bean.getStage() != null) {
				conditionSql_task = conditionSql_task + " and st.STAGE = "
						+ bean.getStage();
				conditionSql_block = conditionSql_block + " and st.STAGE = "
						+ bean.getStage();
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
						
						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
						subtask.put("name", rs.getString("NAME"));
						subtask.put("descp", rs.getString("DESCP"));

						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("type", rs.getInt("TYPE"));
						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
						subtask.put("status", rs.getInt("STATUS"));
						subtask.put("dbId", rs.getString("GEOMETRY"));
						
						try {
							List<Integer> gridIds = getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
							subtask.put("gridIds", gridIds);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
												
						subtask.put("geometry", rs.getString("GEOMETRY"));
						
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
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
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
		}

	}
}
