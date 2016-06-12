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
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONObject;

/** 
* @ClassName:  CityService 
* @author code generator
* @date 2016-06-06 08:19:11 
* @Description: TODO
*/
@Service
public class CityService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			City  bean = (City)JSONObject.toBean(json, City.class);	
			
			String createSql = "insert into CITY (CITY_ID, CITY_NAME, PROVINCE_NAME, GEOMETRY, REGION_ID, PLAN_STATUS) values(?,?,?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getCityId() , bean.getCityName(), bean.getProvinceName(), bean.getGeometry(), bean.getRegionId(), bean.getPlanStatus()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			City  bean = (City)JSONObject.toBean(obj, City.class);	
			
			String updateSql = "update CITY set CITY_ID=?, CITY_NAME=?, PROVINCE_NAME=?, GEOMETRY=?, REGION_ID=?, PLAN_STATUS=? where 1=1 CITY_ID=? and CITY_NAME=? and PROVINCE_NAME=? and GEOMETRY=? and REGION_ID=? and PLAN_STATUS=?";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getCityId()!=null && StringUtils.isNotEmpty(bean.getCityId().toString())){
				updateSql+=" and CITY_ID=? ";
				values.add(bean.getCityId());
			};
			if (bean!=null&&bean.getCityName()!=null && StringUtils.isNotEmpty(bean.getCityName().toString())){
				updateSql+=" and CITY_NAME=? ";
				values.add(bean.getCityName());
			};
			if (bean!=null&&bean.getProvinceName()!=null && StringUtils.isNotEmpty(bean.getProvinceName().toString())){
				updateSql+=" and PROVINCE_NAME=? ";
				values.add(bean.getProvinceName());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				updateSql+=" and GEOMETRY=? ";
				values.add(bean.getGeometry());
			};
			if (bean!=null&&bean.getRegionId()!=null && StringUtils.isNotEmpty(bean.getRegionId().toString())){
				updateSql+=" and REGION_ID=? ";
				values.add(bean.getRegionId());
			};
			if (bean!=null&&bean.getPlanStatus()!=null && StringUtils.isNotEmpty(bean.getPlanStatus().toString())){
				updateSql+=" and PLAN_STATUS=? ";
				values.add(bean.getPlanStatus());
			};
			run.update(conn, 
					   updateSql, 
					   bean.getCityId() ,bean.getCityName(),bean.getProvinceName(),bean.getGeometry(),bean.getRegionId(),bean.getPlanStatus(),
					   values.toArray()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void delete(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			City  bean = (City)JSONObject.toBean(obj, City.class);	
			
			String deleteSql = "delete from  CITY where 1=1 ";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getCityId()!=null && StringUtils.isNotEmpty(bean.getCityId().toString())){
				deleteSql+=" and CITY_ID=? ";
				values.add(bean.getCityId());
			};
			if (bean!=null&&bean.getCityName()!=null && StringUtils.isNotEmpty(bean.getCityName().toString())){
				deleteSql+=" and CITY_NAME=? ";
				values.add(bean.getCityName());
			};
			if (bean!=null&&bean.getProvinceName()!=null && StringUtils.isNotEmpty(bean.getProvinceName().toString())){
				deleteSql+=" and PROVINCE_NAME=? ";
				values.add(bean.getProvinceName());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				deleteSql+=" and GEOMETRY=? ";
				values.add(bean.getGeometry());
			};
			if (bean!=null&&bean.getRegionId()!=null && StringUtils.isNotEmpty(bean.getRegionId().toString())){
				deleteSql+=" and REGION_ID=? ";
				values.add(bean.getRegionId());
			};
			if (bean!=null&&bean.getPlanStatus()!=null && StringUtils.isNotEmpty(bean.getPlanStatus().toString())){
				deleteSql+=" and PLAN_STATUS=? ";
				values.add(bean.getPlanStatus());
			};
			if (values.size()==0){
	    		run.update(conn, deleteSql);
	    	}else{
	    		run.update(conn, deleteSql,values.toArray());
	    	}
	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public Page list(JSONObject json ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			City  bean = (City)JSONObject.toBean(obj, City.class);
			
			String selectSql = "select * from CITY where 1=1 ";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getCityId()!=null && StringUtils.isNotEmpty(bean.getCityId().toString())){
				selectSql+=" and CITY_ID=? ";
				values.add(bean.getCityId());
			};
			if (bean!=null&&bean.getCityName()!=null && StringUtils.isNotEmpty(bean.getCityName().toString())){
				selectSql+=" and CITY_NAME=? ";
				values.add(bean.getCityName());
			};
			if (bean!=null&&bean.getProvinceName()!=null && StringUtils.isNotEmpty(bean.getProvinceName().toString())){
				selectSql+=" and PROVINCE_NAME=? ";
				values.add(bean.getProvinceName());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				selectSql+=" and GEOMETRY=? ";
				values.add(bean.getGeometry());
			};
			if (bean!=null&&bean.getRegionId()!=null && StringUtils.isNotEmpty(bean.getRegionId().toString())){
				selectSql+=" and REGION_ID=? ";
				values.add(bean.getRegionId());
			};
			if (bean!=null&&bean.getPlanStatus()!=null && StringUtils.isNotEmpty(bean.getPlanStatus().toString())){
				selectSql+=" and PLAN_STATUS=? ";
				values.add(bean.getPlanStatus());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List list = new ArrayList();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						HashMap map = new HashMap();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("cityName", rs.getString("CITY_NAME"));
						map.put("provinceName", rs.getString("PROVINCE_NAME"));
						map.put("geometry", rs.getObject("GEOMETRY"));
						map.put("regionId", rs.getInt("REGION_ID"));
						map.put("planStatus", rs.getInt("PLAN_STATUS"));
						list.add(map);
					}
					page.setResult(list);
					return page;
				}
	    		
	    	}	;
			if (values.size()==0){
	    		return run.query(currentPageNum, 20, conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(currentPageNum, 20, conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	public List<HashMap> queryListByWkt(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
					
			final String wkt= json.getString("wkt");
			int planningStatus = json.getInt("planningStatus");
			
			String selectSql = "select t.CITY_ID,t.CITY_NAME, t.geometry.get_wkt() as geometry from CITY t where t.PLAN_STATUS="+planningStatus;
		
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						try {
							if (GeometryUtils.IsIntersectPolygon(wkt,rs.getObject("geometry"))){
								HashMap<String,Object> map = new HashMap<String,Object>();
								map.put("cityId", rs.getInt("CITY_ID"));
								map.put("cityName", rs.getString("CITY_NAME"));
								map.put("geometry", rs.getObject("geometry"));
								list.add(map);
							}
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
	    	return run.query(conn, selectSql, rsHandler);
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
