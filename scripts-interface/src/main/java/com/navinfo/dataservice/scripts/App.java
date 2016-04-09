package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Connection conn = null;
    	try{
        	QueryRunner runner = new QueryRunner();
    		conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
    		String sql = "select row_id from log_detail t,log_detail_grid p where t.op_id=p.op_id and p.grid_id in (2,4)";
    		long t1 = System.currentTimeMillis();
    		runner.query(conn, sql, new ResultSetHandler<String>(){

				@Override
				public String handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						
					}
					return "";
				}
    			
    		});
    		System.out.println((System.currentTimeMillis()-t1)/1000);
    	}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
    }
}
