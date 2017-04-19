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
import com.navinfo.dataservice.engine.meta.model.ScRoadnameAbb;
import com.navinfo.dataservice.engine.meta.model.ScRoadnameEngnmQj;
import com.navinfo.dataservice.engine.meta.model.ScRoadnameHwCode;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnameAbbService 
* @author code generator
* @date 2017-03-23 07:08:32 
* @Description: TODO
*/
@Service
public class ScRoadnameAbbService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScRoadnameAbb  bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_ROADNAME_ABB (ADMIN_ID, NAME_GROUPID, NAME, NAME_GROUPID_ABB, NAME_ABB) values(?,?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getAdminId() , bean.getNameGroupid(), bean.getName(), bean.getNameGroupidAbb(), bean.getNameAbb()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(ScRoadnameAbb bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_ROADNAME_ABB set ADMIN_ID=?, NAME_GROUPID=?, NAME=?, NAME_GROUPID_ABB=?, NAME_ABB=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getAdminId()!=null && StringUtils.isNotEmpty(bean.getAdminId().toString())){
				updateSql+=" and ADMIN_ID=? ";
				values.add(bean.getAdminId());
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				updateSql+=" and NAME_GROUPID=? ";
				values.add(bean.getNameGroupid());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				updateSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getNameGroupidAbb()!=null && StringUtils.isNotEmpty(bean.getNameGroupidAbb().toString())){
				updateSql+=" and NAME_GROUPID_ABB=? ";
				values.add(bean.getNameGroupidAbb());
			};
			if (bean!=null&&bean.getNameAbb()!=null && StringUtils.isNotEmpty(bean.getNameAbb().toString())){
				updateSql+=" and NAME_ABB=? ";
				values.add(bean.getNameAbb());
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
	public void delete(ScRoadnameAbb bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_ROADNAME_ABB where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getAdminId()!=null && StringUtils.isNotEmpty(bean.getAdminId().toString())){
				deleteSql+=" and ADMIN_ID=? ";
				values.add(bean.getAdminId());
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				deleteSql+=" and NAME_GROUPID=? ";
				values.add(bean.getNameGroupid());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				deleteSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getNameGroupidAbb()!=null && StringUtils.isNotEmpty(bean.getNameGroupidAbb().toString())){
				deleteSql+=" and NAME_GROUPID_ABB=? ";
				values.add(bean.getNameGroupidAbb());
			};
			if (bean!=null&&bean.getNameAbb()!=null && StringUtils.isNotEmpty(bean.getNameAbb().toString())){
				deleteSql+=" and NAME_ABB=? ";
				values.add(bean.getNameAbb());
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
	public Page list(ScRoadnameAbb bean ,final int currentPageNum,final int pageSize)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_ROADNAME_ABB where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getAdminId()!=null && StringUtils.isNotEmpty(bean.getAdminId().toString())){
				selectSql+=" and ADMIN_ID=? ";
				values.add(bean.getAdminId());
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				selectSql+=" and NAME_GROUPID=? ";
				values.add(bean.getNameGroupid());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				selectSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getNameGroupidAbb()!=null && StringUtils.isNotEmpty(bean.getNameGroupidAbb().toString())){
				selectSql+=" and NAME_GROUPID_ABB=? ";
				values.add(bean.getNameGroupidAbb());
			};
			if (bean!=null&&bean.getNameAbb()!=null && StringUtils.isNotEmpty(bean.getNameAbb().toString())){
				selectSql+=" and NAME_ABB=? ";
				values.add(bean.getNameAbb());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScRoadnameAbb> list = new ArrayList<ScRoadnameAbb>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScRoadnameAbb model = new ScRoadnameAbb();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setAdminId(rs.getLong("ADMIN_ID"));
						model.setNameGroupid(rs.getLong("NAME_GROUPID"));
						model.setName(rs.getString("NAME"));
						model.setNameGroupidAbb(rs.getLong("NAME_GROUPID_ABB"));
						model.setNameAbb(rs.getString("NAME_ABB"));
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
			ScRoadnameAbb bean =(ScRoadnameAbb) JSONObject.toBean(dataJson, ScRoadnameAbb.class);
			
			String selectSql = "select * from SC_ROADNAME_ABB where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getAdminId()!=null && StringUtils.isNotEmpty(bean.getAdminId().toString())){
				selectSql+=" and ADMIN_ID=? ";
				values.add(bean.getAdminId());
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				selectSql+=" and NAME_GROUPID=? ";
				values.add(bean.getNameGroupid());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				selectSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getNameGroupidAbb()!=null && StringUtils.isNotEmpty(bean.getNameGroupidAbb().toString())){
				selectSql+=" and NAME_GROUPID_ABB=? ";
				values.add(bean.getNameGroupidAbb());
			};
			if (bean!=null&&bean.getNameAbb()!=null && StringUtils.isNotEmpty(bean.getNameAbb().toString())){
				selectSql+=" and NAME_ABB=? ";
				values.add(bean.getNameAbb());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScRoadnameAbb> list = new ArrayList<ScRoadnameAbb>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						ScRoadnameAbb model = new ScRoadnameAbb();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setAdminId(rs.getLong("ADMIN_ID"));
						model.setNameGroupid(rs.getLong("NAME_GROUPID"));
						model.setName(rs.getString("NAME"));
						model.setNameGroupidAbb(rs.getLong("NAME_GROUPID_ABB"));
						model.setNameAbb(rs.getString("NAME_ABB"));
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
	public List<ScRoadnameAbb> list(ScRoadnameAbb bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_ROADNAME_ABB where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getAdminId()!=null && StringUtils.isNotEmpty(bean.getAdminId().toString())){
				selectSql+=" and ADMIN_ID=? ";
				values.add(bean.getAdminId());
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				selectSql+=" and NAME_GROUPID=? ";
				values.add(bean.getNameGroupid());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				selectSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getNameGroupidAbb()!=null && StringUtils.isNotEmpty(bean.getNameGroupidAbb().toString())){
				selectSql+=" and NAME_GROUPID_ABB=? ";
				values.add(bean.getNameGroupidAbb());
			};
			if (bean!=null&&bean.getNameAbb()!=null && StringUtils.isNotEmpty(bean.getNameAbb().toString())){
				selectSql+=" and NAME_ABB=? ";
				values.add(bean.getNameAbb());
			};
			ResultSetHandler<List<ScRoadnameAbb>> rsHandler = new ResultSetHandler<List<ScRoadnameAbb>>(){
				public List<ScRoadnameAbb> handle(ResultSet rs) throws SQLException {
					List<ScRoadnameAbb> list = new ArrayList<ScRoadnameAbb>();
					while(rs.next()){
						ScRoadnameAbb model = new ScRoadnameAbb();
						model.setAdminId(rs.getLong("ADMIN_ID"));
						model.setNameGroupid(rs.getLong("NAME_GROUPID"));
						model.setName(rs.getString("NAME"));
						model.setNameGroupidAbb(rs.getLong("NAME_GROUPID_ABB"));
						model.setNameAbb(rs.getString("NAME_ABB"));
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
	public ScRoadnameAbb query(ScRoadnameAbb bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_ROADNAME_ABB where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getAdminId()!=null && StringUtils.isNotEmpty(bean.getAdminId().toString())){
				selectSql+=" and ADMIN_ID=? ";
				values.add(bean.getAdminId());
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				selectSql+=" and NAME_GROUPID=? ";
				values.add(bean.getNameGroupid());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				selectSql+=" and NAME=? ";
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getNameGroupidAbb()!=null && StringUtils.isNotEmpty(bean.getNameGroupidAbb().toString())){
				selectSql+=" and NAME_GROUPID_ABB=? ";
				values.add(bean.getNameGroupidAbb());
			};
			if (bean!=null&&bean.getNameAbb()!=null && StringUtils.isNotEmpty(bean.getNameAbb().toString())){
				selectSql+=" and NAME_ABB=? ";
				values.add(bean.getNameAbb());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<ScRoadnameAbb> rsHandler = new ResultSetHandler<ScRoadnameAbb>(){
				public ScRoadnameAbb handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScRoadnameAbb model = new ScRoadnameAbb();
						model.setAdminId(rs.getLong("ADMIN_ID"));
						model.setNameGroupid(rs.getLong("NAME_GROUPID"));
						model.setName(rs.getString("NAME"));
						model.setNameGroupidAbb(rs.getLong("NAME_GROUPID_ABB"));
						model.setNameAbb(rs.getString("NAME_ABB"));
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
		ScRoadnameAbb bean = (ScRoadnameAbb) JSONObject.toBean(dataJson, ScRoadnameAbb.class);
		if(bean.getNameGroupid() != null && bean.getNameGroupid() > 0){
			ScRoadnameAbb newBean = new ScRoadnameAbb();
			newBean.setNameGroupid(bean.getNameGroupid());
			ScRoadnameAbb oldBean = query(newBean);
			if(oldBean != null ){//存在  ,更新数据
				update(bean);
			}else{//不存在  执行新增
				create(bean);
			}
		}
		
	}
	public void deleteByIds(JSONArray idsJson) throws ServiceException {
		for(Object beanObj : idsJson){
			Long nameGroupid = (Long) beanObj;
			if(nameGroupid != null && nameGroupid >0){
				ScRoadnameAbb newBean = new ScRoadnameAbb();
				newBean.setNameGroupid(nameGroupid);
				delete(newBean);
			}
		}
		
	}
	
	
}
