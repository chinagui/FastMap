package com.navinfo.dataservice.engine.meta.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.meta.model.ScModelMatchG;
import com.navinfo.dataservice.engine.meta.model.ScRoadnameHwInfo;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.database.Page;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnameHwInfoService 
* @author code generator
* @date 2017-03-23 07:03:28 
* @Description: TODO
*/
@Service
public class ScRoadnameHwInfoService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(ScRoadnameHwInfo  bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String createSql = "insert into SC_ROADNAME_HW_INFO (HW_PID_UP, HW_PID_DW, NAME_GROUPID, MEMO, U_RECORD, U_FIELDS) values(?,?,?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getHwPidUp() , bean.getHwPidDw(), bean.getNameGroupid(), bean.getMemo(), bean.getuRecord(), bean.getuFields()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(ScRoadnameHwInfo bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String updateSql = "update SC_ROADNAME_HW_INFO set ";
		//"HW_PID_UP=?, HW_PID_DW=?, NAME_GROUPID=?, MEMO=?, U_RECORD=?, U_FIELDS=? where 1=1 ";
			String valueSql = "";
			List<Object> values=new ArrayList<Object>();
			
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getuRecord()!=null && StringUtils.isNotEmpty(bean.getuRecord().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  U_RECORD=? ";
				values.add(bean.getuRecord());
			};
			if (bean!=null&&bean.getuFields()!=null && StringUtils.isNotEmpty(bean.getuFields().toString())){
				if(StringUtils.isNotEmpty(valueSql)){valueSql+=" , ";}
				valueSql+="  U_FIELDS=? ";
				values.add(bean.getuFields());
			};
			
			if (bean!=null&&bean.getHwPidUp()!=null && StringUtils.isNotEmpty(bean.getHwPidUp().toString())){
				valueSql+=" where  HW_PID_UP=? ";
				values.add(bean.getHwPidUp());
			};
			log.info("SC_ROADNAME_HW_INFO updateSql : "+updateSql+valueSql);
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
	public void delete(ScRoadnameHwInfo bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String deleteSql = "delete from  SC_ROADNAME_HW_INFO where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getHwPidUp()!=null && StringUtils.isNotEmpty(bean.getHwPidUp().toString())){
				deleteSql+=" and HW_PID_UP=? ";
				values.add(bean.getHwPidUp());
			};
			if (bean!=null&&bean.getHwPidDw()!=null && StringUtils.isNotEmpty(bean.getHwPidDw().toString())){
				deleteSql+=" and HW_PID_DW=? ";
				values.add(bean.getHwPidDw());
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				deleteSql+=" and NAME_GROUPID=? ";
				values.add(bean.getNameGroupid());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				deleteSql+=" and MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getuRecord()!=null && StringUtils.isNotEmpty(bean.getuRecord().toString())){
				deleteSql+=" and U_RECORD=? ";
				values.add(bean.getuRecord());
			};
			if (bean!=null&&bean.getuFields()!=null && StringUtils.isNotEmpty(bean.getuFields().toString())){
				deleteSql+=" and U_FIELDS=? ";
				values.add(bean.getuFields());
			};
			log.info("deleteSql: "+deleteSql);
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
	public Page list(ScRoadnameHwInfo bean ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			
			String selectSql = "select * from SC_ROADNAME_HW_INFO where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getHwPidUp()!=null && StringUtils.isNotEmpty(bean.getHwPidUp().toString())){
				selectSql+=" and HW_PID_UP=? ";
				values.add(bean.getHwPidUp());
			};
			if (bean!=null&&bean.getHwPidDw()!=null && StringUtils.isNotEmpty(bean.getHwPidDw().toString())){
				selectSql+=" and HW_PID_DW=? ";
				values.add(bean.getHwPidDw());
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				selectSql+=" and NAME_GROUPID=? ";
				values.add(bean.getNameGroupid());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				selectSql+=" and MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getuRecord()!=null && StringUtils.isNotEmpty(bean.getuRecord().toString())){
				selectSql+=" and U_RECORD=? ";
				values.add(bean.getuRecord());
			};
			if (bean!=null&&bean.getuFields()!=null && StringUtils.isNotEmpty(bean.getuFields().toString())){
				selectSql+=" and U_FIELDS=? ";
				values.add(bean.getuFields());
			};
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List<ScRoadnameHwInfo> list = new ArrayList<ScRoadnameHwInfo>();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						ScRoadnameHwInfo model = new ScRoadnameHwInfo();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						model.setHwPidUp(rs.getInt("HW_PID_UP"));
						model.setHwPidDw(rs.getInt("HW_PID_DW"));
						model.setNameGroupid(rs.getInt("NAME_GROUPID"));
						model.setMemo(rs.getString("MEMO"));
						model.setuRecord(rs.getInt("U_RECORD"));
						model.setuFields(rs.getString("U_FIELDS"));
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
	public Page list(JSONObject dataJson,final int curPageNum, final int pageSize, String sortby) throws ServiceException {
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();
			//处理"memo":[],"uRecords":[]
			JSONArray memoArr = dataJson.getJSONArray("memo");
			dataJson.remove("memo");
			String memoStr="";
			if(memoArr != null && memoArr.size() > 0 ){
				//List<Integer> memoList = (List<Integer>) JSONArray.toCollection(memoArr);
				for(Object memoObj :memoArr){
					if(StringUtils.isNotEmpty(memoStr)){
						memoStr+=",";
					}
					memoStr+="'"+memoObj+"'";
					
				}
				log.info(" memoStr :"+memoStr);
			}
			//memoStr = memoArr.join(",");
			dataJson.put("memo", memoStr);
			
			JSONArray uRecordsArr = dataJson.getJSONArray("uRecords");
			//dataJson.remove("uRecords");
			
			String uRecordStr="";
			if(uRecordsArr != null && uRecordsArr.size() > 0 ){
//				List<Integer> uRecordList = (List<Integer>) JSONArray.toCollection(uRecordsArr);
				/*for(Object uRecordObj :uRecordsArr){
					//int uRecord = (int) uRecordObj;
					if(StringUtils.isNotEmpty(uRecordStr)){
						uRecordStr+=",";
					}
					uRecordStr+="'"+uRecordObj+"'";
					
				}*/
				uRecordStr=uRecordsArr.join(",");
				log.info(" uRecordStr :"+uRecordStr);
			}
			dataJson.put("uRecords", uRecordStr);
			
			
			
			ScRoadnameHwInfo bean =(ScRoadnameHwInfo) JSONObject.toBean(dataJson, ScRoadnameHwInfo.class);
			
			String selectSql = "select * from SC_ROADNAME_HW_INFO where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getHwPidUp()!=null && StringUtils.isNotEmpty(bean.getHwPidUp().toString())){
				selectSql+=" and HW_PID_UP like ? ";
				values.add("%"+bean.getHwPidUp()+"%");
			};
			if (bean!=null&&bean.getHwPidDw()!=null && StringUtils.isNotEmpty(bean.getHwPidDw().toString())){
				selectSql+=" and HW_PID_DW like ? ";
				values.add("%"+bean.getHwPidDw()+"%");
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				selectSql+=" and NAME_GROUPID like ? ";
				values.add("%"+bean.getNameGroupid()+"%");
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				selectSql+=" and MEMO in("+bean.getMemo()+") ";
//				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getuRecords()!=null && StringUtils.isNotEmpty(bean.getuRecords())){
				selectSql+=" and U_RECORD in("+bean.getuRecords()+") ";
				//selectSql+=" and U_RECORD in(?) ";
				//values.add(bean.getuRecords());
			};
			if (bean!=null&&bean.getuFields()!=null && StringUtils.isNotEmpty(bean.getuFields().toString())){
				selectSql+=" and U_FIELDS like ? ";
				values.add("%"+bean.getuFields()+"%");
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
					List<ScRoadnameHwInfo> list = new ArrayList<ScRoadnameHwInfo>();
		            Page page = new Page();
		            page.setPageNum(curPageNum);
		            page.setPageSize(pageSize);
		            int total = 0;
					while(rs.next()){
						ScRoadnameHwInfo model = new ScRoadnameHwInfo();
						if(total == 0){
							total=rs.getInt(QueryRunner.TOTAL_RECORD_NUM);
							log.info(" total : "+rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						}
						
						
						model.setHwPidUp(rs.getInt("HW_PID_UP"));
						model.setHwPidDw(rs.getInt("HW_PID_DW"));
						model.setNameGroupid(rs.getInt("NAME_GROUPID"));
						model.setMemo(rs.getString("MEMO"));
						model.setuRecord(rs.getInt("U_RECORD"));
						model.setuFields(rs.getString("U_FIELDS"));
						list.add(model);
					}
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}
	    		
	    	};
	    	log.info(" hw sql select :"+selectSql +"  values:"+values);
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
	public List<ScRoadnameHwInfo> list(ScRoadnameHwInfo bean)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = "select * from SC_ROADNAME_HW_INFO where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getHwPidUp()!=null && StringUtils.isNotEmpty(bean.getHwPidUp().toString())){
				selectSql+=" and HW_PID_UP=? ";
				values.add(bean.getHwPidUp());
			};
			if (bean!=null&&bean.getHwPidDw()!=null && StringUtils.isNotEmpty(bean.getHwPidDw().toString())){
				selectSql+=" and HW_PID_DW=? ";
				values.add(bean.getHwPidDw());
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				selectSql+=" and NAME_GROUPID=? ";
				values.add(bean.getNameGroupid());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				selectSql+=" and MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getuRecord()!=null && StringUtils.isNotEmpty(bean.getuRecord().toString())){
				selectSql+=" and U_RECORD=? ";
				values.add(bean.getuRecord());
			};
			if (bean!=null&&bean.getuFields()!=null && StringUtils.isNotEmpty(bean.getuFields().toString())){
				selectSql+=" and U_FIELDS=? ";
				values.add(bean.getuFields());
			};
			ResultSetHandler<List<ScRoadnameHwInfo>> rsHandler = new ResultSetHandler<List<ScRoadnameHwInfo>>(){
				public List<ScRoadnameHwInfo> handle(ResultSet rs) throws SQLException {
					List<ScRoadnameHwInfo> list = new ArrayList<ScRoadnameHwInfo>();
					while(rs.next()){
						ScRoadnameHwInfo model = new ScRoadnameHwInfo();
						model.setHwPidUp(rs.getInt("HW_PID_UP"));
						model.setHwPidDw(rs.getInt("HW_PID_DW"));
						model.setNameGroupid(rs.getInt("NAME_GROUPID"));
						model.setMemo(rs.getString("MEMO"));
						model.setuRecord(rs.getInt("U_RECORD"));
						model.setuFields(rs.getString("U_FIELDS"));
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
	public ScRoadnameHwInfo query(ScRoadnameHwInfo bean)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
			
			String selectSql = "select * from SC_ROADNAME_HW_INFO where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getHwPidUp()!=null && StringUtils.isNotEmpty(bean.getHwPidUp().toString())){
				selectSql+=" and HW_PID_UP=? ";
				values.add(bean.getHwPidUp());
			};
			if (bean!=null&&bean.getHwPidDw()!=null && StringUtils.isNotEmpty(bean.getHwPidDw().toString())){
				selectSql+=" and HW_PID_DW=? ";
				values.add(bean.getHwPidDw());
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				selectSql+=" and NAME_GROUPID=? ";
				values.add(bean.getNameGroupid());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				selectSql+=" and MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getuRecord()!=null && StringUtils.isNotEmpty(bean.getuRecord().toString())){
				selectSql+=" and U_RECORD=? ";
				values.add(bean.getuRecord());
			};
			if (bean!=null&&bean.getuFields()!=null && StringUtils.isNotEmpty(bean.getuFields().toString())){
				selectSql+=" and U_FIELDS=? ";
				values.add(bean.getuFields());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<ScRoadnameHwInfo> rsHandler = new ResultSetHandler<ScRoadnameHwInfo>(){
				public ScRoadnameHwInfo handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScRoadnameHwInfo model = new ScRoadnameHwInfo();
						model.setHwPidUp(rs.getInt("HW_PID_UP"));
						model.setHwPidDw(rs.getInt("HW_PID_DW"));
						model.setNameGroupid(rs.getInt("NAME_GROUPID"));
						model.setMemo(rs.getString("MEMO"));
						model.setuRecord(rs.getInt("U_RECORD"));
						model.setuFields(rs.getString("U_FIELDS"));
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
	
	public void create(ScRoadnameHwInfo  bean,Connection conn)throws ServiceException{
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String createSql = "insert into SC_ROADNAME_HW_INFO (HW_PID_UP, HW_PID_DW, NAME_GROUPID, MEMO, U_RECORD, U_FIELDS) values(?,?,?,?,?,?)";			
			run.update(conn, 
					   createSql, 
					   bean.getHwPidUp() , bean.getHwPidDw(), bean.getNameGroupid(), bean.getMemo(), bean.getuRecord(), bean.getuFields()
					   );
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	public ScRoadnameHwInfo query(ScRoadnameHwInfo bean,Connection conn)throws ServiceException{
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String selectSql = "select * from SC_ROADNAME_HW_INFO where 1=1 ";
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getHwPidUp()!=null && StringUtils.isNotEmpty(bean.getHwPidUp().toString())){
				selectSql+=" and HW_PID_UP=? ";
				values.add(bean.getHwPidUp());
			};
			if (bean!=null&&bean.getHwPidDw()!=null && StringUtils.isNotEmpty(bean.getHwPidDw().toString())){
				selectSql+=" and HW_PID_DW=? ";
				values.add(bean.getHwPidDw());
			};
			if (bean!=null&&bean.getNameGroupid()!=null && StringUtils.isNotEmpty(bean.getNameGroupid().toString())){
				selectSql+=" and NAME_GROUPID=? ";
				values.add(bean.getNameGroupid());
			};
			if (bean!=null&&bean.getMemo()!=null && StringUtils.isNotEmpty(bean.getMemo().toString())){
				selectSql+=" and MEMO=? ";
				values.add(bean.getMemo());
			};
			if (bean!=null&&bean.getuRecord()!=null && StringUtils.isNotEmpty(bean.getuRecord().toString())){
				selectSql+=" and U_RECORD=? ";
				values.add(bean.getuRecord());
			};
			if (bean!=null&&bean.getuFields()!=null && StringUtils.isNotEmpty(bean.getuFields().toString())){
				selectSql+=" and U_FIELDS=? ";
				values.add(bean.getuFields());
			};
			selectSql+=" and rownum=1";
			ResultSetHandler<ScRoadnameHwInfo> rsHandler = new ResultSetHandler<ScRoadnameHwInfo>(){
				public ScRoadnameHwInfo handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						ScRoadnameHwInfo model = new ScRoadnameHwInfo();
						model.setHwPidUp(rs.getInt("HW_PID_UP"));
						model.setHwPidDw(rs.getInt("HW_PID_DW"));
						model.setNameGroupid(rs.getInt("NAME_GROUPID"));
						model.setMemo(rs.getString("MEMO"));
						model.setuRecord(rs.getInt("U_RECORD"));
						model.setuFields(rs.getString("U_FIELDS"));
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
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public void update(JSONObject dataJson) throws ServiceException {
//		ScRoadnameHwInfo bean =(ScRoadnameHwInfo) JSONObject.toBean(dataJson, ScRoadnameHwInfo.class);
		ScRoadnameHwInfo bean =(ScRoadnameHwInfo) JsonOperation.jsonToBean(dataJson,ScRoadnameHwInfo.class);
		if(bean.getuRecord() == null || bean.getuRecord() == 0){
			bean.setuRecord(3);
		}	
		if(bean != null && bean.getHwPidUp() != null && bean.getHwPidUp() > 0){
			update(bean);
		}
	}
	
	public void saveHwInfo(Connection conn, Integer nameGroupid) throws Exception{
		ScRoadnameHwInfo bean = new ScRoadnameHwInfo();
		bean.setNameGroupid(nameGroupid);
		ScRoadnameHwInfo oldBean = query(bean, conn);
		if(oldBean == null){//数据库中不存在
			Integer hwPidUp=getPid();
			Integer hwPidDw=hwPidUp + 1;
			bean.setHwPidUp(hwPidUp);
			bean.setHwPidDw(hwPidDw);
			bean.setMemo("1");
			bean.setuRecord(1);
			create(bean, conn);
		}
		
	}
	
	
	public void deleteByIds(JSONArray idsJson) throws ServiceException {
		for(Object beanObj : idsJson){
			Integer hwPidUp = (Integer) beanObj;
			if(hwPidUp != null && hwPidUp >0){
				ScRoadnameHwInfo bean = new ScRoadnameHwInfo();
				bean.setHwPidUp(hwPidUp);
				delete(bean);
			}
		}
		
	}
	
	private Integer getPid() throws Exception{
		Connection conn = null;
		PreparedStatement pstmt=null;
		
		ResultSet rs=null;
		
		try{
		conn = DBConnector.getInstance().getMetaConnection();	
		//max（）+1
		String sql=" SELECT  max(hw_pid_up)+1  hw_pid_up  from  sc_roadname_hw_info  ";
		
		pstmt = conn.prepareStatement(sql);
		
		rs= pstmt.executeQuery();
		
		if(rs.next()){
			return rs.getInt("hw_pid_up");
		}
		
		}catch (Exception e) {
			throw e;
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
		return 0;
		
		
	}
}
