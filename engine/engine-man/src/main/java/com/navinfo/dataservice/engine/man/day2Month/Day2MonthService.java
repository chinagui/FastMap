package com.navinfo.dataservice.engine.man.day2Month;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.navicommons.database.Page;

public class Day2MonthService {
	private Logger log=LoggerRepos.getLogger(getClass());
	
	private Day2MonthService(){}

	private static class SingletonHolder{
		private static final Day2MonthService INSTANCE=new Day2MonthService();
	}
	
	public static Day2MonthService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * 更改man_config表参数值
	 * @param userId
	 * @param dataJson
	 * @throws Exception
	 */
	public void update(long userId, int confId,int status) throws Exception{
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getManConnection();
			String updateSql="update DAY2MONTH_CONFIG set status='"+status+"',"
					+ "exe_user_id="+userId+" where conf_id='"+confId+"'";
			QueryRunner runner=new QueryRunner();
			runner.update(conn, updateSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("修改参数失败:"+confId, e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询配置列表
	 * @param curPageSize 
	 * @param curPageNum 
	 * @param condition 
	 * @param dataJson
	 * @return
	 * @throws Exception
	 */
	public Page list(JSONObject conditionJson, final int currentPageNum, final int pageSize) throws Exception {
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getManConnection();
			long pageStartNum = (currentPageNum - 1) * pageSize + 1;
			long pageEndNum = currentPageNum * pageSize;
			String conditionSql="";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("status".equals(key)) {conditionSql+=" AND T.STATUS IN ("+conditionJson.getJSONArray(key).join(",")+")";}
					}}
			QueryRunner runner=new QueryRunner();
			String sql="With day2Month as(SELECT D.CONF_ID,"
					+ "       D.CITY_ID,"
					+ "       C.CITY_NAME,"
					+ "       D.TYPE,"
					+ "       D.STATUS,"
					+ "       NVL(I.USER_REAL_NAME,'') EXE_USER_NAME,"
					+ "       D.EXE_DATE,F.CUR_TOTAL, F.ACCUMULATIVE_TOTAL"
					+ "  FROM DAY2MONTH_CONFIG D, CITY C, USER_INFO I, FM_STAT_DAY2MONTH F"
					+ " WHERE D.CITY_ID = C.CITY_ID"
					+ "   AND D.TYPE = 'POI'"
					+ "   AND D.CITY_ID = F.CITY_ID(+)"
					+ "   AND D.TYPE = F.TYPE(+)"
					+ "   AND D.EXE_USER_ID = I.USER_ID(+))"
					+ " SELECT /*+FIRST_ROWS ORDERED*/"
					+ " T.*, (SELECT COUNT(1) FROM day2Month) AS TOTAL_RECORD_NUM"
					+ "  FROM (SELECT T.*, ROWNUM AS ROWNUM_ FROM day2Month T WHERE ROWNUM <= "+pageEndNum+") T"
					+ " WHERE T.ROWNUM_ >= "+pageStartNum
					+conditionSql
					+ " ORDER BY T.CITY_NAME";
			Page result=runner.query(conn, sql, new ResultSetHandler<Page>(){

				@Override
				public Page handle(ResultSet rs)
						throws SQLException {
					List<Map<String, Object>> result=new ArrayList<Map<String, Object>>();
					Page page = new Page(currentPageNum);
				    page.setPageSize(pageSize);
				    int total=0;
					while (rs.next()) {
						Map<String, Object> tmp=new HashMap<String, Object>();
						tmp.put("confId", rs.getString("CONF_ID"));
						tmp.put("cityId", rs.getInt("CITY_ID"));
						tmp.put("cityName", rs.getString("CITY_NAME"));	
						tmp.put("type", rs.getString("TYPE"));
						tmp.put("status", rs.getInt("STATUS"));
						tmp.put("curTotal", rs.getInt("CUR_TOTAL"));
						tmp.put("accumulativeTotal", rs.getInt("ACCUMULATIVE_TOTAL"));
						tmp.put("exeUserName", rs.getString("EXE_USER_NAME"));
						tmp.put("exeDate", DateUtils.dateToString(rs.getTimestamp("EXE_DATE")));
						result.add(tmp);
						total=rs.getInt("TOTAL_RECORD_NUM");
					}
					page.setTotalCount(total);
					page.setResult(result);
					return page;
				}
				
			});
			return result;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("查询列表错误", e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
		/**
		 * 查询配置列表
		 * @param condition 
		 * @param dataJson
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>> list(JSONObject conditionJson) throws Exception {
			Connection conn=null;
			try{
				conn=DBConnector.getInstance().getManConnection();
				String conditionSql="";
				if(null!=conditionJson && !conditionJson.isEmpty()){
					Iterator keys = conditionJson.keys();
					while (keys.hasNext()) {
						String key = (String) keys.next();
						if ("status".equals(key)) {conditionSql+=" AND T.STATUS IN ("+conditionJson.getJSONArray(key).join(",")+")";}
						}}
				QueryRunner runner=new QueryRunner();
				String sql="With day2Month as(SELECT D.CONF_ID,"
						+ "       D.CITY_ID,"
						+ "       C.CITY_NAME,"
						+ "       D.TYPE,"
						+ "       D.STATUS,"
						+ "       NVL(I.USER_REAL_NAME,'') EXE_USER_NAME,"
						+ "       D.EXE_DATE"
						+ "  FROM DAY2MONTH_CONFIG D, CITY C, USER_INFO I"
						+ " WHERE D.CITY_ID = C.CITY_ID"
						+ "   AND D.TYPE = 'POI'"
						+ "   AND D.EXE_USER_ID = I.USER_ID(+))"
						+ " SELECT T.*"
						+ "  FROM day2Month T "
						+ " WHERE 1=1"
						+conditionSql
						+ " ORDER BY T.CITY_NAME";
				List<Map<String, Object>> result=runner.query(conn, sql, new ResultSetHandler<List<Map<String, Object>>>(){

					@Override
					public List<Map<String, Object>> handle(ResultSet rs)
							throws SQLException {
						List<Map<String, Object>> result=new ArrayList<Map<String, Object>>();
						while (rs.next()) {
							Map<String, Object> tmp=new HashMap<String, Object>();
							tmp.put("confId", rs.getString("CONF_ID"));
							tmp.put("cityId", rs.getInt("CITY_ID"));
							tmp.put("cityName", rs.getString("CITY_NAME"));	
							tmp.put("type", rs.getString("TYPE"));
							tmp.put("status", rs.getInt("STATUS"));
//							tmp.put("exeUserName", rs.getString("EXE_USER_NAME"));
//							tmp.put("exeDate", DateUtils.dateToString(rs.getTimestamp("EXE_DATE")));
							result.add(tmp);
						}
						return result;
					}
					
				});
				return result;
			}catch(Exception e){
				DbUtils.rollbackAndCloseQuietly(conn);
				log.error("查询列表错误", e);
				throw e;
			}finally{
				DbUtils.commitAndCloseQuietly(conn);
			}
			}
}
