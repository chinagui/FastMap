package com.navinfo.dataservice.engine.man.grid;

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

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;

import oracle.sql.CLOB;

public class GridOperation {
	private static Logger log = LoggerRepos.getLogger(GridOperation.class);
	
	public GridOperation() {
		// TODO Auto-generated constructor stub
	}

	public static List<HashMap> queryGirdBySql(Connection conn,String selectSql,List<?> grids) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						HashMap map = new HashMap<String, Integer>();
						map.put("gridId", rs.getString("grid_id"));
						map.put("status", rs.getInt("status"));
						list.add(map);
					}
					return list;
				}
	    		
	    	};
	    	if (null==grids || grids.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,grids.toArray()
					);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("鏌ヨ澶辫触锛屽師鍥犱负:"+e.getMessage(),e);
		}
	}
	
	
	public static List<HashMap> queryProduceBlock(Connection conn,String selectSql,List<Object> values) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						HashMap map = new HashMap<String, Integer>();
						//block下grid日完成度为100%，block才可出品
						try {
							if (GridOperation.checkGridFinished(rs.getInt("BLOCK_ID"))){
								map.put("blockId", rs.getInt("BLOCK_ID"));
								map.put("blockName", rs.getInt("BLOCK_NAME"));
								CLOB clob = (CLOB)rs.getObject("geometry");
								String clobStr = DataBaseUtils.clob2String(clob);
								try {
									map.put("geometry",Geojson.wkt2Geojson(clobStr));
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								list.add(map);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					return list;
				}
	    		
	    	}		;
	    	if (null==values || values.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray()
					);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("鏌ヨ澶辫触锛屽師鍥犱负:"+e.getMessage(),e);
		}
	}
	
	
	public static List<HashMap> queryBlockByGroup(Connection conn,String selectSql,List<Object> values) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						HashMap map = new HashMap<String, Integer>();
						System.out.println(rs.getInt("BLOCK_ID"));
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("planStartDate", rs.getDate("planStartDate"));
						map.put("planEndDate", rs.getDate("planEndDate"));
						map.put("descp", rs.getString("DESCP"));
		
						list.add(map);
					}
					return list;
				}
	    		
	    	}		;
	    	if (null==values || values.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray()
					);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("鏌ヨ澶辫触锛屽師鍥犱负:"+e.getMessage(),e);
		}
	}
	
	public static boolean checkGridFinished(int blockId) throws Exception{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String sqlByblockId="select grid_id from grid where block_id="+blockId;
			
			PreparedStatement stmt = conn.prepareStatement(sqlByblockId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int grid_id = rs.getInt(1);
				//调用统计模块，查询grid完成度,若不为100%，返回false
				//TODO
				return false;
			}
			return true;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
