package com.navinfo.dataservice.dao.log;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

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
	public Set<Integer> statGridsBySubtaskId(int subtaskId)throws Exception{
		try{
			Set<Integer> grids = new HashSet<Integer>();
			String sql = "SELECT DISTINCT G.GRID_ID FROM";
			
			
			return grids;
		}catch(Exception e){
			log.error("统计子任务包含的作业范围grids出错，原因为："+e.getMessage(),e);
			throw e;
		}
	}
}
