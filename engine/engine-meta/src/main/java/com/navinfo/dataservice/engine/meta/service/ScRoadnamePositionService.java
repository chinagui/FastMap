package com.navinfo.dataservice.engine.meta.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.meta.model.ScRoadnamePosition;
import com.navinfo.dataservice.engine.meta.model.ScRoadnameTypename;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnamePositionService 
* @author code generator
* @date 2017-03-23 07:07:14 
* @Description: TODO
*/
@Service
public class ScRoadnamePositionService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScRoadnamePosition  bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_ROADNAME_POSITION (ID, NAME, REGION_FLAG, LANG_CODE) values(?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getId() , bean.getName(), bean.getRegionFlag(), bean.getLangCode()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(ScRoadnamePosition bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_ROADNAME_POSITION set ID=?, NAME=?, REGION_FLAG=?, LANG_CODE=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				updateSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				updateSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getRegionFlag()!=null && StringUtils.isNotEmpty(bean.getRegionFlag().toString())){
				updateSql+=" and REGION_FLAG=? ";
				values.add(bean.getRegionFlag());
			};
			if (bean!=null&&bean.getLangCode()!=null && StringUtils.isNotEmpty(bean.getLangCode().toString())){
				updateSql+=" and LANG_CODE=? ";
				values.add(bean.getLangCode());
			};
			run.update(conn, 
					   updateSql, 
					   values.toArray(),
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
	public void delete(ScRoadnamePosition bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_ROADNAME_POSITION where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				deleteSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				deleteSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getRegionFlag()!=null && StringUtils.isNotEmpty(bean.getRegionFlag().toString())){
				deleteSql+=" and REGION_FLAG=? ";
				values.add(bean.getRegionFlag());
			};
			if (bean!=null&&bean.getLangCode()!=null && StringUtils.isNotEmpty(bean.getLangCode().toString())){
				deleteSql+=" and LANG_CODE=? ";
				values.add(bean.getLangCode());
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
	public Page list(ScRoadnamePosition bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_ROADNAME_POSITION where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				selectSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getRegionFlag()!=null && StringUtils.isNotEmpty(bean.getRegionFlag().toString())){
				selectSql+=" and REGION_FLAG=? ";
				values.add(bean.getRegionFlag());
			};
			if (bean!=null&&bean.getLangCode()!=null && StringUtils.isNotEmpty(bean.getLangCode().toString())){
				selectSql+=" and LANG_CODE=? ";
				values.add(bean.getLangCode());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScRoadnamePosition> list = new ArrayList<ScRoadnamePosition>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScRoadnamePosition model = new ScRoadnamePosition();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setId(rs.getInt("ID"));
						model.setName(rs.getString("NAME"));
						model.setRegionFlag(rs.getInt("REGION_FLAG"));
						model.setLangCode(rs.getString("LANG_CODE"));
						list.add(model);
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
	public Page list(JSONObject dataJson,final int curPageNum, final int pageSize, String sortby) throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			ScRoadnamePosition bean =(ScRoadnamePosition) JSONObject.toBean(dataJson, ScRoadnamePosition.class);
			
			String selectSql = "select * from SC_ROADNAME_POSITION where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				selectSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getRegionFlag()!=null && StringUtils.isNotEmpty(bean.getRegionFlag().toString())){
				selectSql+=" and REGION_FLAG=? ";
				values.add(bean.getRegionFlag());
			};
			if (bean!=null&&bean.getLangCode()!=null && StringUtils.isNotEmpty(bean.getLangCode().toString())){
				selectSql+=" and LANG_CODE=? ";
				values.add(bean.getLangCode());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScRoadnamePosition> list = new ArrayList<ScRoadnamePosition>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						ScRoadnamePosition model = new ScRoadnamePosition();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setId(rs.getInt("ID"));
						model.setName(rs.getString("NAME"));
						model.setRegionFlag(rs.getInt("REGION_FLAG"));
						model.setLangCode(rs.getString("LANG_CODE"));
						list.add(model);
					}
					page.setResult(list);
					return page;
				}
	    		
	    	}	;
			if (values.size()==0){
	    		return run.query(curPageNum, pageSize, conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(curPageNum, pageSize, conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	public List<ScRoadnamePosition> list(ScRoadnamePosition bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_ROADNAME_POSITION where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				selectSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getRegionFlag()!=null && StringUtils.isNotEmpty(bean.getRegionFlag().toString())){
				selectSql+=" and REGION_FLAG=? ";
				values.add(bean.getRegionFlag());
			};
			if (bean!=null&&bean.getLangCode()!=null && StringUtils.isNotEmpty(bean.getLangCode().toString())){
				selectSql+=" and LANG_CODE=? ";
				values.add(bean.getLangCode());
			};
			ResultSetHandler<List<ScRoadnamePosition>> rsHandler = new ResultSetHandler<List<ScRoadnamePosition>>(){
				public List<ScRoadnamePosition> handle(ResultSet rs) throws SQLException {
					List<ScRoadnamePosition> list = new ArrayList<ScRoadnamePosition>();
					while(rs.next()){
						ScRoadnamePosition model = new ScRoadnamePosition();
						model.setId(rs.getInt("ID"));
						model.setName(rs.getString("NAME"));
						model.setRegionFlag(rs.getInt("REGION_FLAG"));
						model.setLangCode(rs.getString("LANG_CODE"));
						list.add(model);
					}
					return list;
				}
	    		
	    	}		;
	    	if (values.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public ScRoadnamePosition query(ScRoadnamePosition bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_ROADNAME_POSITION where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				selectSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getRegionFlag()!=null && StringUtils.isNotEmpty(bean.getRegionFlag().toString())){
				selectSql+=" and REGION_FLAG=? ";
				values.add(bean.getRegionFlag());
			};
			if (bean!=null&&bean.getLangCode()!=null && StringUtils.isNotEmpty(bean.getLangCode().toString())){
				selectSql+=" and LANG_CODE=? ";
				values.add(bean.getLangCode());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<ScRoadnamePosition> rsHandler = new ResultSetHandler<ScRoadnamePosition>(){
				public ScRoadnamePosition handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScRoadnamePosition model = new ScRoadnamePosition();
						model.setId(rs.getInt("ID"));
						model.setName(rs.getString("NAME"));
						model.setRegionFlag(rs.getInt("REGION_FLAG"));
						model.setLangCode(rs.getString("LANG_CODE"));
						return model;
					}
					return null;
				}
	    		
	    	}		;				
			if (values.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void saveUpdate(JSONObject dataJson) throws ServiceException {
		ScRoadnamePosition bean =(ScRoadnamePosition) JSONObject.toBean(dataJson, ScRoadnamePosition.class);
		if(bean.getName() != null && StringUtils.isNotEmpty(bean.getName()) 
				&& bean.getLangCode() != null && StringUtils.isNotEmpty(bean.getLangCode())){
			ScRoadnamePosition newBean = new ScRoadnamePosition();
			newBean.setName( bean.getName());
			newBean.setLangCode(bean.getLangCode());
			ScRoadnamePosition oldBean = query(newBean);
			if(oldBean != null ){//存在  ,更新数据
				update(bean);
			}else{//不存在 新增
				create(bean);
			}
		}else{
			log.error("保存失败 name或langCode 不能为空: "+dataJson.toString());
		}
		
	}
	public void deleteByIds(JSONArray idsJson) throws ServiceException {
		for(Object beanObj : idsJson){
			JSONObject idsObj = (JSONObject) beanObj;
			if(idsObj.containsKey("name") && idsObj.getString("name") != null && StringUtils.isNotEmpty(idsObj.getString("name")) 
					&& idsObj.containsKey("langCode") && idsObj.getString("langCode") != null && StringUtils.isNotEmpty(idsObj.getString("langCode"))){
				String name = idsObj.getString("name");
				String langCode = idsObj.getString("langCode");
				
				ScRoadnamePosition newBean = new ScRoadnamePosition();
				newBean.setName(name);
				newBean.setLangCode(langCode);
				
				if(newBean != null ){
					delete(newBean);
				}
			}else{
				log.error("删除失败 name或langCode 不能为空: "+idsJson.toString());
			}
			
		}
		
	}
	
	
}
