package com.navinfo.dataservice.engine.meta.scPointPointAddrck;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.QueryRunner;

public class ScPointPointAddrck {
	
	
	private static class SingletonHolder {
		private static final ScPointPointAddrck INSTANCE = new ScPointPointAddrck();
	}

	public static final ScPointPointAddrck getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public List<String> getPointAddrck(int type) throws Exception {
		Connection conn = null;
		String sql = "select s.keyword from SC_POINT_POINTADDRCK s where s.type = ?";
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<String>> rsHandler = new ResultSetHandler<List<String>>() {

				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<String> data = new ArrayList<>();
					while (rs.next()) {
						String keyWord = rs.getString("keyword");
						data.add(keyWord);
					}
					return data;
				}
			};
			return run.query(conn, sql, rsHandler, type);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	
	public List<String> getPointAddrck(int type, String hmFlag) throws Exception {
		Connection conn = null;
		String sql = "select s.keyword from SC_POINT_POINTADDRCK s where s.type = ? and s.hm_flag = ? ";
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<String>> rsHandler = new ResultSetHandler<List<String>>() {

				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<String> data = new ArrayList<>();
					while (rs.next()) {
						String keyWord = rs.getString("keyword");
						data.add(keyWord);
					}
					return data;
				}
			};
			return run.query(conn, sql, rsHandler, type, hmFlag);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
