package com.navinfo.dataservice.dao.log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: LogGridStat
 * @author xiaoxiaowen4127
 * @date 2017年7月25日
 * @Description: LogGridStat.java
 */
public class LogGridStat {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private Connection conn;
	private QueryRunner run;
	public LogGridStat(Connection conn) {
		this.conn = conn;
		run = new QueryRunner();
	}
	public List<Integer> statGridsBySubtaskId(int subtaskId)throws Exception{
		try{
			String sql = "SELECT DISTINCT G.GRID_ID FROM LOG_DETAIL_GRID G,LOG_DETAIL D,LOG_OPERATION O,LOG_ACTION A WHERE G.LOG_ROW_ID=D.ROW_ID AND D.OP_ID=O.OP_ID AND O.ACT_ID=A.ACT_ID AND A.STK_ID=?";
			return run.query(conn, sql,new ResultSetHandler<List<Integer>>(){

				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> grids = new ArrayList<Integer>();
					while(rs.next()){
						grids.add(rs.getInt(1));
					}
					return grids;
				}
				
			},subtaskId);
		}catch(Exception e){
			log.error("统计子任务包含的作业范围grids出错，原因为："+e.getMessage(),e);
			throw e;
		}
	}
	
	
}
