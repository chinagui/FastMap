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
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

/** 
* @ClassName:  ScBranchCommcService 
* @author code generator
* @date 2017-03-22 09:23:11 
* @Description: TODO
*/
@Service
public class ScBranchCommcService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScBranchCommc  bean,Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_BRANCH_COMMC (BRANCH_1, BRANCH_2, BRANCH_3, BRANCH_4, BRANCH_5, SERIESBRANCH_1, SERIESBRANCH_2, SERIESBRANCH_3, SERIESBRANCH_4) values(?,?,?,?,?,?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getBranch1() , bean.getBranch2(), bean.getBranch3(), bean.getBranch4(), bean.getBranch5(), bean.getSeriesbranch1(), bean.getSeriesbranch2(), bean.getSeriesbranch3(), bean.getSeriesbranch4()
					   );
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
//		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
//		}
	}
	public void update(ScBranchCommc bean,Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_BRANCH_COMMC set ";
			//"BRANCH_1=?, BRANCH_2=?, BRANCH_3=?, BRANCH_4=?, BRANCH_5=?, 
			//SERIESBRANCH_1=?, SERIESBRANCH_2=?, 
			//SERIESBRANCH_3=?, SERIESBRANCH_4=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			String valueSql = "";
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getBranch3()!=null && StringUtils.isNotEmpty(bean.getBranch3().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  BRANCH_3=? ";
				values.add(bean.getBranch3());
			};
			if (bean!=null&&bean.getBranch4()!=null && StringUtils.isNotEmpty(bean.getBranch4().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  BRANCH_4=? ";
				values.add(bean.getBranch4());
			};
			if (bean!=null&&bean.getBranch5()!=null && StringUtils.isNotEmpty(bean.getBranch5().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  BRANCH_5=? ";
				values.add(bean.getBranch5());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			if (bean!=null&&bean.getSeriesbranch2()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch2().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  SERIESBRANCH_2=? ";
				values.add(bean.getSeriesbranch2());
			};
			if (bean!=null&&bean.getSeriesbranch3()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch3().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  SERIESBRANCH_3=? ";
				values.add(bean.getSeriesbranch3());
			};
			if (bean!=null&&bean.getSeriesbranch4()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch4().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  SERIESBRANCH_4=? ";
				values.add(bean.getSeriesbranch4());
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
			if (bean!=null&&bean.getBranch3()!=null && StringUtils.isNotEmpty(bean.getBranch3().toString())){
				whereSql+=" and BRANCH_3=? ";
				values.add(bean.getBranch3());
			};
			if (bean!=null&&bean.getBranch4()!=null && StringUtils.isNotEmpty(bean.getBranch4().toString())){
				whereSql+=" and BRANCH_4=? ";
				values.add(bean.getBranch4());
			};
			if (bean!=null&&bean.getBranch5()!=null && StringUtils.isNotEmpty(bean.getBranch5().toString())){
				whereSql+=" and BRANCH_5=? ";
				values.add(bean.getBranch5());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				whereSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			if (bean!=null&&bean.getSeriesbranch2()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch2().toString())){
				whereSql+=" and SERIESBRANCH_2=? ";
				values.add(bean.getSeriesbranch2());
			};
			if (bean!=null&&bean.getSeriesbranch3()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch3().toString())){
				whereSql+=" and SERIESBRANCH_3=? ";
				values.add(bean.getSeriesbranch3());
			};
			if (bean!=null&&bean.getSeriesbranch4()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch4().toString())){
				whereSql+=" and SERIESBRANCH_4=? ";
				values.add(bean.getSeriesbranch4());
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
	public void delete(ScBranchCommc bean, Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_BRANCH_COMMC where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				deleteSql+=" and BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				deleteSql+=" and BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getBranch3()!=null && StringUtils.isNotEmpty(bean.getBranch3().toString())){
				deleteSql+=" and BRANCH_3=? ";
				values.add(bean.getBranch3());
			};
			if (bean!=null&&bean.getBranch4()!=null && StringUtils.isNotEmpty(bean.getBranch4().toString())){
				deleteSql+=" and BRANCH_4=? ";
				values.add(bean.getBranch4());
			};
			if (bean!=null&&bean.getBranch5()!=null && StringUtils.isNotEmpty(bean.getBranch5().toString())){
				deleteSql+=" and BRANCH_5=? ";
				values.add(bean.getBranch5());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				deleteSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			if (bean!=null&&bean.getSeriesbranch2()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch2().toString())){
				deleteSql+=" and SERIESBRANCH_2=? ";
				values.add(bean.getSeriesbranch2());
			};
			if (bean!=null&&bean.getSeriesbranch3()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch3().toString())){
				deleteSql+=" and SERIESBRANCH_3=? ";
				values.add(bean.getSeriesbranch3());
			};
			if (bean!=null&&bean.getSeriesbranch4()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch4().toString())){
				deleteSql+=" and SERIESBRANCH_4=? ";
				values.add(bean.getSeriesbranch4());
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
	public Page list(ScBranchCommc bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_BRANCH_COMMC where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				selectSql+=" and BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				selectSql+=" and BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getBranch3()!=null && StringUtils.isNotEmpty(bean.getBranch3().toString())){
				selectSql+=" and BRANCH_3=? ";
				values.add(bean.getBranch3());
			};
			if (bean!=null&&bean.getBranch4()!=null && StringUtils.isNotEmpty(bean.getBranch4().toString())){
				selectSql+=" and BRANCH_4=? ";
				values.add(bean.getBranch4());
			};
			if (bean!=null&&bean.getBranch5()!=null && StringUtils.isNotEmpty(bean.getBranch5().toString())){
				selectSql+=" and BRANCH_5=? ";
				values.add(bean.getBranch5());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				selectSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			if (bean!=null&&bean.getSeriesbranch2()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch2().toString())){
				selectSql+=" and SERIESBRANCH_2=? ";
				values.add(bean.getSeriesbranch2());
			};
			if (bean!=null&&bean.getSeriesbranch3()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch3().toString())){
				selectSql+=" and SERIESBRANCH_3=? ";
				values.add(bean.getSeriesbranch3());
			};
			if (bean!=null&&bean.getSeriesbranch4()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch4().toString())){
				selectSql+=" and SERIESBRANCH_4=? ";
				values.add(bean.getSeriesbranch4());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScBranchCommc> list = new ArrayList<ScBranchCommc>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScBranchCommc model = new ScBranchCommc();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setBranch1(rs.getString("BRANCH_1"));
						model.setBranch2(rs.getString("BRANCH_2"));
						model.setBranch3(rs.getString("BRANCH_3"));
						model.setBranch4(rs.getString("BRANCH_4"));
						model.setBranch5(rs.getString("BRANCH_5"));
						model.setSeriesbranch1(rs.getString("SERIESBRANCH_1"));
						model.setSeriesbranch2(rs.getString("SERIESBRANCH_2"));
						model.setSeriesbranch3(rs.getString("SERIESBRANCH_3"));
						model.setSeriesbranch4(rs.getString("SERIESBRANCH_4"));
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
	public List<ScBranchCommc> list(ScBranchCommc bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_BRANCH_COMMC where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				selectSql+=" and BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				selectSql+=" and BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getBranch3()!=null && StringUtils.isNotEmpty(bean.getBranch3().toString())){
				selectSql+=" and BRANCH_3=? ";
				values.add(bean.getBranch3());
			};
			if (bean!=null&&bean.getBranch4()!=null && StringUtils.isNotEmpty(bean.getBranch4().toString())){
				selectSql+=" and BRANCH_4=? ";
				values.add(bean.getBranch4());
			};
			if (bean!=null&&bean.getBranch5()!=null && StringUtils.isNotEmpty(bean.getBranch5().toString())){
				selectSql+=" and BRANCH_5=? ";
				values.add(bean.getBranch5());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				selectSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			if (bean!=null&&bean.getSeriesbranch2()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch2().toString())){
				selectSql+=" and SERIESBRANCH_2=? ";
				values.add(bean.getSeriesbranch2());
			};
			if (bean!=null&&bean.getSeriesbranch3()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch3().toString())){
				selectSql+=" and SERIESBRANCH_3=? ";
				values.add(bean.getSeriesbranch3());
			};
			if (bean!=null&&bean.getSeriesbranch4()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch4().toString())){
				selectSql+=" and SERIESBRANCH_4=? ";
				values.add(bean.getSeriesbranch4());
			};
			ResultSetHandler<List<ScBranchCommc>> rsHandler = new ResultSetHandler<List<ScBranchCommc>>(){
				public List<ScBranchCommc> handle(ResultSet rs) throws SQLException {
					List<ScBranchCommc> list = new ArrayList<ScBranchCommc>();
					while(rs.next()){
						ScBranchCommc model = new ScBranchCommc();
						model.setBranch1(rs.getString("BRANCH_1"));
						model.setBranch2(rs.getString("BRANCH_2"));
						model.setBranch3(rs.getString("BRANCH_3"));
						model.setBranch4(rs.getString("BRANCH_4"));
						model.setBranch5(rs.getString("BRANCH_5"));
						model.setSeriesbranch1(rs.getString("SERIESBRANCH_1"));
						model.setSeriesbranch2(rs.getString("SERIESBRANCH_2"));
						model.setSeriesbranch3(rs.getString("SERIESBRANCH_3"));
						model.setSeriesbranch4(rs.getString("SERIESBRANCH_4"));
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
	public ScBranchCommc query(ScBranchCommc bean,Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_BRANCH_COMMC where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				selectSql+=" and BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				selectSql+=" and BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getBranch3()!=null && StringUtils.isNotEmpty(bean.getBranch3().toString())){
				selectSql+=" and BRANCH_3=? ";
				values.add(bean.getBranch3());
			};
			if (bean!=null&&bean.getBranch4()!=null && StringUtils.isNotEmpty(bean.getBranch4().toString())){
				selectSql+=" and BRANCH_4=? ";
				values.add(bean.getBranch4());
			};
			if (bean!=null&&bean.getBranch5()!=null && StringUtils.isNotEmpty(bean.getBranch5().toString())){
				selectSql+=" and BRANCH_5=? ";
				values.add(bean.getBranch5());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				selectSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			if (bean!=null&&bean.getSeriesbranch2()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch2().toString())){
				selectSql+=" and SERIESBRANCH_2=? ";
				values.add(bean.getSeriesbranch2());
			};
			if (bean!=null&&bean.getSeriesbranch3()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch3().toString())){
				selectSql+=" and SERIESBRANCH_3=? ";
				values.add(bean.getSeriesbranch3());
			};
			if (bean!=null&&bean.getSeriesbranch4()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch4().toString())){
				selectSql+=" and SERIESBRANCH_4=? ";
				values.add(bean.getSeriesbranch4());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<ScBranchCommc> rsHandler = new ResultSetHandler<ScBranchCommc>(){
				public ScBranchCommc handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScBranchCommc model = new ScBranchCommc();
						model.setBranch1(rs.getString("BRANCH_1"));
						model.setBranch2(rs.getString("BRANCH_2"));
						model.setBranch3(rs.getString("BRANCH_3"));
						model.setBranch4(rs.getString("BRANCH_4"));
						model.setBranch5(rs.getString("BRANCH_5"));
						model.setSeriesbranch1(rs.getString("SERIESBRANCH_1"));
						model.setSeriesbranch2(rs.getString("SERIESBRANCH_2"));
						model.setSeriesbranch3(rs.getString("SERIESBRANCH_3"));
						model.setSeriesbranch4(rs.getString("SERIESBRANCH_4"));
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
			conn = DBConnector.getInstance().getMetaConnection();
			
			ScBranchCommc bean =(ScBranchCommc) JSONObject.toBean(dataJson, ScBranchCommc.class);
			ScBranchCommc oldBean = query(bean,conn);
			if(oldBean != null ){//存在  ,更新数据
				update(bean,conn);
			}else{//不存在  执行新增
				create(bean,conn);
			}
		
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("操作失败，原因为:"+e.getMessage(),e);
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
				ScBranchCommc bean =(ScBranchCommc) JSONObject.toBean(beanJson, ScBranchCommc.class);
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
			ScBranchCommc bean =(ScBranchCommc) JSONObject.toBean(dataJson, ScBranchCommc.class);
			
			String selectSql = "select * from SC_BRANCH_COMMC where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getBranch1()!=null && StringUtils.isNotEmpty(bean.getBranch1().toString())){
				selectSql+=" and BRANCH_1=? ";
				values.add(bean.getBranch1());
			};
			if (bean!=null&&bean.getBranch2()!=null && StringUtils.isNotEmpty(bean.getBranch2().toString())){
				selectSql+=" and BRANCH_2=? ";
				values.add(bean.getBranch2());
			};
			if (bean!=null&&bean.getBranch3()!=null && StringUtils.isNotEmpty(bean.getBranch3().toString())){
				selectSql+=" and BRANCH_3=? ";
				values.add(bean.getBranch3());
			};
			if (bean!=null&&bean.getBranch4()!=null && StringUtils.isNotEmpty(bean.getBranch4().toString())){
				selectSql+=" and BRANCH_4=? ";
				values.add(bean.getBranch4());
			};
			if (bean!=null&&bean.getBranch5()!=null && StringUtils.isNotEmpty(bean.getBranch5().toString())){
				selectSql+=" and BRANCH_5=? ";
				values.add(bean.getBranch5());
			};
			if (bean!=null&&bean.getSeriesbranch1()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch1().toString())){
				selectSql+=" and SERIESBRANCH_1=? ";
				values.add(bean.getSeriesbranch1());
			};
			if (bean!=null&&bean.getSeriesbranch2()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch2().toString())){
				selectSql+=" and SERIESBRANCH_2=? ";
				values.add(bean.getSeriesbranch2());
			};
			if (bean!=null&&bean.getSeriesbranch3()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch3().toString())){
				selectSql+=" and SERIESBRANCH_3=? ";
				values.add(bean.getSeriesbranch3());
			};
			if (bean!=null&&bean.getSeriesbranch4()!=null && StringUtils.isNotEmpty(bean.getSeriesbranch4().toString())){
				selectSql+=" and SERIESBRANCH_4=? ";
				values.add(bean.getSeriesbranch4());
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
					List<ScBranchCommc> list = new ArrayList<ScBranchCommc>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						ScBranchCommc model = new ScBranchCommc();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setBranch1(rs.getString("BRANCH_1"));
						model.setBranch2(rs.getString("BRANCH_2"));
						model.setBranch3(rs.getString("BRANCH_3"));
						model.setBranch4(rs.getString("BRANCH_4"));
						model.setBranch5(rs.getString("BRANCH_5"));
						model.setSeriesbranch1(rs.getString("SERIESBRANCH_1"));
						model.setSeriesbranch2(rs.getString("SERIESBRANCH_2"));
						model.setSeriesbranch3(rs.getString("SERIESBRANCH_3"));
						model.setSeriesbranch4(rs.getString("SERIESBRANCH_4"));
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
