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

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
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
}
