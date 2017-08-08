package com.navinfo.dataservice.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.DbUtils;
import org.junit.Test;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rdname.RdNameSelector;
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
public class RdNameResultsTest {
	
	//@Test
	public void checkResultList(){
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.3.228:1521/orcl", "metadata_pd_17_sum", "metadata_pd_17_sum").getConnection();
					//"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();

			JSONObject jsonReq = JSONObject.fromObject("{'pageSize':20,'pageNum':1,'subtaskId':78,'dbId':17}");	
			Integer jobId = 1;//jsonReq.getInt("jobId");
			String jobUuid = "B83UtMscIO6AEYF5ZF5ePtA351EojAmI";
			/*JobApi jobApiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
			if(jobId != null && jobId >0){
				//根据jobId 查询jobUuid 
				JobInfo jobInfo = jobApiService.getJobById(jobId);
				jobUuid = jobInfo.getGuid();
			}else{
				JSONObject jobObj = jobApiService.getLatestJob(subtaskId);
				if(jobObj != null && jobObj.size() >0){
					jobUuid= jobObj.getString("jobGuid");
					jobId = jobObj.getInt("jobId");
				}
			}*/
			
			
			
				NiValExceptionSelector a = new NiValExceptionSelector(conn);
				
				
				//JSONObject data = jsonReq.getJSONObject("data");
				JSONObject jso = JSONObject.fromObject("{'tips':[{'id':'021901d7e8ed4c7c604242a1392291a530fbb2'},{'id':'021901404F5A9DE3AB4ECCACE7B512207BC00B'},{'id':'02190151EEF41E16D34C5C8976B5DD6292DEAC'}]}");
				//int subtaskId = jsonReq.getInt("subtaskId");
				
				//ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
				
//				int dbId = subtask.getDbId();
				
				//FccApi apiFcc=(FccApi) ApplicationContextUtil.getBean("fccApi");
				//JSONObject jsorule = JSONObject.fromObject("{'rules':[{'ruleCode':'GLM02216'},{'ruleCode':'GLM02262'}]}");
				//获取规则号
				//JSONArray ruleCodes = jsorule.getJSONArray("rules");//CheckService.getInstance().getCkRuleCodes(type);
				JSONArray tips = jso.getJSONArray("tips");
				// apiFcc.searchDataBySpatial(subtask.getGeometry(),1901,new JSONArray());
				System.out.println(tips.toString());
				Page page = null;
				//List<JSONObject> page =null;
				try {
				//	page = a.listCheckResultsByJobId(jsonReq, jobId, jobUuid, 78, tips);
					 //page =a.listCheckResults(jsonReq, tips,ruleCodes);
					 System.out.println(page.getResult());
					 System.out.println(page.getTotalCount());
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
	

	//@Test
	public void rdnameSearch() throws Exception{
		Connection conn =null;
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_sp6_d_1", "fm_regiondb_sp6_d_1").getConnection();
		RdNameSelector selector = new RdNameSelector();
		//JSONObject json = selector.searchByName("八通", 5, 1, 17,conn);
		//System.out.println(json);
	}
	
	
//	@Test
	public void checkResultListByTask(){
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.3.227:1521/orcl", "metadata_pd_17sum", "metadata_pd_17sum").getConnection();
					//"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();
			
			JSONObject jsonReq = JSONObject.fromObject("{'pageSize':20,'pageNum':1,'taskName':'b8764b6d6a0e4ada802c36f07ac9af45','tableName':'rdName','params':{'name':'东河沿南','nameId':'3862625','adminId':'320000','namePhonetic':'Dong He Yan Nan Lu','ruleCode':'CHR60010','information':'汉字与'}}");	
			
				NiValExceptionSelector a = new NiValExceptionSelector(conn);
				
				
				Page page = null;
				//List<JSONObject> page =null;
				try {
					Map adminMap = new HashMap();
					page = a.listCheckResultsByTaskName(jsonReq, adminMap);
					 //page =a.listCheckResults(jsonReq, tips,ruleCodes);
					 System.out.println(page.getResult());
					 System.out.println(page.getTotalCount());
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
	public void checkResultListByJobId(){
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.3.227:1521/orcl", "metadata_pd_17sum", "metadata_pd_17sum").getConnection();
					//"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();
			
			JSONObject jsonReq = JSONObject.fromObject("{'pageSize':20,'pageNum':1,'taskName':'b8764b6d6a0e4ada802c36f07ac9af45','tableName':'rdName','params':{'name':'','nameId':'','adminId':'','namePhonetic':'','ruleCode':'','information':''}}");	
			
				NiValExceptionSelector a = new NiValExceptionSelector(conn);
				
				
				Page page = null;
				//List<JSONObject> page =null;
				try {
					Map adminMap = new HashMap();
					page = a.listCheckResultsByJobId(jsonReq, 29, "2851c8a8173941e3995b362ab4e2e8c8");
					 //page =a.listCheckResults(jsonReq, tips,ruleCodes);
					 System.out.println("哈哈哈: "+page.getResult());
					 System.out.println(page.getTotalCount());
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
	
	//@Test
	public void listCheckResultsRuleIds(){
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.3.227:1521/orcl", "metadata_pd_17sum", "metadata_pd_17sum").getConnection();
					//"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();
			
			JSONObject jsonReq = JSONObject.fromObject("{'taskName':'2d6475dcd01a41d6b3b8588544d83db6','tableName':'rdName'}");	
			
				NiValExceptionSelector a = new NiValExceptionSelector(conn);
				
				
				JSONArray arr = null;
				//List<JSONObject> page =null;
				try {
					arr = a.listCheckResultsRuleIds(jsonReq);
					 //page =a.listCheckResults(jsonReq, tips,ruleCodes);
					 System.out.println(arr);
					 System.out.println(arr.size());
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
	public void checkResultsStatis(){
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.3.227:1521/orcl", "metadata_pd_17sum", "metadata_pd_17sum").getConnection();
					//"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();
			
			JSONObject jsonReq = JSONObject.fromObject("{'taskName':'b8764b6d6a0e4ada802c36f07ac9af45','data':['rule','level','information','adminName']}");
//			JSONObject jsonReq = JSONObject.fromObject("{'taskName':'2d6475dcd01a41d6b3b8588544d83db6','data':['ruleid']}");
				String taskName = "";
				taskName = jsonReq.getString("taskName");
				NiValExceptionSelector a = new NiValExceptionSelector(conn);
				List<String> groupList = new ArrayList<String>();
				JSONArray groupDate = jsonReq.getJSONArray("data");
				if(groupDate != null && groupDate.size() > 0){
					groupList = (List<String>) JSONArray.toCollection(groupDate);
				}
				JSONArray newdata = new JSONArray();
				JSONArray arr = null;
				//List<JSONObject> page =null;
				try {
					arr = a.checkResultsStatis(taskName,groupList);
					
					Map<String,String> adminMap =null;
					if(groupList.contains("adminName")){
//						MetadataApi metadataApiService = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
//						adminMap = metadataApiService.getAdminMap();
						adminMap = new HashMap<String,String>();
					}
					if(arr != null && arr.size() >0){
						for(Object obj : arr){
							JSONObject jobj = (JSONObject) obj;
							/*JSONObject newjobj = new JSONObject();
							newjobj.put("ruleid", "");
							newjobj.put("ruleName", "");
							newjobj.put("adminName", "");
							newjobj.put("information", "");
							newjobj.put("level", "");
							newjobj.put("count", 0);*/
							
							if(jobj.containsKey("ruleid")){
								//查询ruleName
								String ruleName ="hhh";
								jobj.put("ruleName", ruleName);
//								newjobj.put("ruleid", jobj.getString("ruleid"));
//								newjobj.put("ruleName", ruleName);
							}
							if(jobj.containsKey("admin_id")){
								int adminId = jobj.getInt("admin_id"); 
								jobj.remove("admin_id");
								System.out.println("jobj.containsKey('admin_id'):"+jobj.containsKey("admin_id"));
								if(adminId == 214){
									jobj.put("adminName","全国");
//									newjobj.put("adminName", "全国");
								}else{
									if (!adminMap.isEmpty()) {
										if (adminMap.containsKey(String.valueOf(adminId))) {
//											newjobj.put("adminName", adminMap.get(String.valueOf(adminId)));
											jobj.put("adminName", adminMap.get(String.valueOf(adminId)));
										} else {
											jobj.put("adminName", "");
//											newjobj.put("adminName", "");
										}
									}
								}
							}
							/*if(jobj.containsKey("information")){
								newjobj.put("information", jobj.getString("information"));
							}
							if(jobj.containsKey("level")){
								newjobj.put("level", jobj.getString("level"));
							}
							if(jobj.containsKey("count")){
								newjobj.put("count", jobj.getString("count"));
							}*/
							newdata.add(jobj);
//							logger.info("newjobj : "+newjobj);
						}
					}
					
					
					
					
					 //page =a.listCheckResults(jsonReq, tips,ruleCodes);
					 System.out.println(arr);
					 System.out.println(arr.size());
					 
					 System.out.println(newdata);
					 System.out.println(newdata.size());
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
	public void updateCheckLogStatusForRdTest(){
			Connection conn =null;
			try{
				conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
						"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.3.227:1521/orcl", "metadata_pd_17sum", "metadata_pd_17sum").getConnection();
						//"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();
				
				JSONObject jsonReq = JSONObject.fromObject("{'id':'4554635','type':3}");	//
				
				String id = jsonReq.getString("id");

				int type = jsonReq.getInt("type");
					NiValExceptionOperator selector = new NiValExceptionOperator(conn);
					
					try {
						selector.updateCheckLogStatusForRd(id, type,"");

						 System.out.println(" end ");
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
	public void checkPoiResultList(){
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_trunk_d_1", "fm_regiondb_trunk_d_1").getConnection();
					//"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();
			
			JSONObject jsonReq = JSONObject.fromObject("{'pid':500000008}");	
			
				NiValExceptionSelector a = new NiValExceptionSelector(conn);
				
				JSONArray checkResultsArr = null;
				//List<JSONObject> page =null;
				try {
					int pid = jsonReq.getInt("pid");
					checkResultsArr = a.poiCheckResultList(pid,new ArrayList());
					 //page =a.listCheckResults(jsonReq, tips,ruleCodes);
					 System.out.println("哈哈哈: "+checkResultsArr);
					 System.out.println(checkResultsArr.size());
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
