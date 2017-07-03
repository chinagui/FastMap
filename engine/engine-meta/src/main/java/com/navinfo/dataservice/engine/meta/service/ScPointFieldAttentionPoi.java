package com.navinfo.dataservice.engine.meta.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

public class ScPointFieldAttentionPoi {

	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private static class SingletonHolder {
		private static final ScPointFieldAttentionPoi INSTANCE = new ScPointFieldAttentionPoi();
	}

	public static final ScPointFieldAttentionPoi getInstance() {
		return SingletonHolder.INSTANCE;
	}
	public List<Integer> queryImportantPid() throws SQLException {

		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getMetaConnection();
			QueryRunner run = new QueryRunner();
			
			String selectSql = "select t.poi_num from SC_POINT_FIELD_ATTENTIONPOI t";
			ResultSetHandler<List<Integer>> rs = new ResultSetHandler<List<Integer>>(){
				public List<Integer> handle(ResultSet rs) throws SQLException {
				List<Integer> pids = new ArrayList<>();
				while(rs.next()){
					pids.add(rs.getInt("poi_num"));
				}
				return pids;
			}
		};
		return run.query(conn, selectSql, rs);
		}catch(Exception e){
			DbUtils.close(conn);
			log.error("从元数据库中获取重要POI异常："+e.getMessage());
			throw e;
		}finally{
			DbUtils.close(conn);
		}
	
	}

}
