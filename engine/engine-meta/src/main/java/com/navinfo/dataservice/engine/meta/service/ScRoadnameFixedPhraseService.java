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
import com.navinfo.dataservice.engine.meta.model.ScRoadnameFixedPhrase;
import com.navinfo.dataservice.engine.meta.model.ScRoadnameInfix;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnameFixedPhraseService 
* @author code generator
* @date 2017-03-23 07:06:21 
* @Description: TODO
*/
@Service
public class ScRoadnameFixedPhraseService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScRoadnameFixedPhrase  bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_ROADNAME_FIXED_PHRASE (ID, NAME, REGION_FLAG, LANG_CODE) values(?,?,?,?)";			
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
	public void update(ScRoadnameFixedPhrase bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_ROADNAME_FIXED_PHRASE set ID=?, NAME=?, REGION_FLAG=?, LANG_CODE=? where 1=1 ";
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
	public void delete(ScRoadnameFixedPhrase bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_ROADNAME_FIXED_PHRASE where 1=1 ";
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
	public Page list(ScRoadnameFixedPhrase bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_ROADNAME_FIXED_PHRASE where 1=1 ";
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
					List<ScRoadnameFixedPhrase> list = new ArrayList<ScRoadnameFixedPhrase>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScRoadnameFixedPhrase model = new ScRoadnameFixedPhrase();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setId(rs.getLong("ID"));
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
	public Page list(JSONObject dataJson, final int curPageNum,final int pageSize, String sortby) throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			ScRoadnameFixedPhrase bean =(ScRoadnameFixedPhrase) JSONObject.toBean(dataJson, ScRoadnameFixedPhrase.class);
			
			
			String selectSql = "select * from SC_ROADNAME_FIXED_PHRASE where 1=1 ";
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
					List<ScRoadnameFixedPhrase> list = new ArrayList<ScRoadnameFixedPhrase>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						ScRoadnameFixedPhrase model = new ScRoadnameFixedPhrase();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setId(rs.getLong("ID"));
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
	
	public List<ScRoadnameFixedPhrase> list(ScRoadnameFixedPhrase bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_ROADNAME_FIXED_PHRASE where 1=1 ";
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
			ResultSetHandler<List<ScRoadnameFixedPhrase>> rsHandler = new ResultSetHandler<List<ScRoadnameFixedPhrase>>(){
				public List<ScRoadnameFixedPhrase> handle(ResultSet rs) throws SQLException {
					List<ScRoadnameFixedPhrase> list = new ArrayList<ScRoadnameFixedPhrase>();
					while(rs.next()){
						ScRoadnameFixedPhrase model = new ScRoadnameFixedPhrase();
						model.setId(rs.getLong("ID"));
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
	public ScRoadnameFixedPhrase query(ScRoadnameFixedPhrase bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_ROADNAME_FIXED_PHRASE where 1=1 ";
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
			ResultSetHandler<ScRoadnameFixedPhrase> rsHandler = new ResultSetHandler<ScRoadnameFixedPhrase>(){
				public ScRoadnameFixedPhrase handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScRoadnameFixedPhrase model = new ScRoadnameFixedPhrase();
						model.setId(rs.getLong("ID"));
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
		ScRoadnameFixedPhrase bean =(ScRoadnameFixedPhrase) JSONObject.toBean(dataJson, ScRoadnameFixedPhrase.class);
		if(bean.getId() != null && bean.getId() > 0){
			ScRoadnameFixedPhrase newBean = new ScRoadnameFixedPhrase();
			newBean.setId(bean.getId());
			ScRoadnameFixedPhrase oldBean = query(newBean);
			if(oldBean != null ){//存在  ,更新数据
				update(bean);
			}else{
				create(bean);
			}
		}else{//不存在  执行新增
			create(bean);
		}
		
		
	}
	public void deleteByIds(JSONArray idsJson) throws ServiceException {
		for(Object beanObj : idsJson){
			Long id = (Long) beanObj;
			if(id != null && id >0){
				ScRoadnameFixedPhrase newBean = new ScRoadnameFixedPhrase();
				newBean.setId(id);
				delete(newBean);
			}
		}
	}
	
	
}
