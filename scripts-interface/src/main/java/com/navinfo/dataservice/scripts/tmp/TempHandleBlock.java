package com.navinfo.dataservice.scripts.tmp;

import java.sql.Clob;
import java.sql.Connection;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: TempHandleBlock
 * @author xiaoxiaowen4127
 * @date 2017年8月13日
 * @Description: TempHandleBlock.java
 */
public class TempHandleBlock {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "fm_sys_bvt", "fm_sys_bvt").getConnection();
    		String sql = "INSERT INTO TEMP_XXW_03 VALUES(1,SDO_GEOMETRY(?,8307))";
//			String sql ="UPDATE SG_RAW_BLOCK SET kind_1_7=0, GEOMETRY = sdo_geometry(?,8307) WHERE BLOCKCODE = 360600200";
    		String p = "";
    		Clob c = ConnectionUtil.createClob(conn,p);
    		new QueryRunner().update(conn, sql,c);
    		conn.commit();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
