package com.navinfo.dataservice.engine.man.city;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.City;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONObject;
import oracle.sql.CLOB;

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
			
			String selectSql = "select t.CITY_ID,t.CITY_NAME, t.geometry.get_wkt() as geometry,t.plan_status from CITY t where t.PLAN_STATUS in "+planningStatus
					+" and SDO_ANYINTERACT(t.geometry,sdo_geometry(?,8307))='TRUE'";
		
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						try {
							HashMap<String,Object> map = new HashMap<String,Object>();
							map.put("cityId", rs.getInt("CITY_ID"));
							map.put("cityName", rs.getString("CITY_NAME"));
							CLOB clob=(CLOB)rs.getObject("geometry");
							String clobStr=DataBaseUtils.clob2String(clob);
							map.put("geometry", Geojson.wkt2Geojson(clobStr));
							map.put("planningStatus", rs.getInt("plan_status"));
							map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
							list.add(map);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
			JSONObject obj = JSONObject.fromObject(json);	
			City  bean = (City)JSONObject.toBean(obj, City.class);	
			
			String selectSql = "select * from CITY where CITY_ID=? and CITY_NAME=? and PROVINCE_NAME=? and GEOMETRY=? and REGION_ID=? and PLAN_STATUS=?";
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>(){
				public HashMap handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						HashMap map = new HashMap();
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("cityName", rs.getString("CITY_NAME"));
						map.put("provinceName", rs.getString("PROVINCE_NAME"));
						map.put("geometry", rs.getObject("GEOMETRY"));
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
					   bean.getCityId(), bean.getCityName(), bean.getProvinceName(), bean.getGeometry(), bean.getRegionId(), bean.getPlanStatus());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
