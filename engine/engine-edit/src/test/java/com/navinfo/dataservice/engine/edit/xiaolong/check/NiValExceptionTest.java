package com.navinfo.dataservice.engine.edit.xiaolong.check;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.check.CheckService;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NiValExceptionTest extends InitApplication {

	@Override
	public void init() {
		initContext();
	}
	
	@Test
	public void testGLM01455() throws Exception{
		//String parameter="{\"command\":\"UPDATE\",\"dbId\":84,\"type\":\"RDLINK\",\"objId\":502000037,\"data\":{\"forms\":[{\"linkPid\":502000037,\"formOfWay\":36,\"extendedForm\":0,\"auxiFlag\":0,\"kgFlag\":0,\"objStatus\":\"INSERT\"}],\"rowId\":\"AE884CC4C8614A00B7E3B20A065A27D3\",\"pid\":502000037,\"objStatus\":\"UPDATE\"}}";
		String parameter = "{\"command\":\"MOVE\",\"type\":\"ZONENODE\",\"objId\":407000041,\"data\":{\"longitude\":115.89215755462646,\"latitude\":40.27495417910794},\"dbId\":13,\"subtaskId\":834}";
		
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}

//	@Test
	public void testLoadByGrid() throws Exception {

		String parameter = "{\"dbId\":42,\"grids\":[60560301,60560302,60560303,60560311,60560312,60560313,60560322,60560323,60560331,60560332,60560333,60560320,60560330,60560300,60560321,60560310]}";

		Connection conn = null;

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			JSONArray grids = jsonReq.getJSONArray("grids");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			int data = selector.loadCountByGrid(grids);

			System.out.println("data:" + data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	@Test
	public void testCheck() throws Exception {
		String parameter = "{\"dbId\":42,\"type\":2,\"id\":\"9aab29cf60bbbc997f12d8368b5920c2\"}";

		JSONObject jsonReq = JSONObject.fromObject(parameter);

		int dbId = jsonReq.getInt("dbId");

		String id = jsonReq.getString("id");

		int type = jsonReq.getInt("type");

		Connection conn = DBConnector.getInstance().getConnectionById(dbId);

		NiValExceptionOperator selector = new NiValExceptionOperator(conn);

		// selector.updateCheckLogStatus(id, type);
	}

	@Test
	public void testList() throws Exception {
		Connection conn = null;
		try {

			Set<String> grids = new HashSet<String>();
			grids.add("59565623");
			grids.add("59565633");
			grids.add("59565710");
			grids.add("59565720");
			grids.add("59565613");
			grids.add("59565730");
			///{"dbId":624,"subtaskId":573,"subtaskType":3,
			//"grids":[59565623,59565633,59565710,59565720,59565613,59565730],
			//"pageSize":15,"pageNum":1,"level":0,"ruleId":"","flag":1,"sortby":"","checkType":0}
			
			//subtaskType, grids,pageSize,pageNum,flag,ruleId,level,sortby,checkType)
			conn = DBConnector.getInstance().getConnectionById(624);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);
			
			Page page = selector.list(3, grids, 15, 1, 0, "", 0, "", 0);
			System.out.println(page.getResult());
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	//@Test
	public void testAddStatus() throws Exception {
		Connection conn = null;
		try {

			String id = "93d0826e5fb9783622acae2fbfb1dd42";
			int oldType = 0;

			int type = 1;

			conn = DBConnector.getInstance().getConnectionById(84);

			NiValExceptionOperator selector = new NiValExceptionOperator(conn);

			selector.updateCheckLogStatus(id, oldType, type, 0, 1736,null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
//	@Test
	public void testAddCheckRun() throws Exception {
		Connection conn = null;
		try {
//{\"subtaskId\":\"353\",\"ckRules\":\"COM20491,CHR70116,CHR70003,GLM60994,COM60038,COM300033,COM20531,CHR63027\",\"checkType\":1}
			String parameter = "{\"subtaskId\":\"245\",\"ckRules\":\"CHR73040,CHR74098,CHR73042,CHR73043,CHR73044,CHR73045,CHR74083,CHR74084,CHR74085,CHR74086,CHR74087,CHR74088,CHR74093,CHR74094,CHR74095,CHR74096,CHR74097,CHR73041\",\"checkType\":5}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int subtaskId=jsonReq.getInt("subtaskId");
			int checkType=jsonReq.getInt("checkType");	
			
			//conn = DBConnector.getInstance().getConnectionById(19);

			long jobId=CheckService.getInstance().checkRun(subtaskId,2,checkType,jsonReq);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	//@Test
	public void testAddCheckRunMetaRdName() throws Exception {
		Connection conn = null;
		try {
			String parameter = "{'isMetaFlag':1,'ckRules':'CHR73040,CHR70108,CHR73042,CHR73043','checkType':7,'name':'雲嶺山莊','nameGroupid':'','adminId':'','roadTypes':[0,2]}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int checkType=jsonReq.getInt("checkType");	
			
			//conn = DBConnector.getInstance().getConnectionById(19);

			long jobId=CheckService.getInstance().metaCheckRun(2,checkType,jsonReq);
			System.out.println("jobId: "+jobId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
//	@Test
	public void testgetsuites() throws Exception {
		try {
			String parameter = "{'type':5,'flag':1}";
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int flag = 0;
			if(jsonReq.containsKey("flag") && jsonReq.getInt("flag") >0 ){
				flag =jsonReq.getInt("flag");
			}
			
			int type=jsonReq.getInt("type");	
			
			JSONArray suites=CheckService.getInstance().getCkSuites(type,flag);
			System.out.println("suites: "+suites);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
	@Test
	public void testgetrules() throws Exception {
		try {
			String parameter = "{'suiteId':'suite7','ruleCode':'CHR73040'}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);
			String suiteId=jsonReq.getString("suiteId");	
			String ruleCode = "";
			if(jsonReq.containsKey("ruleCode") && jsonReq.getString("ruleCode") != null 
					&& StringUtils.isNotEmpty(jsonReq.getString("ruleCode"))){
				ruleCode = jsonReq.getString("ruleCode");
			}
			JSONArray rules=CheckService.getInstance().getCkRulesBySuiteId(suiteId,ruleCode);
			System.out.println("rules: "+rules);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
	
//	@Test
	public void testgetnameids() throws Exception {
		try {
			JSONArray tips=new JSONArray();
			JSONObject tipObj1= new JSONObject(); 
				tipObj1.put("id", "02190164f80db1ccf74e9da50c8288ab439423");
			JSONObject tipObj2= new JSONObject(); 
				tipObj2.put("id", "021901cb57f7f997f148bd800ff8e508072ead");
				tips.add(tipObj1);
				tips.add(tipObj2);
			List<Integer> rules=CheckService.getInstance().getNameIds(305, tips);
			
			System.out.println("rules: "+rules);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
	
	public static void main(String[] args) {
		JSONObject jobj = new JSONObject();
		jobj.put("ruleid", "1");
		jobj.put("ruleName", "2");
		jobj.put("adminName", "3");
		jobj.put("information", "4");
		jobj.put("level", "5");
		jobj.put("count", 0);
		
		JSONObject newjobj = new JSONObject();
		newjobj.put("ruleid", "");
		newjobj.put("ruleName", "");
		newjobj.put("adminName", "");
		newjobj.put("information", "");
		newjobj.put("level", "");
		newjobj.put("count", 0);
		if(jobj.containsKey("information")){
			newjobj.put("information", jobj.getString("information"));
		}
		if(jobj.containsKey("level")){
			newjobj.put("level", jobj.getString("level"));
		}
		if(jobj.containsKey("count")){
			newjobj.put("count", jobj.getString("count"));
		}
		System.out.println(newjobj);
	}
	
	@Test
	public void testSaveQaProblem() throws Exception {
		Connection qualityConn = null;
		try {
			String paramter = "{\"logId\":\"5f0fb389510d1ad677aa53c83d607ec6\",\"checkTaskId\":849,\"quDesc\":\"1111111111111111\",\"reason\":\"录入错误\",\"erContent\":\"大门\",\"quRank\":\"B\",\"isPrefer\":1,\"worker\":\"范京伟 1672\",\"objectType\":\"道路\",\"objectId\":\"40923045\",\"erType\":1}";
			JSONObject data = JSONObject.fromObject(paramter);
			qualityConn = DBConnector.getInstance().getCheckConnection();
			
			Timestamp timeStamp = new Timestamp(new Date().getTime());
			
			NiValExceptionOperator op = new NiValExceptionOperator(null);
			
			op.saveQaProblem(qualityConn, data, timeStamp);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(qualityConn);
			e.printStackTrace();
		} finally {
			DbUtils.commitAndCloseQuietly(qualityConn);
		}
	}
}
