package com.navinfo.dataservice.dao;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
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
public class RdNameResultsTest {
	
	@Test
	public void checkResultList(){
		Connection conn =null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "fm_meta_all_sp6", "fm_meta_all_sp6").getConnection();
					//"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();

			NiValExceptionSelector a = new NiValExceptionSelector(conn);
				JSONObject jsonReq = JSONObject.fromObject("{'pageSize':20,'pageNum':1,'subtaskId':78,'dbId':17}");
				
				//JSONObject data = jsonReq.getJSONObject("data");
				JSONObject jso = JSONObject.fromObject("{'tips':[{'id':'021901d7e8ed4c7c604242a1392291a530fbb2'},{'id':'021901404F5A9DE3AB4ECCACE7B512207BC00B'},{'id':'02190151EEF41E16D34C5C8976B5DD6292DEAC'}]}");
				//int subtaskId = jsonReq.getInt("subtaskId");
				
				//ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
				
//				int dbId = subtask.getDbId();
				
				//FccApi apiFcc=(FccApi) ApplicationContextUtil.getBean("fccApi");
				JSONObject jsorule = JSONObject.fromObject("{'rules':[{'ruleCode':'GLM02216'},{'ruleCode':'GLM02262'}]}");
				//获取规则号
				JSONArray ruleCodes = jsorule.getJSONArray("rules");//CheckService.getInstance().getCkRuleCodes(type);
				JSONArray tips = jso.getJSONArray("tips");
				// apiFcc.searchDataBySpatial(subtask.getGeometry(),1901,new JSONArray());
				System.out.println(tips.toString());
				Page page = null;
				//List<JSONObject> page =null;
				try {
					 page =a.listCheckResults(jsonReq, tips,ruleCodes);
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
	

}
