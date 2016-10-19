package com.navinfo.dataservice.engine.statics.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.TimestampUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

public class OracleDao {
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
	 * 根据 subtask_id 表返回 hashmap： key（subtask_id）=value（grid_id list）
	 */
	public static List<Subtask> getSubtaskList() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getManConnection();
			//目前只统计采集（POI，道路，一体化）日编（POI,一体化GRID粗编）子任务
			//如果FM_STAT_OVERVIEW_SUBTASK中该子任务记录为已完成，则不再统计
			String sql = "SELECT DISTINCT S.SUBTASK_ID, S.STAGE,S.TYPE,S.STATUS,S.PLAN_START_DATE,S.PLAN_END_DATE,S.BLOCK_MAN_ID"
					+ " FROM SUBTASK S, FM_STAT_OVERVIEW_SUBTASK FSOS"
					+ " WHERE S.STAGE IN (0, 1)"
					+ " AND S.TYPE IN (0, 1, 2, 3)"
					+ " AND NOT EXISTS (SELECT 1"
					+ " FROM FM_STAT_OVERVIEW_SUBTASK FSOS"
					+ " WHERE S.SUBTASK_ID = FSOS.SUBTASK_ID"
					+ " AND FSOS.STATUS <> 0)"
					+ " ORDER BY SUBTASK_ID";
			
			return run.query(conn, sql, new ResultSetHandler<List<Subtask>>() {

				@Override
				public List<Subtask> handle(ResultSet rs) throws SQLException {
					List<Subtask> list = new ArrayList<Subtask>();
					ManApi api=(ManApi) ApplicationContextUtil.getBean("manApi");
					while (rs.next()) {
						Subtask subtask = new Subtask();

						subtask.setSubtaskId(rs.getInt("SUBTASK_ID"));
						subtask.setStage(rs.getInt("STAGE"));
						subtask.setType(rs.getInt("TYPE"));
						subtask.setStatus(rs.getInt("STATUS"));
						subtask.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						subtask.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						subtask.setBlockManId(rs.getInt("BLOCK_MAN_ID"));
						
//						List<Integer> gridIds = null;
//						try {
//							gridIds = api.getGridIdsBySubtaskId(rs.getInt("SUBTASK_ID"));
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						subtask.setGridIds(gridIds);
						
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
}
