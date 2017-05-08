package com.navinfo.dataservice.engine.meta.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.meta.model.ScModelMatchG;
import com.navinfo.dataservice.engine.meta.model.ScModelRepdelG;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

/** 
* @ClassName:  ScModelRepdelGService 
* @author code generator
* @date 2017-03-22 09:22:39 
* @Description: TODO
*/
@Service
public class ScModelRepdelGService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScModelRepdelG  bean, Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_MODEL_REPDEL_G (CONV_BEFORE, CONV_OUT, KIND) values(?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getConvBefore() , bean.getConvOut(), bean.getKind()
					   );
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
//		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
//		}
	}
	public void update(ScModelRepdelG bean, Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_MODEL_REPDEL_G set ";
					//+ "CONV_BEFORE=?, CONV_OUT=?, KIND=? where 1=1 ";
			String valueSql = "";
			List<Object> values=new ArrayList<Object>();
			
			if (bean!=null&&bean.getConvOut()!=null && StringUtils.isNotEmpty(bean.getConvOut().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  CONV_OUT=? ";
				values.add(bean.getConvOut());
			};
			if (bean!=null&&bean.getKind()!=null && StringUtils.isNotEmpty(bean.getKind().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  KIND=? ";
				values.add(bean.getKind());
			};
			if (bean!=null&&bean.getConvBefore()!=null && StringUtils.isNotEmpty(bean.getConvBefore().toString())){
				valueSql+=" where  CONV_BEFORE=? ";
				values.add(bean.getConvBefore());
			};
			log.info("sql : "+updateSql+valueSql);
			run.update(conn, 
					   updateSql+valueSql, 
//					   values.toArray(),
					   values.toArray()
					   );
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
//		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
//		}
	}
	public void delete(ScModelRepdelG bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_MODEL_REPDEL_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getConvBefore()!=null && StringUtils.isNotEmpty(bean.getConvBefore().toString())){
				deleteSql+=" and CONV_BEFORE=? ";
				values.add(bean.getConvBefore());
			};
			if (bean!=null&&bean.getConvOut()!=null && StringUtils.isNotEmpty(bean.getConvOut().toString())){
				deleteSql+=" and CONV_OUT=? ";
				values.add(bean.getConvOut());
			};
			if (bean!=null&&bean.getKind()!=null && StringUtils.isNotEmpty(bean.getKind().toString())){
				deleteSql+=" and KIND=? ";
				values.add(bean.getKind());
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
	private void delete(String convBefore, Connection conn) throws Exception {
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_MODEL_REPDEL_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (convBefore!=null&& StringUtils.isNotEmpty(convBefore)){
				deleteSql+=" and CONV_BEFORE=? ";
				values.add(convBefore);
			};
			
			if (values.size()==0){
	    		run.update(conn, deleteSql);
	    	}else{
	    		run.update(conn, deleteSql,values.toArray());
	    	}
	    	
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("删除失败，原因为:"+e.getMessage(),e);
//		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
//		}
		
	}
	public Page list(ScModelRepdelG bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_MODEL_REPDEL_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getConvBefore()!=null && StringUtils.isNotEmpty(bean.getConvBefore().toString())){
				selectSql+=" and CONV_BEFORE=? ";
				values.add(bean.getConvBefore());
			};
			if (bean!=null&&bean.getConvOut()!=null && StringUtils.isNotEmpty(bean.getConvOut().toString())){
				selectSql+=" and CONV_OUT=? ";
				values.add(bean.getConvOut());
			};
			if (bean!=null&&bean.getKind()!=null && StringUtils.isNotEmpty(bean.getKind().toString())){
				selectSql+=" and KIND=? ";
				values.add(bean.getKind());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScModelRepdelG> list = new ArrayList<ScModelRepdelG>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScModelRepdelG model = new ScModelRepdelG();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setConvBefore(rs.getString("CONV_BEFORE"));
						model.setConvOut(rs.getString("CONV_OUT"));
						model.setKind(rs.getString("KIND"));
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
	public List<ScModelRepdelG> list(ScModelRepdelG bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_MODEL_REPDEL_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getConvBefore()!=null && StringUtils.isNotEmpty(bean.getConvBefore().toString())){
				selectSql+=" and CONV_BEFORE=? ";
				values.add(bean.getConvBefore());
			};
			if (bean!=null&&bean.getConvOut()!=null && StringUtils.isNotEmpty(bean.getConvOut().toString())){
				selectSql+=" and CONV_OUT=? ";
				values.add(bean.getConvOut());
			};
			if (bean!=null&&bean.getKind()!=null && StringUtils.isNotEmpty(bean.getKind().toString())){
				selectSql+=" and KIND=? ";
				values.add(bean.getKind());
			};
			ResultSetHandler<List<ScModelRepdelG>> rsHandler = new ResultSetHandler<List<ScModelRepdelG>>(){
				public List<ScModelRepdelG> handle(ResultSet rs) throws SQLException {
					List<ScModelRepdelG> list = new ArrayList<ScModelRepdelG>();
					while(rs.next()){
						ScModelRepdelG model = new ScModelRepdelG();
						model.setConvBefore(rs.getString("CONV_BEFORE"));
						model.setConvOut(rs.getString("CONV_OUT"));
						model.setKind(rs.getString("KIND"));
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
	public ScModelRepdelG query(ScModelRepdelG bean, Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_MODEL_REPDEL_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getConvBefore()!=null && StringUtils.isNotEmpty(bean.getConvBefore().toString())){
				selectSql+=" and CONV_BEFORE=? ";
				values.add(bean.getConvBefore());
			};
			if (bean!=null&&bean.getConvOut()!=null && StringUtils.isNotEmpty(bean.getConvOut().toString())){
				selectSql+=" and CONV_OUT=? ";
				values.add(bean.getConvOut());
			};
			if (bean!=null&&bean.getKind()!=null && StringUtils.isNotEmpty(bean.getKind().toString())){
				selectSql+=" and KIND=? ";
				values.add(bean.getKind());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<ScModelRepdelG> rsHandler = new ResultSetHandler<ScModelRepdelG>(){
				public ScModelRepdelG handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScModelRepdelG model = new ScModelRepdelG();
						model.setConvBefore(rs.getString("CONV_BEFORE"));
						model.setConvOut(rs.getString("CONV_OUT"));
						model.setKind(rs.getString("KIND"));
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
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("查询明细失败，原因为:"+e.getMessage(),e);
//		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
//		}
	}
	public void saveUpdate(JSONObject dataJson) throws ServiceException {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getMetaConnection();	
			ScModelRepdelG bean =(ScModelRepdelG) JSONObject.toBean(dataJson, ScModelRepdelG.class);
			ScModelRepdelG newBean = new ScModelRepdelG();
			newBean.setConvBefore(bean.getConvBefore());
			ScModelRepdelG oldBean = query(newBean,conn);
			if(oldBean != null ){//存在  ,更新数据
				update(bean,conn);
			}else{//不存在  执行新增
				create(bean,conn);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}	
		
	}
	public void deleteByIds(JSONArray idsJson) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getMetaConnection();	
			for(Object beanObj : idsJson){
				String convBefore = (String) beanObj;
				if(convBefore != null && StringUtils.isNotEmpty(convBefore)){
					delete(convBefore,conn);
				}
			}
		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		}
	public Page list(JSONObject dataJson, final int curPageNum, final int pageSize, String sortby) throws ServiceException {
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			ScModelRepdelG bean =(ScModelRepdelG) JSONObject.toBean(dataJson, ScModelRepdelG.class);
			
			String selectSql = "select * from SC_MODEL_REPDEL_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getConvBefore()!=null && StringUtils.isNotEmpty(bean.getConvBefore().toString())){
				selectSql+=" and CONV_BEFORE=? ";
				values.add(bean.getConvBefore());
			};
			if (bean!=null&&bean.getConvOut()!=null && StringUtils.isNotEmpty(bean.getConvOut().toString())){
				selectSql+=" and CONV_OUT=? ";
				values.add(bean.getConvOut());
			};
			if (bean!=null&&bean.getKind()!=null && StringUtils.isNotEmpty(bean.getKind().toString())){
				selectSql+=" and KIND=? ";
				values.add(bean.getKind());
			};
			//添加分页
			com.navinfo.dataservice.commons.util.StringUtils sUtils = new com.navinfo.dataservice.commons.util.StringUtils();
			if (sortby.length()>0) {
				int index = sortby.indexOf("-");
				if (index != -1) {
					selectSql+=" ORDER BY ";
					String sortbyName = sUtils.toColumnName(sortby.substring(1));
					selectSql+= sortbyName;
					selectSql+=" DESC ";
				} else {
					selectSql+=" ORDER BY ";
					String sortbyName = sUtils.toColumnName(sortby.substring(1));
					selectSql+= sortbyName;
				}
			} 
			/*else {
				selectSql+=" ORDER BY FILE_ID DESC";
			}*/
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScModelRepdelG> list = new ArrayList<ScModelRepdelG>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						ScModelRepdelG model = new ScModelRepdelG();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setConvBefore(rs.getString("CONV_BEFORE"));
						model.setConvOut(rs.getString("CONV_OUT"));
						model.setKind(rs.getString("KIND"));
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
	
	
}
