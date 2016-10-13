package com.navinfo.dataservice.engine.man.city;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONObject;
import oracle.sql.CLOB;
import oracle.sql.STRUCT;

/** 
* @ClassName:  CityService 
* @author code generator
* @date 2016-06-06 08:19:11 
* @Description: TODO
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

	public List<HashMap> queryListByWkt(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
					
			String planningStatus = ((json.getJSONArray("planningStatus").toString()).replace('[', '(')).replace(']', ')');
			
			String selectSql = " select t.CITY_ID,t.CITY_NAME, t.geometry,t.plan_status,k.percent, k.task_id from CITY t, "
					+ "(SELECT T.TASK_ID,T.CITY_ID,nvl(O.PERCENT,0) percent FROM TASK T,FM_STAT_OVERVIEW_TASK O "
					+ "WHERE T.TASK_ID=O.TASK_ID(+) and latest=1) k where t.city_id=k.city_id(+) and t.PLAN_STATUS in "+planningStatus
					+" and SDO_ANYINTERACT(t.geometry,sdo_geometry(?,8307))='TRUE'";
		
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
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
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							map.put("cityPlanStatus", rs.getInt("plan_status"));
							map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
							map.put("taskId", rs.getInt("task_id"));
							map.put("percent", rs.getInt("percent"));
							list.add(map);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
	
	public List<HashMap> queryListByAlloc(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
					
			String selectSql = "select c.CITY_ID,c.CITY_NAME, c.geometry,    case  when exists (select 1      from task t, subtask s     where c.city_id = t.city_id       and t.task_id = s.task_id) then   1  else   0    end subtask_status,    case when exists(select 1 from task t where c.city_id=t.city_id) then 1 else 0 end task_status   from city c where" 
			  +	" SDO_ANYINTERACT(c.geometry,sdo_geometry(?,8307))='TRUE'";
		
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
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
								// TODO Auto-generated catch block
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
							// TODO Auto-generated catch block
							e.printStackTrace();
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
	
	public HashMap query(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select C.CITY_ID,C.CITY_NAME, C.PROVINCE_NAME,C.GEOMETRY,C.REGION_ID,C.PLAN_STATUS from CITY C where C.CITY_ID=?";
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>(){
				public HashMap handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						HashMap map = new HashMap();
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("cityName", rs.getString("CITY_NAME"));
						map.put("provinceName", rs.getString("PROVINCE_NAME"));	
						STRUCT struct=(STRUCT)rs.getObject("GEOMETRY");
						try {
							String clobStr = GeoTranslator.struct2Wkt(struct);
							map.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
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
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();

			String querySql = "select c.city_id from city c, task t where c.city_id = t.city_id and t.latest = 1 and t.task_id = " + taskId;

			int cityId = Integer.valueOf(run
					.query(conn, querySql, new MapHandler()).get("city_id")
					.toString());
			return cityId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
}
