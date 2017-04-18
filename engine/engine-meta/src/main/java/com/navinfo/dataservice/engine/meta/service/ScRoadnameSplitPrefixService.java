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
import com.navinfo.dataservice.engine.meta.model.ScRoadnameSplitPrefix;
import com.navinfo.dataservice.engine.meta.model.ScRoadnameSuffix;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

/** 
* @ClassName:  ScRoadnameSplitPrefixService 
* @author code generator
* @date 2017-03-23 07:07:40 
* @Description: TODO
*/
@Service
public class ScRoadnameSplitPrefixService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScRoadnameSplitPrefix  bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_ROADNAME_SPLIT_PREFIX (ID, WORD_CAN_SPLIT, WORD_CANNOT_SPLIT, REGION_FLAG, LANG_CODE) values(?,?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getId() , bean.getWordCanSplit(), bean.getWordCannotSplit(), bean.getRegionFlag(), bean.getLangCode()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(ScRoadnameSplitPrefix bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_ROADNAME_SPLIT_PREFIX set ID=?, WORD_CAN_SPLIT=?, WORD_CANNOT_SPLIT=?, REGION_FLAG=?, LANG_CODE=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				updateSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getWordCanSplit()!=null && StringUtils.isNotEmpty(bean.getWordCanSplit().toString())){
				updateSql+=" and WORD_CAN_SPLIT=? ";
				values.add(bean.getWordCanSplit());
			};
			if (bean!=null&&bean.getWordCannotSplit()!=null && StringUtils.isNotEmpty(bean.getWordCannotSplit().toString())){
				updateSql+=" and WORD_CANNOT_SPLIT=? ";
				values.add(bean.getWordCannotSplit());
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
	public void delete(ScRoadnameSplitPrefix bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_ROADNAME_SPLIT_PREFIX where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				deleteSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getWordCanSplit()!=null && StringUtils.isNotEmpty(bean.getWordCanSplit().toString())){
				deleteSql+=" and WORD_CAN_SPLIT=? ";
				values.add(bean.getWordCanSplit());
			};
			if (bean!=null&&bean.getWordCannotSplit()!=null && StringUtils.isNotEmpty(bean.getWordCannotSplit().toString())){
				deleteSql+=" and WORD_CANNOT_SPLIT=? ";
				values.add(bean.getWordCannotSplit());
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
	public Page list(ScRoadnameSplitPrefix bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_ROADNAME_SPLIT_PREFIX where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getWordCanSplit()!=null && StringUtils.isNotEmpty(bean.getWordCanSplit().toString())){
				selectSql+=" and WORD_CAN_SPLIT=? ";
				values.add(bean.getWordCanSplit());
			};
			if (bean!=null&&bean.getWordCannotSplit()!=null && StringUtils.isNotEmpty(bean.getWordCannotSplit().toString())){
				selectSql+=" and WORD_CANNOT_SPLIT=? ";
				values.add(bean.getWordCannotSplit());
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
					List<ScRoadnameSplitPrefix> list = new ArrayList<ScRoadnameSplitPrefix>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScRoadnameSplitPrefix model = new ScRoadnameSplitPrefix();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setId(rs.getInt("ID"));
						model.setWordCanSplit(rs.getString("WORD_CAN_SPLIT"));
						model.setWordCannotSplit(rs.getString("WORD_CANNOT_SPLIT"));
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
			ScRoadnameSplitPrefix bean =(ScRoadnameSplitPrefix) JSONObject.toBean(dataJson, ScRoadnameSplitPrefix.class);
			
			String selectSql = "select * from SC_ROADNAME_SPLIT_PREFIX where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getWordCanSplit()!=null && StringUtils.isNotEmpty(bean.getWordCanSplit().toString())){
				selectSql+=" and WORD_CAN_SPLIT=? ";
				values.add(bean.getWordCanSplit());
			};
			if (bean!=null&&bean.getWordCannotSplit()!=null && StringUtils.isNotEmpty(bean.getWordCannotSplit().toString())){
				selectSql+=" and WORD_CANNOT_SPLIT=? ";
				values.add(bean.getWordCannotSplit());
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
					List<ScRoadnameSplitPrefix> list = new ArrayList<ScRoadnameSplitPrefix>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						ScRoadnameSplitPrefix model = new ScRoadnameSplitPrefix();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setId(rs.getInt("ID"));
						model.setWordCanSplit(rs.getString("WORD_CAN_SPLIT"));
						model.setWordCannotSplit(rs.getString("WORD_CANNOT_SPLIT"));
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
	
	public List<ScRoadnameSplitPrefix> list(ScRoadnameSplitPrefix bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_ROADNAME_SPLIT_PREFIX where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getWordCanSplit()!=null && StringUtils.isNotEmpty(bean.getWordCanSplit().toString())){
				selectSql+=" and WORD_CAN_SPLIT=? ";
				values.add(bean.getWordCanSplit());
			};
			if (bean!=null&&bean.getWordCannotSplit()!=null && StringUtils.isNotEmpty(bean.getWordCannotSplit().toString())){
				selectSql+=" and WORD_CANNOT_SPLIT=? ";
				values.add(bean.getWordCannotSplit());
			};
			if (bean!=null&&bean.getRegionFlag()!=null && StringUtils.isNotEmpty(bean.getRegionFlag().toString())){
				selectSql+=" and REGION_FLAG=? ";
				values.add(bean.getRegionFlag());
			};
			if (bean!=null&&bean.getLangCode()!=null && StringUtils.isNotEmpty(bean.getLangCode().toString())){
				selectSql+=" and LANG_CODE=? ";
				values.add(bean.getLangCode());
			};
			ResultSetHandler<List<ScRoadnameSplitPrefix>> rsHandler = new ResultSetHandler<List<ScRoadnameSplitPrefix>>(){
				public List<ScRoadnameSplitPrefix> handle(ResultSet rs) throws SQLException {
					List<ScRoadnameSplitPrefix> list = new ArrayList<ScRoadnameSplitPrefix>();
					while(rs.next()){
						ScRoadnameSplitPrefix model = new ScRoadnameSplitPrefix();
						model.setId(rs.getInt("ID"));
						model.setWordCanSplit(rs.getString("WORD_CAN_SPLIT"));
						model.setWordCannotSplit(rs.getString("WORD_CANNOT_SPLIT"));
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
	public ScRoadnameSplitPrefix query(ScRoadnameSplitPrefix bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_ROADNAME_SPLIT_PREFIX where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getId()!=null && StringUtils.isNotEmpty(bean.getId().toString())){
				selectSql+=" and ID=? ";
				values.add(bean.getId());
			};
			if (bean!=null&&bean.getWordCanSplit()!=null && StringUtils.isNotEmpty(bean.getWordCanSplit().toString())){
				selectSql+=" and WORD_CAN_SPLIT=? ";
				values.add(bean.getWordCanSplit());
			};
			if (bean!=null&&bean.getWordCannotSplit()!=null && StringUtils.isNotEmpty(bean.getWordCannotSplit().toString())){
				selectSql+=" and WORD_CANNOT_SPLIT=? ";
				values.add(bean.getWordCannotSplit());
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
			ResultSetHandler<ScRoadnameSplitPrefix> rsHandler = new ResultSetHandler<ScRoadnameSplitPrefix>(){
				public ScRoadnameSplitPrefix handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScRoadnameSplitPrefix model = new ScRoadnameSplitPrefix();
						model.setId(rs.getInt("ID"));
						model.setWordCanSplit(rs.getString("WORD_CAN_SPLIT"));
						model.setWordCannotSplit(rs.getString("WORD_CANNOT_SPLIT"));
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
		ScRoadnameSplitPrefix bean =(ScRoadnameSplitPrefix) JSONObject.toBean(dataJson, ScRoadnameSplitPrefix.class);
		if(bean.getWordCanSplit() != null && StringUtils.isNotEmpty(bean.getWordCanSplit()) 
				&& bean.getLangCode() != null && StringUtils.isNotEmpty(bean.getLangCode())){
			ScRoadnameSplitPrefix newBean = new ScRoadnameSplitPrefix();
			newBean.setWordCannotSplit( bean.getWordCanSplit());
			newBean.setLangCode(bean.getLangCode());
			ScRoadnameSplitPrefix oldBean = query(newBean);
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
			if(idsObj.containsKey("wordCannotSplit") && idsObj.getString("wordCannotSplit") != null && StringUtils.isNotEmpty(idsObj.getString("name")) 
					&& idsObj.containsKey("langCode") && idsObj.getString("langCode") != null && StringUtils.isNotEmpty(idsObj.getString("langCode"))){
				String wordCannotSplit = idsObj.getString("wordCannotSplit");
				String langCode = idsObj.getString("langCode");
				
				ScRoadnameSplitPrefix newBean = new ScRoadnameSplitPrefix();
				newBean.setWordCanSplit(wordCannotSplit);
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
