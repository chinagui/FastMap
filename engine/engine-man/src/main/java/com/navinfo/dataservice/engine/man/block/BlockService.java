package com.navinfo.dataservice.engine.man.block;

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

import net.sf.json.JSONObject;

/** 
* @ClassName:  BlockService 
* @author code generator
* @date 2016-06-08 01:32:00 
* @Description: TODO
*/
@Service
public class BlockService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn =  DBConnector.getInstance().getManConnection();	
			Block  bean = (Block)JSONObject.toBean(json, Block.class);	
			
			String createSql = "insert into BLOCK (BLOCK_ID, CITY_ID, BLOCK_NAME, GEOMETRY, PLAN_STATUS) values(?,?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getBlockId() , bean.getCityId(), bean.getBlockName(), bean.getGeometry(), bean.getPlanStatus()
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
			conn =  DBConnector.getInstance().getManConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			Block  bean = (Block)JSONObject.toBean(obj, Block.class);	
			
			String updateSql = "update BLOCK set BLOCK_ID=?, CITY_ID=?, BLOCK_NAME=?, GEOMETRY=?, PLAN_STATUS=? where 1=1 BLOCK_ID=? and CITY_ID=? and BLOCK_NAME=? and GEOMETRY=? and PLAN_STATUS=?";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBlockId()!=null && StringUtils.isNotEmpty(bean.getBlockId().toString())){
				updateSql+=" and BLOCK_ID=? ";
				values.add(bean.getBlockId());
			};
			if (bean!=null&&bean.getCityId()!=null && StringUtils.isNotEmpty(bean.getCityId().toString())){
				updateSql+=" and CITY_ID=? ";
				values.add(bean.getCityId());
			};
			if (bean!=null&&bean.getBlockName()!=null && StringUtils.isNotEmpty(bean.getBlockName().toString())){
				updateSql+=" and BLOCK_NAME=? ";
				values.add(bean.getBlockName());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				updateSql+=" and GEOMETRY=? ";
				values.add(bean.getGeometry());
			};
			if (bean!=null&&bean.getPlanStatus()!=null && StringUtils.isNotEmpty(bean.getPlanStatus().toString())){
				updateSql+=" and PLAN_STATUS=? ";
				values.add(bean.getPlanStatus());
			};
			run.update(conn, 
					   updateSql, 
					   bean.getBlockId() ,bean.getCityId(),bean.getBlockName(),bean.getGeometry(),bean.getPlanStatus(),
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
			conn =  DBConnector.getInstance().getManConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			Block  bean = (Block)JSONObject.toBean(obj, Block.class);	
			
			String deleteSql = "delete from  BLOCK where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBlockId()!=null && StringUtils.isNotEmpty(bean.getBlockId().toString())){
				deleteSql+=" and BLOCK_ID=? ";
				values.add(bean.getBlockId());
			};
			if (bean!=null&&bean.getCityId()!=null && StringUtils.isNotEmpty(bean.getCityId().toString())){
				deleteSql+=" and CITY_ID=? ";
				values.add(bean.getCityId());
			};
			if (bean!=null&&bean.getBlockName()!=null && StringUtils.isNotEmpty(bean.getBlockName().toString())){
				deleteSql+=" and BLOCK_NAME=? ";
				values.add(bean.getBlockName());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				deleteSql+=" and GEOMETRY=? ";
				values.add(bean.getGeometry());
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
			conn =  DBConnector.getInstance().getManConnection();		
			JSONObject obj = JSONObject.fromObject(json);	
			Block  bean = (Block)JSONObject.toBean(obj, Block.class);
			
			String selectSql = "select * from BLOCK where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBlockId()!=null && StringUtils.isNotEmpty(bean.getBlockId().toString())){
				selectSql+=" and BLOCK_ID=? ";
				values.add(bean.getBlockId());
			};
			if (bean!=null&&bean.getCityId()!=null && StringUtils.isNotEmpty(bean.getCityId().toString())){
				selectSql+=" and CITY_ID=? ";
				values.add(bean.getCityId());
			};
			if (bean!=null&&bean.getBlockName()!=null && StringUtils.isNotEmpty(bean.getBlockName().toString())){
				selectSql+=" and BLOCK_NAME=? ";
				values.add(bean.getBlockName());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				selectSql+=" and GEOMETRY=? ";
				values.add(bean.getGeometry());
			};
			if (bean!=null&&bean.getPlanStatus()!=null && StringUtils.isNotEmpty(bean.getPlanStatus().toString())){
				selectSql+=" and PLAN_STATUS=? ";
				values.add(bean.getPlanStatus());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						HashMap<String, Object> map = new HashMap<String, Object>();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("blockName", rs.getString("BLOCK_NAME"));
						map.put("geometry", rs.getObject("GEOMETRY"));
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
	public List<HashMap> listByWkt(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			
			conn =  DBConnector.getInstance().getManConnection();	
			
			String selectSql ="select t.BLOCK_ID,t.BLOCK_NAME,t.GEOMETRY.get_wkt() as GEOMETRY, from BLOCK t where PLAN_STATUS="+json.getInt("planningStatus");
					
			if (StringUtils.isNotEmpty(json.getString("snapshot"))){
				if ("1".equals(json.getString("snapshot"))){
					selectSql="select t.BLOCK_ID,t.BLOCK_NAME, from BLOCK t where PLAN_STATUS="+json.getInt("planningStatus");
				}	
			};
			if (!json.containsKey("relation") || ("intersect".equals(json.getString("relation")))){
				selectSql+=" and SDO_ANYINTERACT(t.geometry,sdo_geometry(?,8307))='TRUE'";
			}else{
				if ("within".equals(json.getString("relation"))){
					selectSql+=" and sdo_within_distance(t.geometry,  sdo_geom.sdo_mbr(sdo_geometry(?, 8307)), 'DISTANCE=0') = 'TRUE'";
				}
			}
			List<Object> list=new ArrayList<Object>();
			list.add(json.getString("wkt"));
			return BlockOperation.queryBlockBySql(conn, selectSql,list);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public HashMap<?, ?> query(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			Block  bean = (Block)JSONObject.toBean(obj, Block.class);	
			
			String selectSql = "select t.BLOCK_ID,t.CITY_ID,t.BLOCK_NAME,t.GEOMETRY.get_wkt() as GEOMETRY,t.PLAN_STATUS from BLOCK t where t.BLOCK_ID=?";
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>(){
				public HashMap<String, Object> handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("cityId", rs.getInt("CITY_ID"));
						map.put("blockName", rs.getString("BLOCK_NAME"));
						map.put("geometry", rs.getObject("GEOMETRY"));
						map.put("planStatus", rs.getInt("PLAN_STATUS"));
						return map;
					}
					return null;
				}
	    		
	    	}		;				
			return run.query(conn, 
					   selectSql,
					   rsHandler, 
					   bean.getBlockId());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
