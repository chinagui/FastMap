package com.navinfo.dataservice.engine.statics.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

			conn=DBConnector.getInstance().getManConnection();
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

			conn=DBConnector.getInstance().getManConnection();
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
