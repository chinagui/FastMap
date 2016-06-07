package com.navinfo.navicommons.geo.computation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: GlmObject2GridUtil 
* @author Xiao Xiaowen 
* @date 2016年4月13日 上午10:06:08 
* @Description: TODO
*/
public class GlmObject2GridUtil {
	public static void test1(){
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			QueryRunner runner = new QueryRunner();
			String sql = "S";
			runner.query(conn, sql, new ResultSetHandler<String>(){

				@Override
				public String handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					return null;
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
