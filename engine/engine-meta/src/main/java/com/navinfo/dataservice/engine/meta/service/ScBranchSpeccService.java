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
import com.navinfo.dataservice.engine.meta.model.ScBranchCommc;
import com.navinfo.dataservice.engine.meta.model.ScBranchSpecc;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

/** 
* @ClassName:  ScBranchSpeccService 
* @author code generator
* @date 2017-03-22 09:23:23 
* @Description: TODO
*/
@Service
public class ScBranchSpeccService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScBranchSpecc  bean,Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_BRANCH_SPECC (BRANCH_1, BRANCH_2, SERIESBRANCH_1) values(?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getBranch1() , bean.getBranch2(), bean.getSeriesbranch1()
					   );
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
//		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
//		}
	}
	public void update(ScBranchSpecc bean,Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_BRANCH_SPECC set ";
			//+ "BRANCH_1=?, BRANCH_2=?, SERIESBRANCH_1=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			String valueSql = "";
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+=" BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			String whereSql = " where 1=1 ";
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				whereSql+=" and BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				whereSql+=" and BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				whereSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			
			run.update(conn, 
					   updateSql+valueSql+whereSql, 
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
	public void delete(ScBranchSpecc bean,Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_BRANCH_SPECC where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				deleteSql+=" and BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				deleteSql+=" and BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				deleteSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
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
	public Page list(ScBranchSpecc bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_BRANCH_SPECC where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				selectSql+=" and BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				selectSql+=" and BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				selectSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScBranchSpecc> list = new ArrayList<ScBranchSpecc>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScBranchSpecc model = new ScBranchSpecc();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setBranch1(rs.getString("BRANCH_1"));
						model.setBranch2(rs.getString("BRANCH_2"));
						model.setSeriesbranch1(rs.getString("SERIESBRANCH_1"));
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
	public List<ScBranchSpecc> list(ScBranchSpecc bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_BRANCH_SPECC where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				selectSql+=" and BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				selectSql+=" and BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				selectSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			ResultSetHandler<List<ScBranchSpecc>> rsHandler = new ResultSetHandler<List<ScBranchSpecc>>(){
				public List<ScBranchSpecc> handle(ResultSet rs) throws SQLException {
					List<ScBranchSpecc> list = new ArrayList<ScBranchSpecc>();
					while(rs.next()){
						ScBranchSpecc model = new ScBranchSpecc();
						model.setBranch1(rs.getString("BRANCH_1"));
						model.setBranch2(rs.getString("BRANCH_2"));
						model.setSeriesbranch1(rs.getString("SERIESBRANCH_1"));
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
	public ScBranchSpecc query(ScBranchSpecc bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_BRANCH_SPECC where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				selectSql+=" and BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				selectSql+=" and BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				selectSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<ScBranchSpecc> rsHandler = new ResultSetHandler<ScBranchSpecc>(){
				public ScBranchSpecc handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScBranchSpecc model = new ScBranchSpecc();
						model.setBranch1(rs.getString("BRANCH_1"));
						model.setBranch2(rs.getString("BRANCH_2"));
						model.setSeriesbranch1(rs.getString("SERIESBRANCH_1"));
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
	public void saveUpdate(JSONObject dataJson) throws ServiceException  {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getMetaConnection();
			
			ScBranchSpecc bean =(ScBranchSpecc) JSONObject.toBean(dataJson, ScBranchSpecc.class);
			ScBranchSpecc oldBean = query(bean);
			if(oldBean != null ){//存在  ,更新数据
				update(bean,conn);
			}else{//不存在  执行新增
				create(bean,conn);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("新增修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void deleteByIds(JSONArray idsJson) throws ServiceException {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getMetaConnection();
			for(Object beanObj : idsJson){
				JSONObject beanJson = (JSONObject) beanObj;
				ScBranchSpecc bean =(ScBranchSpecc) JSONObject.toBean(beanJson, ScBranchSpecc.class);
				delete(bean,conn);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	public Page list(JSONObject dataJson,final int curPageNum, final int pageSize, String sortby) throws ServiceException {
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			ScBranchSpecc bean =(ScBranchSpecc) JSONObject.toBean(dataJson, ScBranchSpecc.class);
			
			String selectSql = "select * from SC_BRANCH_SPECC where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				selectSql+=" and BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				selectSql+=" and BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				selectSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
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
					List<ScBranchSpecc> list = new ArrayList<ScBranchSpecc>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						ScBranchSpecc model = new ScBranchSpecc();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setBranch1(rs.getString("BRANCH_1"));
						model.setBranch2(rs.getString("BRANCH_2"));
						model.setSeriesbranch1(rs.getString("SERIESBRANCH_1"));
						list.add(model);
					}
					page.setResult(list);
					return page;
				}
	    		
	    	};
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
