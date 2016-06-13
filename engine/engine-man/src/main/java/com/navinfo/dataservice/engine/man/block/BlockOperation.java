package com.navinfo.dataservice.engine.man.block;

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

public class BlockOperation {
	private static Logger log = LoggerRepos.getLogger(BlockOperation.class);
	
	public BlockOperation() {
		// TODO Auto-generated constructor stub
	}

	public static List<HashMap> queryBlockBySql(Connection conn,String selectSql,List<Object> values) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						HashMap<String, Integer> map = new HashMap<String, Integer>();
						map.put("blockId", rs.getInt("BLOCK_ID"));
						map.put("blockName", rs.getInt("BLOCK_NAME"));
						map.put("geometry", rs.getInt("GEOMETRY"));
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
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
}
