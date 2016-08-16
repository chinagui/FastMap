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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.commons.util.ArrayUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.xinge.XingeUtil;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.userDevice.UserDeviceService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

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
					if(100 > (int)gridStatInfoColArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//道路
			else if(1==subtask.getType()){
				for(int j=0;j<gridStatInfoColArr.size();j++){
					if(100 > (int)gridStatInfoColArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}
			//一体化
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
			List<String> gridIds = ArrayUtil.reConvertList(subtask.getGridIds());
			List<GridStatInfo> gridStatInfoDailyEditArr = staticsApi.getLatestDailyEditStatByGrids(gridIds);
			//POI
			if(0==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if(100 > (int)gridStatInfoDailyEditArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//道路
			else if(1==subtask.getType()){
				for(int j=0;j<gridStatInfoDailyEditArr.size();j++){
					if(100 > (int)gridStatInfoDailyEditArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}
			//一体化
			else if(2==subtask.getType()){
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
			List<String> gridIds = ArrayUtil.reConvertList(subtask.getGridIds());
			List<GridStatInfo> gridStatInfoMonthlyEditArr = staticsApi.getLatestDailyEditStatByGrids(gridIds);
			//POI
			if(0==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if(100 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentPoi()){
						return false;
					}
				}
				return true;
			}
			//道路
			else if(1==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if(100 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentRoad()){
						return false;
					}
				}
				return true;
			}
			//一体化
			else if(2==subtask.getType()){
				for(int j=0;j<gridStatInfoMonthlyEditArr.size();j++){
					if((100 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentPoi()) 
							|| 
							(100 > (int)gridStatInfoMonthlyEditArr.get(j).getPercentRoad())){
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
			
			List<Object> value=new ArrayList();
			value.add(bean.getSubtaskId());
			value.add(bean.getName());
			
			SqlClause inClause = SqlClause.genGeoClauseWithGeoString(conn,bean.getGeometry());
			if (inClause!=null)
//				value.add(inClause.getSql());
				value.add(inClause.getValues().get(0));
			
			value.add(bean.getStage());
			value.add(bean.getType());
			value.add(bean.getCreateUserId());
			value.add(bean.getExeUserId());
			value.add(new Timestamp(System.currentTimeMillis()).toString().substring(0, 10));
			value.add(bean.getStatus());
			value.add(bean.getPlanStartDate().toString().substring(0, 10));
			value.add(bean.getPlanEndDate().toString().substring(0, 10));
			value.add(bean.getDescp());

			String createSql = "insert into SUBTASK " ;
			String column = "(SUBTASK_ID, NAME, GEOMETRY, STAGE, TYPE, CREATE_USER_ID, EXE_USER_ID, CREATE_DATE, STATUS, PLAN_START_DATE, PLAN_END_DATE, DESCP";
			String values = "values(?,?,sdo_geometry(?,8307),?,?,?,?,to_date(?,'yyyy-MM-dd HH24:MI:ss'),?,to_date(?,'yyyy-MM-dd HH24:MI:ss'),to_date(?,'yyyy-MM-dd HH24:MI:ss'),?";

			if(0!=bean.getBlockId()){
				column += ", BLOCK_ID)";
				value.add(bean.getBlockId());
				values += ",?)";

			}else{
				column += ", TASK_ID)";
				value.add(bean.getTaskId());
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
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
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
					+ ",st.GEOMETRY";

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
	

	/**
	 * @param createUserId
	 * @param exeUserId 
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public static Object pushMessage(Integer createUserId, Integer exeUserId, String subtaskName) throws Exception {
		// TODO Auto-generated method stub
		try{
			String msgTitle="子任务通知";
			UserDeviceService userDeviceService=new UserDeviceService();
			UserInfoService userService=UserInfoService.getInstance();
			UserInfo userObj=userService.queryUserInfoByUserId((int)createUserId);
			String msgContent="【Fastmap】通知："+userObj.getUserRealName()+"已分配“"+subtaskName+"”子任务；请下载数据，安排作业！";
			userDeviceService.pushMessage(exeUserId, msgTitle, msgContent, 
					XingeUtil.PUSH_MSG_TYPE_PROJECT, "");
			
			return null;
		
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
		
	}



	/**
	 * @param stage
	 * @param conditionJson
	 * @param orderJson
	 * @param pageSize
	 * @param curPageNum
	 * @return
	 * @throws ServiceException 
	 */
	public static Page getList(int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
			final int curPageNum) throws ServiceException {
		Connection conn = null;
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select s.SUBTASK_ID,s.STAGE,s.EXE_USER_ID,u.user_real_name,s.TYPE,s.PLAN_START_DATE,s.PLAN_END_DATE,s.DESCP,s.NAME,s.STATUS,s.GEOMETRY";
			
			String selectBlockSql = ",b.block_id,b.block_name,-1 as task_id,'' as task_name";
			String selectTaskSql = ",-1 as block_id,'' as block_name,t.task_id,t.name as task_name";
	
			// 0采集，1日编，2月编，
			if (0 == stage) {
				selectBlockSql += ",bm.COLLECT_PLAN_START_DATE AS COLLECT_PLAN_START_DATE,bm.COLLECT_PLAN_END_DATE AS COLLECT_PLAN_END_DATE,bm.COLLECT_GROUP_ID AS group_id";
			} else if (1 == stage) {
				selectBlockSql += ",bm.DAY_EDIT_PLAN_START_DATE AS DAY_EDIT_PLAN_START_DATE,bm.DAY_EDIT_PLAN_END_DATE AS DAY_EDIT_PLAN_END_DATE,bm.DAY_EDIT_GROUP_ID AS group_id";
			} else if (2 == stage) {
				selectBlockSql += ",bm.MONTH_EDIT_PLAN_START_DATE AS MONTH_EDIT_PLAN_START_DATE,bm.MONTH_EDIT_PLAN_END_DATE AS MONTH_EDIT_PLAN_END_DATE,bm.MONTH_EDIT_GROUP_ID AS group_id";
				selectTaskSql += ",t.MONTH_EDIT_PLAN_START_DATE AS MONTH_EDIT_PLAN_START_DATE,t.MONTH_EDIT_PLAN_END_DATE AS MONTH_EDIT_PLAN_END_DATE,t.MONTH_EDIT_GROUP_ID AS group_id";
			}
	
			String fromBlockSql = " from SUBTASK s, Block b, Block_man bm, user_info u where s.block_id = b.block_id and u.user_id = s.exe_user_id and b.block_id = bm.block_id and bm.latest = 1";
			String fromTaskSql = " from SUBTASK s, Task t, user_info u where s.task_id = t.task_id and u.user_id = s.exe_user_id";
			String conditionSql = " and s.stage = " + stage;
			
			//查询条件
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("subtaskId".equals(key)) {conditionSql+=" and s.SUBTASK_ID="+conditionJson.getInt(key);}
					if ("subtaskName".equals(key)) {	
						conditionSql+=" and s.NAME like '%" + conditionJson.getString(key) +"%'";
					}
					if ("ExeUserId".equals(key)) {conditionSql+=" and s.EXE_USER_ID="+conditionJson.getInt(key);}
					if ("ExeUserName".equals(key)) {
						conditionSql+=" and u.user_real_name like '%" + conditionJson.getString(key) +"%'";
					}
					if ("blockName".equals(key)) {
						conditionSql+=" and s.block_id = b.block_id and b.block_name like '%" + conditionJson.getString(key) +"%'";
					}
					if ("blockId".equals(key)) {conditionSql+=" and s.block_id="+conditionJson.getInt(key);}
					if ("taskId".equals(key)) {conditionSql+=" and s.task_id="+conditionJson.getInt(key);}
					if ("taskName".equals(key)) {
						conditionSql+=" and s.task_id = t.task_id and t.name like '%" + conditionJson.getInt(key) +"%'";
					}
					if ("status".equals(key)) {
						conditionSql+=" and s.status in (" + StringUtils.join(conditionJson.getJSONArray(key).toArray(),",") +")";
					}
				}
			}
			
			String orderSql = "";
			
			// 排序
			if(null!=orderJson && !orderJson.isEmpty()){
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("status".equals(key)) {orderSql+=" order by s.status "+orderJson.getString(key);}
					if ("subtaskId".equals(key)) {orderSql+=" order by s.SUBTASK_ID "+orderJson.getString(key);}
					if ("blockId".equals(key)) {orderSql+=" order by block_id "+orderJson.getString(key);}
					if ("planStartDate".equals(key)) {orderSql+=" order by s.PLAN_START_DATE "+orderJson.getString(key);}
					if ("planEndDate".equals(key)) {orderSql+=" order by s.PLAN_END_DATE "+orderJson.getString(key);}
				}
			}else{orderSql += " order by block_id";}
	
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
						subtask.put("subtaskName", rs.getString("NAME"));
						subtask.put("descp", rs.getString("DESCP"));
	
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.put("geometry", GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	
						subtask.put("stage", rs.getInt("STAGE"));
						subtask.put("type", rs.getInt("TYPE"));
						subtask.put("status", rs.getInt("STATUS"));
						subtask.put("ExeUserId", rs.getInt("EXE_USER_ID"));
						subtask.put("planStartDate", df.format(rs.getTimestamp("PLAN_START_DATE")));
						subtask.put("planEndDate", df.format(rs.getTimestamp("PLAN_END_DATE")));
						subtask.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						subtask.put("groupId", rs.getInt("group_id"));
						
						// 与block关联，返回block信息。
						if (rs.getInt("block_id") > 0) {
							// block
							subtask.put("blockId", rs.getInt("block_id"));
							subtask.put("blockName", rs.getString("block_name"));
							//采集
							if(0 == rs.getInt("STAGE")){
								subtask.put("BlockCollectPlanStartDate", df.format(rs.getTimestamp("COLLECT_PLAN_START_DATE")));
								subtask.put("BlockCollectPlanEndDate",df.format(rs.getTimestamp("COLLECT_PLAN_END_DATE")));
							}
							//日编
							else if(1 == rs.getInt("STAGE")){
								subtask.put("BlockDayEditPlanStartDate", df.format(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE")));
								subtask.put("BlockDayEditPlanEndDate", df.format(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE")));
							}
							//月编
							else if(2 == rs.getInt("STAGE")){
								subtask.put("BlockCMonthEditPlanStartDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
								subtask.put("BlockCMonthEditPlanEndDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
							}
						}
						// 与task关联，返回task信息。
						if (rs.getInt("task_id") > 0) {
							// task
							subtask.put("taskId", rs.getInt("task_id"));
							subtask.put("taskName", rs.getString("task_name"));
							// 月编
							if(2 == rs.getInt("STAGE")){
								subtask.put("TaskCMonthEditPlanStartDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE")));
								subtask.put("TaskCMonthEditPlanEndDate", df.format(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE")));
							}
						}
	
						list.add(subtask);
					}
	
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}
	
			};
			if (2 == stage){
				selectSql = selectSql + selectBlockSql + fromBlockSql + conditionSql + " union all " + selectSql + selectTaskSql + fromTaskSql + conditionSql + orderSql;
			}else{
				selectSql = selectSql + selectBlockSql + fromBlockSql + conditionSql + orderSql;
			}
			return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


	/**
	 * @param stage
	 * @param conditionJson
	 * @param orderJson
	 * @param pageSize
	 * @param curPageNum
	 * @return
	 * @throws ServiceException 
	 */
	public static Page getListSnapshot(int stage, JSONObject conditionJson, JSONObject orderJson, final int pageSize,
			final int curPageNum) throws ServiceException {
		Connection conn = null;
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select s.SUBTASK_ID,s.STAGE,s.NAME,s.STATUS from subtask s where s.stage=" + stage;
			
			//查询条件
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("subtaskId".equals(key)) {selectSql+=" and s.SUBTASK_ID="+conditionJson.getInt(key);}
					if ("subtaskName".equals(key)) {	
						selectSql+=" and s.NAME like '%" + conditionJson.getString(key) +"%'";
					}
					if ("status".equals(key)) {
						selectSql+=" and s.status in (" + StringUtils.join(conditionJson.getJSONArray(key).toArray(),",") +")";
					}
				}
			}
			
			// 排序
			if(null!=orderJson && !orderJson.isEmpty()){
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("status".equals(key)) {selectSql+=" order by s.status "+orderJson.getString(key);}
					if ("subtaskId".equals(key)) {selectSql+=" order by s.SUBTASK_ID "+orderJson.getString(key);}
				}
			}else{
				selectSql+=" order by s.status desc";
			}
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
						subtask.put("subtaskName", rs.getString("NAME"));

						subtask.put("status", rs.getInt("STATUS"));
	
						list.add(subtask);
					}
	
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
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
