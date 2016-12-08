package com.navinfo.dataservice.control.column;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.column.core.ColumnCoreControl;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.dao.glm.search.IxPoiSearch;

import net.sf.json.JSONArray;

public class ixpoitest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void test() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);
			IxPoiSearch search = new IxPoiSearch(conn);
			List<String> rowIds = new ArrayList<String>();
			rowIds.add("3AE1FB4B0B6992F7E050A8C08304EE4C");
			//search.searchColumnPoiByRowId("poi_name", "namePinyin", rowIds, "1", "CHI");
//			ColumnCoreControl control = new ColumnCoreControl();
//
//			control.applyData(0, "poi_name", 2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(conn);
		}

	}
	
	@Test
	public void testLockData() throws Exception{
		long timeCur = new Date().getTime();
		Timestamp time = new Timestamp(timeCur);
		System.out.print(time.toString());
		int dbId = 19;
		long userId = 4994;
		String rowId = "3AE1FB4B0B6492F7E050A8C08304EE4C";
		Connection conn = DBConnector.getInstance().getConnectionById(dbId);
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE poi_deep_status SET handler=:1,update_date=:2 WHERE row_id =:3");
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setLong(1, userId);
			pstmt.setTimestamp(2, time);
			pstmt.setString(3, rowId);
			
			pstmt.execute();
			conn.commit();
			conn.close();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt); 
		}
	}
	
	
	@Test
	public void testCheck() throws Exception{
		int dbId = 19;
		List<Integer> pids = new ArrayList<Integer>();
		Connection conn = DBConnector.getInstance().getConnectionById(dbId);
		try{
			
			pids.add(602474);
			JSONArray checkRules = new JSONArray();
			checkRules.add("FM-ZY-20-152");
//			checkRules.add("FM-ZY-20-153");
			checkRules.add("FM-ZY-20-198");
			checkRules.add("FM-ZY-20-199");
			checkRules.add("FM-ZY-20-238");
			checkRules.add("FM-ZY-20-154");
			checkRules.add("FM-ZY-20-155");
			checkRules.add("FM-YW-20-227");
			checkRules.add("FM-YW-20-225");
			checkRules.add("FM-YW-20-235");
			
			DeepCoreControl deep = new DeepCoreControl();
			deep.deepCheckRun(pids, checkRules, "IXPOI", "UPDATE", conn);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.commitAndClose(conn);
		}
	}
	
	
	@Test
	public void testApplyData() throws Exception{
		long userId = 4994;
		int subtaskId = 84;
		
		String firstWorkItem = "poi_deep";
		String secondWorkItem = "deepDetail";
		try {
			
			DeepCoreControl deepCore = new DeepCoreControl();
			//List<String> batchRuleList = deepCore.getDeepBatchRules(type);
			//申请数据，返回本次申请成功的数据条数
			int applyNum = deepCore.applyData(subtaskId, userId, firstWorkItem, secondWorkItem);
			System.out.println(applyNum);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	@Test
	public void testSave() throws Exception{
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":19,\"type\":\"IXPOI\",\"objId\":602474,\"data\":{\"parkings\":[{\"tollWay\":\"1234\",\"rowId\":\"659FAC5F4DFD41E8BDE447D1475ED3DB\",\"pid\":206000858,\"objStatus\":\"UPDATE\"}],\"rowId\":\"3AE1FB4C35C492F7E050A8C08304EE4C\",\"pid\":602474}}";
		try {
			DeepCoreControl deepCore = new DeepCoreControl();
			JSONObject result = deepCore.save(parameter, 111);
			System.out.println(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	@Test
	public void testRelease() throws Exception{
		String parameter = "{\"subtaskId\":84,\"dbId\":19,\"secondWorkItem\":'deepDetail'}";
		try {
			DeepCoreControl deepCore = new DeepCoreControl();
			JSONObject result = deepCore.release(parameter, 111);
			System.out.println(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	@Test
	public void testApplyColumnData() throws Exception{
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("taskId", 84);
		jsonReq.put("firstWorkItem","poi_name");
		jsonReq.put("secondWorkItem", "");
		 
		long userId = 4994;
		
		try {
			ColumnCoreControl column = new ColumnCoreControl();
			int count = column.applyData(jsonReq, userId);
			System.out.println(count);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	@Test
	public void testSecondColumnStatics() throws Exception{
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("taskId", 84);
		jsonReq.put("firstWorkItem","poi_name");
		 
		long userId = 4994;
		
		try {
			ColumnCoreControl column = new ColumnCoreControl();
			JSONObject data = column.secondWorkStatistics(jsonReq, userId);
			System.out.println(data);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	@Test
	public void testCleanCkResult() throws Exception {
		
		try {
			
			long userId = 4994;
			
			JSONObject jsonReq = new JSONObject();
			jsonReq.put("subtaskId", 84);
			jsonReq.put("checkType", 0);
			jsonReq.put("firstWorkItem", "poi_name");
			jsonReq.put("secondWorkItem", "nameUnify");
			
			List<Integer> pids = new ArrayList<Integer>();
			//pids.add(307000165);
			jsonReq.put("pids", pids);
			
			List<String> ckRules = new ArrayList<String>();
			ckRules.add("FM-ZY-20-237");
			jsonReq.put("ckRules", ckRules);
			
			DeepCoreControl deepControl = new DeepCoreControl();
			deepControl.cleanCheck(jsonReq, userId);
			
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	
	@Test
	public void testDeepQuery() throws Exception{
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("subtaskId", 84);
		jsonReq.put("dbId", 19);
		jsonReq.put("type", "deepDetail");
		jsonReq.put("status", 1);
		jsonReq.put("pageNum", 1);
		jsonReq.put("pageSize", 10);
		try {
			long userId = 4994;
			
			DeepCoreControl deepCore = new DeepCoreControl();
			
			JSONObject result = deepCore.queryPoi(jsonReq, userId);
			System.out.println(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	@Test
	public void testcolumnKc() throws Exception{
		int taskId = 84;
		long userId = 4994;
		try {
			ColumnCoreControl column = new ColumnCoreControl();
			JSONObject result = column.getLogCount(taskId, userId);
			System.out.println(result);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
