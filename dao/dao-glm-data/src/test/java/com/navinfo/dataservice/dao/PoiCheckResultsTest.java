package com.navinfo.dataservice.dao;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.dbutils.DbUtils;
import org.junit.Test;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;

/** 
 * @ClassName: RdNameImportTest.java
 * @author y
 * @date 2016-7-2下午3:07:28
 * @Description: TODO
 *  
 */
public class PoiCheckResultsTest {
	
	@Test
	public void checkResultList(){
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_sp6_d_1", "fm_regiondb_sp6_d_1").getConnection();

			NiValExceptionSelector a = new NiValExceptionSelector(conn);
			Set<String> grids = new HashSet<String>();
//			60561231,60561210,60561220,60561230,60561232,60561201,60561202,
//			60561203,60561211,60561212,60561213,60561223,60561221,60561233,60561200
			grids.add("60561222");
			grids.add("60561231");
			grids.add("60561210");
			grids.add("60561220");
			grids.add("60561230");
			grids.add("60561232");
			grids.add("60561201");
			grids.add("60561202");
			grids.add("60561203");
				
				Page page = null;
				//List<JSONObject> page =null;
				try {
					 page =a.poiCheckResultList(0, grids, 5, 1);
					//page =a.list(2, grids, 5, 1);
					 System.out.println(page.getResult());
					 System.out.println(page.getTotalCount());
					
					/*JSONArray results = a.queryRefFeatures(1810842);
					System.out.println(results);*/
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	

}
