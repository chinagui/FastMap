package com.navinfo.dataservice.engine.meta.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import com.navinfo.dataservice.engine.meta.model.ScModelMatchG;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.json.TimestampMorpher;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtilsEx;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import oracle.sql.BLOB;
import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

/** 
* @ClassName:  ScModelMatchGService 
* @author code generator
* @date 2017-03-22 09:13:09 
* @Description: TODO
*/
@Service
public class ScModelMatchGService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScModelMatchG  bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_MODEL_MATCH_G (FILE_ID, PRODUCT_LINE, VERSION, PROJECT_NM, SPECIFICATION, B_TYPE, M_TYPE, S_TYPE, FILE_NAME, \"SIZE\", FORMAT, IMP_WORKER, IMP_DATE, URL_DB, URL_FILE, MEMO, FILE_CONTENT, UPDATE_TIME) values(?,?,?,?,?,?,?,?,?,?,?,?,TO_DATE(?,'yyyy-MM-dd HH24:MI:ss'),?,?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getFileId() , bean.getProductLine(), bean.getVersion(), bean.getProjectNm(), bean.getSpecification(), bean.getbType(), bean.getmType(), bean.getsType(), bean.getFileName(), bean.getSize(), bean.getFormat(), bean.getImpWorker(), bean.getImpDate(), bean.getUrlDb(), bean.getUrlFile(), bean.getMemo(), bean.getFileContent(), bean.getUpdateTime()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void updateByFileId(ScModelMatchG bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_MODEL_MATCH_G set ";
			String valueSql = "";
//"FILE_ID=?, PRODUCT_LINE=?, VERSION=?, PROJECT_NM=?, SPECIFICATION=?, B_TYPE=?, M_TYPE=?, S_TYPE=?, FILE_NAME=?, \"SIZE\"=?, FORMAT=?, IMP_WORKER=?, IMP_DATE=?, URL_DB=?, URL_FILE=?, MEMO=?, FILE_CONTENT=?, FILE_TYPE=?, UPDATE_TIME=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			
			if (bean!=null&&bean.getProductLine()!=null && StringUtils.isNotEmpty(bean.getProductLine().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+=" PRODUCT_LINE=? ";
				values.add(bean.getProductLine());
			};
			if (bean!=null&&bean.getVersion()!=null && StringUtils.isNotEmpty(bean.getVersion().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+=" VERSION=? ";
				values.add(bean.getVersion());
			};
			if (bean!=null&&bean.getProjectNm()!=null && StringUtils.isNotEmpty(bean.getProjectNm().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  PROJECT_NM=? ";
				values.add(bean.getProjectNm());
			};
			if (bean!=null&&bean.getSpecification()!=null && StringUtils.isNotEmpty(bean.getSpecification().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+=" SPECIFICATION=? ";
				values.add(bean.getSpecification());
			};
			if (bean!=null&&bean.getbType()!=null && StringUtils.isNotEmpty(bean.getbType().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+=" B_TYPE=? ";
				values.add(bean.getbType());
			};
			if (bean!=null&&bean.getmType()!=null && StringUtils.isNotEmpty(bean.getmType().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  M_TYPE=? ";
				values.add(bean.getmType());
			};
			if (bean!=null&&bean.getsType()!=null && StringUtils.isNotEmpty(bean.getsType().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  S_TYPE=? ";
				values.add(bean.getsType());
			};
			if (bean!=null&&bean.getFileName()!=null && StringUtils.isNotEmpty(bean.getFileName().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  FILE_NAME=? ";
				values.add(bean.getFileName());
			};
			if (bean!=null&&bean.getSize()!=null && StringUtils.isNotEmpty(bean.getSize().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  \"SIZE\"=? ";
				values.add(bean.getSize());
			};
			if (bean!=null&&bean.getFormat()!=null && StringUtils.isNotEmpty(bean.getFormat().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  FORMAT=? ";
				values.add(bean.getFormat());
			};
			if (bean!=null&&bean.getImpWorker()!=null && StringUtils.isNotEmpty(bean.getImpWorker().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  IMP_WORKER=? ";
				values.add(bean.getImpWorker());
			};
			if (bean!=null&&bean.getImpDate()!=null && StringUtils.isNotEmpty(bean.getImpDate().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+=" IMP_DATE=TO_DATE(?,'yyyy-MM-dd HH24:MI:ss') ";
//				valueSql+="  IMP_DATE=? ";
				values.add(bean.getImpDate());
			};
			if (bean!=null&&bean.getUrlDb()!=null && StringUtils.isNotEmpty(bean.getUrlDb().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  URL_DB=? ";
				values.add(bean.getUrlDb());
			};
			if (bean!=null&&bean.getUrlFile()!=null && StringUtils.isNotEmpty(bean.getUrlFile().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  URL_FILE=? ";
				values.add(bean.getUrlFile());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getFileContent()!=null && StringUtils.isNotEmpty(bean.getFileContent().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  FILE_CONTENT=? ";
				values.add(bean.getFileContent());
			};
			if (bean!=null&&bean.getUpdateTime()!=null && StringUtils.isNotEmpty(bean.getUpdateTime().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  UPDATE_TIME=? ";
				values.add(bean.getUpdateTime());
			};
			if (bean!=null&&bean.getFileId()!=null && StringUtils.isNotEmpty(bean.getFileId().toString())){
				valueSql+=" where   FILE_ID=? ";
				values.add(bean.getFileId());
			};
			log.info("sql: "+updateSql+valueSql);
			run.update(conn, 
					   updateSql+valueSql, 
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
	public void update(ScModelMatchG bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_MODEL_MATCH_G set FILE_ID=?, PRODUCT_LINE=?, VERSION=?, PROJECT_NM=?, SPECIFICATION=?, B_TYPE=?, M_TYPE=?, S_TYPE=?, FILE_NAME=?, \"SIZE\"=?, FORMAT=?, IMP_WORKER=?, IMP_DATE=?, URL_DB=?, URL_FILE=?, MEMO=?, FILE_CONTENT=?, UPDATE_TIME=? where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getFileId()!=null && StringUtils.isNotEmpty(bean.getFileId().toString())){
				updateSql+=" and FILE_ID=? ";
				values.add(bean.getFileId());
			};
			if (bean!=null&&bean.getProductLine()!=null && StringUtils.isNotEmpty(bean.getProductLine().toString())){
				updateSql+=" and PRODUCT_LINE=? ";
				values.add(bean.getProductLine());
			};
			if (bean!=null&&bean.getVersion()!=null && StringUtils.isNotEmpty(bean.getVersion().toString())){
				updateSql+=" and VERSION=? ";
				values.add(bean.getVersion());
			};
			if (bean!=null&&bean.getProjectNm()!=null && StringUtils.isNotEmpty(bean.getProjectNm().toString())){
				updateSql+=" and PROJECT_NM=? ";
				values.add(bean.getProjectNm());
			};
			if (bean!=null&&bean.getSpecification()!=null && StringUtils.isNotEmpty(bean.getSpecification().toString())){
				updateSql+=" and SPECIFICATION=? ";
				values.add(bean.getSpecification());
			};
			if (bean!=null&&bean.getbType()!=null && StringUtils.isNotEmpty(bean.getbType().toString())){
				updateSql+=" and B_TYPE=? ";
				values.add(bean.getbType());
			};
			if (bean!=null&&bean.getmType()!=null && StringUtils.isNotEmpty(bean.getmType().toString())){
				updateSql+=" and M_TYPE=? ";
				values.add(bean.getmType());
			};
			if (bean!=null&&bean.getsType()!=null && StringUtils.isNotEmpty(bean.getsType().toString())){
				updateSql+=" and S_TYPE=? ";
				values.add(bean.getsType());
			};
			if (bean!=null&&bean.getFileName()!=null && StringUtils.isNotEmpty(bean.getFileName().toString())){
				updateSql+=" and FILE_NAME=? ";
				values.add(bean.getFileName());
			};
			if (bean!=null&&bean.getSize()!=null && StringUtils.isNotEmpty(bean.getSize().toString())){
				updateSql+=" and \"SIZE\"=? ";
				values.add(bean.getSize());
			};
			if (bean!=null&&bean.getFormat()!=null && StringUtils.isNotEmpty(bean.getFormat().toString())){
				updateSql+=" and FORMAT=? ";
				values.add(bean.getFormat());
			};
			if (bean!=null&&bean.getImpWorker()!=null && StringUtils.isNotEmpty(bean.getImpWorker().toString())){
				updateSql+=" and IMP_WORKER=? ";
				values.add(bean.getImpWorker());
			};
			if (bean!=null&&bean.getImpDate()!=null && StringUtils.isNotEmpty(bean.getImpDate().toString())){
				updateSql+=" and IMP_DATE=TO_DATE(?,'yyyy-MM-dd HH24:MI:ss') ";
//				updateSql+=" and IMP_DATE=? ";
				values.add(bean.getImpDate());
			};
			if (bean!=null&&bean.getUrlDb()!=null && StringUtils.isNotEmpty(bean.getUrlDb().toString())){
				updateSql+=" and URL_DB=? ";
				values.add(bean.getUrlDb());
			};
			if (bean!=null&&bean.getUrlFile()!=null && StringUtils.isNotEmpty(bean.getUrlFile().toString())){
				updateSql+=" and URL_FILE=? ";
				values.add(bean.getUrlFile());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				updateSql+=" and MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getFileContent()!=null && StringUtils.isNotEmpty(bean.getFileContent().toString())){
				updateSql+=" and FILE_CONTENT=? ";
				values.add(bean.getFileContent());
			};
			if (bean!=null&&bean.getUpdateTime()!=null && StringUtils.isNotEmpty(bean.getUpdateTime().toString())){
				updateSql+=" and UPDATE_TIME=? ";
				values.add(bean.getUpdateTime());
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
	public void delete(ScModelMatchG bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_MODEL_MATCH_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getFileId()!=null && StringUtils.isNotEmpty(bean.getFileId().toString())){
				deleteSql+=" and FILE_ID=? ";
				values.add(bean.getFileId());
			};
			if (bean!=null&&bean.getProductLine()!=null && StringUtils.isNotEmpty(bean.getProductLine().toString())){
				deleteSql+=" and PRODUCT_LINE=? ";
				values.add(bean.getProductLine());
			};
			if (bean!=null&&bean.getVersion()!=null && StringUtils.isNotEmpty(bean.getVersion().toString())){
				deleteSql+=" and VERSION=? ";
				values.add(bean.getVersion());
			};
			if (bean!=null&&bean.getProjectNm()!=null && StringUtils.isNotEmpty(bean.getProjectNm().toString())){
				deleteSql+=" and PROJECT_NM=? ";
				values.add(bean.getProjectNm());
			};
			if (bean!=null&&bean.getSpecification()!=null && StringUtils.isNotEmpty(bean.getSpecification().toString())){
				deleteSql+=" and SPECIFICATION=? ";
				values.add(bean.getSpecification());
			};
			if (bean!=null&&bean.getbType()!=null && StringUtils.isNotEmpty(bean.getbType().toString())){
				deleteSql+=" and B_TYPE=? ";
				values.add(bean.getbType());
			};
			if (bean!=null&&bean.getmType()!=null && StringUtils.isNotEmpty(bean.getmType().toString())){
				deleteSql+=" and M_TYPE=? ";
				values.add(bean.getmType());
			};
			if (bean!=null&&bean.getsType()!=null && StringUtils.isNotEmpty(bean.getsType().toString())){
				deleteSql+=" and S_TYPE=? ";
				values.add(bean.getsType());
			};
			if (bean!=null&&bean.getFileName()!=null && StringUtils.isNotEmpty(bean.getFileName().toString())){
				deleteSql+=" and FILE_NAME=? ";
				values.add(bean.getFileName());
			};
			if (bean!=null&&bean.getSize()!=null && StringUtils.isNotEmpty(bean.getSize().toString())){
				deleteSql+=" and \"SIZE\"=? ";
				values.add(bean.getSize());
			};
			if (bean!=null&&bean.getFormat()!=null && StringUtils.isNotEmpty(bean.getFormat().toString())){
				deleteSql+=" and FORMAT=? ";
				values.add(bean.getFormat());
			};
			if (bean!=null&&bean.getImpWorker()!=null && StringUtils.isNotEmpty(bean.getImpWorker().toString())){
				deleteSql+=" and IMP_WORKER=? ";
				values.add(bean.getImpWorker());
			};
			if (bean!=null&&bean.getImpDate()!=null && StringUtils.isNotEmpty(bean.getImpDate().toString())){
//				deleteSql+=" and IMP_DATE=? ";
				deleteSql+=" and IMP_DATE=TO_DATE(?,'yyyy-MM-dd HH24:MI:ss') ";
				values.add(bean.getImpDate());
			};
			if (bean!=null&&bean.getUrlDb()!=null && StringUtils.isNotEmpty(bean.getUrlDb().toString())){
				deleteSql+=" and URL_DB=? ";
				values.add(bean.getUrlDb());
			};
			if (bean!=null&&bean.getUrlFile()!=null && StringUtils.isNotEmpty(bean.getUrlFile().toString())){
				deleteSql+=" and URL_FILE=? ";
				values.add(bean.getUrlFile());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				deleteSql+=" and MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getFileContent()!=null && StringUtils.isNotEmpty(bean.getFileContent().toString())){
				deleteSql+=" and FILE_CONTENT=? ";
				values.add(bean.getFileContent());
			};
			if (bean!=null&&bean.getUpdateTime()!=null && StringUtils.isNotEmpty(bean.getUpdateTime().toString())){
				deleteSql+=" and UPDATE_TIME=? ";
				values.add(bean.getUpdateTime());
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
	public void delete(Long fileId, Connection conn)throws ServiceException, SQLException{
//		Connection conn = null;
//		try{
//			//持久化
			QueryRunner run = new QueryRunner();
//			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_MODEL_MATCH_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (fileId!=null){
				deleteSql+=" and FILE_ID=? ";
				values.add(fileId);
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
	public Page list(ScModelMatchG bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_MODEL_MATCH_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getFileId()!=null && StringUtils.isNotEmpty(bean.getFileId().toString())){
				selectSql+=" and FILE_ID=? ";
				values.add(bean.getFileId());
			};
			if (bean!=null&&bean.getProductLine()!=null && StringUtils.isNotEmpty(bean.getProductLine().toString())){
				selectSql+=" and PRODUCT_LINE=? ";
				values.add(bean.getProductLine());
			};
			if (bean!=null&&bean.getVersion()!=null && StringUtils.isNotEmpty(bean.getVersion().toString())){
				selectSql+=" and VERSION=? ";
				values.add(bean.getVersion());
			};
			if (bean!=null&&bean.getProjectNm()!=null && StringUtils.isNotEmpty(bean.getProjectNm().toString())){
				selectSql+=" and PROJECT_NM=? ";
				values.add(bean.getProjectNm());
			};
			if (bean!=null&&bean.getSpecification()!=null && StringUtils.isNotEmpty(bean.getSpecification().toString())){
				selectSql+=" and SPECIFICATION=? ";
				values.add(bean.getSpecification());
			};
			if (bean!=null&&bean.getbType()!=null && StringUtils.isNotEmpty(bean.getbType().toString())){
				selectSql+=" and B_TYPE=? ";
				values.add(bean.getbType());
			};
			if (bean!=null&&bean.getmType()!=null && StringUtils.isNotEmpty(bean.getmType().toString())){
				selectSql+=" and M_TYPE=? ";
				values.add(bean.getmType());
			};
			if (bean!=null&&bean.getsType()!=null && StringUtils.isNotEmpty(bean.getsType().toString())){
				selectSql+=" and S_TYPE=? ";
				values.add(bean.getsType());
			};
			if (bean!=null&&bean.getFileName()!=null && StringUtils.isNotEmpty(bean.getFileName().toString())){
				selectSql+=" and FILE_NAME=? ";
				values.add(bean.getFileName());
			};
			if (bean!=null&&bean.getSize()!=null && StringUtils.isNotEmpty(bean.getSize().toString())){
				selectSql+=" and \"SIZE\"=? ";
				values.add(bean.getSize());
			};
			if (bean!=null&&bean.getFormat()!=null && StringUtils.isNotEmpty(bean.getFormat().toString())){
				selectSql+=" and FORMAT=? ";
				values.add(bean.getFormat());
			};
			if (bean!=null&&bean.getImpWorker()!=null && StringUtils.isNotEmpty(bean.getImpWorker().toString())){
				selectSql+=" and IMP_WORKER=? ";
				values.add(bean.getImpWorker());
			};
			if (bean!=null&&bean.getImpDate()!=null && StringUtils.isNotEmpty(bean.getImpDate().toString())){
				selectSql+=" and IMP_DATE=TO_DATE(?,'yyyy-MM-dd HH24:MI:ss') ";
//				selectSql+=" and IMP_DATE=? ";
				values.add(bean.getImpDate());
			};
			if (bean!=null&&bean.getUrlDb()!=null && StringUtils.isNotEmpty(bean.getUrlDb().toString())){
				selectSql+=" and URL_DB=? ";
				values.add(bean.getUrlDb());
			};
			if (bean!=null&&bean.getUrlFile()!=null && StringUtils.isNotEmpty(bean.getUrlFile().toString())){
				selectSql+=" and URL_FILE=? ";
				values.add(bean.getUrlFile());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				selectSql+=" and MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getFileContent()!=null && StringUtils.isNotEmpty(bean.getFileContent().toString())){
				selectSql+=" and FILE_CONTENT=? ";
				values.add(bean.getFileContent());
			};
			if (bean!=null&&bean.getUpdateTime()!=null && StringUtils.isNotEmpty(bean.getUpdateTime().toString())){
				selectSql+=" and UPDATE_TIME=? ";
				values.add(bean.getUpdateTime());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScModelMatchG> list = new ArrayList<ScModelMatchG>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScModelMatchG model = new ScModelMatchG();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setFileId(rs.getLong("FILE_ID"));
						model.setProductLine(rs.getString("PRODUCT_LINE"));
						model.setVersion(rs.getString("VERSION"));
						model.setProjectNm(rs.getString("PROJECT_NM"));
						model.setSpecification(rs.getString("SPECIFICATION"));
						model.setbType(rs.getString("B_TYPE"));
						model.setmType(rs.getString("M_TYPE"));
						model.setsType(rs.getString("S_TYPE"));
						model.setFileName(rs.getString("FILE_NAME"));
						model.setSize(rs.getString("SIZE"));
						model.setFormat(rs.getString("FORMAT"));
						model.setImpWorker(rs.getString("IMP_WORKER"));
						
						String impDateStr = DateUtilsEx.getTimeStr(rs.getTimestamp("IMP_DATE"), "yyyy-MM-dd HH:mm:ss");
						System.out.println(impDateStr);
						model.setImpDate(impDateStr);
						model.setImpDate(impDateStr);
						model.setUrlDb(rs.getString("URL_DB"));
						model.setUrlFile(rs.getString("URL_FILE"));
						model.setMemo(rs.getString("MEMO"));
						model.setFileContent((BLOB) rs.getBlob("FILE_CONTENT"));
						model.setUpdateTime(rs.getString("UPDATE_TIME"));
						list.add(model);
					}
					page.setResult(list);
					return page;
				}
	    		
	    	};
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
	public List<ScModelMatchG> list(ScModelMatchG bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_MODEL_MATCH_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getFileId()!=null && StringUtils.isNotEmpty(bean.getFileId().toString())){
				selectSql+=" and FILE_ID=? ";
				values.add(bean.getFileId());
			};
			if (bean!=null&&bean.getProductLine()!=null && StringUtils.isNotEmpty(bean.getProductLine().toString())){
				selectSql+=" and PRODUCT_LINE=? ";
				values.add(bean.getProductLine());
			};
			if (bean!=null&&bean.getVersion()!=null && StringUtils.isNotEmpty(bean.getVersion().toString())){
				selectSql+=" and VERSION=? ";
				values.add(bean.getVersion());
			};
			if (bean!=null&&bean.getProjectNm()!=null && StringUtils.isNotEmpty(bean.getProjectNm().toString())){
				selectSql+=" and PROJECT_NM=? ";
				values.add(bean.getProjectNm());
			};
			if (bean!=null&&bean.getSpecification()!=null && StringUtils.isNotEmpty(bean.getSpecification().toString())){
				selectSql+=" and SPECIFICATION=? ";
				values.add(bean.getSpecification());
			};
			if (bean!=null&&bean.getbType()!=null && StringUtils.isNotEmpty(bean.getbType().toString())){
				selectSql+=" and B_TYPE=? ";
				values.add(bean.getbType());
			};
			if (bean!=null&&bean.getmType()!=null && StringUtils.isNotEmpty(bean.getmType().toString())){
				selectSql+=" and M_TYPE=? ";
				values.add(bean.getmType());
			};
			if (bean!=null&&bean.getsType()!=null && StringUtils.isNotEmpty(bean.getsType().toString())){
				selectSql+=" and S_TYPE=? ";
				values.add(bean.getsType());
			};
			if (bean!=null&&bean.getFileName()!=null && StringUtils.isNotEmpty(bean.getFileName().toString())){
				selectSql+=" and FILE_NAME=? ";
				values.add(bean.getFileName());
			};
			if (bean!=null&&bean.getSize()!=null && StringUtils.isNotEmpty(bean.getSize().toString())){
				selectSql+=" and \"SIZE\"=? ";
				values.add(bean.getSize());
			};
			if (bean!=null&&bean.getFormat()!=null && StringUtils.isNotEmpty(bean.getFormat().toString())){
				selectSql+=" and FORMAT=? ";
				values.add(bean.getFormat());
			};
			if (bean!=null&&bean.getImpWorker()!=null && StringUtils.isNotEmpty(bean.getImpWorker().toString())){
				selectSql+=" and IMP_WORKER=? ";
				values.add(bean.getImpWorker());
			};
			if (bean!=null&&bean.getImpDate()!=null && StringUtils.isNotEmpty(bean.getImpDate().toString())){
//				selectSql+=" and IMP_DATE=? ";
				selectSql+=" and IMP_DATE=TO_DATE(?,'yyyy-MM-dd HH24:MI:ss') ";
				values.add(bean.getImpDate());
			};
			if (bean!=null&&bean.getUrlDb()!=null && StringUtils.isNotEmpty(bean.getUrlDb().toString())){
				selectSql+=" and URL_DB=? ";
				values.add(bean.getUrlDb());
			};
			if (bean!=null&&bean.getUrlFile()!=null && StringUtils.isNotEmpty(bean.getUrlFile().toString())){
				selectSql+=" and URL_FILE=? ";
				values.add(bean.getUrlFile());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				selectSql+=" and MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getFileContent()!=null && StringUtils.isNotEmpty(bean.getFileContent().toString())){
				selectSql+=" and FILE_CONTENT=? ";
				values.add(bean.getFileContent());
			};
			if (bean!=null&&bean.getUpdateTime()!=null && StringUtils.isNotEmpty(bean.getUpdateTime().toString())){
				selectSql+=" and UPDATE_TIME=? ";
				values.add(bean.getUpdateTime());
			};
			ResultSetHandler<List<ScModelMatchG>> rsHandler = new ResultSetHandler<List<ScModelMatchG>>(){
				public List<ScModelMatchG> handle(ResultSet rs) throws SQLException {
					List<ScModelMatchG> list = new ArrayList<ScModelMatchG>();
					while(rs.next()){
						ScModelMatchG model = new ScModelMatchG();
						model.setFileId(rs.getLong("FILE_ID"));
						model.setProductLine(rs.getString("PRODUCT_LINE"));
						model.setVersion(rs.getString("VERSION"));
						model.setProjectNm(rs.getString("PROJECT_NM"));
						model.setSpecification(rs.getString("SPECIFICATION"));
						model.setbType(rs.getString("B_TYPE"));
						model.setmType(rs.getString("M_TYPE"));
						model.setsType(rs.getString("S_TYPE"));
						model.setFileName(rs.getString("FILE_NAME"));
						model.setSize(rs.getString("SIZE"));
						model.setFormat(rs.getString("FORMAT"));
						model.setImpWorker(rs.getString("IMP_WORKER"));
						
						String impDateStr = DateUtilsEx.getTimeStr(rs.getTimestamp("IMP_DATE"), "yyyy-MM-dd HH:mm:ss");
						System.out.println(impDateStr);
						model.setImpDate(impDateStr);
						
						model.setUrlDb(rs.getString("URL_DB"));
						model.setUrlFile(rs.getString("URL_FILE"));
						model.setMemo(rs.getString("MEMO"));
						model.setFileContent((BLOB) rs.getBlob("FILE_CONTENT"));
						model.setUpdateTime(rs.getString("UPDATE_TIME"));
						list.add(model);
					}
					return list;
				}
	    		
	    	};
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
	public ScModelMatchG query(ScModelMatchG bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_MODEL_MATCH_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getFileId()!=null && StringUtils.isNotEmpty(bean.getFileId().toString())){
				selectSql+=" and FILE_ID=? ";
				values.add(bean.getFileId());
			};
			if (bean!=null&&bean.getProductLine()!=null && StringUtils.isNotEmpty(bean.getProductLine().toString())){
				selectSql+=" and PRODUCT_LINE=? ";
				values.add(bean.getProductLine());
			};
			if (bean!=null&&bean.getVersion()!=null && StringUtils.isNotEmpty(bean.getVersion().toString())){
				selectSql+=" and VERSION=? ";
				values.add(bean.getVersion());
			};
			if (bean!=null&&bean.getProjectNm()!=null && StringUtils.isNotEmpty(bean.getProjectNm().toString())){
				selectSql+=" and PROJECT_NM=? ";
				values.add(bean.getProjectNm());
			};
			if (bean!=null&&bean.getSpecification()!=null && StringUtils.isNotEmpty(bean.getSpecification().toString())){
				selectSql+=" and SPECIFICATION=? ";
				values.add(bean.getSpecification());
			};
			if (bean!=null&&bean.getbType()!=null && StringUtils.isNotEmpty(bean.getbType().toString())){
				selectSql+=" and B_TYPE=? ";
				values.add(bean.getbType());
			};
			if (bean!=null&&bean.getmType()!=null && StringUtils.isNotEmpty(bean.getmType().toString())){
				selectSql+=" and M_TYPE=? ";
				values.add(bean.getmType());
			};
			if (bean!=null&&bean.getsType()!=null && StringUtils.isNotEmpty(bean.getsType().toString())){
				selectSql+=" and S_TYPE=? ";
				values.add(bean.getsType());
			};
			if (bean!=null&&bean.getFileName()!=null && StringUtils.isNotEmpty(bean.getFileName().toString())){
				selectSql+=" and FILE_NAME=? ";
				values.add(bean.getFileName());
			};
			if (bean!=null&&bean.getSize()!=null && StringUtils.isNotEmpty(bean.getSize().toString())){
				selectSql+=" and \"SIZE\"=? ";
				values.add(bean.getSize());
			};
			if (bean!=null&&bean.getFormat()!=null && StringUtils.isNotEmpty(bean.getFormat().toString())){
				selectSql+=" and FORMAT=? ";
				values.add(bean.getFormat());
			};
			if (bean!=null&&bean.getImpWorker()!=null && StringUtils.isNotEmpty(bean.getImpWorker().toString())){
				selectSql+=" and IMP_WORKER=? ";
				values.add(bean.getImpWorker());
			};
			if (bean!=null&&bean.getImpDate()!=null && StringUtils.isNotEmpty(bean.getImpDate().toString())){
//				selectSql+=" and IMP_DATE=? ";
				selectSql+=" and IMP_DATE=TO_DATE(?,'yyyy-MM-dd HH24:MI:ss') ";
				values.add(bean.getImpDate());
			};
			if (bean!=null&&bean.getUrlDb()!=null && StringUtils.isNotEmpty(bean.getUrlDb().toString())){
				selectSql+=" and URL_DB=? ";
				values.add(bean.getUrlDb());
			};
			if (bean!=null&&bean.getUrlFile()!=null && StringUtils.isNotEmpty(bean.getUrlFile().toString())){
				selectSql+=" and URL_FILE=? ";
				values.add(bean.getUrlFile());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				selectSql+=" and MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getFileContent()!=null && StringUtils.isNotEmpty(bean.getFileContent().toString())){
				selectSql+=" and FILE_CONTENT=? ";
				values.add(bean.getFileContent());
			};
			if (bean!=null&&bean.getUpdateTime()!=null && StringUtils.isNotEmpty(bean.getUpdateTime().toString())){
				selectSql+=" and UPDATE_TIME=? ";
				values.add(bean.getUpdateTime());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<ScModelMatchG> rsHandler = new ResultSetHandler<ScModelMatchG>(){
				public ScModelMatchG handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScModelMatchG model = new ScModelMatchG();
						model.setFileId(rs.getLong("FILE_ID"));
						model.setProductLine(rs.getString("PRODUCT_LINE"));
						model.setVersion(rs.getString("VERSION"));
						model.setProjectNm(rs.getString("PROJECT_NM"));
						model.setSpecification(rs.getString("SPECIFICATION"));
						model.setbType(rs.getString("B_TYPE"));
						model.setmType(rs.getString("M_TYPE"));
						model.setsType(rs.getString("S_TYPE"));
						model.setFileName(rs.getString("FILE_NAME"));
						model.setSize(rs.getString("SIZE"));
						model.setFormat(rs.getString("FORMAT"));
						model.setImpWorker(rs.getString("IMP_WORKER"));
						
						String impDateStr = DateUtilsEx.getTimeStr(rs.getTimestamp("IMP_DATE"), "yyyy-MM-dd HH:mm:ss");
						System.out.println(impDateStr);
						model.setImpDate(impDateStr);
						
						model.setUrlDb(rs.getString("URL_DB"));
						model.setUrlFile(rs.getString("URL_FILE"));
						model.setMemo(rs.getString("MEMO"));
						model.setFileContent((BLOB) rs.getBlob("FILE_CONTENT"));
						model.setUpdateTime(rs.getString("UPDATE_TIME"));
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
	public void saveUpdate(JSONObject dataJson) throws Exception {
		long fileId = 0 ;
		String[] formats={"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd"}; 
		JSONUtils.getMorpherRegistry().registerMorpher(new TimestampMorpher(formats));  
		JSONObject taskJson=JSONObject.fromObject(dataJson); 
		if(taskJson.get("fileId") != null && !taskJson.get("fileId").equals("null")){//存在 fileId ,更新数据
			fileId = taskJson.getLong("fileId");
			
			ScModelMatchG bean =(ScModelMatchG) JSONObject.toBean(taskJson, ScModelMatchG.class);
			updateByFileId(bean);
			//update(bean);
		}else{//不存在 fileId 执行新增
			fileId = getFileId();
			try{
				ScModelMatchG bean =(ScModelMatchG) JSONObject.toBean(taskJson, ScModelMatchG.class);
				bean.setFileId(fileId);
				create(bean);
			}catch(Exception e){
				System.out.println("bean : "+e.getMessage());
			}
			
		}
		
		
	}
	
	public void deleteByIds(JSONArray idsJson) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getMetaConnection();
		
			for(Object beanObj : idsJson){
				Long fileId = (Long) beanObj;
				if(fileId != null && fileId >0){
					delete(fileId,conn);
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
			ScModelMatchG bean =(ScModelMatchG) JsonOperation.jsonToBean(dataJson, ScModelMatchG.class);
			
			String selectSql = "select * from SC_MODEL_MATCH_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getFileId()!=null && StringUtils.isNotEmpty(bean.getFileId().toString())){
				selectSql+=" and FILE_ID=? ";
				values.add(bean.getFileId());
			};
			if (bean!=null&&bean.getProductLine()!=null && StringUtils.isNotEmpty(bean.getProductLine().toString())){
				selectSql+=" and PRODUCT_LINE=? ";
				values.add(bean.getProductLine());
			};
			if (bean!=null&&bean.getVersion()!=null && StringUtils.isNotEmpty(bean.getVersion().toString())){
				selectSql+=" and VERSION=? ";
				values.add(bean.getVersion());
			};
			if (bean!=null&&bean.getProjectNm()!=null && StringUtils.isNotEmpty(bean.getProjectNm().toString())){
				selectSql+=" and PROJECT_NM=? ";
				values.add(bean.getProjectNm());
			};
			if (bean!=null&&bean.getSpecification()!=null && StringUtils.isNotEmpty(bean.getSpecification().toString())){
				selectSql+=" and SPECIFICATION=? ";
				values.add(bean.getSpecification());
			};
			if (bean!=null&&bean.getbType()!=null && StringUtils.isNotEmpty(bean.getbType().toString())){
				selectSql+=" and B_TYPE=? ";
				values.add(bean.getbType());
			};
			if (bean!=null&&bean.getmType()!=null && StringUtils.isNotEmpty(bean.getmType().toString())){
				selectSql+=" and M_TYPE=? ";
				values.add(bean.getmType());
			};
			if (bean!=null&&bean.getsType()!=null && StringUtils.isNotEmpty(bean.getsType().toString())){
				selectSql+=" and S_TYPE=? ";
				values.add(bean.getsType());
			};
			if (bean!=null&&bean.getFileName()!=null && StringUtils.isNotEmpty(bean.getFileName().toString())){
				selectSql+=" and FILE_NAME=? ";
				values.add(bean.getFileName());
			};
			if (bean!=null&&bean.getSize()!=null && StringUtils.isNotEmpty(bean.getSize().toString())){
				selectSql+=" and \"SIZE\"=? ";
				values.add(bean.getSize());
			};
			if (bean!=null&&bean.getFormat()!=null && StringUtils.isNotEmpty(bean.getFormat().toString())){
				selectSql+=" and FORMAT=? ";
				values.add(bean.getFormat());
			};
			if (bean!=null&&bean.getImpWorker()!=null && StringUtils.isNotEmpty(bean.getImpWorker().toString())){
				selectSql+=" and IMP_WORKER=? ";
				values.add(bean.getImpWorker());
			};
			if (bean!=null&&bean.getImpDate()!=null && StringUtils.isNotEmpty(bean.getImpDate().toString())){
				selectSql+=" and IMP_DATE=TO_DATE(?,'yyyy-MM-dd HH24:MI:ss') ";
//				selectSql+=" and IMP_DATE=? ";
				values.add(bean.getImpDate());
			};
			if (bean!=null&&bean.getUrlDb()!=null && StringUtils.isNotEmpty(bean.getUrlDb().toString())){
				selectSql+=" and URL_DB=? ";
				values.add(bean.getUrlDb());
			};
			if (bean!=null&&bean.getUrlFile()!=null && StringUtils.isNotEmpty(bean.getUrlFile().toString())){
				selectSql+=" and URL_FILE=? ";
				values.add(bean.getUrlFile());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				selectSql+=" and MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getFileContent()!=null && StringUtils.isNotEmpty(bean.getFileContent().toString())){
				selectSql+=" and FILE_CONTENT=? ";
				values.add(bean.getFileContent());
			};
			if (bean!=null&&bean.getUpdateTime()!=null && StringUtils.isNotEmpty(bean.getUpdateTime().toString())){
				selectSql+=" and UPDATE_TIME=? ";
				values.add(bean.getUpdateTime());
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
					List<ScModelMatchG> list = new ArrayList<ScModelMatchG>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
					while(rs.next()){
						ScModelMatchG model = new ScModelMatchG();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setFileId(rs.getLong("FILE_ID"));
						model.setProductLine(rs.getString("PRODUCT_LINE"));
						model.setVersion(rs.getString("VERSION"));
						model.setProjectNm(rs.getString("PROJECT_NM"));
						model.setSpecification(rs.getString("SPECIFICATION"));
						model.setbType(rs.getString("B_TYPE"));
						model.setmType(rs.getString("M_TYPE"));
						model.setsType(rs.getString("S_TYPE"));
						model.setFileName(rs.getString("FILE_NAME"));
						model.setSize(rs.getString("SIZE"));
						model.setFormat(rs.getString("FORMAT"));
						model.setImpWorker(rs.getString("IMP_WORKER"));
						
						String impDateStr = DateUtilsEx.getTimeStr(rs.getTimestamp("IMP_DATE"), "yyyy-MM-dd HH:mm:ss");
						System.out.println(impDateStr);
						model.setImpDate(impDateStr);
						model.setUrlDb(rs.getString("URL_DB"));
						model.setUrlFile(rs.getString("URL_FILE"));
						model.setMemo(rs.getString("MEMO"));
						//System.out.println(rs.getObject("FILE_CONTENT"));
						//System.out.println((oracle.sql.BLOB)rs.getBlob("FILE_CONTENT"));
						//model.setFileContent((BLOB) rs.getBlob("FILE_CONTENT"));
						model.setUpdateTime(rs.getString("UPDATE_TIME"));
						list.add(model);
					}
					page.setResult(list);
					return page;
				}
	    		
	    	};
			if (values.size()==0){
	    		return run.query(curPageNum, pageSize, conn, selectSql, rsHandler);
	    	}
			log.info("ScModelMatchG selectSql: "+selectSql+" paramters: "+values);
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
	
	
	/**
	 * 
	 * @Description:获取fileId
	 * 原则：
	 * 1. 前4位：当前年份
	 * 2. 第5位：0
	 * 3. 后8位：顺序编号 ：max（数据中后8位）+1
	 * @param conn
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-2-8 上午9:40:02
	 */
	private long getFileId() throws Exception{
		Connection conn = null;
		PreparedStatement pstmt=null;
		
		ResultSet rs=null;
		
		try{
		conn = DBConnector.getInstance().getMetaConnection();	
		//1. 前4位：当前年份
		Calendar c = Calendar.getInstance();
		
		c.setTime(new Date());
		
		String year=String.valueOf((c.get(Calendar.YEAR))) ;
		
		//max（数据中后8位）+1
		String sql="SELECT  max(SUBSTR(file_id,-7,7))+1  file_id FROM SC_MODEL_MATCH_G ";
		
		pstmt = conn.prepareStatement(sql);
		
		rs= pstmt.executeQuery();
		
		if(rs.next()){
			
			return Long.valueOf(year+"0"+String.valueOf(rs.getLong("file_id")));
		}
		
		}catch (Exception e) {
			throw e;
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		return 0;
		
		
	}
	public BLOB getFileContentById(String id) throws ServiceException {
		
		ScModelMatchG bean = queryFileContentByid(id);
		
		return bean.getFileContent();
	}
	
	public ScModelMatchG queryFileContentByid(String id)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_MODEL_MATCH_G where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (id !=null&& StringUtils.isNotEmpty(id)){
				selectSql+=" and FILE_ID=? ";
				values.add(id);
			};
			
			selectSql+=" and rownum=1";
			ResultSetHandler<ScModelMatchG> rsHandler = new ResultSetHandler<ScModelMatchG>(){
				public ScModelMatchG handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScModelMatchG model = new ScModelMatchG();
						model.setFileId(rs.getLong("FILE_ID"));
						model.setProductLine(rs.getString("PRODUCT_LINE"));
						model.setVersion(rs.getString("VERSION"));
						model.setProjectNm(rs.getString("PROJECT_NM"));
						model.setSpecification(rs.getString("SPECIFICATION"));
						model.setbType(rs.getString("B_TYPE"));
						model.setmType(rs.getString("M_TYPE"));
						model.setsType(rs.getString("S_TYPE"));
						model.setFileName(rs.getString("FILE_NAME"));
						model.setSize(rs.getString("SIZE"));
						model.setFormat(rs.getString("FORMAT"));
						model.setImpWorker(rs.getString("IMP_WORKER"));
						
						String impDateStr = DateUtilsEx.getTimeStr(rs.getTimestamp("IMP_DATE"), "yyyy-MM-dd HH:mm:ss");
						System.out.println(impDateStr);
						model.setImpDate(impDateStr);
						
						model.setUrlDb(rs.getString("URL_DB"));
						model.setUrlFile(rs.getString("URL_FILE"));
						model.setMemo(rs.getString("MEMO"));
						model.setFileContent((BLOB) rs.getBlob("FILE_CONTENT"));
						model.setUpdateTime(rs.getString("UPDATE_TIME"));
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
			throw new ServiceException("根据fileId查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
