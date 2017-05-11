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
import com.navinfo.dataservice.engine.meta.model.ScBcrossnodeMatchck;
import com.navinfo.dataservice.engine.meta.model.ScVectorMatch;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

/** 
* @ClassName:  ScBcrossnodeMatchckService 
* @author code generator
* @date 2017-03-22 09:23:34 
* @Description: TODO
*/
@Service
public class ScBcrossnodeMatchckService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScBcrossnodeMatchck  bean,Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_BCROSSNODE_MATCHCK (SCHEMATIC_CODE, ARROW_CODE, SEQ) values(?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getSchematicCode() , bean.getArrowCode(), bean.getSeq()
					   );
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
//		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
//		}
	}
	public void update(ScBcrossnodeMatchck bean,Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_BCROSSNODE_MATCHCK set ";
				//	+ "SCHEMATIC_CODE=?, ARROW_CODE=?, SEQ=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			String valueSql = "";
			if (bean!=null&&bean.getSchematicCode()!=null && StringUtils.isNotEmpty(bean.getSchematicCode().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  SCHEMATIC_CODE=? ";
				values.add(bean.getSchematicCode());
			};
			if (bean!=null&&bean.getArrowCode()!=null && StringUtils.isNotEmpty(bean.getArrowCode().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  ARROW_CODE=? ";
				values.add(bean.getArrowCode());
			};
			if (bean!=null&&bean.getSeq()!=null && StringUtils.isNotEmpty(bean.getSeq().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  SEQ=? ";
				values.add(bean.getSeq());
			};
			String whereSql = " where 1=1 ";
			if (bean!=null&&bean.getSeq()!=null && StringUtils.isNotEmpty(bean.getSeq().toString())){
				whereSql+=" and SEQ=? ";
				values.add(bean.getSeq());
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
	public void delete(ScBcrossnodeMatchck bean,Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_BCROSSNODE_MATCHCK where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getSchematicCode()!=null && StringUtils.isNotEmpty(bean.getSchematicCode().toString())){
				deleteSql+=" and SCHEMATIC_CODE=? ";
				values.add(bean.getSchematicCode());
			};
			if (bean!=null&&bean.getArrowCode()!=null && StringUtils.isNotEmpty(bean.getArrowCode().toString())){
				deleteSql+=" and ARROW_CODE=? ";
				values.add(bean.getArrowCode());
			};
			if (bean!=null&&bean.getSeq()!=null && StringUtils.isNotEmpty(bean.getSeq().toString())){
				deleteSql+=" and SEQ=? ";
				values.add(bean.getSeq());
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
	public void delete(Integer seq,Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_BCROSSNODE_MATCHCK where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			
			if (seq!=null){
				deleteSql+=" and SEQ=? ";
				values.add(seq);
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
	public Page list(ScBcrossnodeMatchck bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_BCROSSNODE_MATCHCK where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getSchematicCode()!=null && StringUtils.isNotEmpty(bean.getSchematicCode().toString())){
				selectSql+=" and SCHEMATIC_CODE=? ";
				values.add(bean.getSchematicCode());
			};
			if (bean!=null&&bean.getArrowCode()!=null && StringUtils.isNotEmpty(bean.getArrowCode().toString())){
				selectSql+=" and ARROW_CODE=? ";
				values.add(bean.getArrowCode());
			};
			if (bean!=null&&bean.getSeq()!=null && StringUtils.isNotEmpty(bean.getSeq().toString())){
				selectSql+=" and SEQ=? ";
				values.add(bean.getSeq());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScBcrossnodeMatchck> list = new ArrayList<ScBcrossnodeMatchck>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScBcrossnodeMatchck model = new ScBcrossnodeMatchck();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setSchematicCode(rs.getString("SCHEMATIC_CODE"));
						model.setArrowCode(rs.getString("ARROW_CODE"));
						model.setSeq(rs.getInt("SEQ"));
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
	public List<ScBcrossnodeMatchck> list(ScBcrossnodeMatchck bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_BCROSSNODE_MATCHCK where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getSchematicCode()!=null && StringUtils.isNotEmpty(bean.getSchematicCode().toString())){
				selectSql+=" and SCHEMATIC_CODE=? ";
				values.add(bean.getSchematicCode());
			};
			if (bean!=null&&bean.getArrowCode()!=null && StringUtils.isNotEmpty(bean.getArrowCode().toString())){
				selectSql+=" and ARROW_CODE=? ";
				values.add(bean.getArrowCode());
			};
			if (bean!=null&&bean.getSeq()!=null && StringUtils.isNotEmpty(bean.getSeq().toString())){
				selectSql+=" and SEQ=? ";
				values.add(bean.getSeq());
			};
			ResultSetHandler<List<ScBcrossnodeMatchck>> rsHandler = new ResultSetHandler<List<ScBcrossnodeMatchck>>(){
				public List<ScBcrossnodeMatchck> handle(ResultSet rs) throws SQLException {
					List<ScBcrossnodeMatchck> list = new ArrayList<ScBcrossnodeMatchck>();
					while(rs.next()){
						ScBcrossnodeMatchck model = new ScBcrossnodeMatchck();
						model.setSchematicCode(rs.getString("SCHEMATIC_CODE"));
						model.setArrowCode(rs.getString("ARROW_CODE"));
						model.setSeq(rs.getInt("SEQ"));
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
	public ScBcrossnodeMatchck query(ScBcrossnodeMatchck bean,Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_BCROSSNODE_MATCHCK where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getSchematicCode()!=null && StringUtils.isNotEmpty(bean.getSchematicCode().toString())){
				selectSql+=" and SCHEMATIC_CODE=? ";
				values.add(bean.getSchematicCode());
			};
			if (bean!=null&&bean.getArrowCode()!=null && StringUtils.isNotEmpty(bean.getArrowCode().toString())){
				selectSql+=" and ARROW_CODE=? ";
				values.add(bean.getArrowCode());
			};
			if (bean!=null&&bean.getSeq()!=null && StringUtils.isNotEmpty(bean.getSeq().toString())){
				selectSql+=" and SEQ=? ";
				values.add(bean.getSeq());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<ScBcrossnodeMatchck> rsHandler = new ResultSetHandler<ScBcrossnodeMatchck>(){
				public ScBcrossnodeMatchck handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScBcrossnodeMatchck model = new ScBcrossnodeMatchck();
						model.setSchematicCode(rs.getString("SCHEMATIC_CODE"));
						model.setArrowCode(rs.getString("ARROW_CODE"));
						model.setSeq(rs.getInt("SEQ"));
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
	public void saveUpdate(JSONObject dataJson) throws Exception {
		Connection conn = null;
		try{
			//持久化
			conn = DBConnector.getInstance().getMetaConnection();
			ScBcrossnodeMatchck bean =(ScBcrossnodeMatchck) JSONObject.toBean(dataJson, ScBcrossnodeMatchck.class);
			
			ScBcrossnodeMatchck newBean = new ScBcrossnodeMatchck();
			newBean.setSeq(bean.getSeq());
			ScBcrossnodeMatchck oldBean = query(newBean,conn);
			if(oldBean != null ){//存在  ,更新数据
				update(bean,conn);
			}else{//不存在  执行新增
				create(bean,conn);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void deleteByIds(JSONArray idsJson) throws ServiceException {
		Connection conn = null;
		try{
			//持久化
			conn = DBConnector.getInstance().getMetaConnection();
			for(Object beanObj : idsJson){
				Integer seq = (Integer) beanObj;
				if(seq != null && seq >=0){
					delete(seq,conn);
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
	public Page list(JSONObject dataJson,final int curPageNum, final int pageSize, String sortby) throws ServiceException {
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			ScBcrossnodeMatchck bean =(ScBcrossnodeMatchck) JSONObject.toBean(dataJson, ScBcrossnodeMatchck.class);
			
			String selectSql = "select * from SC_BCROSSNODE_MATCHCK where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getSchematicCode()!=null && StringUtils.isNotEmpty(bean.getSchematicCode().toString())){
				selectSql+=" and SCHEMATIC_CODE=? ";
				values.add(bean.getSchematicCode());
			};
			if (bean!=null&&bean.getArrowCode()!=null && StringUtils.isNotEmpty(bean.getArrowCode().toString())){
				selectSql+=" and ARROW_CODE=? ";
				values.add(bean.getArrowCode());
			};
			if (bean!=null&&bean.getSeq()!=null && StringUtils.isNotEmpty(bean.getSeq().toString())){
				selectSql+=" and SEQ=? ";
				values.add(bean.getSeq());
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
					List<ScBcrossnodeMatchck> list = new ArrayList<ScBcrossnodeMatchck>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						ScBcrossnodeMatchck model = new ScBcrossnodeMatchck();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setSchematicCode(rs.getString("SCHEMATIC_CODE"));
						model.setArrowCode(rs.getString("ARROW_CODE"));
						model.setSeq(rs.getInt("SEQ"));
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
