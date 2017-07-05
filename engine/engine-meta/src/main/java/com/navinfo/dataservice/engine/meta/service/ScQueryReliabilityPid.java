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

public class ScQueryReliabilityPid {

private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private static class SingletonHolder {
		private static final ScQueryReliabilityPid INSTANCE = new ScQueryReliabilityPid();
	}

	public static final ScQueryReliabilityPid getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public List<Integer> ScQueryReliabilityPid(int minNumber, int maxNumber) throws SQLException {

		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getMetaConnection();
			QueryRunner run = new QueryRunner();
			
			String selectSql = "select t.pid from reliability_table t where t.reliability between "+minNumber+" and "+maxNumber;
			ResultSetHandler<List<Integer>> rs = new ResultSetHandler<List<Integer>>(){
				public List<Integer> handle(ResultSet rs) throws SQLException {
				List<Integer> pids = new ArrayList<>();
				while(rs.next()){
					pids.add(rs.getInt("PID"));
				}
				return pids;
			}
		};
		return run.query(conn, selectSql, rs);
		}catch(Exception e){
			DbUtils.close(conn);
			log.error("从元数据库依据置信度范围检索PID异常："+e.getMessage());
			throw e;
		}finally{
			DbUtils.close(conn);
		}
	}
}
