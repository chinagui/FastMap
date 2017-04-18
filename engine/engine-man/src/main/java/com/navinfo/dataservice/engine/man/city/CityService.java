package com.navinfo.dataservice.engine.man.city;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/** 
* @ClassName:  CityService 
* @author code generator
* @date 2016-06-06 08:19:11 
* @Description: 
*/
public class CityService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private CityService() {
	}
	
	private static class SingletonHolder{
		private static final CityService INSTANCE =new CityService();
	}
	public static CityService getInstance(){
		return SingletonHolder.INSTANCE;
	}

	public List<HashMap<String,Object>> queryListByWkt(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
					
			String planningStatus = ((json.getJSONArray("planningStatus").toString()).replace('[', '(')).replace(']', ')');
			
			String selectSql = "SELECT T.CITY_ID,"
					+ "       T.CITY_NAME,"
					+ "       T.GEOMETRY,"
					+ "       T.PLAN_STATUS,"
					+ "       K.PERCENT,"
					+ "       K.PROGRAM_ID"
					+ "  FROM CITY T,"
					+ "       (SELECT T.PROGRAM_ID, T.CITY_ID, NVL(O.PERCENT, 0) PERCENT "
					+ "         FROM PROGRAM T, FM_STAT_OVERVIEW_PROGRAM O"
					+ "         WHERE T.PROGRAM_ID = O.PROGRAM_ID(+)"
					+ "           AND LATEST = 1) K"
					+ " WHERE T.CITY_ID = K.CITY_ID(+)"
					+ "   AND T.PLAN_STATUS IN "+planningStatus
					+ "   AND SDO_ANYINTERACT(T.GEOMETRY,SDO_GEOMETRY(?,8307))='TRUE'";		
			ResultSetHandler<List<HashMap<String,Object>>> rsHandler = new ResultSetHandler<List<HashMap<String,Object>>>(){
				public List<HashMap<String,Object>> handle(ResultSet rs) throws SQLException {
					List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
					while(rs.next()){
						try {
							HashMap<String,Object> map = new HashMap<String,Object>();
							map.put("cityId", rs.getInt("CITY_ID"));
							map.put("cityName", rs.getString("CITY_NAME"));
							
							try {
								STRUCT struct=(STRUCT)rs.getObject("geometry");
								String clobStr = GeoTranslator.struct2Wkt(struct);
								map.put("geometry", Geojson.wkt2Geojson(clobStr));
							} catch (Exception e1) {
								e1.printStackTrace();
							}
							map.put("planStatus", rs.getInt("plan_status"));
							map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
							map.put("programId", rs.getInt("program_id"));
							map.put("percent", rs.getInt("percent"));
							list.add(map);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						
					}
					return list;
				}
	    		
	    	}		;
	    	log.debug(selectSql);
	    	log.debug(json.getString("wkt"));
	    	return run.query(conn, selectSql, rsHandler,json.getString("wkt"));
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public List<HashMap<String,Object>> queryListByAlloc(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
					
			String selectSql = "select c.CITY_ID,c.CITY_NAME, c.geometry,    case  when exists (select 1      from task t, subtask s     where c.city_id = t.city_id       and t.task_id = s.task_id) then   1  else   0    end subtask_status,    case when exists(select 1 from task t where c.city_id=t.city_id) then 1 else 0 end task_status   from city c where" 
			  +	" SDO_ANYINTERACT(c.geometry,sdo_geometry(?,8307))='TRUE'";
		
			ResultSetHandler<List<HashMap<String,Object>>> rsHandler = new ResultSetHandler<List<HashMap<String,Object>>>(){
				public List<HashMap<String,Object>> handle(ResultSet rs) throws SQLException {
					List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
					while(rs.next()){
						try {
							HashMap<String,Object> map = new HashMap<String,Object>();
							map.put("cityId", rs.getInt("CITY_ID"));
							map.put("cityName", rs.getString("CITY_NAME"));
							STRUCT struct=(STRUCT)rs.getObject("geometry");
							try {
								String clobStr = GeoTranslator.struct2Wkt(struct);
								map.put("geometry", Geojson.wkt2Geojson(clobStr));
							} catch (Exception e1) {
								e1.printStackTrace();
							}
							int taskStatus = rs.getInt("task_status");
							int subtaskStatus = rs.getInt("subtask_status");
							
							int planStatus=0;
							if(subtaskStatus==1){
								planStatus=2;
							}
							else if (taskStatus==1){
								planStatus=1;
							}
							map.put("planStatus", planStatus);
							
							map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
							list.add(map);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						
					}
					return list;
				}
	    		
	    	}		;

	    	return run.query(conn, selectSql, rsHandler,json.getString("wkt"));
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public HashMap<String,Object> query(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select C.CITY_ID,C.CITY_NAME, C.PROVINCE_NAME,C.GEOMETRY,C.REGION_ID,C.PLAN_STATUS from CITY C where C.CITY_ID=?";
			ResultSetHandler<HashMap<String,Object>> rsHandler = new ResultSetHandler<HashMap<String,Object>>(){
				public HashMap<String,Object> handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						HashMap<String,Object> map = new HashMap<String,Object>();
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("cityName", rs.getString("CITY_NAME"));
						map.put("provinceName", rs.getString("PROVINCE_NAME"));	
						STRUCT struct=(STRUCT)rs.getObject("GEOMETRY");
						try {
							String clobStr = GeoTranslator.struct2Wkt(struct);
							map.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							log.error(e1.getMessage(),e1);
						}
						map.put("regionId", rs.getInt("REGION_ID"));
						map.put("planStatus", rs.getInt("PLAN_STATUS"));
						return map;
					}
					return null;
				}
	    		
	    	}		;				
			return run.query(conn, 
					   selectSql,
					   rsHandler, 
					   json.getInt("cityId"));
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public int queryCityIdByTaskId(int taskId) throws Exception {
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String querySql = "select c.city_id from city c, task t where c.city_id = t.city_id and t.latest = 1 and t.task_id = " + taskId;

			int cityId = Integer.valueOf(run
					.query(conn, querySql, new MapHandler()).get("city_id")
					.toString());
			return cityId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public List<Map<String, Object>> cityMonitor(JSONObject dataJson)throws Exception {
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String extentSql="";
			if(dataJson.containsKey("planStatus")){
				extentSql="   AND PLAN_STATUS IN "+dataJson.getJSONArray("planStatus").toString()
						.replace("[", "(").replace("]", ")");
			}
			String querySql = "SELECT CITY_ID, ADMIN_GEO, PLAN_STATUS"
					+ "  FROM CITY"
					+ " WHERE 1 = 1"
					+ extentSql ;
			return run.query(conn, querySql, new ResultSetHandler<List<Map<String, Object>>>(){

				@Override
				public List<Map<String, Object>> handle(ResultSet rs)
						throws SQLException {
					List<Map<String, Object>> res=new ArrayList<Map<String,Object>>();
					while(rs.next()){
						Map<String, Object> cityMap=new HashMap<String, Object>();
						cityMap.put("cityId", rs.getInt("CITY_ID"));
						cityMap.put("geometry",null);
						STRUCT struct=(STRUCT)rs.getObject("ADMIN_GEO");
						try {
							String clobStr = GeoTranslator.struct2Wkt(struct);
							cityMap.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							log.error(e1.getMessage(),e1);
						}
						cityMap.put("planStatus", rs.getInt("PLAN_STATUS"));
						res.add(cityMap);
					}
					return res;
				}});
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
