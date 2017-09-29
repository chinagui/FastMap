package com.navinfo.dataservice.dao;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Test;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.dao.glm.search.IxPointaddressSearch;

public class IxPoiaddressTest {
@Test
	public void searchMainDataByPid(){

		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_trunk_d_1", "fm_regiondb_trunk_d_1").getConnection();
			int pid = 630127;
			IxPointaddressSearch ixPointaddressSearch = new IxPointaddressSearch(conn);
			System.out.println(ixPointaddressSearch.searchMainDataByPid(pid));

			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
}
