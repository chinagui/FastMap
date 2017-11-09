package com.navinfo.dataservice.engine.man.city;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

public class CityOperation {
	private static Logger log = LoggerRepos.getLogger(CityOperation.class);

	public CityOperation() {
		// TODO Auto-generated constructor stub
	}
	
	public static void updatePlanStatus(Connection conn,int cityId,int PlanStatus) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String updateCity="UPDATE CITY SET PLAN_STATUS="+PlanStatus+" WHERE CITY_ID="+cityId;
			run.update(conn,updateCity);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static void close(Connection conn)throws Exception{
		try{
			String updateCity="UPDATE CITY C"
					+ "   SET C.PLAN_STATUS = 2"
					+ " WHERE NOT EXISTS (SELECT 1"
					+ "          FROM TASK T"
					+ "         WHERE T.CITY_ID = C.CITY_ID"
					+ "           AND T.STATUS <> 0"
					+ "			  AND T.LATEST=1)"
					+ "   AND C.PLAN_STATUS = 1";
			QueryRunner run = new QueryRunner();
			run.update(conn,updateCity);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭city失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
	public static List<Integer> queryTaskByCityId(Connection conn, int cityId) throws Exception {
		// TODO Auto-generated method stub

		try {
			QueryRunner run = new QueryRunner();

			String selectSql = "select t.task_id from task t where t.city_id=? and t.latest=1";

			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> list = new ArrayList<Integer>();
					while (rs.next()) {
						String[] s = rs.getString("status").split(",");
						List<String> status = (List<String>) Arrays.asList(s);
						if(!status.contains("1")){
							list.add(rs.getInt("block_id"));
						}
						
					}
					return list;
				}

			};

			List<Integer> blockList = run.query(conn, selectSql, rsHandler);
			return blockList;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		}
	}

}
