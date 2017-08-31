package com.navinfo.dataservice.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datarow.DataRowTool;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.dao.glm.search.IxPoiSearch;
import com.navinfo.dataservice.dao.glm.search.RdLinkSearch;
import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.navicommons.database.Page;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: RdNameImportTest.java
 * @author y
 * @date 2016-7-2下午3:07:28
 * @Description: TODO
 *  
 */
public class IxPoiSearchTest {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
//	@Test
	public void checkResultList(){
		log.info("start");
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "regiondb_staging_d_9", "regiondb_staging_d_9").getConnection();

			IxPoiSearch rdLinkSearch = new IxPoiSearch(conn);
			//"taskId":0,"x":107889,"y":49697,"z":17}
			Object list = rdLinkSearch.searchDataByTileWithGap(103489, 56781, 17, 10,482);
			log.info("end");
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	
//	@Test
	public void test(){
		log.info("start");
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_trunk_d_1", "fm_regiondb_trunk_d_1").getConnection();

			List<Long> pidList = new ArrayList<Long>();
			pidList.add(407000002L);
			pidList.add(502000007L);
			Map<Long, Map<String, Object>> list = PoiEditStatus.getFreshData(conn, pidList);
			System.out.println(list.toString());
			log.info("end");
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	
	@Test
	public void getTableColumn(){
		log.info("start");
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_trunk_d_1", "fm_regiondb_trunk_d_1").getConnection();

			String str =DataRowTool.getSelectColumnString(conn,"NI_VAL_EXCEPTION");
			String sql = "INSERT INTO NI_VAL_EXCEPTION SELECT "+DataRowTool.getSelectColumnString(conn,"NI_VAL_EXCEPTION")+" FROM NI_VAL_EXCEPTION ";
			System.out.println("str: "+str);
			System.out.println("newStr: "+str.replace("\"RESERVED\"", "1 \"RESERVED\""));
			System.out.println("sql: "+sql);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

}
