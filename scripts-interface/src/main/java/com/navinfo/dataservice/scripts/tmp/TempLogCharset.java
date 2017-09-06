package com.navinfo.dataservice.scripts.tmp;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: TempLogCharset
 * @author xiaoxiaowen4127
 * @date 2017年8月23日
 * @Description: TempLogCharset.java
 */
public class TempLogCharset {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "LOG_TEST", "LOG_TEST").getConnection();
    		String sql1 ="select new from log_detail a where a.row_id='AC0AC693E88343429EB455CA034F2562'";
    		String sql2 ="select new from log_detail a where a.row_id in ('3A2A00EA1A7A42B1BCD55B2D3B231180','AC0AC693E88343429EB455CA034F2562','98DB809ED3764C81AFE16DF0BE61F584','D7E58B5378F54A1181A42ABD2B748227')";
    		Statement st = conn.createStatement();
    		ResultSet rs = st.executeQuery(sql2);
    		String encode="UTF-8";
    		while(rs.next()){
    			String s = rs.getString(1);
    			
    			if(s.equals(new String(s.getBytes(encode),encode))){
    				System.out.println("true");
    			}else{
    				System.out.println("false");
    			}
    		}
    		conn.commit();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
}
