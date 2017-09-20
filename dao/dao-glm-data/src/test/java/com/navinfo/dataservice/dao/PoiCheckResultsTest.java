package com.navinfo.dataservice.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.dbutils.DbUtils;
import org.junit.Test;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
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
public class PoiCheckResultsTest {
	
//	@Test
	public void checkResultList(){
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_trunk_d_1", "fm_regiondb_trunk_d_1").getConnection();

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
				
			JSONObject data = new JSONObject();//selector.poiCheckResults(pid);
			
				//List<JSONObject> page =null;
				try {
					 JSONArray checkResultsArr = a.poiCheckResultList(507000006,new ArrayList());
						data.put("data", checkResultsArr);
						data.put("total", checkResultsArr.size());
					//page =a.list(2, grids, 5, 1);
					 System.out.println(data);
					
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
	
	@Test
	public void checkListPoiResults(){
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_trunk_d_1", "fm_regiondb_trunk_d_1").getConnection();

			NiValExceptionSelector a = new NiValExceptionSelector(conn);
				
			JSONObject jsonReq = JSONObject.fromObject("{'pageSize':20,'pageNum':1,'subtaskId':68,'dbId':13,'sortby':'-ruleid'}");	
			JSONObject data = new JSONObject();//selector.poiCheckResults(pid);
			
				//List<JSONObject> page =null;
				try {
					 Page page1 = a.listPoiCheckResultList(jsonReq,306);
					 System.out.println(page1.getResult());
						
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
//	@Test
	public void getListPoiResultsCount(){
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_trunk_d_1", "fm_regiondb_trunk_d_1").getConnection();

			NiValExceptionSelector a = new NiValExceptionSelector(conn);
				
			JSONObject jsonReq = JSONObject.fromObject("{'pageSize':20,'pageNum':1,'subtaskId':306,'dbId':13,'sortby':'-ruleid'}");	
			JSONObject data = new JSONObject();//selector.poiCheckResults(pid);
			
				//List<JSONObject> page =null;
				try {
//					int count = a.getListPoiResultCount(conn);
//					System.out.println(count);
					
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
	/*public int getListPoiResultCount(Connection conn)
			throws Exception {

		List<Long> pids = new ArrayList<Long>();
			pids.add((long) 401000129);
			pids.add((long) 406000007);
			pids.add((long) 420000008);
		int poiResCount = 0;
		if (pids != null && pids.size() > 0) {
			try {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				// 行编有针对删除数据进行的检查，此处要把删除数据也加载出来
				StringBuilder sql = new StringBuilder(
						"select count(1) total from ( "
								+ "select O.PID "
								+ "from "
								+ "ni_val_exception a  , CK_RESULT_OBJECT O  "
								+ "WHERE  (O.table_name like 'IX_POI\\_%' ESCAPE '\\' OR O.table_name ='IX_POI')  AND O.MD5_CODE=a.MD5_CODE "
								+ " and o.pid in (select column_value from table(clob_to_table(?)) "
								+ ") "
								+ " union all "
								+ "select O.PID "
								+ "from "
								+ "ck_exception c , CK_RESULT_OBJECT O "
								+ "  WHERE (O.table_name like 'IX_POI\\_%' ESCAPE '\\' OR O.table_name ='IX_POI')  AND O.MD5_CODE=c.MD5_CODE "
								+ " and o.pid in (select column_value from table(clob_to_table(?)) "
								+ " )  " + " )  b ");
				
				log.info("sql: " +sql.toString());
				QueryRunner run = new QueryRunner();
				poiResCount = run.query(conn, sql.toString(),
						new ResultSetHandler<Integer>() {

							@Override
							public Integer handle(ResultSet rs)
									throws SQLException {
								Integer resCount = 0;
								if (rs.next()) {
									resCount = rs.getInt("total");
								}
								return resCount;
							}
						},clob,clob);

			} catch (Exception e) {
				log.error("行编获取检查数据报错", e);
				// DbUtils.rollbackAndCloseQuietly(conn);
				throw new Exception(e);
			}
		}
		log.info("poiResCount: " + poiResCount);
		return poiResCount;
	}*/
	public static void main(String[] args) {
		/*int pid = 176;
		String pids = "170,176";
		String[] pidsArr = pids.split(",");
		
		if(pidsArr != null && pidsArr.length >1){
			for(String pidStr :pidsArr){
				System.out.println(pidStr.equals(pid));
				if(pidStr != null && StringUtils.isNotEmpty(pidStr) ){
					int refPid = Integer.parseInt(pidStr);
					if(refPid != pid){
						System.out.println(pidStr);
						System.out.println(Integer.parseInt(pidStr)+1);
					}
					
				}
			}
		}*/
		com.navinfo.dataservice.commons.util.StringUtils sUtils = new com.navinfo.dataservice.commons.util.StringUtils();
		String sortby = "ruleid";
		String orderSql = "";
		if (sortby.length()>0) {
			int index = sortby.indexOf("-");
			if (index != -1) {
				orderSql+=" ORDER BY ";
				String sortbyName = sUtils.toColumnName(sortby.substring(1));
				orderSql+="  ";
				orderSql+=sortbyName;
				orderSql+=" DESC";
			} else {
				orderSql+=" ORDER BY ";
				String sortbyName = sUtils.toColumnName(sortby.substring(1));
				orderSql+="  ";
				orderSql+=sortbyName;
			}
		}
		System.out.println(orderSql);
		
	}

}
