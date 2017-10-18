package com.navinfo.dataservice.engine.statics.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.statics.overview.FmStatOverviewProgram;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.StringUtil;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;
import oracle.sql.CLOB;
import oracle.sql.STRUCT;

public class OracleDao {
	private static Logger log = LogManager.getLogger(OracleDao.class);
	/**
	 * 根据 日大区库的 db_id
	 */

	public static List<Integer> getDbIdDaily() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getManConnection();
			String sql = "select distinct daily_db_id from region";
			return run.query(conn, sql, new ResultSetHandler<List<Integer>>() {

				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> list = new ArrayList<Integer>();
					while (rs.next()) {
						list.add(rs.getInt("daily_db_id"));
					}
					return list;
				}
			});
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 根据 日大区库的 db_id
	 */

	public static List<Integer> getDbIdMonth() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getManConnection();
			String sql = "select distinct monthly_db_id from region";
			return run.query(conn, sql, new ResultSetHandler<List<Integer>>() {

				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> list = new ArrayList<Integer>();
					while (rs.next()) {
						list.add(rs.getInt("monthly_db_id"));
					}
					return list;
				}
			});
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 根据 grid 表返回 hashmap： key（grid_id）=value（block_id）
	 */

	public static Map<String, String> getGrid2Block() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getManConnection();
			String sql = "select grid_id,block_id from grid where block_id is not null";
			return run.query(conn, sql, new ResultSetHandler<Map<String, String>>() {

				@Override
				public Map<String, String> handle(ResultSet rs) throws SQLException {
					Map<String, String> map = new HashMap<String, String>();
					while (rs.next()) {
						map.put(String.valueOf(rs.getInt("grid_id")), String.valueOf(rs.getInt("block_id")));
					}
					return map;
				}
			});
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 根据 grid 表返回 hashmap： key（grid_id）=value（city_id）
	 */

	public static Map<String, String> getGrid2City() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getManConnection();
			String sql = "select grid_id,city_id from grid where city_id is not null";
			return run.query(conn, sql, new ResultSetHandler<Map<String, String>>() {

				@Override
				public Map<String, String> handle(ResultSet rs) throws SQLException {
					Map<String, String> map = new HashMap<String, String>();
					while (rs.next()) {
						map.put(String.valueOf(rs.getInt("grid_id")), String.valueOf(rs.getInt("city_id")));
					}
					return map;
				}
			});
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	

	/**
	 * 未使用废弃
	 */

	@Deprecated
	public static List<Integer> getDbId() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "SELECT DB_ID FROM DB_HUB WHERE UPPER(BIZ_TYPE)=UPPER('regionRoad') and db_id in (8,9)";
			return run.query(conn, sql, new ResultSetHandler<List<Integer>>() {

				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> list = new ArrayList<Integer>();
					while (rs.next()) {
						list.add(rs.getInt("db_id"));
					}
					return list;
				}
			});
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public static List<BlockMan> getBlockManList() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getManConnection();
			String sql = "select * from block_man where latest=1";
			
			return run.query(conn, sql, new ResultSetHandler<List<BlockMan>>() {

				@Override
				public List<BlockMan> handle(ResultSet rs) throws SQLException {
					List<BlockMan> list = new ArrayList<BlockMan>();
					while (rs.next()) {
						BlockMan man = new BlockMan();
						
						man.setBlockId(rs.getInt("BLOCK_ID"));
						
						man.setBlockManId(rs.getInt("block_man_id"));
						
						man.setCollectPlanStartDate(rs.getTimestamp("COLLECT_PLAN_START_DATE"));
						
						man.setCollectPlanEndDate(rs.getTimestamp("COLLECT_PLAN_END_DATE"));
						
						man.setDayEditPlanStartDate(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE"));
						
						man.setDayEditPlanEndDate(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE"));
						
						man.setMonthEditPlanStartDate(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE"));
						
						man.setMonthEditPlanEndDate(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE"));
						
						man.setDayProducePlanStartDate(rs.getTimestamp("DAY_PRODUCE_PLAN_START_DATE"));

						man.setDayProducePlanEndDate(rs.getTimestamp("DAY_PRODUCE_PLAN_END_DATE"));

						man.setMonthProducePlanStartDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_START_DATE"));

						man.setMonthProducePlanEndDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_END_DATE"));

						list.add(man);
						
					}
					return list;
				}
			});
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 获取所有需要被统计的子任务列表
	 */
	public static List<Subtask> getSubtaskListNeedStatistics() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getManConnection();
			//目前只统计采集（POI，道路，一体化）日编（POI,一体化GRID粗编,一体化区域粗编）子任务，月编（poi专项）
			//如果FM_STAT_OVERVIEW_SUBTASK中该子任务记录为已完成，则不再统计
			String sql = "SELECT S.SUBTASK_ID, S.STAGE,S.TYPE,S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,"
					+ " S.TASK_ID,P.TYPE PROGRAM_TYPE,s.geometry FROM SUBTASK S,TASK T,PROGRAM P "
					+ " WHERE S.TASK_ID = T.TASK_ID AND T.PROGRAM_ID = P.PROGRAM_ID "
					+ " AND S.STATUS IN (0, 1) ORDER BY S.SUBTASK_ID";
			
			return run.query(conn, sql, new ResultSetHandler<List<Subtask>>() {

				@Override
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					while (rs.next()) {
						Subtask subtask = new Subtask();

						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setStatus(rs.getInt("STATUS"));
						subtask.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						subtask.setTaskId(rs.getInt("TASK_ID"));
						subtask.setSubType(rs.getInt("PROGRAM_TYPE"));
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							subtask.setGeometry(GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							log.warn("子任务geometry查询错误,"+e1.getMessage());
						}
						list.add(subtask);		
					}
					return list;
				}
			});
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 获取所有日编子任务对应的采集任务id集合
	 * @param subtaskId
	 * @throws ServiceException 
	 */
	public static Map<Integer,Set<Integer>> getCollectTaskIdByDaySubtask() throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String sql = "SELECT s.subtask_id,TT.TASK_ID"
					+ "  FROM SUBTASK S, TASK T, TASK TT"
					+ " WHERE S.TASK_ID = T.TASK_ID"
					+ "   AND TT.BLOCK_ID = T.BLOCK_ID"
					+ "   AND T.PROGRAM_ID = TT.PROGRAM_ID"
					+ "   AND T.region_ID = TT.region_ID"
					+ "   AND s.stage = 1"
					+ "   AND TT.TYPE = 0"
					+ "   order by s.subtask_id";
			
			log.info("getCollectTaskIdByDaySubtask sql :" + sql);
			
			
			ResultSetHandler<Map<Integer,Set<Integer>>> rsHandler = new ResultSetHandler<Map<Integer,Set<Integer>>>() {
				public Map<Integer,Set<Integer>> handle(ResultSet rs) throws SQLException {
					Map<Integer,Set<Integer>> result=new HashMap<>();					
					while(rs.next()) {
						int subtaskId=rs.getInt("SUBTASK_ID");
						if(!result.containsKey(subtaskId)){
							result.put(subtaskId, new HashSet<Integer>());
						}
						result.get(subtaskId).add(rs.getInt("TASK_ID"));
					}
					return result;
				}
			};
			Map<Integer,Set<Integer>> result =  run.query(conn, sql,rsHandler);
			return result;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("getCollectTaskIdByDaySubtask失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 获取所有任务对应的子任务id集合
	 * @param subtaskId
	 * @throws ServiceException 
	 */
	public static Map<Integer,Set<Subtask>> getSubtaskByTaskId() throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String sql = "SELECT s.subtask_id,s.task_id,s.type,s.status,s.work_kind"
					+ "  FROM SUBTASK S"
					+ "  order by s.task_id";
			
			log.info("getSubtaskByTaskId sql :" + sql);			
			
			ResultSetHandler<Map<Integer,Set<Subtask>>> rsHandler = new ResultSetHandler<Map<Integer,Set<Subtask>>>() {
				public Map<Integer,Set<Subtask>> handle(ResultSet rs) throws SQLException {
					Map<Integer,Set<Subtask>> result=new HashMap<>();					
					while(rs.next()) {
						int taskId=rs.getInt("TASK_ID");
						if(!result.containsKey(taskId)){
							result.put(taskId, new HashSet<Subtask>());
						}
						Subtask subtask=new Subtask();
						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setStatus(rs.getInt("STATUS"));
						subtask.setWorkKind(rs.getInt("WORK_KIND"));
						result.get(taskId).add(subtask);
					}
					return result;
				}
			};
			Map<Integer,Set<Subtask>> result =  run.query(conn, sql,rsHandler);
			return result;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("getSubtaskByTaskId失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 获取所有日编任务对应的采集任务id集合
	 * 
	 * @throws ServiceException 
	 */
	public static Map<Integer,Set<Integer>> getCollectTaskIdByDayTask() throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String sql = "SELECT T.TASK_ID,TT.TASK_ID CTASK_ID"
					+ "  FROM TASK T, TASK TT"
					+ " WHERE TT.BLOCK_ID = T.BLOCK_ID"
					+ "   AND T.PROGRAM_ID = TT.PROGRAM_ID"
					+ "   AND T.region_ID = TT.region_ID"
					+ "   AND T.TYPE = 1"
					+ "   AND TT.TYPE = 0"
					+ "   order by T.TASK_ID";
			
			log.info("getCollectTaskIdByDayTask sql :" + sql);
			
			
			ResultSetHandler<Map<Integer,Set<Integer>>> rsHandler = new ResultSetHandler<Map<Integer,Set<Integer>>>() {
				public Map<Integer,Set<Integer>> handle(ResultSet rs) throws SQLException {
					Map<Integer,Set<Integer>> result=new HashMap<>();					
					while(rs.next()) {
						int taskId=rs.getInt("TASK_ID");
						if(!result.containsKey(taskId)){
							result.put(taskId, new HashSet<Integer>());
						}
						result.get(taskId).add(rs.getInt("CTASK_ID"));
					}
					return result;
				}
			};
			Map<Integer,Set<Integer>> result =  run.query(conn, sql,rsHandler);
			return result;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("getCollectTaskIdByDayTask失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}	
	
	/**
	 * 获取所有不需要被统计的子任务列表
	 */
	public static List<Integer> getSubtaskListWithStatistics() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getManConnection();
			//如果FM_STAT_OVERVIEW_SUBTASK中该子任务记录为已完成，则不再统计，读取结果
			String sql = "SELECT FSOS.SUBTASK_ID,FSOS.PERCENT,FSOS.DIFF_DATE,FSOS.PROGRESS,FSOS.STAT_DATE,FSOS.STATUS"
					+ ",FSOS.TOTAL_POI,FSOS.FINISHED_POI,FSOS.TOTAL_ROAD,FSOS.FINISHED_ROAD,FSOS.PERCENT_POI,FSOS.PERCENT_ROAD"
					+ ",FSOS.PLAN_START_DATE,FSOS.PLAN_END_DATE,FSOS.ACTUAL_START_DATE,FSOS.ACTUAL_END_DATE"
					+ ",FSOS.STAT_TIME,FSOS.GRID_PERCENT_DETAILS,FSOS.TASK_ID,fsos.plan_date"
					+ " FROM FM_STAT_OVERVIEW_SUBTASK FSOS"
					+ " WHERE FSOS.STATUS = 0";
			
			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>(){
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> list = new ArrayList<Integer>();
					while(rs.next()){
						int subtaskId = rs.getInt("SUBTASK_ID");
						list.add(subtaskId);
//						Document subtask = new Document();
//						subtask.put("subtaskId", rs.getInt("SUBTASK_ID"));
//						subtask.put("taskId", rs.getInt("TASK_ID"));
//						subtask.put("percent", rs.getInt("PERCENT"));
//						subtask.put("diffDate", rs.getInt("DIFF_DATE"));
//						subtask.put("planDate", rs.getInt("PLAN_DATE"));
//						subtask.put("progress", rs.getInt("PROGRESS"));
//						subtask.put("statDate", DateUtils.timestamptoString(rs.getTimestamp("STAT_DATE"),DateUtils.DATE_COMPACTED_FORMAT));
//						subtask.put("statTime", DateUtils.timestamptoString(rs.getTimestamp("STAT_TIME"),DateUtils.DATE_COMPACTED_FORMAT));
//						subtask.put("status", rs.getInt("STATUS"));
//						
//						
//						subtask.put("planStartDate", DateUtils.timestamptoString(rs.getTimestamp("PLAN_START_DATE"),DateUtils.DATE_COMPACTED_FORMAT));
//						subtask.put("planEndDate", DateUtils.timestamptoString(rs.getTimestamp("PLAN_END_DATE"),DateUtils.DATE_COMPACTED_FORMAT));
//						subtask.put("actualStartDate", DateUtils.timestamptoString(rs.getTimestamp("ACTUAL_START_DATE"),DateUtils.DATE_COMPACTED_FORMAT));
//						subtask.put("actualEndDate", DateUtils.timestamptoString(rs.getTimestamp("ACTUAL_END_DATE"),DateUtils.DATE_COMPACTED_FORMAT));
//						
//						subtask.put("totalPoi", rs.getInt("TOTAL_POI"));
//						subtask.put("finishedPoi", rs.getInt("FINISHED_POI"));
//						subtask.put("percentPoi", rs.getInt("PERCENT_POI"));
//						
//						subtask.put("totalRoad", rs.getInt("TOTAL_ROAD"));
//						subtask.put("finishedRoad", rs.getInt("FINISHED_ROAD"));
//						subtask.put("percentRoad", rs.getInt("PERCENT_ROAD"));
//
//						CLOB gridPercentDetails = (CLOB) rs.getClob("GRID_PERCENT_DETAILS");
//						String gridPercentDetails1 = StringUtil.ClobToString(gridPercentDetails);
//						JSONObject dataJson = null;
//						if(!gridPercentDetails1.isEmpty()){
//							dataJson = JSONObject.fromObject(gridPercentDetails1);
//						}
//						subtask.put("gridPercentDetails", dataJson);
//
//						list.add(subtask);
					}
					return list;
				}
	    	};
			
			return run.query(conn, sql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	

	/**
	 * @Title: getBlockManListWithStat
	 * @Description: 获取BlockMan集合  根据 status latest
	 * @return
	 * @throws ServiceException  List<Subtask>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年10月20日 下午4:17:58 
	 */
	public static List<Map<String,Object>> getBlockManListWithStat() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getManConnection();
			//目前只统计采集（POI，道路，一体化）日编（POI,一体化GRID粗编）BLOCKMAN
			//如果FM_STAT_OVERVIEW_BLOCKMAN中该记录为已完成，则将其实际结束时间存入Mongo 和 oracle ,
			//重:保持 mongo 库 和 oracle 库的统计数据一致
			
			String sql = "select  "
					+ "DISTINCT "
					+ "B.BLOCK_MAN_ID, B.STATUS,B.COLLECT_PLAN_START_DATE,B.COLLECT_PLAN_END_DATE, "
					+ "B.DAY_EDIT_PLAN_START_DATE,B.DAY_EDIT_PLAN_END_DATE,B.TASK_ID, "
					+ " B.ROAD_PLAN_TOTAL,B.POI_PLAN_TOTAL,"
					+ " B.COLLECT_GROUP_ID ,B.DAY_EDIT_GROUP_ID, "
					+ " FSOB.STATUS F_STATUS,FSOB.COLLECT_ACTUAL_END_DATE F_COLLECT_ACTUAL_END_DATE, "
					+ " FSOB.DAILY_ACTUAL_END_DATE F_DAILY_ACTUAL_END_DATE  "
					+ "from   "
					+ "BLOCK_MAN B left join FM_STAT_OVERVIEW_BLOCKMAN FSOB  on B.BLOCK_MAN_ID = FSOB.BLOCK_MAN_ID  "
					+ "WHERE  "
					+ "B.STATUS IN (0, 1) AND B.LATEST =1   "
					+ "ORDER BY BLOCK_MAN_ID";
			return run.query(conn, sql, new ResultSetHandler<List<Map<String,Object>>>() {

				@Override
				public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
					//ManApi api=(ManApi) ApplicationContextUtil.getBean("manApi");
					while (rs.next()) {
						Map<String,Object> blockManMap = new HashMap<String,Object>();

						blockManMap.put("blockManId",rs.getInt("BLOCK_MAN_ID"));
						blockManMap.put("status",rs.getInt("STATUS"));
						blockManMap.put("collectGroupId",rs.getInt("COLLECT_GROUP_ID"));
						blockManMap.put("dailyGroupId",rs.getInt("DAY_EDIT_GROUP_ID"));
						
						blockManMap.put("collectPlanStartDate",rs.getTimestamp("COLLECT_PLAN_START_DATE"));
						blockManMap.put("collectPlanEndDate",rs.getTimestamp("COLLECT_PLAN_END_DATE"));
						
						blockManMap.put("dailyPlanStartDate",rs.getTimestamp("DAY_EDIT_PLAN_START_DATE"));
						blockManMap.put("dailyPlanEndDate",rs.getTimestamp("DAY_EDIT_PLAN_END_DATE"));
						blockManMap.put("taskId",rs.getInt("TASK_ID"));
						blockManMap.put("roadPlanTotal",rs.getInt("ROAD_PLAN_TOTAL"));
						blockManMap.put("poiPlanTotal",rs.getInt("POI_PLAN_TOTAL"));
						
						blockManMap.put("fStatus",rs.getInt("F_STATUS"));
						blockManMap.put("fCollectActualEndDate",rs.getTimestamp("F_COLLECT_ACTUAL_END_DATE"));
						blockManMap.put("fDailyActualEndDate",rs.getTimestamp("F_DAILY_ACTUAL_END_DATE"));
//						List<Integer> gridIds = null;
//						try {
//							gridIds = api.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						subtask.setGridIds(gridIds);
						
						list.add(blockManMap);
						
					}
					return list;
				}
			});
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @Title: getProgramListWithStat
	 * @Description: 获取Program集合  根据 status latest
	 * @return
	 * @throws ServiceException 
	 * @throws 
	 * @date 2016年10月20日 下午4:17:58 
	 */
	public static List<FmStatOverviewProgram> getProgramListWithStat() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getManConnection();
			
			String sql = "SELECT P.PROGRAM_ID,"
					+ "       P.STATUS,"
					+ "       P.TYPE,"
					+ "       P.PLAN_START_DATE,"
					+ "       P.PLAN_END_DATE,"
					+ "       P.COLLECT_PLAN_START_DATE,"
					+ "       P.COLLECT_PLAN_END_DATE,"
					+ "       P.DAY_EDIT_PLAN_START_DATE,"
					+ "       P.DAY_EDIT_PLAN_END_DATE,"
					+ "       P.MONTH_EDIT_PLAN_START_DATE,"
					+ "       P.MONTH_EDIT_PLAN_END_DATE,"
					+ "       F.PERCENT,"
					+ "       F.DIFF_DATE,"
					+ "       F.PROGRESS,"
					+ "       NVL(F.STATUS, 9) OLD_STATUS,"
					+ "       F.COLLECT_PROGRESS,"
					+ "       F.COLLECT_PERCENT,"
					+ "       F.DAILY_PROGRESS,"
					+ "       F.DAILY_PERCENT,"
					+ "       F.MONTHLY_PROGRESS,"
					+ "       F.MONTHLY_PERCENT,"
					+ "       F.ACTUAL_END_DATE,"
					+ "       F.COLLECT_ACTUAL_END_DATE,"
					+ "       F.COLLECT_DIFF_DATE,"
					+ "       F.DAILY_ACTUAL_END_DATE,"
					+ "       F.DAILY_DIFF_DATE,"
					+ "       F.POI_PLAN_TOTAL,"
					+ "       F.ROAD_PLAN_TOTAL,"
					+ "       F.MONTHLY_ACTUAL_END_DATE,"
					+ "       F.MONTHLY_DIFF_DATE,"
					+ "       SUM(CASE T.TYPE WHEN 0 THEN T.POI_PLAN_TOTAL ELSE 0 END) NEW_POI_PLAN_TOTAL,"
					+ "       SUM(CASE T.TYPE WHEN 0 THEN T.ROAD_PLAN_TOTAL ELSE 0 END) NEW_ROAD_PLAN_TOTAL,"
					+ "       SUM(CASE T.TYPE WHEN 0 THEN T.PERCENT ELSE 0 END) NEW_COLLECT_SUM,"
					+ "       SUM(CASE T.TYPE WHEN 0 THEN 1 ELSE 0 END) NEW_COLLECT_COUNT,"
					+ "       SUM(CASE T.TYPE WHEN 2 THEN NVL(T.PROGRESS, 1) - 1 ELSE 0 END) NEW_COLLECT_PROGRESS,"
					+ "       SUM(CASE T.TYPE WHEN 1 THEN T.PERCENT ELSE 0 END) NEW_DAILY_SUM,"
					+ "       SUM(CASE T.TYPE WHEN 1 THEN 1 ELSE 0 END) NEW_DAILY_COUNT,"
					+ "       SUM(CASE T.TYPE WHEN 2 THEN NVL(T.PROGRESS, 1) - 1 ELSE 0 END) NEW_DAILY_PROGRESS,"
					+ "       SUM(CASE T.TYPE WHEN 2 THEN T.PERCENT ELSE 0 END) NEW_MONTHLY_SUM,"
					+ "       SUM(CASE T.TYPE WHEN 2 THEN 1 ELSE 0 END) NEW_MONTHLY_COUNT,"
					+ "       SUM(CASE T.TYPE WHEN 2 THEN NVL(T.PROGRESS, 1) - 1 ELSE 0 END) NEW_MONTHLY_PROGRESS"
					+ "  FROM PROGRAM P, FM_STAT_OVERVIEW_PROGRAM F, FM_STAT_OVERVIEW_TASK T"
					+ " WHERE P.PROGRAM_ID = F.PROGRAM_ID(+)"
					+ "   AND P.PROGRAM_ID = T.PROGRAM_ID(+)"
					+ "   AND P.LATEST = 1"
					+ "   AND P.STATUS IN (0, 1)"
					+ " GROUP BY P.PROGRAM_ID,"
					+ "          P.STATUS,"
					+ "          P.TYPE,"
					+ "          P.PLAN_START_DATE,"
					+ "          P.PLAN_END_DATE,"
					+ "          P.COLLECT_PLAN_START_DATE,"
					+ "          P.COLLECT_PLAN_END_DATE,"
					+ "          P.DAY_EDIT_PLAN_START_DATE,"
					+ "          P.DAY_EDIT_PLAN_END_DATE,"
					+ "          P.MONTH_EDIT_PLAN_START_DATE,"
					+ "          P.MONTH_EDIT_PLAN_END_DATE,"
					+ "          F.PERCENT,"
					+ "          F.DIFF_DATE,"
					+ "          F.PROGRESS,"
					+ "          NVL(F.STATUS, 9),"
					+ "          F.COLLECT_PROGRESS,"
					+ "          F.COLLECT_PERCENT,"
					+ "          F.DAILY_PROGRESS,"
					+ "          F.DAILY_PERCENT,"
					+ "          F.MONTHLY_PROGRESS,"
					+ "          F.MONTHLY_PERCENT,"
					+ "          F.ACTUAL_END_DATE,"
					+ "          F.COLLECT_ACTUAL_END_DATE,"
					+ "          F.COLLECT_DIFF_DATE,"
					+ "          F.DAILY_ACTUAL_END_DATE,"
					+ "          F.DAILY_DIFF_DATE,"
					+ "          F.POI_PLAN_TOTAL,"
					+ "          F.ROAD_PLAN_TOTAL,"
					+ "          F.MONTHLY_ACTUAL_END_DATE,"
					+ "          F.MONTHLY_DIFF_DATE";
			log.info("getProgramListWithStat sql:"+sql);
			return run.query(conn, sql, new ResultSetHandler<List<FmStatOverviewProgram>>() {

				@Override
				public List<FmStatOverviewProgram> handle(ResultSet rs) throws SQLException {
					List<FmStatOverviewProgram> list = new ArrayList<FmStatOverviewProgram>();
					//ManApi api=(ManApi) ApplicationContextUtil.getBean("manApi");
					while (rs.next()) {
						FmStatOverviewProgram program=new FmStatOverviewProgram();
						program.setProgramId(rs.getInt("PROGRAM_ID"));
						program.setStatus(rs.getInt("STATUS"));
						program.setType(rs.getInt("TYPE"));
						program.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						program.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						program.setCollectPlanStartDate(rs.getTimestamp("COLLECT_PLAN_START_DATE"));
						program.setCollectPlanEndDate(rs.getTimestamp("COLLECT_PLAN_END_DATE"));
						program.setDailyPlanStartDate(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE"));
						program.setDailyPlanEndDate(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE"));
						program.setMonthlyPlanStartDate(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE"));
						program.setMonthlyPlanEndDate(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE"));
						program.setCollectProgress(rs.getInt("COLLECT_PROGRESS"));
						program.setCollectPercent(rs.getInt("COLLECT_PERCENT"));
						program.setDailyProgress(rs.getInt("DAILY_PROGRESS"));
						program.setDailyPercent(rs.getInt("DAILY_PERCENT"));
						program.setMonthlyProgress(rs.getInt("MONTHLY_PROGRESS"));
						program.setMonthlyPercent(rs.getInt("MONTHLY_PERCENT"));
						program.setActualEndDate(rs.getTimestamp("ACTUAL_END_DATE"));
						program.setCollectActualEndDate(rs.getTimestamp("COLLECT_ACTUAL_END_DATE"));
						program.setDailyActualEndDate(rs.getTimestamp("DAILY_ACTUAL_END_DATE"));
						program.setMonthlyActualEndDate(rs.getTimestamp("MONTHLY_ACTUAL_END_DATE"));
						program.setPoiPlanTotal(rs.getInt("POI_PLAN_TOTAL"));
						program.setPoiPlanTotal(rs.getInt("ROAD_PLAN_TOTAL"));
						
						int oldStatus=rs.getInt("OLD_STATUS");
						if(oldStatus==9||oldStatus==1||program.getStatus()==1){
							//之前没有统计数据/之前统计的信息中项目处于开启状态/现在项目处于开启状态,需要重新计算采日月的进度
							int collectPercentSum=rs.getInt("NEW_COLLECT_SUM");
							int collectCount=rs.getInt("NEW_COLLECT_COUNT");
							int collectProgress=rs.getInt("NEW_COLLECT_PROGRESS");
							program.setCollectProgress(collectProgress);
							if(collectCount==0){program.setCollectPercent(0);}
							else{program.setCollectPercent(collectPercentSum/collectCount);}
							
							int dailyPercentSum=rs.getInt("NEW_DAILY_SUM");
							int dailyCount=rs.getInt("NEW_DAILY_COUNT");
							int dailyProgress=rs.getInt("NEW_DAILY_PROGRESS");
							program.setDailyProgress(dailyProgress);
							if(dailyCount==0){program.setDailyPercent(0);}
							else{program.setDailyPercent(dailyPercentSum/dailyCount);}
							
							int monthlyPercentSum=rs.getInt("NEW_MONTHLY_SUM");
							int monthlyCount=rs.getInt("NEW_MONTHLY_COUNT");
							int monthlyProgress=rs.getInt("NEW_MONTHLY_PROGRESS");
							program.setMonthlyProgress(monthlyProgress);
							if(monthlyCount==0){program.setMonthlyPercent(0);}
							else{program.setMonthlyPercent(monthlyPercentSum/monthlyCount);}
							
							program.setPoiPlanTotal(rs.getInt("NEW_POI_PLAN_TOTAL"));
							program.setPoiPlanTotal(rs.getInt("NEW_ROAD_PLAN_TOTAL"));	
							if(program.getStatus()==0){//已关闭项目
								program.setActualEndDate(new Timestamp(System.currentTimeMillis()));
								program.setCollectActualEndDate(new Timestamp(System.currentTimeMillis()));
								program.setDailyActualEndDate(new Timestamp(System.currentTimeMillis()));
								program.setMonthlyActualEndDate(new Timestamp(System.currentTimeMillis()));
							}
						}												
						list.add(program);						
					}
					return list;
				}
			});
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * Map<Integer, Long>
	 * @return
	 */
	public static ResultSetHandler<Map<Integer, Long>> numRsHandler(){
		ResultSetHandler<Map<Integer, Long>> rsHandler = new ResultSetHandler<Map<Integer, Long>>() {
			public Map<Integer, Long> handle(ResultSet rs) throws SQLException {
				Map<Integer, Long> result=new HashMap<>();
				while (rs.next()) {
					int subtaskId = rs.getInt("ID");
					long num=rs.getLong("NUM");
					result.put(subtaskId, num);
				}
				return result;
			}	
		};
	return rsHandler;
	}
	
}