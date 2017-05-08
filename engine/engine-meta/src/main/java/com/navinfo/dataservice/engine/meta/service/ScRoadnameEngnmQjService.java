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
import com.navinfo.dataservice.engine.meta.model.ScRoadnameEngnmQj;
import com.navinfo.dataservice.engine.meta.model.ScRoadnameFixedPhrase;
import com.navinfo.dataservice.engine.meta.model.ScRoadnameSplitPrefix;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnameEngnmQjService 
* @author code generator
* @date 2017-03-23 07:07:56 
* @Description: TODO
*/
@Service
public class ScRoadnameEngnmQjService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScRoadnameEngnmQj  bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_ROADNAME_ENGNM_QJ (ID, NAME_Q, NAME_J, LANG_CODE) values(?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getId() , bean.getNameQ(), bean.getNameJ(), bean.getLangCode()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(ScRoadnameEngnmQj bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_ROADNAME_ENGNM_QJ set ID=?, NAME_Q=?, NAME_J=?, LANG_CODE=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				updateSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getNameQ()!=null && StringUtils.isNotEmpty(bean.getNameQ().toString())){
				updateSql+=" and NAME_Q=? ";
				values.add(bean.getNameQ());
			};
			if (bean!=null&&bean.getNameJ()!=null && StringUtils.isNotEmpty(bean.getNameJ().toString())){
				updateSql+=" and NAME_J=? ";
				values.add(bean.getNameJ());
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
	public void delete(ScRoadnameEngnmQj bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_ROADNAME_ENGNM_QJ where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				deleteSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getNameQ()!=null && StringUtils.isNotEmpty(bean.getNameQ().toString())){
				deleteSql+=" and NAME_Q=? ";
				values.add(bean.getNameQ());
			};
			if (bean!=null&&bean.getNameJ()!=null && StringUtils.isNotEmpty(bean.getNameJ().toString())){
				deleteSql+=" and NAME_J=? ";
				values.add(bean.getNameJ());
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
	public Page list(ScRoadnameEngnmQj bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_ROADNAME_ENGNM_QJ where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getNameQ()!=null && StringUtils.isNotEmpty(bean.getNameQ().toString())){
				selectSql+=" and NAME_Q=? ";
				values.add(bean.getNameQ());
			};
			if (bean!=null&&bean.getNameJ()!=null && StringUtils.isNotEmpty(bean.getNameJ().toString())){
				selectSql+=" and NAME_J=? ";
				values.add(bean.getNameJ());
			};
			if (bean!=null&&bean.getLangCode()!=null && StringUtils.isNotEmpty(bean.getLangCode().toString())){
				selectSql+=" and LANG_CODE=? ";
				values.add(bean.getLangCode());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScRoadnameEngnmQj> list = new ArrayList<ScRoadnameEngnmQj>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScRoadnameEngnmQj model = new ScRoadnameEngnmQj();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setId(rs.getLong("ID"));
						model.setNameQ(rs.getString("NAME_Q"));
						model.setNameJ(rs.getString("NAME_J"));
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
			ScRoadnameEngnmQj bean =(ScRoadnameEngnmQj) JSONObject.toBean(dataJson, ScRoadnameEngnmQj.class);
			
			String selectSql = "select * from SC_ROADNAME_ENGNM_QJ where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getNameQ()!=null && StringUtils.isNotEmpty(bean.getNameQ().toString())){
				selectSql+=" and NAME_Q=? ";
				values.add(bean.getNameQ());
			};
			if (bean!=null&&bean.getNameJ()!=null && StringUtils.isNotEmpty(bean.getNameJ().toString())){
				selectSql+=" and NAME_J=? ";
				values.add(bean.getNameJ());
			};
			if (bean!=null&&bean.getLangCode()!=null && StringUtils.isNotEmpty(bean.getLangCode().toString())){
				selectSql+=" and LANG_CODE=? ";
				values.add(bean.getLangCode());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScRoadnameEngnmQj> list = new ArrayList<ScRoadnameEngnmQj>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						ScRoadnameEngnmQj model = new ScRoadnameEngnmQj();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setId(rs.getLong("ID"));
						model.setNameQ(rs.getString("NAME_Q"));
						model.setNameJ(rs.getString("NAME_J"));
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
	
	public List<ScRoadnameEngnmQj> list(ScRoadnameEngnmQj bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_ROADNAME_ENGNM_QJ where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getNameQ()!=null && StringUtils.isNotEmpty(bean.getNameQ().toString())){
				selectSql+=" and NAME_Q=? ";
				values.add(bean.getNameQ());
			};
			if (bean!=null&&bean.getNameJ()!=null && StringUtils.isNotEmpty(bean.getNameJ().toString())){
				selectSql+=" and NAME_J=? ";
				values.add(bean.getNameJ());
			};
			if (bean!=null&&bean.getLangCode()!=null && StringUtils.isNotEmpty(bean.getLangCode().toString())){
				selectSql+=" and LANG_CODE=? ";
				values.add(bean.getLangCode());
			};
			ResultSetHandler<List<ScRoadnameEngnmQj>> rsHandler = new ResultSetHandler<List<ScRoadnameEngnmQj>>(){
				public List<ScRoadnameEngnmQj> handle(ResultSet rs) throws SQLException {
					List<ScRoadnameEngnmQj> list = new ArrayList<ScRoadnameEngnmQj>();
					while(rs.next()){
						ScRoadnameEngnmQj model = new ScRoadnameEngnmQj();
						model.setId(rs.getLong("ID"));
						model.setNameQ(rs.getString("NAME_Q"));
						model.setNameJ(rs.getString("NAME_J"));
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
	public ScRoadnameEngnmQj query(ScRoadnameEngnmQj bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_ROADNAME_ENGNM_QJ where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getNameQ()!=null && StringUtils.isNotEmpty(bean.getNameQ().toString())){
				selectSql+=" and NAME_Q=? ";
				values.add(bean.getNameQ());
			};
			if (bean!=null&&bean.getNameJ()!=null && StringUtils.isNotEmpty(bean.getNameJ().toString())){
				selectSql+=" and NAME_J=? ";
				values.add(bean.getNameJ());
			};
			if (bean!=null&&bean.getLangCode()!=null && StringUtils.isNotEmpty(bean.getLangCode().toString())){
				selectSql+=" and LANG_CODE=? ";
				values.add(bean.getLangCode());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<ScRoadnameEngnmQj> rsHandler = new ResultSetHandler<ScRoadnameEngnmQj>(){
				public ScRoadnameEngnmQj handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScRoadnameEngnmQj model = new ScRoadnameEngnmQj();
						model.setId(rs.getLong("ID"));
						model.setNameQ(rs.getString("NAME_Q"));
						model.setNameJ(rs.getString("NAME_J"));
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
		ScRoadnameEngnmQj bean =(ScRoadnameEngnmQj) JSONObject.toBean(dataJson, ScRoadnameEngnmQj.class);
		if(bean.getId() != null && bean.getId() >0){
			ScRoadnameEngnmQj newBean = new ScRoadnameEngnmQj();
			newBean.setId(bean.getId());
			ScRoadnameEngnmQj oldBean = query(newBean);
			if(oldBean != null ){//存在  ,更新数据
				update(bean);
			}else{//不存在  执行新增
				create(bean);
			}
		}else{
			create(bean);
		}
		
		
	}
	public void deleteByIds(JSONArray idsJson) throws ServiceException {
		for(Object beanObj : idsJson){
			Long id = (Long) beanObj;
			if(id != null && id >0){
				ScRoadnameEngnmQj newBean = new ScRoadnameEngnmQj();
				newBean.setId(id);
				delete(newBean);
			}
		}
		
	}
	
	
}
