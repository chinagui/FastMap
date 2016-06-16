package com.navinfo.dataservice.engine.edit.zhaokk.poi;



import java.sql.Connection;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class IxPoiTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	@Test
	public void addPoi() throws Exception{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":42,\"data\":{\"longitude\":116.39552235603331,\"latitude\":39.90676527744907,\"x_guide\":0,\"y_guide\":0,\"linkPid\":0}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void MovePoi(){
		String parameter = "{\"command\":\"MOVE\",\"type\":\"IXPOI\",\"dbId\":8,\"data\":{\"longitude\":116.39552235603331,\"latitude\":39.90676527744907,\"linkPid\":625962}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void queryPoi() throws Exception{
		int pageNum = 0;
		int pageSize = 10;
		int pid = 0;
		String pidName = "电";
		Connection conn = DBConnector.getInstance().getConnectionById(42);
		IxPoiSelector  ixPoiSelector  = new IxPoiSelector(conn);
		JSONObject jsonObject = ixPoiSelector.loadPids(false,pid,pidName,pageSize, pageNum);
		
		System.out.println(jsonObject);
	}
	
	
}
