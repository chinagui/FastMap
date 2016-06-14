package com.navinfo.dataservice.engine.man.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

public class DbOperation {
	private static Logger log = LoggerRepos.getLogger(DbOperation.class);

	public DbOperation() {
		// TODO Auto-generated constructor stub
	}
	
	public static void exeUpdateOrInsertBySql(Connection conn,String sql) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			run.update(conn,sql);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("语句修改失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static List<List<String>> exeSelectBySql(Connection conn,String selectSql,List<String> values) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<List<String>>> rsHandler = new ResultSetHandler<List<List<String>>>(){
				public List<List<String>> handle(ResultSet rs) throws SQLException {
					List<List<String>> list = new ArrayList<List<String>>();
					int columnCount=rs.getMetaData().getColumnCount();
					while(rs.next()){
						List<String> listTmp=new ArrayList<String>();
						for(int i=1;i<=columnCount;i++){
							listTmp.add(rs.getString(i));
						}
						list.add(listTmp);
					}
					return list;
				}
	    		
	    	}		;
	    	if (null==values || values.size()==0){
	    		return run.query(conn, selectSql, rsHandler);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("语句查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static List<HashMap> exeSelectBySql2(Connection conn,String selectSql,List<String> values) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();						
					int columnCount=rs.getMetaData().getColumnCount();
					List<String> columnNameList=new ArrayList<String>();
					boolean hasColumnName=true;
					while(rs.next()){
						HashMap map = new HashMap();
						for(int i=1;i<=columnCount;i++){
							if(!hasColumnName){columnNameList.add(rs.getMetaData().getColumnName(i));}
							map.put(columnNameList.get(i), rs.getString(i));
						}
						list.add(map);
					}
					return list;
				}
	    		
	    	}		;
	    	if (null==values || values.size()==0){
	    		return run.query(conn, selectSql, rsHandler);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("语句查询失败，原因为:"+e.getMessage(),e);
		}
	}

}
