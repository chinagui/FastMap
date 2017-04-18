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
import com.navinfo.dataservice.engine.meta.model.ScRoadnameHwCode;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnameHwCodeService 
* @author code generator
* @date 2017-03-23 07:08:10 
* @Description: TODO
*/
@Service
public class ScRoadnameHwCodeService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScRoadnameHwCode  bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_ROADNAME_HW_CODE (ROADNAME, ROADCODE) values(?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getRoadname() , bean.getRoadcode()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(ScRoadnameHwCode bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_ROADNAME_HW_CODE set ROADNAME=?, ROADCODE=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getRoadname()!=null && StringUtils.isNotEmpty(bean.getRoadname().toString())){
				updateSql+=" and ROADNAME=? ";
				values.add(bean.getRoadname());
			};
			if (bean!=null&&bean.getRoadcode()!=null && StringUtils.isNotEmpty(bean.getRoadcode().toString())){
				updateSql+=" and ROADCODE=? ";
				values.add(bean.getRoadcode());
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
	public void delete(ScRoadnameHwCode bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_ROADNAME_HW_CODE where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getRoadname()!=null && StringUtils.isNotEmpty(bean.getRoadname().toString())){
				deleteSql+=" and ROADNAME=? ";
				values.add(bean.getRoadname());
			};
			if (bean!=null&&bean.getRoadcode()!=null && StringUtils.isNotEmpty(bean.getRoadcode().toString())){
				deleteSql+=" and ROADCODE=? ";
				values.add(bean.getRoadcode());
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
	public Page list(ScRoadnameHwCode bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_ROADNAME_HW_CODE where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getRoadname()!=null && StringUtils.isNotEmpty(bean.getRoadname().toString())){
				selectSql+=" and ROADNAME=? ";
				values.add(bean.getRoadname());
			};
			if (bean!=null&&bean.getRoadcode()!=null && StringUtils.isNotEmpty(bean.getRoadcode().toString())){
				selectSql+=" and ROADCODE=? ";
				values.add(bean.getRoadcode());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScRoadnameHwCode> list = new ArrayList<ScRoadnameHwCode>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScRoadnameHwCode model = new ScRoadnameHwCode();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setRoadname(rs.getString("ROADNAME"));
						model.setRoadcode(rs.getString("ROADCODE"));
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
	public Page list(JSONObject dataJson,final int curPageNum, final int pageSize, String sortby)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			ScRoadnameHwCode bean =(ScRoadnameHwCode) JSONObject.toBean(dataJson, ScRoadnameHwCode.class);
			
			String selectSql = "select * from SC_ROADNAME_HW_CODE where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getRoadname()!=null && StringUtils.isNotEmpty(bean.getRoadname().toString())){
				selectSql+=" and ROADNAME=? ";
				values.add(bean.getRoadname());
			};
			if (bean!=null&&bean.getRoadcode()!=null && StringUtils.isNotEmpty(bean.getRoadcode().toString())){
				selectSql+=" and ROADCODE=? ";
				values.add(bean.getRoadcode());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScRoadnameHwCode> list = new ArrayList<ScRoadnameHwCode>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						ScRoadnameHwCode model = new ScRoadnameHwCode();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setRoadname(rs.getString("ROADNAME"));
						model.setRoadcode(rs.getString("ROADCODE"));
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
	
	public List<ScRoadnameHwCode> list(ScRoadnameHwCode bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_ROADNAME_HW_CODE where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getRoadname()!=null && StringUtils.isNotEmpty(bean.getRoadname().toString())){
				selectSql+=" and ROADNAME=? ";
				values.add(bean.getRoadname());
			};
			if (bean!=null&&bean.getRoadcode()!=null && StringUtils.isNotEmpty(bean.getRoadcode().toString())){
				selectSql+=" and ROADCODE=? ";
				values.add(bean.getRoadcode());
			};
			ResultSetHandler<List<ScRoadnameHwCode>> rsHandler = new ResultSetHandler<List<ScRoadnameHwCode>>(){
				public List<ScRoadnameHwCode> handle(ResultSet rs) throws SQLException {
					List<ScRoadnameHwCode> list = new ArrayList<ScRoadnameHwCode>();
					while(rs.next()){
						ScRoadnameHwCode model = new ScRoadnameHwCode();
						model.setRoadname(rs.getString("ROADNAME"));
						model.setRoadcode(rs.getString("ROADCODE"));
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
	public ScRoadnameHwCode query(ScRoadnameHwCode bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_ROADNAME_HW_CODE where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getRoadname()!=null && StringUtils.isNotEmpty(bean.getRoadname().toString())){
				selectSql+=" and ROADNAME=? ";
				values.add(bean.getRoadname());
			};
			if (bean!=null&&bean.getRoadcode()!=null && StringUtils.isNotEmpty(bean.getRoadcode().toString())){
				selectSql+=" and ROADCODE=? ";
				values.add(bean.getRoadcode());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<ScRoadnameHwCode> rsHandler = new ResultSetHandler<ScRoadnameHwCode>(){
				public ScRoadnameHwCode handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScRoadnameHwCode model = new ScRoadnameHwCode();
						model.setRoadname(rs.getString("ROADNAME"));
						model.setRoadcode(rs.getString("ROADCODE"));
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
		ScRoadnameHwCode bean =(ScRoadnameHwCode) JSONObject.toBean(dataJson, ScRoadnameHwCode.class);
		if(bean.getRoadcode() != null && StringUtils.isNotEmpty(bean.getRoadcode()) 
				&& bean.getRoadname() != null && StringUtils.isNotEmpty(bean.getRoadname())){
			ScRoadnameHwCode newBean = new ScRoadnameHwCode();
			newBean.setRoadcode(bean.getRoadcode());
			newBean.setRoadname(bean.getRoadname());
			ScRoadnameHwCode oldBean = query(newBean);
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
			if(idsObj.containsKey("roadCode") && idsObj.getString("roadCode") != null && StringUtils.isNotEmpty(idsObj.getString("roadCode")) 
					&& idsObj.containsKey("roadName") && idsObj.getString("roadName") != null && StringUtils.isNotEmpty(idsObj.getString("roadName"))){
				String roadCode = idsObj.getString("roadCode");
				String roadName = idsObj.getString("roadName");
				
				ScRoadnameHwCode newBean = new ScRoadnameHwCode();
				newBean.setRoadcode(roadCode);
				newBean.setRoadname(roadName);
				
				if(newBean != null ){
					delete(newBean);
				}
			}else{
				log.error("删除失败 name或langCode 不能为空: "+idsJson.toString());
			}
			
		}
		
	}

	
}
